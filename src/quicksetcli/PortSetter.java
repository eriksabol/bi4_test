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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortSetter implements IProcessBehaviour {

    private final Service service;

    public PortSetter(Service service) {

        this.service = service;
    }

    private Map<String, IServer> serverMap = new LinkedHashMap<>();

    public static void displayModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports:");
        System.out.println("1) View");
        System.out.println("2) Single server modification");
        System.out.println("3) Mass modification");
        System.out.println("4) \u2190 Back");
        System.out.println();
    }

    @Override
    public void process() throws SDKException {

        String portQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";
        initializeServerMap(this.serverMap, portQuery);
        displayPortTable(this.serverMap);
    }

    public void handleModifyRequestPortMenu(Scanner scanner) throws SDKException {
        while (true) {

            displayModifyRequestPortMenu();
            int requestPortMenuChoice = getUserChoice(scanner, 1, 4);

            switch (requestPortMenuChoice) {
                case 1:
                    System.out.println("Performing actualize state.");
                    process();
                    break;
                case 2:
                    System.out.println("Performing modification of single server request port.");
                    handleModifyRequestPortActivityMenu(scanner);
                    break;
                case 3:
                    System.out.println("Performing mass modification of request ports.");
                    break;
                case 4:
                    return; // Return to previous menu
            }
        }
    }

    private void displayPortTable(Map<String, IServer> serverMap) throws SDKException {

        int increment = 0;

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

        for (String key : serverMap.keySet()) {
            IExecProps serverExecProps = getActualServerProps(serverMap, key);
            StringBuffer stringBuffer = new StringBuffer();

            appendValueToBuffer(formatterMap, stringBuffer, "ID", String.valueOf(increment));
            appendValueToBuffer(formatterMap, stringBuffer, "stale", serverMap.get(key).getRequiresRestart() ? "  *" : "");
            appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", serverMap.get(key).getTitle());
            appendValueToBuffer(formatterMap, stringBuffer, "serverStatus", serverMap.get(key).getState().toString());
            appendValueToBuffer(formatterMap, stringBuffer, "serverState", serverMap.get(key).isDisabled() ? "Disabled" : "Enabled");
            appendValueToBuffer(formatterMap, stringBuffer, "runningPort", getRunningPort(serverMap, key));
            appendValueToBuffer(formatterMap, stringBuffer, "setMethod", getPortSetMethod(serverExecProps.getArgs()));
            appendValueToBuffer(formatterMap, stringBuffer, "actualPort", getActualPort(serverExecProps));
            appendValueToBuffer(formatterMap, stringBuffer, "CUID", serverMap.get(key).getCUID());
            System.out.println(stringBuffer);
            increment++;

        }

        System.out.println();

    }

    private IExecProps getActualServerProps(Map<String, IServer> serverMap, String key) throws SDKException {

        IConfiguredContainer configuredContainer = serverMap.get(key).getContainer();
        return configuredContainer.getExecProps();

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

    private void initializeServerMap(Map<String, IServer> serverMap, String portQuery) throws SDKException {

        System.out.print("Initializing server map...");

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query(portQuery);

        for (Object e : myInfoObjects) {

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;
            serverMap.put(server.getCUID(), server);

        }
        System.out.print("done.\n");
        System.out.println();
    }

    private void handleModifyRequestPortActivityMenu(Scanner scanner) throws SDKException {

        System.out.print("Choose Server ID: ");
        int serverID = getIntInput(scanner);

        Object[] serverArray = serverMap.keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serverMap.get(key);
        String serverCUID = selectedServer.getCUID();
        String serverName = selectedServer.getTitle();
        System.out.println("Server Name: " + serverName);

        String actualRequestPort = getRunningPort(serverMap, serverCUID);
        System.out.println("Actual Request Port: " + actualRequestPort);

        System.out.print("Choose new Request Port [6401-6499]: ");
        int newRequestPort = getIntInput(scanner);

        System.out.println("\n--- Summary ---");
        System.out.println("Server Name: " + serverName);
        System.out.println("Actual Request Port: " + actualRequestPort);
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
                },
                () -> {
                    System.out.println("Doing nothing!");
                    System.out.println();
                }
        );
    }
}
