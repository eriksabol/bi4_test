package quicksetcli;

import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.Map;
import java.util.Scanner;

import static quicksetcli.Helper.printEmptyLines;

public class MenuRequestPortMassModify implements Menu {
    private final Scanner scanner;
    private final Map<String, IServer> serverMap;

    public MenuRequestPortMassModify(Scanner scanner, Map<String, IServer> serverMap) {
        this.scanner = scanner;
        this.serverMap = serverMap;
    }

    public static void displayMassModifyRequestPortMenu() {
        System.out.println("Main Menu > Request Ports > Modify multiple servers:");
        System.out.println("1 Modify on all servers (except CMS)");
        System.out.println("2 Modify only on servers with port set to Auto assign (except CMS)");
        System.out.println("b " + Constants.BACK_MENU_OPTION);
        printEmptyLines(1);
    }

    private void handleMassModifyRequestPortMenu() {
        while (true) {

            displayMassModifyRequestPortMenu();
            System.out.print("Enter your choice: ");
            int requestPortMenuChoice = Helper.getIntInput(scanner, 1, 2, null, null);

            switch (requestPortMenuChoice) {
                case 1:
                    printEmptyLines(1);
                    RequestPortMultipleSet requestPortMultipleSetAll = new RequestPortMultipleSet(Constants.ALL, scanner, serverMap);
                    requestPortMultipleSetAll.execute();
                    break;
                case 2:
                    printEmptyLines(1);
                    RequestPortMultipleSet requestPortMultipleSetAuto = new RequestPortMultipleSet(Constants.ONLY_AUTO, scanner, serverMap);
                    requestPortMultipleSetAuto.execute();
                    break;
                case -1:
                    return;
            }
        }
    }

    @Override
    public void view() {
        handleMassModifyRequestPortMenu();
    }
}
