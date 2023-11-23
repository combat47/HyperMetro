package metro;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CommandParser {
    private final BufferedReader reader;

    private final List<String> validCommands = List.of("/append", "/add-head", "/remove", "/output", "/exit",
            "/connect", "/route", "/fastest-route");

    public CommandParser(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Read a line and parse it into command tokens.
     * <p>
     * Reads a line from the reader, then has it parsed into tokens. Will only return a valid command.
     *
     * @return Valid command as a list of strings.
     */
    List<String> getCommand() {
        List<String> command = new ArrayList<>(List.of(""));

        while (!validCommands.contains(command.get(0))) {
            try {
                var input = reader.readLine();
                if (input != null && !input.isEmpty()) {
                    command = parseString(input);
                    if (!validCommands.contains(command.get(0))) {
                        System.out.printf("Invalid command: %s%n", command.get(0));
                        System.out.printf("Valid commands are: %s%n", validCommands);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return command;
    }

    /**
     * Parse a string into parts.
     * <p>
     * Parse the supplied string into parts splitting it at spaces while keeping anything inside single/double quotes
     * together.
     *
     * @param commandLine
     *         String to be parsed.
     *
     * @return List of parts from the parsed string.
     */
    private List<String> parseString(final String commandLine) {
        StringBuilder segment      = new StringBuilder();
        List<String>  parts        = new ArrayList<>();
        boolean       doubleQuotes = false;
        boolean       singleQuotes = false;

        for (var ch : commandLine.toCharArray()) {
            switch (ch) {
                case ' ' -> {
                    if (singleQuotes || doubleQuotes) {
                        segment.append(ch);
                        break;
                    }

                    if (!segment.isEmpty()) {
                        parts.add(segment.toString());
                        segment.setLength(0);
                    }
                }

                case '"' -> {
                    if (singleQuotes) {
                        segment.append(ch);
                        break;
                    }

                    if (doubleQuotes) {
                        parts.add(segment.toString());
                        segment.setLength(0);
                        doubleQuotes = false;
                        break;
                    }

                    doubleQuotes = true;
                }

                case '\'' -> {
                    if (doubleQuotes) {
                        segment.append(ch);
                        break;
                    }

                    if (singleQuotes) {
                        parts.add(segment.toString());
                        segment.setLength(0);
                        singleQuotes = false;
                        break;
                    }

                    singleQuotes = true;
                }

                case '\n' -> {
                    if (!segment.isEmpty()) {
                        parts.add(segment.toString());
                        segment.setLength(0);
                    }
                }

                default -> segment.append(ch);
            }
        }

        if (!segment.isEmpty()) {
            parts.add(segment.toString());
        }

        return parts;
    }
}
