package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static quicksetcli.Helper.*;

public class HeapSizeView extends BaseCommand{

    private final Scanner scanner;
    private Map<String, IServer> serverMap;
    private final Service service;

    public HeapSizeView(Scanner scanner, Service service) {
        this.scanner = scanner;
        this.service = service;
        this.serverMap = initializeServerMap(service, Constants.SERVER_QUERY);
    }

    private void displayHeapSizeTable(Map<String, IServer> serverMap) {

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("ID", 4);
        formatterMap.put("stale", 7);
        formatterMap.put("serverTitle", 50);
        formatterMap.put("hostname", 20);
        formatterMap.put("serverStatus", 25);
        formatterMap.put("serverState", 13);
        formatterMap.put("activeXmx", 15);
        formatterMap.put("setXmx", 15);

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
                        appendValueToBuffer(formatterMap, stringBuffer, "activeXmx", getActiveXmx(server));
                        appendValueToBuffer(formatterMap, stringBuffer, "setXmx", getConfiguredXmx(server));

                    } catch (SDKException e) {
                        throw new RuntimeException(e);
                    }

                    return stringBuffer.toString();
                })
                .forEach(System.out::println);

        printEmptyLines(1);

    }

    @Override
    public void execute() {
        displayHeapSizeTable(serverMap);
    }
}
