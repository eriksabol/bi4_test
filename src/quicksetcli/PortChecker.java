package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredContainer;
import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.businessobjects.sdk.plugin.desktop.common.IMetric;
import com.businessobjects.sdk.plugin.desktop.common.IMetrics;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IServerMetrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortChecker implements IProcessBehaviour {

    private final Service service;

    public PortChecker(Service service) {

        this.service = service;
    }

    @Override
    public void process() throws SDKException {

        String portQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query(portQuery);

        int increment = 0;

        // Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("ID", 4);
        formatterMap.put("stale", 7);
        formatterMap.put("serverTitle", 50);
        formatterMap.put("serverStatus", 25);
        formatterMap.put("serverState", 13);
        formatterMap.put("runningPort", 15);
        formatterMap.put("setMethod", 15);
        formatterMap.put("nowSetTo", 10);
        formatterMap.put("CUID", 40);

        this.printOverallHeader(formatterMap);

        for (Object e : myInfoObjects) {

            increment++;

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;

            StringBuffer stringBuffer = new StringBuffer();

            this.appendValueToBuffer(formatterMap, stringBuffer, "ID", String.valueOf(increment));
            this.appendValueToBuffer(formatterMap, stringBuffer, "stale", server.getRequiresRestart() ? "  *" : "");
            this.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", server.getTitle());
            this.appendValueToBuffer(formatterMap, stringBuffer, "serverStatus", server.getState().toString());
            this.appendValueToBuffer(formatterMap, stringBuffer, "serverState", server.isDisabled() ? "Disabled" : "Enabled");

            if (server.isAlive() && server.getState() != null) {

                IServerMetrics serverMetrics = server.getMetrics();

                IMetrics metrics = serverMetrics.getMetrics("ISGeneralAdmin");

                for (Object m : metrics) {

                    IMetric metric = (IMetric) m;

                    if (metric.getName().equals("ISPROP_GEN_HOST_PORT")) {

                        this.appendValueToBuffer(formatterMap, stringBuffer, "runningPort", metric.getValue().toString());
                    }

                }

            }

            else {

                this.appendValueToBuffer(formatterMap, stringBuffer, "runningPort", "-");

            }

            IConfiguredContainer configuredContainer = server.getContainer();
            IExecProps serverExecProps = configuredContainer.getExecProps();
            String actualServerExecProps = serverExecProps.getArgs();

            if(actualServerExecProps.contains("-requestport")) {

                this.appendValueToBuffer(formatterMap, stringBuffer, "setMethod", "Manual");

            }

            else {

                this.appendValueToBuffer(formatterMap, stringBuffer, "setMethod", "Auto");
            }

            Pattern pattern = Pattern.compile("-requestport\\s[0-9]{1,5}");
            Matcher matcher = pattern.matcher(serverExecProps.getArgs());

            if(matcher.find()) {

                String requestPortString = matcher.group();
                String[] portNumberArray = requestPortString.split("-requestport\\s");
                String portNumber = String.join("", portNumberArray);

                this.appendValueToBuffer(formatterMap, stringBuffer, "nowSetTo", portNumber);

            }

            else {

                this.appendValueToBuffer(formatterMap, stringBuffer, "nowSetTo", "-");
            }

            this.appendValueToBuffer(formatterMap, stringBuffer, "CUID", server.getCUID());

            System.out.println(stringBuffer);

        }

    }

}
