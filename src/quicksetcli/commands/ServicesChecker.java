package quicksetcli.commands;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.common.IConfiguredServices;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import quicksetcli.Service;
import quicksetcli.commands.BaseCommand;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static quicksetcli.others.Helper.*;

public class ServicesChecker extends BaseCommand {

    private final Service service;

    public ServicesChecker(Service service) {
        this.service = service;
    }

    @Override
    public void execute() {

        String serverQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";

        IInfoObjects myInfoObjects = null;
        try {
            myInfoObjects = this.service.getMyInfoStore().query(serverQuery);
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 47);
        formatterMap.put("hostedServices", 62);
        formatterMap.put("serviceCUID", 25);

        printOverallHeader(formatterMap);

        for (Object e : myInfoObjects) {

            IInfoObject myInfoObject = (IInfoObject) e;
            IServer server = (IServer) myInfoObject;

            IConfiguredServices configuredServices = null;
            try {
                configuredServices = server.getHostedServices();
            } catch (SDKException ex) {
                throw new RuntimeException(ex);
            }

            Set<Integer> configuredServiceIDs = configuredServices.getConfiguredServiceIDs();

            boolean serverNameAlreadyDisplayed = false;

            for (Integer configuredServiceId : configuredServiceIDs) {

                IConfiguredService configuredService = configuredServices.get(configuredServiceId);
                String serviceCUID = configuredService.getCUID();
                String serverTitle = server.getTitle();

                String serviceQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_CUID='" + serviceCUID + "'";
                IInfoObjects serviceInfoObjects;
                try {
                    serviceInfoObjects = this.service.getMyInfoStore().query(serviceQuery);
                } catch (SDKException ex) {
                    throw new RuntimeException(ex);
                }
                IInfoObject serviceInfoObject = (IInfoObject) serviceInfoObjects.get(0);

                StringBuffer stringBuffer = new StringBuffer();

                if (!serverNameAlreadyDisplayed) {

                    appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", serverTitle);
                    appendValueToBuffer(formatterMap, stringBuffer, "hostedServices", serviceInfoObject.getDescription());
                    appendValueToBuffer(formatterMap, stringBuffer, "serviceCUID", serviceCUID);
                    serverNameAlreadyDisplayed = true;
                } else {

                    appendValueToBuffer(formatterMap, stringBuffer, "serverTitle", "");
                    appendValueToBuffer(formatterMap, stringBuffer, "hostedServices", serviceInfoObject.getDescription());
                    appendValueToBuffer(formatterMap, stringBuffer, "serviceCUID", serviceCUID);

                }

                System.out.println(stringBuffer);

            }

            printDashedSpacer(formatterMap);

        }

    }
}
