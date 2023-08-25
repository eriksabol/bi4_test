package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class MenuRequestPortModify implements Menu {

    private final Scanner scanner;
    private final Map<String, IServer> serverMap;
    private final Service service;

    public MenuRequestPortModify(Scanner scanner, Service service) {
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
        System.out.println();
    }

    private void handleModifyRequestPortMenu() {
        while (true) {

            displayModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 3, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    Helper.printEmptyLines(1);
                    viewRequestPorts();
                    break;
                case 2:
                    Helper.printEmptyLines(1);
                    RequestPortSingleSet requestPortSingleSet = new RequestPortSingleSet(scanner, serverMap);
                    requestPortSingleSet.execute();
                    break;
                case 3:
                    Helper.printEmptyLines(1);
                    MenuRequestPortMassModify menuMassModifyRequestPort = new MenuRequestPortMassModify(scanner, serverMap);
                    menuMassModifyRequestPort.view();
                    break;
                case -1:
                    return;
            }
        }
    }

    private void viewRequestPorts() {

        initializeServerMap();
        RequestPortView requestPortView = new RequestPortView(serverMap);
        requestPortView.execute();
    }

    private Map<String, IServer> initializeServerMap() {

        Map<String, IServer> serverMap = new HashMap<>();

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
        System.out.println();

        return serverMap;

    }

    @Override
    public void view() {
        handleModifyRequestPortMenu();
    }

}
