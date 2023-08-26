package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static quicksetcli.Helper.printEmptyLines;

public class RequestPortMultipleSet extends BaseCommand {

    private final String constant;
    private final Scanner scanner;
    private final Map<String, IServer> serverMap;

    public RequestPortMultipleSet(String constant, Scanner scanner, Map<String, IServer> serverMap) {
        this.constant = constant;
        this.scanner = scanner;
        this.serverMap = serverMap;
    }

    @Override
    public void execute() {

        Map<String, HashSet<Integer>> hostToAvailablePorts = getHostWithAvailablePorts(constant);
        Map<IServer, Integer> serverWithPort = getServerWithPort(serverMap, hostToAvailablePorts, constant);

        for (Map.Entry<String, HashSet<Integer>> entry : hostToAvailablePorts.entrySet()) {
            System.out.println("Host: " + entry.getKey());
            System.out.println("Available Ports: " + entry.getValue());
        }

        for (Map.Entry<IServer, Integer> entry : serverWithPort.entrySet()) {
            System.out.println("Host: " + entry.getKey().getTitle());
            System.out.println("Available Ports: " + entry.getValue());
        }

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 50);
        formatterMap.put("port", 12);

        Helper.printOverallHeader(formatterMap);

        if (serverWithPort.isEmpty()) {

            System.out.println("No servers found for modification.");
            printEmptyLines(1);

        } else {

            serverWithPort.entrySet().stream()
                    .map(entry -> {
                        StringBuffer stringBuffer = new StringBuffer();

                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", entry.getKey().getTitle());
                        Helper.appendValueToBuffer(formatterMap, stringBuffer, "port", String.valueOf(entry.getValue()));

                        return stringBuffer.toString();
                    })
                    .forEach(System.out::println);

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
                        printEmptyLines(1);
                    },
                    () -> {
                        System.out.println("Doing nothing!");
                        printEmptyLines(1);
                    }
            );
        }
    }

    private Map<String, HashSet<Integer>> getHostWithAvailablePorts(String constant) {
        Map<String, HashSet<Integer>> hostToAvailablePorts = new HashMap<>();
        Map<String, List<IServer>> hostToServersMap = new HashMap<>();

        for (IServer server : serverMap.values()) {
            String host = server.getSIAHostname();
            hostToServersMap.computeIfAbsent(host, value -> new ArrayList<>()).add(server);
        }

        List<Integer> myRunningPorts = getRunningPorts(serverMap);
        List<Integer> myActualPorts = getActualPorts(serverMap);

        List<Integer> actualPlusRunning = mergeTwoLists(myRunningPorts, myActualPorts);
        HashSet<Integer> mergedHashSet = new HashSet<>(actualPlusRunning);
        HashSet<Integer> myRunningPortHashSet = new HashSet<>(myRunningPorts);

        hostToServersMap.forEach((host, listOfServers) -> {

            int startPort = 6401;
            int endPort = 6499;

            List<Integer> additionalExcludedPorts = Arrays.asList(Constants.SIA_PORT, Constants.WACS_PORT, Constants.CMS_REQUEST_PORT);

//          Adding other ports for exclusion
            mergedHashSet.addAll(additionalExcludedPorts);
            myRunningPortHashSet.addAll(additionalExcludedPorts);

            if (constant.equals(Constants.ONLY_AUTO)) {

                HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, mergedHashSet);
                hostToAvailablePorts.put(host, availablePorts);
            }

            if (constant.equals(Constants.ALL)) {

                HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, myRunningPortHashSet);
                hostToAvailablePorts.put(host, availablePorts);
            }
        });
        return hostToAvailablePorts;
    }

    private HashSet<Integer> generateRemainingPorts(int startPort, int endPort, HashSet<Integer> excludedPorts) {
        return IntStream.range(startPort, endPort + 1) // +1 to include endPort
                .filter(port -> !excludedPorts.contains(port))
                .boxed()
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<Integer> mergeTwoLists(List<Integer> myRunningPorts, List<Integer> myActualPorts) {

        List<Integer> merged = new ArrayList<>();

        merged.addAll(myRunningPorts);
        merged.addAll(myActualPorts);

        return merged;
    }

    private Map<IServer, Integer> getServerWithPort(Map<String, IServer> serverMap, Map<String, HashSet<Integer>> hostToAvailablePorts, String constant) {

        return serverMap.entrySet().stream()
                .filter(entry -> {
                    IServer server = entry.getValue();
                    return isServerAbbreviationNotCms(server);
                }).filter(entry -> {
                    IServer server = entry.getValue();
                    String port = getActualPort(server);
                    return constant.equals(Constants.ALL) || (constant.equals(Constants.ONLY_AUTO) && port.equals(Constants.DASH));
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

    private static Integer nextAvailableValue(HashSet<Integer> set) {
        if (!set.isEmpty()) {
            Iterator<Integer> iterator = set.iterator();
            Integer removedValue = iterator.next();
            iterator.remove();
            return removedValue;
        }
        throw new RuntimeException("Not enough ports per single VM available between 6401-6499!");
    }

    private boolean isServerAbbreviationNotCms(IServer server) {
        try {
            return !server.getServerAbbreviation().equals("cms");
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }
}
