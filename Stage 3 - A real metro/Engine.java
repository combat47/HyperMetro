package metro;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Engine {
    static Map<String, Line> metro = new HashMap<>();

    static String[] commands;

    static void readFile(File file) throws IOException {
        GsonStreamApiRead.read(file);
        runCommand();
    }

    static String getInput() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    static void runCommand() {
        String command;

        while (true) {
            commands = getInput().trim().split("(?<!(\"\\w{1,10}))\\s(?!(\\w+\"))");
            command = commands[0];
            try {
                switch (command) {
                    case "/append" -> metro.get(commands[1].replaceAll("\"", "")).add(commands[2].replaceAll("\"", ""));
                    case "/add-head" ->
                            metro.get(commands[1].replaceAll("\"", "")).addHead(commands[2].replaceAll("\"", ""));
                    case "/remove" ->
                            metro.get(commands[1].replaceAll("\"", "")).remove(commands[2].replaceAll("\"", ""));
                    case "/connect" -> connect();
                    case "/output" -> metro.get(commands[1].replaceAll("\"", "")).printStations();
                    case "/exit" -> System.exit(0);
                    default -> System.out.println("Invalid command");
                }
            } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid command");
            }
        }
    }

    private static void connect() {
        try {
            String line1 = commands[1].replaceAll("\"", "");
            String station1 = commands[2].replaceAll("\"", "");
            String line2 = commands[3].replaceAll("\"", "");
            String station2 = commands[4].replaceAll("\"", "");
            //add connections to stations;
            metro.get(line1).getStationByName(station1).addTransfer(line2, station2);
            metro.get(line2).getStationByName(station2).addTransfer(line1, station1);

        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid command");
        }
    }
}
