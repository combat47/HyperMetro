package metro;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        Pattern pattern = Pattern.compile(".+\\.json");
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.matches()) {
            System.out.println("Incorrect file");
            System.exit(0);
        }
        File file;
        file = new File(fileName);
        if (!file.exists()) {
            System.out.println("This file doesn't exist");
            System.exit(0);
        }
        Engine.readFile(file);
    }
}
