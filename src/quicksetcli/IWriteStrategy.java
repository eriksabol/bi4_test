package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

public class IWriteStrategy implements IProcessBehaviour {
    @Override
    public void process() throws SDKException {
        System.out.println("Writing to CMS database.");
    }
}
