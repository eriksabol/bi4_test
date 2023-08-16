package quicksetcli;

import java.util.Scanner;

public class Helper {

    static int getIntInput(Scanner scanner, int min, int max) {
        while (true) {
            try {
                String inputString = scanner.next();
                if (!inputString.isEmpty()) {
                    int input = Integer.parseInt(inputString);
                    if (input >= min && input <= max) {
                        return input;
                    } else {
                        System.out.print("Invalid choice. Please enter a number between " + min + " and " + max + ": ");
                    }
                } else {
                    System.out.print("Invalid choice. Please enter a non-empty number: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid choice. Please enter a valid number: ");
            }
        }
    }
}
