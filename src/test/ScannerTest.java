package test;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ScannerTest {

    public static void main(String[] args) {

        try {

            validateArgs(args).entrySet()
                    .forEach(System.out::println);

        } catch (Exception e) {

            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter value: ");

        String entry;

        boolean searchingValue = true;

        do {

            entry = scanner.nextLine();

            if (!entry.isEmpty() && entry.trim().length() != 0) {

                switch (entry) {

                    case "1":
                        System.out.print("You pressed number 1.\n");
                        searchingValue = false;
                        System.out.println("Found the value! :)");
                        break;

                    case "2":
                        System.out.print("You pressed 2.\n");
                        System.out.print("Enter value: ");
                        break;

                    default:
                        System.out.print("You didn't press 1 or 2!\n");
                        System.out.print("Enter value: ");
                        break;

                }

            } else {

                System.out.print("Empty or blank value! Please enter new value.\n");
                System.out.print("Enter value: ");
            }

        } while (searchingValue);

    }

    private static boolean validateArgumentsPattern(String[] args) throws Exception {

        if (args.length != 4) {

            System.out.println("Invalid number of arguments -> " + args.length + ". Required number of arguments is 4.");
            throw new Exception("Invalid number of arguments -> " + args.length + ". Required number of arguments is 4.");

        }

        if ("-U".equals(args[0].substring(0, 2)) && "-P".equals(args[1].substring(0, 2)) && "-S".equals(args[2].substring(0, 2)) && "-A".equals(args[3].substring(0, 2))) {

            if (args[0].substring(2).isEmpty() || args[1].substring(2).isEmpty() || args[2].substring(2).isEmpty() || args[3].substring(2).isEmpty()) {

                System.out.println("Supplied empty value(s)!");

                return false;

            }

            return true;

        }

        System.out.println("Arguments have incorrect pattern!");
        return false;

    }

    private static Map<String, String> validateArgs(String[] args) throws Exception {

        if (validateArgumentsPattern(args)) {

            System.out.println("Validation successful.");

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", args[0].substring(2));
            credentials.put("password", args[1].substring(2));
            credentials.put("system", args[2].substring(2));
            credentials.put("authentication", args[3].substring(2));

            return credentials;

        }

        throw new Exception("Validation failed.");

    }


}