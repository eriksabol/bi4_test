package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;

import java.util.LinkedHashMap;
import java.util.Map;

public class UsersAndGroupsChecker extends BaseCommand {

    private final Service service;

    public UsersAndGroupsChecker(Service service) {
        this.service = service;
    }

    @Override
    public void execute() {

        String usersQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_PROGID='CrystalEnterprise.User'";
        String groupsQuery = "SELECT SI_NAME FROM CI_SYSTEMOBJECTS WHERE SI_KIND='UserGroup'";

        IInfoObjects usersInfoObjects;
        IInfoObjects groupsInfoObjects;
        try {
            usersInfoObjects = this.service.getMyInfoStore().query(usersQuery);
            groupsInfoObjects = this.service.getMyInfoStore().query(groupsQuery);
        }
        catch (SDKException e) {
            throw new RuntimeException(e);
        }

        Integer numberOfUsers = usersInfoObjects.getResultSize();
        Integer numberOfGroups = groupsInfoObjects.getResultSize();

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("cluster", 50);
        formatterMap.put("noOfUsers", 12);
        formatterMap.put("noOfGroups", 12);

        System.out.println();
        Helper.printHeaderLine(formatterMap);
        Helper.printDashedSpacer(formatterMap);

            StringBuffer stringBuffer = new StringBuffer();

        Helper.appendValueToBuffer(formatterMap, stringBuffer, "cluster", this.service.getMyEnterpriseSession().getClusterName());
        Helper.appendValueToBuffer(formatterMap, stringBuffer, "noOfUsers", String.valueOf(numberOfUsers));
        Helper.appendValueToBuffer(formatterMap, stringBuffer, "noOfGroups", String.valueOf(numberOfGroups));

            System.out.println(stringBuffer);

        Helper.printDashedSpacer(formatterMap);

    }

}