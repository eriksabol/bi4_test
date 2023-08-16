package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

public interface IProcessBehaviour {

    default void printHeaderLine(Map<String, Integer> formatterMap) {

        formatterMap.forEach((key, value) -> System.out.printf("%-" + value + "s", key));
        System.out.println();

    }

    default void printDashedSpacer(Map<String, Integer> formatterMap) {

        Integer sumOfFormattedSpace = formatterMap.values().stream().reduce(0, Integer::sum);
        System.out.println(String.join("", Collections.nCopies(sumOfFormattedSpace, "-")));

    }

    default void printOverallHeader(Map<String, Integer> formatterMap) {

        this.printHeaderLine(formatterMap);
        this.printDashedSpacer(formatterMap);

    }

    default void appendValueToBuffer(Map<String, Integer> formatterMap, StringBuffer stringBuffer, String keyInMap, String valueToBuffer) {

        stringBuffer.append(String.format("%-" + formatterMap.get(keyInMap) + "s", valueToBuffer));

    }

    default void askYesNoQuestion(Scanner scanner, String question, Runnable yesAction, Runnable noAction) {

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

    void process() throws SDKException;

}
