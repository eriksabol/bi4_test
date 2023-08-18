package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.businessobjects.sdk.plugin.desktop.common.IMetric;
import com.businessobjects.sdk.plugin.desktop.common.IMetrics;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IServerMetrics;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortSetter {

    private final Service service;

    public PortSetter(Service service) {

        this.service = service;
    }

    private final Map<String, IServer> serverMap = new LinkedHashMap<>();

    public static void displayModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports:");
        System.out.println("1 View");
        System.out.println("2 Modify single server");
        System.out.println("3 Modify multiple servers");
        System.out.println("4 ← Back");
        System.out.println();
    }

    public static void displayMassModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports > Modify multiple servers:");
        System.out.println("1 Modify on all servers (except CMS)");
        System.out.println("2 Modify only on servers with port set to Auto assign (except CMS)");
        System.out.println("3 ← Back");
        System.out.println();
    }

    public void viewRequestPorts() throws SDKException {

        initializeServerMap(this.serverMap);
        displayPortTable(this.serverMap);
    }

    public void handleModifyRequestPortMenu(Scanner scanner) throws SDKException {
        while (true) {

            displayModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 4, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    System.out.println("Performing actualize state.");
                    viewRequestPorts();
                    break;
                case 2:
                    System.out.println("Performing modification of single server request port.");
                    modifySingleRequestPort(scanner);
                    break;
                case 3:
                    System.out.println("Performing mass modification of request ports.");
                    handleMassModifyRequestPortMenu(scanner);
                    break;
                case 4:
                    return; // Return to previous menu
            }
        }
    }

    private void handleMassModifyRequestPortMenu(Scanner scanner) {
        while (true) {

            displayMassModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 3, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    System.out.println("Performing modification on all servers (except CMS)");
                    modifyMultipleRequestPorts(scanner, Constants.ALL);
                    break;
                case 2:
                    System.out.println("Performing modification on servers set to Auto (except CMS)");
                    modifyMultipleRequestPorts(scanner, Constants.ONLY_AUTO);
                    break;
                case 3:
                    return; // Return to previous menu
            }
        }
    }

    private void modifyMultipleRequestPorts(Scanner scanner, String constant) {

        Map<String, HashSet<Integer>> hostToAvailablePorts = getHostWithAvailablePorts();
        Map<IServer, Integer> serverWithPort = getServerWithPort(serverMap, hostToAvailablePorts, constant);

        System.out.println("The following ports will be set:");
        serverWithPort.forEach((server, portNumber) -> System.out.println(portNumber + " " + server.getTitle()));

        Helper.askYesNoQuestion(scanner, "\nWould you like to save these Request Port values?",
                () -> {
                    System.out.print("Saving values...");

                    serverWithPort.forEach((currentServer, port) -> {

                        try {

                            IExecProps serverExecProps = getExecProps(currentServer);
                            String serverArgs = serverExecProps.getArgs();

                            String pattern = "-requestport\\s+\\d{4}";
                            String replacementString = "-requestport " + port;

                            String modifiedArguments = getModifiedArguments(pattern, replacementString, serverArgs);

                            System.out.println(serverExecProps.getArgs());
                            System.out.println(modifiedArguments);

                            serverExecProps.setArgs(modifiedArguments);
                            currentServer.save();

                        } catch (SDKException e) {
                            throw new RuntimeException(e);
                        }

                    });

                    System.out.print("done\n");
                    System.out.println();
                },
                () -> {
                    System.out.println("Doing nothing!");
                    System.out.println();
                }
        );
    }

    private Map<IServer, Integer> getServerWithPort(Map<String, IServer> serverMap, Map<String, HashSet<Integer>> hostToAvailablePorts, String constant) {

        return serverMap.entrySet().stream()
                .filter(entry -> {
                    IServer server = entry.getValue();
                    return isServerAbbreviationNotCms(server);
                }).filter(entry -> {
                    IServer server = entry.getValue();
                    IExecProps execProps = getExecProps(server);
                    String port = getActualPort(execProps);
                    return constant.equals(Constants.ALL) || (constant.equals(Constants.ONLY_AUTO) && port.equals("-"));
                }).collect(Collectors.toMap(
                        Map.Entry::getValue,
                        entry -> {
                            IServer server = entry.getValue();
                            String siaHostname = server.getSIAHostname();
                            HashSet<Integer> portIntegers = hostToAvailablePorts.get(siaHostname);
                            return nextAvailableValue(portIntegers);
                        },
                        (oldValue, newValue) -> oldValue,   // in case there will be duplicate => no real way, but handled
                        LinkedHashMap::new
                ));
    }

    private IExecProps getExecProps(IServer server) {
        try {
            return server.getContainer().getExecProps();
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, HashSet<Integer>> getHostWithAvailablePorts() {
        Map<String, HashSet<Integer>> hostToAvailablePorts = new HashMap<>();
        Map<String, List<IServer>> hostToServersMap = new HashMap<>();

        for (IServer server : serverMap.values()) {
            String host = server.getSIAHostname();
            hostToServersMap.computeIfAbsent(host, value -> new ArrayList<>()).add(server);
        }

        hostToServersMap.forEach((host, listOfServers) -> {
            HashSet<Integer> usedAndExcludedPortsOnHostNew = listOfServers.stream()
                    .map(this::getRunningPort)
                    .filter(port -> port.matches("\\b(640[1-9]|64[1-8]\\d|6499)\\b"))
                    .map(Integer::valueOf).collect(Collectors.toCollection(HashSet::new));

            int startPort = 6401;
            int endPort = 6499;

            usedAndExcludedPortsOnHostNew.addAll(Arrays.asList(Constants.SIA_PORT, Constants.WACS_PORT, 6411));

            HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, usedAndExcludedPortsOnHostNew);
            hostToAvailablePorts.put(host, availablePorts);

        });
        return hostToAvailablePorts;
    }

    private boolean isServerAbbreviationNotCms(IServer server) {
        try {
            return !server.getServerAbbreviation().equals("cms");
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

    private String getModifiedArguments(String pattern, String replacementString, String serverArgs) throws SDKException {

        String modifiedServerExecProps = serverArgs.replaceAll(pattern, replacementString);

        if (modifiedServerExecProps.equals(serverArgs)) {
            modifiedServerExecProps += " " + replacementString;
        }

        return modifiedServerExecProps;
    }

    private static Integer nextAvailableValue(HashSet<Integer> set) {
        if (!set.isEmpty()) {
            Iterator<Integer> iterator = set.iterator();
            Integer removedValue = iterator.next();
            iterator.remove();
            return removedValue;
        }
        throw new RuntimeException("Not enough ports per single VM available between 6401-6499!");
    }

    private HashSet<Integer> generateRemainingPorts(int startPort, int endPort, HashSet<Integer> unwantedPorts) {
        return IntStream.range(startPort, endPort + 1) // +1 to include endPort
                .filter(port -> !unwantedPorts.contains(port))
                .boxed()
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void displayPortTable(Map<String, IServer> serverMap) {

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("ID", 4);
        formatterMap.put("stale", 7);
        formatterMap.put("serverTitle", 50);
        formatterMap.put("hostname", 20);
        formatterMap.put("serverStatus", 25);
        formatterMap.put("serverState", 13);
        formatterMap.put("runningPort", 15);
        formatterMap.put("setMethod", 15);
        formatterMap.put("actualPort", 12);

        Helper.printOverallHeader(formatterMap);

        AtomicInteger increment = new AtomicInteger(0);
        serverMap.keySet().stream()
                .map(key -> {
                    IServer server = serverMap.get(key);
                    IExecProps serverExecProps = getExecProps(server);
                    StringBuffer stringBuffer = new StringBuffer();

                    try {

                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "ID", String.valueOf(increment.getAndIncrement()));
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "stale", server.getRequiresRestart() ? "  *" : "");
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", server.getTitle());
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "hostname", server.getSIAHostname());
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "serverStatus", server.getState().toString());
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "serverState", server.isDisabled() ? "Disabled" : "Enabled");
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "runningPort", getRunningPort(server));
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "setMethod", getPortSetMethod(serverExecProps.getArgs()));
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "actualPort", getActualPort(serverExecProps));

                    } catch (SDKException e) {
                        throw new RuntimeException(e);
                    }

                    return stringBuffer.toString();
                })
                .forEach(System.out::println);

        System.out.println();

    }

    private String getActualPort(IExecProps serverExecProps) {
        Pattern pattern = Pattern.compile("-requestport\\s[0-9]{1,5}");
        Matcher matcher = pattern.matcher(serverExecProps.getArgs());

        if (matcher.find()) {

            String requestPortString = matcher.group();
            String[] portNumberArray = requestPortString.split("-requestport\\s");

            return String.join("", portNumberArray);

        }

        return "-";
    }

    private String getRunningPort(IServer server) {

        try {

            if (server.isAlive() && server.getState() != null) {

                IServerMetrics serverMetrics = server.getMetrics();
                IMetrics metrics = serverMetrics.getMetrics("ISGeneralAdmin");

                for (Object m : metrics) {

                    IMetric metric = (IMetric) m;

                    if (metric.getName().equals("ISPROP_GEN_HOST_PORT")) return metric.getValue().toString();

                }

            }
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

        return "-";
    }

    private String getPortSetMethod(String actualServerExecProps) {

        if (actualServerExecProps.contains("-requestport")) return "Manual";

        return "Auto";

    }

    private void initializeServerMap(Map<String, IServer> serverMap) throws SDKException {

        if (serverMap.isEmpty()) {
            System.out.print("Initializing server map...");
        } else {
            System.out.print("Updating server map...");
        }

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query("SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC");

        for (Object e : myInfoObjects) {

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;
            serverMap.put(server.getCUID(), server);

        }
        System.out.print("done.\n");
        System.out.println();
    }

    private void modifySingleRequestPort(Scanner scanner) throws SDKException {

        System.out.print("Choose Server ID: ");
        int serverID = Helper.getIntInput(scanner, 0, serverMap.size() - 1, null, null);

        Object[] serverArray = serverMap.keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serverMap.get(key);
        IExecProps serverExecProps = getExecProps(selectedServer);
        String actualServerArguments = serverExecProps.getArgs();

        List<Integer> runningPorts = serverMap.values().stream()
                .map(this::getRunningPort)
                .filter(port -> port.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        List<Integer> actualPorts = serverMap.values().stream().map(server -> {

                    IExecProps execProps = getExecProps(server);
                    return getActualPort(execProps);

                }).filter(port -> port.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());

        String serverName = selectedServer.getTitle();
        System.out.println("Server Name: " + serverName);

        String actualRunningPort = getRunningPort(selectedServer);
        System.out.println("Actual Running Port: " + actualRunningPort);

        String actualRequestPort = getActualPort(serverExecProps);
        System.out.println("Actual Request Port: " + actualRequestPort);

        System.out.print("Choose new Request Port [6401-6499]: ");
        int newRequestPort = Helper.getIntInput(scanner, 6401, 6499,
                port -> !runningPorts.contains(port) && !actualPorts.contains(port),   // output from the function is an input for this predicate
                "Your request port is already taken or currently set on another server! Please choose another one");

        System.out.println("\n--- Summary ---");
        System.out.println("Server Name: " + serverName);
        System.out.println("Actual Running Port: " + actualRunningPort);
        System.out.println("Actual Request Port: " + actualRequestPort);
        System.out.println("Hostname: " + selectedServer.getSIAHostname());
        System.out.println("New Request Port: " + newRequestPort);

        String pattern = "-requestport\\s+\\d{4}";
        String replacementString = "-requestport " + newRequestPort;
        String modifiedArguments = getModifiedArguments(pattern, replacementString, actualServerArguments);

        Helper.askYesNoQuestion(scanner, "\nDo you want to save the new Request Port value?",
                () -> {
                    System.out.print("Saving values...");
                    serverExecProps.setArgs(modifiedArguments);
                    try {
                        selectedServer.save();
                    } catch (SDKException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.print("done\n");
                    System.out.println();
                },
                () -> {
                    System.out.println("Doing nothing!");
                    System.out.println();
                }
        );
    }
}
