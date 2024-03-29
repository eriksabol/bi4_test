package quicksetcli.commands;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import quicksetcli.queries.ServersQuery;
import quicksetcli.Service;

import java.util.Scanner;

import static quicksetcli.others.Helper.*;

public class HeapSizeSingleSet extends BaseCommand {

    private final Scanner scanner;
    private final ServersQuery serversQuery;

    public HeapSizeSingleSet(Scanner scanner, Service service) {
        this.scanner = scanner;
        this.serversQuery = new ServersQuery(service);
    }

    @Override
    public void execute() {

        System.out.print("Choose Server ID: ");
        int serverID = getIntInput(scanner, 0, serversQuery.getServersMap().size() - 1, null, null);

        Object[] serverArray = serversQuery.getServersMap().keySet().toArray();
        String key = (String) serverArray[serverID];
        IServer selectedServer = serversQuery.getServersMap().get(key);

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
