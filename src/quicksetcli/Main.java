package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

//    Windows OS level execution:
//    C:\Users\Erik\IdeaProjects\Test\out\production\Test>java -Djava.ext.dirs="C:\Users\Erik\Documents\APPS\BI42_SP7_jars" quicksetcli/QuickSetToolNew2 -UAdministrator -PPenguin7 -Slinux-1bji:6400 -AsecEnterprise

    private static Service service = null;
    private static Boolean searchingStatus = true;

    public static void main(String[] args) {

        printProgramHeader();

        try {

            Map<String, String> credentials = validateArgs(args);

            System.out.print("Logging on...");
            service = Service.createServiceSession(credentials);
            System.out.print("successfully logged on as " + service.getMyEnterpriseSession().getUserInfo().getUserName() + " to " + service.getMyEnterpriseSession().getClusterName() + "\n");
            Main.displayOptions(service);

            } catch (SDKException exception) {

            exception.printStackTrace();

        } finally {

            if(service != null) {

                service.destroyServiceSession();

            }

            else {

                System.out.println("Service session could not be initialized.");

            }

            System.out.println("Good bye.");

        }
    }

    private static void printProgramHeader() {
        String welcomeMessage = "--- Welcome to QUICKSET CLI tool v1.00 ---";
        final int welcomeLength = welcomeMessage.length();
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
        System.out.println(welcomeMessage);
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
    }

    private static void displayOptions(Service service) throws SDKException {

        // Define scanner object
        Scanner scanner = new Scanner(System.in);

        // Show selection options
        showOptions();

        // Define selection entry
        String entry;

        do {

            entry = scanner.nextLine();

            if (!entry.isEmpty() && entry.trim().length() != 0) {

                    switch (entry) {

                        case "1":
                            System.out.println("Executing Check Request Ports workflow...");
                            IProcessBehaviour portChecker = new PortChecker(service);
                            portChecker.process();
                            showOptions();
                            break;

                        case "2":
                            System.out.println("Executing Check Heap Size workflow...");
                            IProcessBehaviour heapChecker = new HeapChecker(service);
                            heapChecker.process();
                            showOptions();
                            break;

                        case "3":
                            System.out.println("Executing Check License Key workflow...");
                            IProcessBehaviour licenseCheckerV1 = new LicenseChecker(service);
                            licenseCheckerV1.process();
                            showOptions();
                            break;

                        case "4":
                            System.out.println("Executing Check Services workflow...");
                            IProcessBehaviour servicesChecker = new ServicesChecker(service);
                            servicesChecker.process();
                            showOptions();
                            break;

                        case "5":
                            System.out.println("Executing Users and Groups workflow...");
                            IProcessBehaviour usersAndGroupsChecker = new UsersAndGroupsChecker(service);
                            usersAndGroupsChecker.process();
                            showOptions();
                            break;

                        case "quit":
                            System.out.println("Quiting program...");
                            searchingStatus = false;
                            break;

                        default:
                            System.out.print("You didn't choose any valid option!\n");
                            System.out.print("Enter value: ");
                            break;

                    }

            }

            else {

                System.out.print("Empty or blank value!\n");
                System.out.print("Enter value: ");

            }

        } while (searchingStatus);

    }

    private static void showOptions() {

        System.out.println();
        System.out.println("Select option:");
        System.out.println("1) Check Request Ports");
        System.out.println("2) Check Heap Size");
        System.out.println("3) Check License Key");
        System.out.println("4) Check Services");
        System.out.println("5) Check Users and Groups");
        System.out.println("quit) Quit");

        System.out.print("\nEnter value: ");

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

    private static Map<String, String> validateArgs(String[] args){

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