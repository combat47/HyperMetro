package metro;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Controller {
    private final HashMap<String, MetroLine> metroLines;
    private final CommandParser              parser;

    Controller(HashMap<String, MetroLine> lines, CommandParser parser) {
        this.metroLines = lines;
        this.parser = parser;
    }

    void start() {
        boolean processCommands = true;

        while (processCommands) {
            var command = parser.getCommand();
            switch (command.get(0)) {
                case "/exit" -> processCommands = false;

                // command(1) is line name to output
                case "/output" -> {
                    if (command.size() == 2) {
                        var lineName = command.get(1);
                        if (isValidLineName(lineName)) {
                            metroLines.get(lineName).printStations();
                        }
                    } else {
                        System.out.println("Invalid format! Command should be: /output LINE");
                    }
                }

                // for /append, /add-head and /remove,
                // command(1) is line name to append station to
                // command(2) is the station name to append/add/remove
                case "/append", "/add-head" -> {
                    if (command.size() == 3 || command.size() == 4) {
                        var lineName = command.get(1);
                        var statName = command.get(2);
                        var time     = command.size() == 4 ? Integer.parseInt(command.get(3)) : 0;
                        if (isValidLineName(lineName)) {
                            if (command.get(0).equals("/append")) {
                                metroLines.get(lineName).append(statName, time);
                            } else {
                                metroLines.get(lineName).addHead(statName, time);
                            }
                        }
                    } else {
                        System.out.printf("Invalid format! Command should be: %s LINE STATION [TIME]%n",
                                command.get(0));
                    }
                }
                case "/remove" -> {
                    if (command.size() == 3) {
                        var lineName = command.get(1);
                        if (isValidLineName(lineName)) {
                            metroLines.get(lineName).remove(command.get(2));
                        }
                    } else {
                        System.out.println("Invalid format! Command should be: /remove LINE STATION");
                    }
                }

                // command(1) and command(3) are the line names to connect,
                // command(2) and command(4) are the station names
                case "/connect" -> {
                    if (command.size() == 5) {
                        var lineFrom = command.get(1);
                        var lineTo   = command.get(3);
                        if (isValidLineName(lineFrom) && isValidLineName(lineTo)) {
                            Station stationFrom = metroLines.get(lineFrom)
                                    .getStation(command.get(2));
                            Station stationTo = metroLines.get(lineTo)
                                    .getStation(command.get(4));
                            if (stationFrom != null && stationTo != null) {
                                stationFrom.setTransfers(stationTo);
                                stationTo.setTransfers(stationFrom);
                            }
                        }
                    } else {
                        System.out.println("Invalid format! Command should be: " +
                                "/connect LINE1 STATION1 LINE2 STATION2");
                    }
                }

                // command(1) and command(2) are the starting line and station name (respectively) of the route
                // to find to command(3) and command(4), the ending line and station name (respectively)
                case "/route" -> {
                    if (command.size() == 5) {
                        Station start = metroLines.get(command.get(1)).getStation(command.get(2));
                        Station end   = metroLines.get(command.get(3)).getStation(command.get(4));
                        printRoute(getRoute(start, end, false).getFirst());
                    } else {
                        System.out.println("Invalid format! Command should be: " +
                                "/route START_LINE START_STATION END_LINE END_STATION");
                    }
                }

                case "/fastest-route" -> {
                    if (command.size() == 5) {
                        Station start = metroLines.get(command.get(1)).getStation(command.get(2));
                        Station end   = metroLines.get(command.get(3)).getStation(command.get(4));
                        var     paths = getRoute(start, end, true);
                        fastestRoute(paths);
                    } else {
                        System.out.println("Invalid format! Command should be: " +
                                "/fastest-route START_LINE START_STATION END_LINE END_STATION");
                    }
                }
            }
        }
    }

    /**
     * Determines if the line name is valid or not.
     *
     * @param lineName
     *         String containing the name of the line to check for
     *
     * @return true if the line is in our lines map
     */
    private boolean isValidLineName(final String lineName) {
        if (metroLines.containsKey(lineName)) {
            return true;
        }
        System.out.println("Invalid line name: " + lineName);
        return false;
    }

    /**
     * Find a route between two stations using breadth first search
     * <p>
     * Takes two stations and attempts to find a route between them. If successful, returns a linked list containing all
     * the stations on the path. Otherwise, we return null indicating failure to find a route.
     *
     * @param start
     *         Station object to start the search from
     * @param end
     *         Station object to begin the search from
     * @param getAllPaths
     *         flag for if we find all paths or just the first one
     *
     * @return a linked list containing all the stations on the route or null if we couldn't find a route
     */
    LinkedList<LinkedList<Station>> getRoute(final Station start, final Station end,
                                             final boolean getAllPaths) {
        LinkedList<LinkedList<Station>> paths = new LinkedList<>();

        if (start != null && end != null) {
            LinkedList<LinkedList<Station>> queue   = new LinkedList<>();
            HashSet<Station>                visited = new HashSet<>();

            queue.addLast(new LinkedList<>(List.of(start)));

            while (!queue.isEmpty()) {
                var path = queue.removeFirst(); // get the first path in the queue
                var node = path.getLast(); // get the last node in the path
                if (node == end) {  // if the node matches the end, we're done
                    paths.add(path);
                    if (getAllPaths) {
                        continue;
                    }
                    return paths;
                }

                if (!visited.contains(node)) {  // check if we've already visited this node
                    for (var neighbor : node.getNeighbors()) {  // add the neighbors of the node to the path
                        var newPath = new LinkedList<>(path);
                        newPath.addLast(neighbor);
                        queue.addLast(newPath); // add the new path with a neighbor to the queue
                    }
                    visited.add(node);  // mark the node as visited
                }
            }
        }

        return (getAllPaths && !paths.isEmpty()) ? paths : null;
    }

    private void fastestRoute(LinkedList<LinkedList<Station>> paths) {
        LinkedList<Station> shortestPath = new LinkedList<>();
        int                 shortestTime = Integer.MAX_VALUE;

        for (var path : paths) {
            int time = 0;

            for (int index = 1; index < path.size(); index++) {
                Station curr = path.get(index);
                Station prev = path.get(index - 1);
                // if the line name doesn't match, we did a transfer
 
