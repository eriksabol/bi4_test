package quicksetcli.menu;

import quicksetcli.Service;
import quicksetcli.commands.RequestPortMultipleSet;
import quicksetcli.others.Constants;

import java.util.Properties;
import java.util.Scanner;

import static quicksetcli.others.Helper.getIntInput;
import static quicksetcli.others.Helper.printEmptyLines;

public class MenuRequestPortMassModify implements Menu {
    private final Scanner scanner;
    private final Properties properties;
    private final Service service;

    public MenuRequestPortMassModify(Scanner scanner, Properties properties, Service service) {
        this.scanner = scanner;
        this.properties = properties;
        this.service = service;
    }

    public static void displayMassModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports > Modify multiple servers:");
        System.out.println("1 Mass-modify all servers");
        System.out.println("2 Mass-modify servers with Auto assign");
        System.out.println("b " + Constants.BACK_MENU_OPTION);
        printEmptyLines(1);
    }

    private void handleMassModifyRequestPortMenu() {
        while (true) {

            displayMassModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = getIntInput(scanner, 1, 2, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    printEmptyLines(1);
                    RequestPortMultipleSet requestPortMultipleSetAll = new RequestPortMultipleSet(Constants.ALL, scanner, properties, service);
                    requestPortMultipleSetAll.execute();
                    break;
                case 2:
                    printEmptyLines(1);
                    RequestPortMultipleSet requestPortMultipleSetAuto = new RequestPortMultipleSet(Constants.ONLY_AUTO, scanner, properties, service);
                    requestPortMultipleSetAuto.execute();
                    break;
                case -1:
                    return;
            }
        }
    }

    @Override
    public void view() {
        handleMassModifyRequestPortMenu();
    }
}
