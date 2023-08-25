package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class RequestPortSingleSet extends BaseCommand{

    private final Scanner scanner;
    private final Map<String, IServer> serverMap;

    public RequestPortSingleSet(Scanner scanner, Map<String, IServer> serverMap) {
        this.scanner = scanner;
        this.serverMap = serverMap;
    }

    @Override
    public void execute() {

        System.out.print("Choose Server ID: ");
        int serverID = Helper.getIntInput(scanner, 0, serverMap.size() - 1, null, null);

        Object[] serverArray = serverMap.keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serverMap.get(key);

        String serverName = selectedServer.getTitle();
        System.out.println("Server Name: " + serverName);

        String actualRunningPort = getRunningPort(selectedServer);
        System.out.println("Actual Running Port: " + actualRunningPort);

        String actualRequestPort = getActualPort(selectedServer);
        System.out.println("Actual Request Port: " + actualRequestPort);

        List<Integer> runningPorts = getRunningPorts(serverMap);
        List<Integer> actualPorts = getActualPorts(serverMap);

        System.out.print("Choose new Request Port [6401-6499]: ");
        int newRequestPort = Helper.getIntInput(scanner, 6401, 6499,
                port -> !runningPorts.contains(port) && !actualPorts.contains(port),   // output from the function is an input for this predicate
                "Your request port is already taken or currently set on another server! Please choose another one");

        displayServerSummary(selectedServer, newRequestPort);

        IExecProps serverExecProps = getExecProps(selectedServer);
        String actualServerArguments = serverExecProps.getArgs();
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

    private void displayServerSummary(IServer selectedServer, int chosenPort) {
        String serverTitle = selectedServer.getTitle();
        String actualRunningPort = getRunningPort(selectedServer);
        String actualRequestPort = getActualPort(selectedServer);
        String serverHostname = selectedServer.getSIAHostname();

        System.out.println("\n--- Summary ---");
        System.out.println("Server Name: " + serverTitle);
        System.out.println("Current Running Port: " + actualRunningPort);
        System.out.println("Configured Request Port: " + actualRequestPort);
        System.out.println("Hostname: " + serverHostname);
        System.out.println("New Request Port: " + chosenPort);
    }

}
