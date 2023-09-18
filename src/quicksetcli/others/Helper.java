package quicksetcli.others;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

    public static int getIntInput(Scanner scanner, int min, int max, Predicate<Integer> additionalCheck, String errorMessage) {
        while (true) {
            try {
                String inputString = scanner.next().toLowerCase();
                if (!inputString.isEmpty()) {
                    if (inputString.equals("b") || inputString.equals("back")) {
                        return -1;
                    }
                    int input = Integer.parseInt(inputString);
                    if (input >= min && input <= max && (additionalCheck == null || additionalCheck.test(input))) {
                        return input;
                    } else {
                        if (additionalCheck != null && !additionalCheck.test(input)) {
                            System.out.print(errorMessage + " ");
                        } else {
                            System.out.print("Please enter a number between " + min + " and " + max + ": ");
                        }
                    }
                } else {
                    System.out.print("Invalid choice. Please enter a non-empty number: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid choice. Please enter a valid number: ");
            }
        }
    }

    public static String validateAndExtractXmxValue(Scanner scanner) {

        while (true) {
            try {
                String input = scanner.next();
                String patternStr = "^(\\d+)([mMgG])$";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(input);

                if (matcher.matches()) {
                    int value = Integer.parseInt(matcher.group(1));
                    String unit = matcher.group(2).toLowerCase();

                    if ((unit.equals("m") && value >= 64 && value <= 1024) ||
                            (unit.equals("g") && value >= 1 && value <= 64)) {
                        return input;
                    } else {
                        System.out.print("Value must be within valid range (64M - 1024M, 1G - 64G): ");
                    }
                } else {
                    System.out.print("Invalid format. Use a number followed by 'm' or 'g': ");
                }
            } catch (Exception e) {
                System.out.print("Unable to validate input!");
            }
        }
    }

    public static void askYesNoQuestion(Scanner scanner, String question, Runnable yesAction, Runnable noAction) {

        System.out.print(question + " (yes/no): ");
        String userInput = scanner.next().trim().toLowerCase();

        if (userInput.equals("yes")) {
            yesAction.run();
        } else if (userInput.equals("no")) {
            noAction.run();
        } else {
            System.out.println("Invalid input. Please enter 'yes' or 'no': ");
        }

    }

    public static void printHeaderLine(Map<String, Integer> formatterMap) {

        formatterMap.forEach((key, value) -> System.out.printf("%-" + value + "s", key));
        printEmptyLines(1);

    }

    public static void printDashedSpacer(Map<String, Integer> formatterMap) {

        Integer sumOfFormattedSpace = formatterMap.values().stream().reduce(0, Integer::sum);
        System.out.println(String.join("", Collections.nCopies(sumOfFormattedSpace, "-")));

    }

    public static void printOverallHeader(Map<String, Integer> formatterMap) {

        printHeaderLine(formatterMap);
        printDashedSpacer(formatterMap);

    }

    public static void appendValueToBuffer(Map<String, Integer> formatterMap, StringBuffer stringBuffer, String keyInMap, String valueToBuffer) {

        stringBuffer.append(String.format("%-" + formatterMap.get(keyInMap) + "s", valueToBuffer));

    }

    public static void printEmptyLines(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println();
        }
    }
}

