package quicksetcli.menu;

import quicksetcli.others.Constants;
import quicksetcli.commands.HeapSizeSingleSet;
import quicksetcli.commands.HeapSizeView;
import quicksetcli.Service;

import java.util.Scanner;

import static quicksetcli.others.Helper.getIntInput;
import static quicksetcli.others.Helper.printEmptyLines;

public class MenuHeapSize implements Menu {

    private final Scanner scanner;
    private final Service service;

    public MenuHeapSize(Scanner scanner, Service service) {
        this.scanner = scanner;
        this.service = service;
    }

    private static void displayHeapSizeMenu() {
        System.out.println("Main Menu > Heap Size:");
        System.out.println("1 " + Constants.VIEW_MENU_OPTION);
        System.out.println("2 " + Constants.MODIFY_SINGLE_SERVER_MENU_OPTION);
        System.out.println("b " + Constants.BACK_MENU_OPTION);
        printEmptyLines(1);
    }

    private void handleHeapSizeMenu() {
        while (true) {

            displayHeapSizeMenu();
            System.out.print("Enter your choice: ");
            int heapSizeMenuChoice = getIntInput(scanner, 1, 3, null, null);

            switch (heapSizeMenuChoice) {
                case 1:
                    printEmptyLines(1);
                    HeapSizeView heapSizeView = new HeapSizeView(service);
                    heapSizeView.execute();
                    break;
                case 2:
                    printEmptyLines(1);
                    HeapSizeSingleSet heapSizeSingleSet = new HeapSizeSingleSet(scanner, service);
                    heapSizeSingleSet.execute();
                    break;
                case -1:
                    return;
            }
        }
    }

    @Override
    public void view() {
        handleHeapSizeMenu();
    }
}
