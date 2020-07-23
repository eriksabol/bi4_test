package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;

import java.util.Collections;
import java.util.Map;

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

    default void printOverallHeader(IInfoObjects infoObjects, Map<String, Integer> formatterMap) {

        this.printNumberOfReturnedObjects(infoObjects);
        this.printHeaderLine(formatterMap);
        this.printDashedSpacer(formatterMap);

    }

    default void appendValueToBuffer(Map<String, Integer> formatterMap, StringBuffer stringBuffer, String keyInMap, String valueToBuffer) {

        stringBuffer.append(String.format("%-" + formatterMap.get(keyInMap) + "s", valueToBuffer));

    }

    public void process() throws SDKException;

}
