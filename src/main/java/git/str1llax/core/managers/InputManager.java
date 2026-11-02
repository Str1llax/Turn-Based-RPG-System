package git.str1llax.core.managers;

import java.util.Scanner;

public class InputManager {
    public static final Scanner SCANNER = new Scanner(System.in);

    public static int getValidInput(int min, int max) {
        int input = min-1;
        while (input < min || input > max) {
            System.out.print("> ");
            if (SCANNER.hasNextInt()) {
                input = SCANNER.nextInt();
            }
            SCANNER.nextLine();
        }
        return input;
    }

    public static double getValidInput(double min, double max) {
        double input = min-1;
        while (input < min || input > max) {
            System.out.print("> ");
            if (SCANNER.hasNextDouble()) {
                input = SCANNER.nextDouble();
            }
            SCANNER.nextLine();
        }
        return input;
    }
}
