package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.framework.ISessionMgr;
import com.crystaldecisions.sdk.occa.infostore.*;

import java.io.File;

import static quicksetcli.Helper.*;

public class CopyFiles {

    private static final String TEST_FOLDER = "C:\\path\\to\\folder\\";
    private static IInfoObject privateInfoObject;

    private static IEnterpriseSession myLogon() throws SDKException {

        ISessionMgr sessionManager = CrystalEnterprise.getSessionMgr();
        return sessionManager.logon("Administrator", "pass", "hostname:6400", "secEnterprise");

    }

    public static void main(String[] args) {


       IEnterpriseSession mySession = null;

            try {

                mySession = myLogon();

                System.out.println(">>> Logged in to " + mySession.getCMSName() + " <<<");
                printEmptyLines(1);
                IInfoStore infoStore = (IInfoStore) mySession.getService("InfoStore");

                // For different reports please change si_kind to corresponding object - it has to contain SI_FILES
                String myQuery = "select top 3 si_id, si_kind, si_guid, si_owner, si_name, si_description, si_files, si_parentid from ci_infoobjects, ci_appobjects, ci_systemobjects where si_kind='Webi' order by SI_PARENTID ASC";

                IInfoObjects infoObjects = infoStore.query(myQuery);

                for (Object infoObject : infoObjects) {

                    privateInfoObject = (IInfoObject) infoObject;

                    System.out.printf("Title: %-50s ID: %-8d Parent kind: %-50s%n", privateInfoObject.getTitle(), privateInfoObject.getID(), privateInfoObject.getParent().getKind());

                    generateFolderPath(privateInfoObject.getTitle(), privateInfoObject, mySession);

                    System.out.println("----------------------------------------------");

                }

            } catch (SDKException e) {

                e.printStackTrace();

            } finally {

                assert mySession != null;
                mySession.logoff();
                System.out.print("\n >>> Logged off from " + mySession.getCMSName() + " <<< \n");
            }

        }


        private static void generateFolderPath(String path, IInfoObject pathObject, IEnterpriseSession session) throws SDKException {

            IInfoObject parentObject = pathObject.getParent();
            String parentTitle = parentObject.getTitle();

            if (!parentTitle.equals(session.getCMSName())) {

                String appendedPath = parentTitle + File.separator + path;

                generateFolderPath(appendedPath, parentObject, session);

            } else {

                String finalString = parentTitle.substring(0, parentTitle.length()-5) + File.separator + path;
                System.out.printf("%-28s %-100s \n", "Path in BI Folders: ", finalString);

                try {

                    String specialPath = CopyFiles.TEST_FOLDER.concat(finalString);
                    File newDirectory = new File(specialPath);
                    boolean dirsCreated = newDirectory.mkdirs();    // Folders cannot exist before creation.
                    System.out.print("[Creating BI Folder directory hierarchy locally...");

                    if(dirsCreated) {

                        System.out.print("done.] \n");

                        copyFileToDirectory(specialPath, privateInfoObject);
                    }

                    else {

                        System.out.println("fail!]");
                    }

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }

        private static void copyFileToDirectory(String destinationDirectory, IInfoObject myInfoObject) {

            try {

                IFiles myFiles = myInfoObject.getFiles();

                for (Object object : myFiles) {

                    IRemoteFile myRemoteFile = (IRemoteFile) object;

                    System.out.printf("%-28s %-100s \n", "File Name (CMS based): ", myRemoteFile.getActualName());
                    System.out.printf("%-28s %-10s \n", "Size (CMS based): ", myRemoteFile.getSize() + " bytes");
                    System.out.print("[Starting to download file locally...");
                    myRemoteFile.download(destinationDirectory + File.separator + myRemoteFile.getActualName());
                    myRemoteFile.commit();
                    System.out.print("done.] \n");
                    System.out.println("File downloaded to: " + myRemoteFile.getLocalFilePath());

                }

            } catch (SDKException e) {

                e.printStackTrace();

            }

        }

}
