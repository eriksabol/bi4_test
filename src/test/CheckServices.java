package test;

import com.businessobjects.sdk.plugin.desktop.common.IConfiguredService;
import com.businessobjects.sdk.plugin.desktop.common.IConfiguredServices;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.Collections;
import java.util.Set;

public class CheckServices {

    private final IEnterpriseSession enterpriseSession;

    public CheckServices(IEnterpriseSession enterpriseSession) {
        this.enterpriseSession = enterpriseSession;
    }

    public void showServices() throws SDKException {

        IInfoStore myInfoStore = (IInfoStore) this.enterpriseSession.getService("InfoStore");
        //String serverQuery = "SELECT SI_NAME, SI_CONFIGURED_CONTAINERS, SI_REQUIRES_RESTART, SI_CURRENT_COMMAND_LINE, SI_ENTERPRISENODE, SI_SERVER_WAITING_FOR_RESOURCES, SI_SERVER_IS_ALIVE, SI_METRICS, SI_KIND, SI_ID, SI_DESCRIPTION FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";
        String serverQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";

        IInfoObjects myInfoObjects = myInfoStore.query(serverQuery);
        System.out.println("\nReturned " + myInfoObjects.size() + " objects.");
        System.out.println();

        System.out.printf("%-47s%-62s%-25s%n", "serverTitle", "hostedServices", "serviceCUID");
        System.out.println(String.join("", Collections.nCopies(134, "-")));

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
                IInfoObjects serviceInfoObjects = myInfoStore.query(serviceQuery);
                IInfoObject serviceInfoObject = (IInfoObject) serviceInfoObjects.get(0);

                if(!serverNameAlreadyDisplayed) {

                    System.out.printf("%-47s%-62s%-25s%n", serverTitle, serviceInfoObject.getDescription(), serviceCUID);
                    serverNameAlreadyDisplayed = true;
                }
                else {

                    System.out.printf("%-47s%-62s%-25s%n", "", serviceInfoObject.getDescription(), serviceCUID);

                }


                //Config properties!!!

//                IConfigProperties configProperties = configuredService.getConfigProps();
//
//                String[] propertyNames = configProperties.getPropNames();
//
//                System.out.println(propertyNames.length);
//
//                if(propertyNames.length == 0) {
//
//                    System.out.println("No property.");
//                }
//
//                else {
//
//                    for(String propertyName : propertyNames) {
//
//                        System.out.println("**** Property name: " + propertyName);
//                        IConfigProperty configProperty = configProperties.getProp(propertyName);
//                        System.out.println("******** Configuration property name: " + configProperty.getDisplayName(Locale.ENGLISH));
//                        System.out.println("******** Configuration property value: " + configProperty.getValue().toString());
//
//                    }
//
//                }

                //Config properties!

            }

            System.out.println(String.join("", Collections.nCopies(134, "-")));

        }

    }
}
