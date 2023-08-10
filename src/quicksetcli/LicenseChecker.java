package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.licensekey.ILicenseKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class LicenseChecker implements IProcessBehaviour {

    private final Service service;

    public LicenseChecker(Service service) {
        this.service = service;
    }

    @Override
    public void process() throws SDKException {

        String licenseQuery = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_PROGID='CrystalEnterprise.LicenseKey'";

        IInfoObjects myInfoObjects = this.service.getMyInfoStore().query(licenseQuery);

        //Definition of fields
        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("licenseKey", 50);
        formatterMap.put("expiresOn", 50);

        this.printOverallHeader(formatterMap);

        for(Object e : myInfoObjects) {

            IInfoObject infoObject = (IInfoObject) e;
            ILicenseKey licenseKey = (ILicenseKey) infoObject;

            StringBuffer stringBuffer = new StringBuffer();

            this.appendValueToBuffer(formatterMap, stringBuffer, "licenseKey", licenseKey.getLicenseKey());
            this.appendValueToBuffer(formatterMap, stringBuffer, "expiresOn", licenseKey.getExpiryDate().toString());

            System.out.println(stringBuffer);

        }

    }

}
