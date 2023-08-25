package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.licensekey.ILicenseKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class LicenseChecker extends BaseCommand {

    private final Service service;

    public LicenseChecker(Service service) {
        this.service = service;
    }

    @Override
    public void execute() {

        String licenseQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_PROGID='CrystalEnterprise.LicenseKey'";

        IInfoObjects myInfoObjects = null;
        try {
            myInfoObjects = this.service.getMyInfoStore().query(licenseQuery);
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("licenseKey", 50);
        formatterMap.put("expiresOn", 50);

        Helper.printOverallHeader(formatterMap);

        for(Object e : myInfoObjects) {

            IInfoObject infoObject = (IInfoObject) e;
            ILicenseKey licenseKey = (ILicenseKey) infoObject;

            StringBuffer stringBuffer = new StringBuffer();

            Helper.appendValueToBuffer(formatterMap, stringBuffer, "licenseKey", licenseKey.getLicenseKey());
            Helper.appendValueToBuffer(formatterMap, stringBuffer, "expiresOn", licenseKey.getExpiryDate().toString());

            System.out.println(stringBuffer);

        }

    }

}
