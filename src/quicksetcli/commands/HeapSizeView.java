package quicksetcli.commands;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import quicksetcli.queries.ServersQuery;
import quicksetcli.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static quicksetcli.others.Helper.*;

public class HeapSizeView extends BaseCommand {

    private final ServersQuery serversQuery;

    public HeapSizeView(Service service) {
        this.serversQuery = new ServersQuery(service);
    }

    private void displayHeapSizeTable(Map<String, IServer> serversMap) {

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
        serversMap.keySet().stream()
                .map(key -> {
                    IServer server = serversMap.get(key);
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
        displayHeapSizeTable(serversQuery.getServersMap());
    }
}
