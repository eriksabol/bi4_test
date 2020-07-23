package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredContainer;
import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeapChecker implements IProcessBehaviour {

    private final Service service;

    public HeapChecker(Service service) {
        this.service = service;
    }

    @Override
    public void process() throws SDKException {


        String serverQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query(serverQuery);

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 50);
        formatterMap.put("serverState", 25);
        formatterMap.put("currentXmx", 15);
        formatterMap.put("setXmx", 15);
        formatterMap.put("rqrsRestart", 15);

        this.printOverallHeader(myInfoObjects, formatterMap);

        for (Object e : myInfoObjects) {

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;

            IConfiguredContainer configuredContainer = server.getContainer();
            IExecProps serverExecProps = configuredContainer.getExecProps();

            String actualServerExecProps = serverExecProps.getArgs();

            StringBuffer stringBuffer = new StringBuffer();

            this.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", server.getTitle());
            this.appendValueToBuffer(formatterMap, stringBuffer, "serverState", server.getState().toString());

            // Pattern for Xmx
            Pattern patternXmx = Pattern.compile("Xmx[0-9]{1,5}[m,g]{1}");

            // Matcher for set value of Xmx
            Matcher matcherSetXmx = patternXmx.matcher(server.getCurrentCommandLine());

            if(matcherSetXmx.find()) {

                String xmxSetString = matcherSetXmx.group();
                this.appendValueToBuffer(formatterMap, stringBuffer, "currentXmx", xmxSetString);

            }

            else {

                this.appendValueToBuffer(formatterMap, stringBuffer, "currentXmx", "-");

            }

            // Matcher for currently active running value of Xmx
            Matcher matcherXmx = patternXmx.matcher(serverExecProps.getArgs());

            if(matcherXmx.find()) {

                String xmxString = matcherXmx.group();
                this.appendValueToBuffer(formatterMap, stringBuffer, "setXmx", xmxString);

            }

            else {

                this.appendValueToBuffer(formatterMap, stringBuffer, "setXmx", "-");

            }

            this.appendValueToBuffer(formatterMap, stringBuffer, "rqrsRestart", String.valueOf(server.getRequiresRestart()));

            System.out.println(stringBuffer);

        }

    }

}
