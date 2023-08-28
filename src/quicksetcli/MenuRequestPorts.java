package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static quicksetcli.Helper.getIntInput;
import static quicksetcli.Helper.printEmptyLines;


public class MenuRequestPorts implements Menu {

    private final Scanner scanner;
    private Map<String, IServer> serverMap;
    private final Service service;

    public MenuRequestPorts(Scanner scanner, Service service) {
        this.scanner = scanner;
        this.service = service;
        this.serverMap = initializeServerMap();

    }

    private static void displayModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports:");
        System.out.println("1 " + Constants.VIEW_MENU_OPTION);
        System.out.println("2 " + Constants.MODIFY_SINGLE_SERVER_MENU_OPTION);
        System.out.println("3 " + Constants.MODIFY_MULTIPLE_SERVERS_MENU_OPTION + " [+]");
        System.out.println("b " + Constants.BACK_MENU_OPTION);
        printEmptyLines(1);
    }

    private void handleModifyRequestPortMenu() {
        while (true) {

            displayModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = getIntInput(scanner, 1, 3, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    printEmptyLines(1);
                    viewRequestPorts();
                    break;
                case 2:
                    printEmptyLines(1);
                    RequestPortSingleSet requestPortSingleSet = new RequestPortSingleSet(scanner, serverMap);
                    requestPortSingleSet.execute();
                    break;
                case 3:
                    printEmptyLines(1);
                    MenuRequestPortMassModify menuMassModifyRequestPort = new MenuRequestPortMassModify(scanner, serverMap);
                    menuMassModifyRequestPort.view();
                    break;
                case -1:
                    return;
            }
        }
    }

    public void setServerMap(Map<String, IServer> serverMap) {
        this.serverMap = serverMap;
    }

    private void viewRequestPorts() {

        Map<String, IServer> updatedServerMap = initializeServerMap();
        setServerMap(updatedServerMap);
        RequestPortView requestPortView = new RequestPortView(serverMap);
        requestPortView.execute();
    }

    private Map<String, IServer> initializeServerMap() {

        Map<String, IServer> serverMap = new LinkedHashMap<>();

        IInfoObjects myInfoObjects;

        System.out.print("Initializing server map...");

        try {
            myInfoObjects = service.getMyInfoStore().query("SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC");

            for (Object e : myInfoObjects) {

                IInfoObject myInfoObject = (IInfoObject) e;
                IServer server = (IServer) myInfoObject;
                serverMap.put(server.getCUID(), server);

            }

        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
        System.out.print("done.\n");
        printEmptyLines(1);

        return serverMap;

    }

    @Override
    public void view() {
        handleModifyRequestPortMenu();
    }

}
