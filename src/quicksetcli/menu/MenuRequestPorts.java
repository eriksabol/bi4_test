package quicksetcli.menu;

import quicksetcli.Service;
import quicksetcli.commands.RequestPortSingleSet;
import quicksetcli.commands.RequestPortView;
import quicksetcli.others.Constants;

import java.util.Properties;
import java.util.Scanner;

import static quicksetcli.others.Helper.getIntInput;
import static quicksetcli.others.Helper.printEmptyLines;

public class MenuRequestPorts implements Menu {

    private final Scanner scanner;
    private final Service service;
    private final Properties properties;

    public MenuRequestPorts(Scanner scanner, Service service, Properties properties) {
        this.scanner = scanner;
        this.service = service;
        this.properties = properties;
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
                    RequestPortView requestPortView = new RequestPortView(service);
                    requestPortView.execute();
                    break;
                case 2:
                    printEmptyLines(1);
                    RequestPortSingleSet requestPortSingleSet = new RequestPortSingleSet(scanner, service);
                    requestPortSingleSet.execute();
                    break;
                case 3:
                    printEmptyLines(1);
                    MenuRequestPortMassModify menuMassModifyRequestPort = new MenuRequestPortMassModify(scanner, properties, service);
                    menuMassModifyRequestPort.view();
                    break;
                case -1:
                    return;
            }
        }
    }

    @Override
    public void view() {
        handleModifyRequestPortMenu();
    }

}
