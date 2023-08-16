package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredContainer;
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

public class PortSetter implements IProcessBehaviour {

    private final Service service;

    public PortSetter(Service service) {

        this.service = service;
    }

    private final Map<String, IServer> serverMap = new LinkedHashMap<>();

    public static void displayModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports:");
        System.out.println("1) View");
        System.out.println("2) Modify single server");
        System.out.println("3) Modify multiple servers");
        System.out.println("4) ← Back");
        System.out.println();
    }

    public static void displayMassModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports > Modify multiple servers:");
        System.out.println("1) Modify on all servers (except CMS)");
        System.out.println("2) Modify only on servers with port set to Auto (except CMS)");
        System.out.println("3) ← Back");
        System.out.println();
    }

    @Override
    public void process() throws SDKException {

        initializeServerMap(this.serverMap);
        displayPortTable(this.serverMap);
    }

    public void handleModifyRequestPortMenu(Scanner scanner) throws SDKException {
        while (true) {

            displayModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 4);

            switch (requestPortMenuChoice) {
                case 1:
                    System.out.println("Performing actualize state.");
                    process();
                    break;
                case 2:
                    System.out.println("Performing modification of single server request port.");
                    modifySingleRequestPort(scanner);
                    break;
                case 3:
                    System.out.println("Performing mass modification of request ports.");
                    handleMassModifyRequestPortActivity(scanner);
                    break;
                case 4:
                    return; // Return to previous menu
            }
        }
    }

    private void handleMassModifyRequestPortActivity(Scanner scanner) {
        while (true) {

            displayMassModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 3);

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

        Map<String, Set<Integer>> hostToAvailablePorts = getHostWithAvailablePorts();

        hostToAvailablePorts.forEach((host, ports) ->
                System.out.println("Host: " + host + ", Available Ports: " + ports)
        );

        HashSet<Integer> usedAndExcludedPorts = this.serverMap.keySet().stream()
                .map(key -> getActualServerProps(serverMap, key))
                .map(this::getActualPort)
                .filter(port -> port.matches("\\d+"))
                .map(Integer::valueOf).collect(Collectors.toCollection(HashSet::new));

        // Adding other ports to exclude - SIA/WACS
        // 6411 is the new ERS SIA port
        usedAndExcludedPorts.addAll(Arrays.asList(6410, 6405, 6411));

        // Starting from 6401 as port 6400 is generally reserved for CMS
        int startPort = 6401;
        int endPort = 6499;

        HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, usedAndExcludedPorts);
        System.out.println(availablePorts);

        LinkedHashMap<String, Integer> cuidWithPort = getMapOfCuidWithPort(this.serverMap, constant, availablePorts);

        System.out.println("The following ports will be set:");
        cuidWithPort.forEach((cuid, port) -> System.out.println(cuid + " " + port + " " + serverMap.get(cuid).getTitle()));

        askYesNoQuestion(scanner, "\nWould you like to save these Request Port values?",
                () -> {
                    System.out.print("Saving values...");

                    cuidWithPort.forEach((cuid, port) -> {

                        IServer currentServer = serverMap.get(cuid);

                        try {

                            IExecProps serverExecProps = currentServer.getContainer().getExecProps();
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

    private Map<String, Set<Integer>> getHostWithAvailablePorts() {
        Map<String, Set<Integer>> hostToAvailablePorts = new HashMap<>();
        Map<String, List<IServer>> hostToServersMap = new HashMap<>();

        for (IServer server : serverMap.values()) {
            String host = server.getSIAHostname();
            hostToServersMap.computeIfAbsent(host, value -> new ArrayList<>()).add(server);
        }

        hostToServersMap.forEach((host, listOfServers) -> {
            HashSet<Integer> usedAndExcludedPortsOnHostNew = listOfServers.stream()
                    .map(server -> {
                        try {
                            return getActualPort(server.getContainer().getExecProps());
                        } catch (SDKException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(port -> port.matches("\\d+"))
                    .map(Integer::valueOf).collect(Collectors.toCollection(HashSet::new));

            int startPort = 6401;
            int endPort = 6499;

            usedAndExcludedPortsOnHostNew.addAll(Arrays.asList(Constants.SIA_PORT, Constants.WACS_PORT, 6411));

            Set<Integer> availablePorts = generateRemainingPorts(startPort, endPort, usedAndExcludedPortsOnHostNew);
            hostToAvailablePorts.put(host, availablePorts);

        });
        return hostToAvailablePorts;
    }

    private LinkedHashMap<String, Integer> getMapOfCuidWithPort(Map<String, IServer> serverMap, String constant, HashSet<Integer> availablePorts) {
        return serverMap.keySet().stream()
                .filter(key -> isServerAbbreviationNotCms(serverMap, key))
                .filter(key -> applyFilterConstant(serverMap, constant, key))
                .collect(Collectors.toMap(
                        key -> getCUIDOrThrowRuntimeException(serverMap, key),
                        value -> nextAvailableValue(availablePorts),
                        (oldValue, newValue) -> oldValue,   // in case there will be duplicate => no real way, but handled
                        LinkedHashMap::new
                ));
    }

    private boolean isServerAbbreviationNotCms(Map<String, IServer> serverMap, String key) {
        try {
            return !serverMap.get(key).getServerAbbreviation().equals("cms");
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean applyFilterConstant(Map<String, IServer> serverMap, String constant, String key) {

        IExecProps execProps = getActualServerProps(serverMap, key);
        String port = getActualPort(execProps);
        return constant.equals(Constants.ALL) || (constant.equals(Constants.ONLY_AUTO) && port.equals("-"));

    }

    private String getCUIDOrThrowRuntimeException(Map<String, IServer> serverMap, String key) {
        try {
            return serverMap.get(key).getCUID();
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
        formatterMap.put("serverStatus", 25);
        formatterMap.put("serverState", 13);
        formatterMap.put("runningPort", 15);
        formatterMap.put("setMethod", 15);
        formatterMap.put("actualPort", 12);
        formatterMap.put("CUID", 38);

        this.printOverallHeader(formatterMap);

        AtomicInteger increment = new AtomicInteger(0);
        serverMap.keySet().stream()
                .map(key -> {
                    IExecProps serverExecProps = getActualServerProps(serverMap, key);
                    StringBuffer stringBuffer = new StringBuffer();

                    try {

                    appendValueToBuffer(formatterMap, stringBuffer, "ID", String.valueOf(increment.getAndIncrement()));
                    appendValueToBuffer(formatterMap, stringBuffer, "stale", serverMap.get(key).getRequiresRestart() ? "  *" : "");
                    appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", serverMap.get(key).getTitle());
                    appendValueToBuffer(formatterMap, stringBuffer, "serverStatus", serverMap.get(key).getState().toString());
                    appendValueToBuffer(formatterMap, stringBuffer, "serverState", serverMap.get(key).isDisabled() ? "Disabled" : "Enabled");
                    appendValueToBuffer(formatterMap, stringBuffer, "runningPort", getRunningPort(serverMap, key));
                    appendValueToBuffer(formatterMap, stringBuffer, "setMethod", getPortSetMethod(serverExecProps.getArgs()));
                    appendValueToBuffer(formatterMap, stringBuffer, "actualPort", getActualPort(serverExecProps));
                    appendValueToBuffer(formatterMap, stringBuffer, "CUID", serverMap.get(key).getCUID());

                    } catch (SDKException e) {
                        throw new RuntimeException(e);
                    }

                    return stringBuffer.toString();
                })
                .forEach(System.out::println);

        System.out.println();

    }

    private IExecProps getActualServerProps(Map<String, IServer> serverMap, String key) {

        try {
            IConfiguredContainer configuredContainer = serverMap.get(key).getContainer();
            return configuredContainer.getExecProps();

        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

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

    private String getRunningPort(Map<String, IServer> serverMap, String key) throws SDKException {

        if (serverMap.get(key).isAlive() && serverMap.get(key).getState() != null) {

            IServerMetrics serverMetrics = serverMap.get(key).getMetrics();
            IMetrics metrics = serverMetrics.getMetrics("ISGeneralAdmin");

            for (Object m : metrics) {

                IMetric metric = (IMetric) m;

                if (metric.getName().equals("ISPROP_GEN_HOST_PORT")) return metric.getValue().toString();

            }

        }

        return "-";
    }

    private String getPortSetMethod(String actualServerExecProps) {

        if (actualServerExecProps.contains("-requestport")) return "Manual";

        return "Auto";

    }

    private void initializeServerMap(Map<String, IServer> serverMap) throws SDKException {

        System.out.print("Initializing server map...");

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
        int serverID = Helper.getIntInput(scanner, 0, serverMap.size() - 1);

        Object[] serverArray = serverMap.keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serverMap.get(key);

        String serverCUID = selectedServer.getCUID();
        String serverName = selectedServer.getTitle();
        System.out.println("Server Name: " + serverName);

        String actualRequestPort = getRunningPort(serverMap, serverCUID);
        System.out.println("Actual Request Port: " + actualRequestPort);

        System.out.print("Choose new Request Port [6401-6499]: ");
        int newRequestPort = Helper.getIntInput(scanner, 6401, 6499);

        System.out.println("\n--- Summary ---");
        System.out.println("Server Name: " + serverName);
        System.out.println("Actual Request Port: " + actualRequestPort);
        System.out.println("Hostname: " + selectedServer.getSIAHostname());
        System.out.println("New Request Port: " + newRequestPort);
        System.out.println("Server CUID: " + serverCUID);

        IExecProps serverExecProps = getActualServerProps(serverMap, serverCUID);
        String actualServerExecProps = serverExecProps.getArgs();

        String pattern = "-requestport\\s+\\d{4}";
        String replacementString = "-requestport " + newRequestPort;

        String modifiedServerExecProps = actualServerExecProps.replaceAll(pattern, replacementString);

        if (modifiedServerExecProps.equals(actualServerExecProps)) {
            modifiedServerExecProps += " " + replacementString;
        }

        final String effectivelyFinalExecProps = modifiedServerExecProps;

        askYesNoQuestion(scanner, "\nDo you want to save the new Request Port value?",
                () -> {
                    System.out.print("Saving values...");
                    serverExecProps.setArgs(effectivelyFinalExecProps);
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
