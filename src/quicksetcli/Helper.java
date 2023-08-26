package quicksetcli;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

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

    public static void askYesNoQuestion(Scanner scanner, String question, Runnable yesAction, Runnable noAction) {

        System.out.print(question + " (yes/no): ");
        String userInput = scanner.next().trim().toLowerCase();

        if (userInput.equals("yes")) {
            yesAction.run();
        } else if (userInput.equals("no")) {
            noAction.run();
        } else {
            System.out.println("Invalid input. Please enter 'yes' or 'no'.");
        }

    }

    public static void printHeaderLine(Map<String, Integer> formatterMap) {

        formatterMap.forEach((key, value) -> System.out.printf("%-" + value + "s", key));
        System.out.println();

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

