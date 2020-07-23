package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;

import java.util.LinkedHashMap;
import java.util.Map;

public class UsersAndGroupsChecker implements IProcessBehaviour {

    private final Service service;

    public UsersAndGroupsChecker(Service service) {
        this.service = service;
    }

    @Override
    public void process() throws SDKException {

        String usersQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_PROGID='CrystalEnterprise.User'";
        String groupsQuery = "SELECT SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND='UserGroup'";

        IInfoObjects usersInfoObjects = this.service.getMyInfoStore().query(usersQuery);
        IInfoObjects groupsInfoObjects = this.service.getMyInfoStore().query(groupsQuery);

        Integer numberOfUsers = usersInfoObjects.getResultSize();
        Integer numberOfGroups = groupsInfoObjects.getResultSize();

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("cluster", 50);
        formatterMap.put("noOfUsers", 12);
        formatterMap.put("noOfGroups", 12);

        System.out.println();
        this.printHeaderLine(formatterMap);
        this.printDashedSpacer(formatterMap);

            StringBuffer stringBuffer = new StringBuffer();

            this.appendValueToBuffer(formatterMap, stringBuffer, "cluster", this.service.getMyEnterpriseSession().getClusterName());
            this.appendValueToBuffer(formatterMap, stringBuffer, "noOfUsers", String.valueOf(numberOfUsers));
            this.appendValueToBuffer(formatterMap, stringBuffer, "noOfGroups", String.valueOf(numberOfGroups));

            System.out.println(stringBuffer);

            this.printDashedSpacer(formatterMap);

    }

}