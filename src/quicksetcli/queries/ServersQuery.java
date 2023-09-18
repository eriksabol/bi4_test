package quicksetcli.queries;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import quicksetcli.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import static quicksetcli.others.Helper.printEmptyLines;

public class ServersQuery implements EnterpriseServers {

    public static final String SERVER_QUERY = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";
    private final Map<String, IServer> serversMap;
    private final Service service;

    public ServersQuery(Service service) {
        this.service = service;
        this.serversMap = initializeServersMap();
    }

    @Override
    public Map<String, IServer> getServersMap() {
        return serversMap;
    }

    private Map<String, IServer> initializeServersMap() {

        Map<String, IServer> serverMap = new LinkedHashMap<>();

        System.out.print("Retrieving servers map...");

        try {

            IInfoObjects myInfoObjects = service.getMyInfoStore().query(SERVER_QUERY);

                for (Object e : myInfoObjects) {

                    IInfoObject myInfoObject = (IInfoObject) e;
                    IServer server = (IServer) myInfoObject;
                    serverMap.put(server.getCUID(), server);

                }

        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

        System.out.print("done.\n");
        printEmptyLines(1);

        return serverMap;
    };

}
