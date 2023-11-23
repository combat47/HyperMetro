package metro;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        var reader = new BufferedReader(new InputStreamReader(System.in));
        if (args.length == 0) {
            System.out.println("Please provide a filename to read from.");
            return;
        }
        if (args.length > 1) {
            System.out.println("Please provide only ONE filename to read from.");
            return;
        }
        readFile(args[0], reader);
    }

    static void readFile(final String filename, final BufferedReader reader) {
        var lines = FileOperations.readJSONFile(filename);
        if (lines == null) {
            return;
        }
        var controller = new Controller(lines, new CommandParser(reader));
        controller.start();
    }
}
