package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static quicksetcli.Helper.printEmptyLines;

public class Main {

//    Windows OS level execution:
//    C:\Users\Erik\IdeaProjects\Test\out\production\Test>java -Djava.ext.dirs="C:\Users\Erik\Documents\APPS\BI42_SP7_jars" quicksetcli/QuickSetToolNew2 -UAdministrator -Ppassword -Shostname:6400 -AsecEnterprise

    private static Service service = null;
    private static Boolean searchingStatus = true;

    public static void main(String[] args) {

        printProgramHeader();

        try {

            Map<String, String> credentials = validateArgs(args);

            System.out.print("Logging on...");
            service = Service.createServiceSession(credentials);
            System.out.print("successfully logged on as " + service.getMyEnterpriseSession().getUserInfo().getUserName() + " to " + service.getMyEnterpriseSession().getClusterName() + "\n");
            displayOptions(service);

        } catch (SDKException e) {

            throw new RuntimeException(e);

        } finally {

            if (service != null) {

                service.destroyServiceSession();

            } else {

                System.out.println("Service session could not be initialized.");

            }

            System.out.println("Exiting, Good bye.");

        }
    }

    private static void printProgramHeader() {
        String welcomeMessage = "--- Welcome to BI4 QUICKSET CLI tool v1.10 ---";
        final int welcomeLength = welcomeMessage.length();
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
        System.out.println(welcomeMessage);
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
    }

    public static void displayOptions(Service service) throws SDKException {

        Scanner scanner = new Scanner(System.in);

        while (searchingStatus) {

            displayMainMenu();
            System.out.print("Enter your choice: ");
            int userChoice = Helper.getIntInput(scanner, 1, 5, null, null);

            switch (userChoice) {

                case 1:
                    printEmptyLines(1);
                    MenuRequestPorts menuModifyRequestPort = new MenuRequestPorts(scanner, service);
                    menuModifyRequestPort.view();
                    break;

                case 2:
                    printEmptyLines(1);
                    HeapChecker heapChecker = new HeapChecker(service);
                    heapChecker.execute();
                    break;

                case 3:
                    printEmptyLines(1);
                    LicenseChecker licenseChecker = new LicenseChecker(service);
                    licenseChecker.execute();
                    break;

                case 4:
                    printEmptyLines(1);
                    ServicesChecker servicesChecker = new ServicesChecker(service);
                    servicesChecker.execute();
                    break;

                case 5:
                    printEmptyLines(1);
                    UsersAndGroupsChecker usersAndGroupsChecker = new UsersAndGroupsChecker(service);
                    usersAndGroupsChecker.execute();
                    break;

                case -1:
                    printEmptyLines(1);
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
        System.out.println("2 Heap Size");
        System.out.println("3 License Key");
        System.out.println("4 Services");
        System.out.println("5 Users and Groups");
        System.out.println("b ← Exit");
        printEmptyLines(1);

    }

    private static boolean validateArgumentsPattern(String[] args) {

        if (args.length != 4) {

            throw new IncorrectArgumentException("Invalid number of arguments -> " + args.length + ". Required number of arguments is 4.");

        }

        if ("-U".equals(args[0].substring(0, 2)) && "-P".equals(args[1].substring(0, 2)) && "-S".equals(args[2].substring(0, 2)) && "-A".equals(args[3].substring(0, 2))) {

            if (args[0].substring(2).isEmpty() || args[1].substring(2).isEmpty() || args[2].substring(2).isEmpty() || args[3].substring(2).isEmpty()) {

                throw new IncorrectArgumentException("Command line arguments supplied empty value(s).");
            }

            return true;
        }

        throw new IncorrectArgumentException("Command line arguments have incorrect pattern.");
    }

    private static Map<String, String> validateArgs(String[] args) {

        if (validateArgumentsPattern(args)) {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", args[0].substring(2));
            credentials.put("password", args[1].substring(2));
            credentials.put("system", args[2].substring(2));
            credentials.put("authentication", args[3].substring(2));

            return credentials;

        }

        throw new IncorrectArgumentException("Arguments validation failed!");
    }

}