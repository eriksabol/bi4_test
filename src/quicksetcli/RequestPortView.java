package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static quicksetcli.Helper.*;

public class RequestPortView extends BaseCommand{

    private final Map<String, IServer> serverMap;

    public RequestPortView(Map<String, IServer> serverMap) {
        this.serverMap = serverMap;
    }

    @Override
    public void execute() {
        displayPortTable(this.serverMap);
    }

    private void displayPortTable(Map<String, IServer> serverMap) {

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("ID", 4);
        formatterMap.put("stale", 7);
        formatterMap.put("serverTitle", 50);
        formatterMap.put("hostname", 20);
        formatterMap.put("serverStatus", 25);
        formatterMap.put("serverState", 13);
        formatterMap.put("runningPort", 15);    //  activePort
        formatterMap.put("setMethod", 15);
        formatterMap.put("actualPort", 12); // configuredPort

        printOverallHeader(formatterMap);

        AtomicInteger increment = new AtomicInteger(0);
        serverMap.keySet().stream()
                .map(key -> {
                    IServer server = serverMap.get(key);
                    StringBuffer stringBuffer = new StringBuffer();

                    try {

                        appendValueToBuffer(formatterMap, stringBuffer, "ID", String.valueOf(increment.getAndIncrement()));
                        appendValueToBuffer(formatterMap, stringBuffer, "stale", server.getRequiresRestart() ? "  *" : "");
                        appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", server.getTitle());
                        appendValueToBuffer(formatterMap, stringBuffer, "hostname", server.getSIAHostname());
                        appendValueToBuffer(formatterMap, stringBuffer, "serverStatus", server.getState().toString());
                        appendValueToBuffer(formatterMap, stringBuffer, "serverState", server.isDisabled() ? "Disabled" : "Enabled");
                        appendValueToBuffer(formatterMap, stringBuffer, "runningPort", getRunningPort(server));
                        appendValueToBuffer(formatterMap, stringBuffer, "setMethod", getPortSetMethod(server));
                        appendValueToBuffer(formatterMap, stringBuffer, "actualPort", getActualPort(server));

                    } catch (SDKException e) {
                        throw new RuntimeException(e);
                    }

                    return stringBuffer.toString();
                })
                .forEach(System.out::println);

        printEmptyLines(1);
    }
}
