package quicksetcli;

import com.crystaldecisions.sdk.exception.SDKException;
import quicksetcli.exceptions.IncorrectArgumentException;
import quicksetcli.menu.MenuMain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {

//    Windows OS level execution:
//    C:\Users\Erik\IdeaProjects\Test\out\production\Test>java -Djava.ext.dirs="C:\Users\Erik\Documents\APPS\BI42_SP7_jars" quicksetcli/QuickSetToolNew2 -UAdministrator -Ppassword -Shostname:6400 -AsecEnterprise

    private static Service SERVICE = null;
    private static final Properties PROPERTIES = new Properties();

    public static void main(String[] args) {

        printProgramHeader();

        try {

            Map<String, String> credentials = validateArgs(args);

            try (InputStream inputStream = Main.class.getResourceAsStream("/quicksetcli/resources/request_port.properties")) {
                if (inputStream != null) {
                    PROPERTIES.load(inputStream);
                } else {
                    throw new RuntimeException("Error: 'request_port.properties' not found.");
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.print("Logging on...");
            SERVICE = Service.createServiceSession(credentials);
            System.out.print("successfully logged on as " + SERVICE.getMyEnterpriseSession().getUserInfo().getUserName() + " to " + SERVICE.getMyEnterpriseSession().getClusterName() + ".\n");
            MenuMain menuMain = new MenuMain(SERVICE, PROPERTIES);
            menuMain.view();

        } catch (SDKException e) {

            throw new RuntimeException(e);

        } finally {

            if (SERVICE != null) {

                SERVICE.destroyServiceSession();

            } else {

                System.out.println("Service session could not be initialized.");
            }

            System.out.println("Exiting, Good bye.");
        }
    }

    private static void printProgramHeader() {
        String welcomeMessage = "--- Welcome to BI4 QUICKSET tool v1.20 ---";
        final int welcomeLength = welcomeMessage.length();
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
        System.out.println(welcomeMessage);
        System.out.println(String.join("", Collections.nCopies(welcomeLength, "-")));
    }

    private static boolean validateArgumentsPattern(String[] args) {

        if ("-U".equals(args[0].substring(0, 2)) && "-P".equals(args[1].substring(0, 2)) && "-S".equals(args[2].substring(0, 2)) && "-A".equals(args[3].substring(0, 2))) {

            if (args[0].substring(2).isEmpty() || args[1].substring(2).isEmpty() || args[2].substring(2).isEmpty() || args[3].substring(2).isEmpty()) {

                throw new IncorrectArgumentException("Command line arguments supplied empty value(s).");
            }

            return true;
        }

        throw new IncorrectArgumentException("Command line arguments have incorrect pattern.");
    }

    private static Map<String, String> validateArgs(String[] args) {

        if (validateArgumentsPattern(args)) {

            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", args[0].substring(2));
            credentials.put("password", args[1].substring(2));
            credentials.put("system", args[2].substring(2));
            credentials.put("authentication", args[3].substring(2));

            return credentials;

        }

        throw new IncorrectArgumentException("Arguments validation failed!");
    }

}