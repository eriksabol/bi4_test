package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

import java.util.Map;

public class Service {

    private static Service INSTANCE = null;

    private ISessionMgr mySessionManager = null;
    private IEnterpriseSession myEnterpriseSession = null;
    private IInfoStore myInfoStore = null;

    private Service(ISessionMgr sessionMgr, IEnterpriseSession enterpriseSession, IInfoStore infoStore) {

        this.mySessionManager = sessionMgr;
        this.myEnterpriseSession = enterpriseSession;
        this.myInfoStore = infoStore;

    }

    static public Service createServiceSession(Map<String, String> credentials) throws SDKException {

        if(INSTANCE == null) {


            ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
            IEnterpriseSession enterpriseSession = sessionManager.logon(credentials.get("username"), credentials.get("password"), credentials.get("system"), credentials.get("authentication"));
            IInfoStore infoStore = (IInfoStore) enterpriseSession.getService("InfoStore");

            return new Service(sessionManager, enterpriseSession, infoStore);

        }

        return INSTANCE;

    }

    public ISessionMgr getMySessionManager() {
        return mySessionManager;
    }

    public IEnterpriseSession getMyEnterpriseSession() {
        return myEnterpriseSession;
    }

    public IInfoStore getMyInfoStore() {
        return myInfoStore;
    }

    public void destroyServiceSession() {

        this.myEnterpriseSession.logoff();
        this.myInfoStore = null;
        this.myEnterpriseSession = null;
        this.mySessionManager = null;
        INSTANCE = null;
        System.out.println("Succesfully logged off.");

    }

}
