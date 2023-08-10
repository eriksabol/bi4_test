package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

public interface IProcessBehaviour {

    default void printHeaderLine(Map<String, Integer> formatterMap) {

        formatterMap.forEach((key, value) -> System.out.print(String.format("%-" + value + "s", key)));
        System.out.println();

    }

    default void printDashedSpacer(Map<String, Integer> formatterMap) {

        Integer sumOfFormattedSpace = formatterMap.values().stream().reduce(0, Integer::sum);
        System.out.println(String.join("", Collections.nCopies(sumOfFormattedSpace, "-")));

    }

    default void printNumberOfReturnedObjects(IInfoObjects infoObjects) {

        if(infoObjects.size() == 1) {

            System.out.println(System.lineSeparator() + "Returned " + infoObjects.size() + " object." + System.lineSeparator());

        }

        else {

            System.out.println(System.lineSeparator() + "Returned " + infoObjects.size() + " objects." + System.lineSeparator());

        }

    }

    default void printOverallHeader(Map<String, Integer> formatterMap) {

        this.printHeaderLine(formatterMap);
        this.printDashedSpacer(formatterMap);

    }

    default void appendValueToBuffer(Map<String, Integer> formatterMap, StringBuffer stringBuffer, String keyInMap, String valueToBuffer) {

        stringBuffer.append(String.format("%-" + formatterMap.get(keyInMap) + "s", valueToBuffer));

    }

    default int getUserChoice(Scanner scanner, int min, int max) {
        int choice;
        do {
            System.out.print("Enter your choice: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
            choice = scanner.nextInt();
            if (choice < min || choice > max) {
                System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
            }
        } while (choice < min || choice > max);
        return choice;
    }

    default int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid integer: ");
            }
        }
    }

    default void askYesNoQuestion(Scanner scanner, String question, Runnable yesAction, Runnable noAction) {

        System.out.print(question + " (yes/no): ");
        String userInput = scanner.nextLine().trim().toLowerCase();

        if (userInput.equals("yes")) {
            yesAction.run();
        } else if (userInput.equals("no")) {
            noAction.run();
        } else {
            System.out.println("Invalid input. Please enter 'yes' or 'no'.");
        }

    }

    public void process() throws SDKException;

}
