package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.Map;
import java.util.Scanner;

import static quicksetcli.Helper.*;

public class HeapSizeSingleSet extends BaseCommand{

    private final Scanner scanner;
    private Map<String, IServer> serverMap;
    private Service service;

    public HeapSizeSingleSet(Scanner scanner, Service service) {
        this.scanner = scanner;
        this.service = service;
        this.serverMap = initializeServerMap(service, Constants.SERVER_QUERY);
    }


    @Override
    public void execute() {

        System.out.print("Choose Server ID: ");
        int serverID = getIntInput(scanner, 0, serverMap.size() - 1, null, null);

        Object[] serverArray = serverMap.keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serverMap.get(key);

        String serverName = selectedServer.getTitle();
        System.out.println("Server Name: " + serverName);

        String activeHeapSize = getActiveXmx(selectedServer);
        System.out.println("Active Heap Size: " + activeHeapSize);

        String configuredHeapSize = getConfiguredXmx(selectedServer);
        System.out.println("Configured Heap Size: " + configuredHeapSize);

        System.out.print("Choose new Heap Size [1g, 512m, etc.]: ");
        String newHeapSize = validateAndExtractXmxValue(scanner);

        displayHeapSummary(selectedServer, newHeapSize);

        IExecProps serverExecProps = getExecProps(selectedServer);
        String actualServerArguments = serverExecProps.getArgs();
        String pattern = "-Xmx[0-9]{1,5}[m,g]{1}";
        String replacementString = "-Xmx" + newHeapSize;
        String modifiedArguments = getModifiedArguments(pattern, replacementString, actualServerArguments);

        askYesNoQuestion(scanner, "\nDo you want to save the new Request Port value?",
                () -> {
                    System.out.print("Saving values...");
                    serverExecProps.setArgs(modifiedArguments);
                    try {
                        selectedServer.save();
                    } catch (SDKException e) {
                        throw new RuntimeException(e);
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

    private void displayHeapSummary(IServer selectedServer, String newHeap) {
        String serverTitle = selectedServer.getTitle();
        String activeXmx = getActiveXmx(selectedServer);
        String configuredXmx = getConfiguredXmx(selectedServer);
        String serverHostname = selectedServer.getSIAHostname();

        System.out.println("\n--- Summary ---");
        System.out.println("Server Name: " + serverTitle);
        System.out.println("Active Heap Size: " + activeXmx);
        System.out.println("Configured Heap Size: " + configuredXmx);
        System.out.println("Hostname: " + serverHostname);
        System.out.println("New Heap Size: " + newHeap);
    }
}
