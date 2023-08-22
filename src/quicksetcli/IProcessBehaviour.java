package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

import java.util.Collections;
import java.util.Map;

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

    void process() throws SDKException;

}
