package quicksetcli.commands;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.common.IConfiguredServices;
import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import me.tongfei.progressbar.ProgressBar;
import quicksetcli.queries.ServersQuery;
import quicksetcli.Service;
import quicksetcli.others.Constants;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static quicksetcli.others.Helper.*;

public class RequestPortMultipleSet extends BaseCommand {

    private final String constant;
    private final Scanner scanner;
    private final ServersQuery serversQuery;
    private final Properties properties;
    private final Service service;

    public RequestPortMultipleSet(String constant, Scanner scanner, Properties properties, Service service) {
        this.constant = constant;
        this.scanner = scanner;
        this.properties = properties;
        this.service = service;
        this.serversQuery = new ServersQuery(service);
    }

    @Override
    public void execute() {

        Map<String, HashSet<Integer>> hostToAvailablePorts = getHostWithAvailablePorts(constant);
        Map<IServer, Integer> serverWithPort = getServerWithPort(serversQuery.getServersMap(), hostToAvailablePorts, constant);

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 50);
        formatterMap.put("port", 12);

        printOverallHeader(formatterMap);

        if (serverWithPort.isEmpty()) {

            System.out.println("No servers found for modification.");
            printEmptyLines(1);

        } else {

            serverWithPort.entrySet().stream()
                    .map(entry -> {
                        StringBuffer stringBuffer = new StringBuffer();

                        appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", entry.getKey().getTitle());
                        appendValueToBuffer(formatterMap, stringBuffer, "port", String.valueOf(entry.getValue()));

                        return stringBuffer.toString();
                    })
                    .forEach(System.out::println);

            askYesNoQuestion(scanner, "\nWould you like to save these Request Port values?",
                    () -> {

                        int taskSize = serverWithPort.size();

                        try (ProgressBar progressBar = new ProgressBar("Progress", taskSize)) {

                            System.out.println("Saving values...");

                            progressBar.stepBy(1);

                            serverWithPort.forEach((currentServer, port) -> {

                                try {

                                    IExecProps serverExecProps = getExecProps(currentServer);
                                    String serverArgs = serverExecProps.getArgs();

                                    String pattern = "-requestport\\s+\\d{4}";
                                    String replacementString = "-requestport " + port;

                                    String modifiedArguments = getModifiedArguments(pattern, replacementString, serverArgs);

                                    serverExecProps.setArgs(modifiedArguments);
                                    currentServer.save();
                                    progressBar.step();

                                } catch (SDKException e) {
                                    throw new RuntimeException(e);
                                }

                            });

                        }

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
        Map<String, HashSet<Integer>> hostWithAvailablePorts = new HashMap<>();
        Map<String, List<IServer>> hostToServersMap = new HashMap<>();

        for (IServer server : serversQuery.getServersMap().values()) {
            String host = server.getSIAHostname();
            hostToServersMap.computeIfAbsent(host, value -> new ArrayList<>()).add(server);
        }

        List<Integer> runningPortList = getRunningPorts(serversQuery.getServersMap());
        List<Integer> actualPortList = getActualPorts(serversQuery.getServersMap());
        List<Integer> actualPlusRunningList = mergeTwoLists(runningPortList, actualPortList);

        HashSet<Integer> actualPlusRunningPorts = new HashSet<>(actualPlusRunningList);
        HashSet<Integer> runningPorts = new HashSet<>(runningPortList);

        hostToServersMap.forEach((host, listOfServers) -> {

            int startPort = 6400;
            int endPort = 6499;

            String exclusionPortsValue = properties.getProperty("exclusion.ports");
            String[] portsArray = exclusionPortsValue.split(",");

            List<Integer> additionalExcludedPorts = Arrays.stream(portsArray)
                    .map(String::trim) // Trim each element
                    .map(Integer::parseInt) // Parse each element to an Integer
                    .collect(Collectors.toList()); // Collect the integers into a list

            actualPlusRunningPorts.addAll(additionalExcludedPorts);
            runningPorts.addAll(additionalExcludedPorts);

            if (constant.equals(Constants.ONLY_AUTO)) {

                HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, actualPlusRunningPorts);
                hostWithAvailablePorts.put(host, availablePorts);
            }

            if (constant.equals(Constants.ALL)) {

                HashSet<Integer> availablePorts = generateRemainingPorts(startPort, endPort, runningPorts);
                hostWithAvailablePorts.put(host, availablePorts);
            }
        });
        return hostWithAvailablePorts;
    }

    private HashSet<Integer> generateRemainingPorts(int startPort, int endPort, HashSet<Integer> excludedPorts) {
        return IntStream.range(startPort, endPort + 1) // +1 to include endPort
                .filter(port -> !excludedPorts.contains(port))
                .boxed()
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<Integer> mergeTwoLists(List<Integer> firstList, List<Integer> secondList) {

        List<Integer> merged = new ArrayList<>();

        merged.addAll(firstList);
        merged.addAll(secondList);

        return merged;
    }

    private Map<IServer, Integer> getServerWithPort(Map<String, IServer> serverMap, Map<String, HashSet<Integer>> hostWithAvailablePorts, String constant) {

        return serverMap.entrySet().stream()
                .filter(entry -> {
                    IServer server = entry.getValue();
                    String port = getActualPort(server);
                    return constant.equals(Constants.ALL) || (constant.equals(Constants.ONLY_AUTO) && port.equals(Constants.DASH));
                }).collect(Collectors.toMap(
                        Map.Entry::getValue,
                        entry -> {
                            IServer server = entry.getValue();
                            String siaHostname = server.getSIAHostname();
                            HashSet<Integer> portIntegers = hostWithAvailablePorts.get(siaHostname);
                            return nextAvailableValue(server, portIntegers);
                        },
                        (oldValue, newValue) -> oldValue,   // in case there will be duplicate => no real way, but handled
                        LinkedHashMap::new
                ));
    }

    private Integer nextAvailableValue(IServer server, HashSet<Integer> set) {

        if (set.isEmpty()) {
            throw new RuntimeException("Not enough ports available per single VM!");
        }

        Iterator<Integer> iterator = set.iterator();

        Integer cmsPort = Integer.valueOf(properties.getProperty("cms.port"));
        Integer ifrsPort = Integer.valueOf(properties.getProperty("ifrs.port"));
        Integer ofrsPort = Integer.valueOf(properties.getProperty("ofrs.port"));
        Integer[] reservedPorts = {cmsPort, ifrsPort, ofrsPort};
        List<Integer> reservedPortsList = new ArrayList<>(Arrays.asList(reservedPorts));

        boolean isCMS = isCMS(server);
        boolean isFRS = isFRS(server);

        if (!isCMS && !isFRS) {

            while (iterator.hasNext()) {

                int port = iterator.next();

                if (reservedPortsList.contains(port)) {

                    continue;
                }

                iterator.remove();
                return port;
            }

        } else {

            if (isFRS) {

                boolean isInputFRS = containsService(server, "InputFileStoreService");
                boolean isOutputFRS = containsService(server, "OutputFileStoreService");

                if (isInputFRS) {
                    set.remove(ifrsPort);
                    return ifrsPort;
                }

                if (isOutputFRS) {
                    set.remove(ofrsPort);
                    return ofrsPort;
                }
            }

            if (isCMS) {
                set.remove(cmsPort);
                return cmsPort;
            }
        }

        throw new RuntimeException("Server information mismatch!");
    }

    private boolean isFRS(IServer server) {
        try {
            return server.getServerKind().contains("fileserver");
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean containsService(IServer server, String serviceTitle) {

        boolean containsService = false;

        try {

            IConfiguredServices configuredServices = server.getHostedServices();
            @SuppressWarnings("rawtypes")
            Iterator myIterator = configuredServices.iterator();
            IInfoObjects serviceInfoObjects;

            while (myIterator.hasNext()) {
                IConfiguredService myConfiguredService = (IConfiguredService) myIterator.next();
                String serviceCUID = myConfiguredService.getCUID();
                String serviceQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_CUID='" + serviceCUID + "'";

                serviceInfoObjects = this.service.getMyInfoStore().query(serviceQuery);
                IInfoObject serviceInfoObject = (IInfoObject) serviceInfoObjects.get(0);

                if (serviceInfoObject.getTitle().equals(serviceTitle)) {

                    containsService = true;
                    break;
                }
            }

        } catch (SDKException ex) {
            throw new RuntimeException(ex);
        }

        return containsService;
    }

    private boolean isCMS(IServer server) {
        try {
            return server.getServerAbbreviation().equals("cms");
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

}
