package quicksetcli;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.common.IConfiguredServices;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ServicesChecker implements IProcessBehaviour {

    private final Service service;

    public ServicesChecker(Service service) {
        this.service = service;
    }

    @Override
    public void process() throws SDKException {


        String serverQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query(serverQuery);

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 47);
        formatterMap.put("hostedServices", 62);
        formatterMap.put("serviceCUID", 25);

        this.printOverallHeader(formatterMap);

        for (Object e : myInfoObjects) {

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;

            IConfiguredServices configuredServices = server.getHostedServices();

            Set<Integer> configuredServiceIDs = configuredServices.getConfiguredServiceIDs();

            boolean serverNameAlreadyDisplayed = false;

            for (Integer configuredServiceId : configuredServiceIDs) {

                IConfiguredService configuredService = configuredServices.get(configuredServiceId);
                String serviceCUID = configuredService.getCUID();
                String serverTitle = server.getTitle();

                String serviceQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_CUID='" + serviceCUID +"'";
                IInfoObjects serviceInfoObjects = this.service.getMyInfoStore().query(serviceQuery);
                IInfoObject serviceInfoObject = (IInfoObject) serviceInfoObjects.get(0);

                StringBuffer stringBuffer = new StringBuffer();

                if(!serverNameAlreadyDisplayed) {

                    this.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", serverTitle);
                    this.appendValueToBuffer(formatterMap, stringBuffer, "hostedServices", serviceInfoObject.getDescription());
                    this.appendValueToBuffer(formatterMap, stringBuffer, "serviceCUID", serviceCUID);
                    serverNameAlreadyDisplayed = true;
                }
                else {

                    this.appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", "");
                    this.appendValueToBuffer(formatterMap, stringBuffer, "hostedServices", serviceInfoObject.getDescription());
                    this.appendValueToBuffer(formatterMap, stringBuffer, "serviceCUID", serviceCUID);

                }

                System.out.println(stringBuffer);

            }

            this.printDashedSpacer(formatterMap);

        }

    }
}
