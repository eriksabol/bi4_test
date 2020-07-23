package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;

public class IReadStrategy implements IProcessBehaviour{
    @Override
    public void process() throws SDKException {
        System.out.println("Reading from CMS database.");
    }
}
