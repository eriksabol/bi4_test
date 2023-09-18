package quicksetcli.menu;

import quicksetcli.commands.LicenseChecker;
import quicksetcli.Service;
import quicksetcli.commands.ServicesChecker;
import quicksetcli.commands.UsersAndGroupsChecker;

import java.util.Properties;
import java.util.Scanner;

import static quicksetcli.others.Helper.getIntInput;
import static quicksetcli.others.Helper.printEmptyLines;

public class MenuMain implements Menu {

private final Service service;
private final Properties properties;
private boolean searchingStatus = true;

    public MenuMain(Service service, Properties properties) {
        this.service = service;
        this.properties = properties;
    }

    private void displayOptions() {

        Scanner scanner = new Scanner(System.in);

        while (searchingStatus) {

            displayMainMenu();
            System.out.print("Enter your choice: ");
            int userChoice = getIntInput(scanner, 1, 5, null, null);

            printEmptyLines(1);

            switch (userChoice) {

                case 1:
                    MenuRequestPorts menuModifyRequestPort = new MenuRequestPorts(scanner, service, properties);
                    menuModifyRequestPort.view();
                    break;

                case 2:
                    MenuHeapSize menuHeapSize = new MenuHeapSize(scanner, service);
                    menuHeapSize.view();
                    break;

                case 3:
                    LicenseChecker licenseChecker = new LicenseChecker(service);
                    licenseChecker.execute();
                    break;

                case 4:
                    ServicesChecker servicesChecker = new ServicesChecker(service);
                    servicesChecker.execute();
                    break;

                case 5:
                    UsersAndGroupsChecker usersAndGroupsChecker = new UsersAndGroupsChecker(service);
                    usersAndGroupsChecker.execute();
                    break;

                case -1:
                    scanner.close();
                    searchingStatus = false;
                    break;
            }
        }
    }

    private static void displayMainMenu() {

        printEmptyLines(1);
        System.out.println("Main Menu:");
        System.out.println("1 Request Ports [+]");
        System.out.println("2 Heap Size [+]");
        System.out.println("3 License Key");
        System.out.println("4 Services");
        System.out.println("5 Users and Groups");
        System.out.println("b ← Exit");
        printEmptyLines(1);

    }

    @Override
    public void view() {
        displayOptions();
    }

}
