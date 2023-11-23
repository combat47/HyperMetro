package metro;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileOperations {
    private static final HashMap<Station, HashMap<String, String>> TRANSFERS = new HashMap<>();

    private static String lineName;

    private FileOperations() {
    }

    /**
     * Read a JSON file.
     * <p>
     * Attempts to read the specified JSON file and return the contents in a map with each line found in a MetroLine
     * object. Outputs an error and returns null if the file doesn't exist.
     *
     * @param filename
     *         String for the filename to read.
     *
     * @return Map of line name and corresponding MetroLine object or null if the file didn't exist.
     */
    static HashMap<String, MetroLine> readJSONFile(final String filename) {
        HashMap<String, MetroLine> metroLines = new HashMap<>();
        try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
            parseJSONFile(file, metroLines);
        } catch (FileNotFoundException e) {
            System.out.println("Error! Such a file doesn't exist!");
            return null;
        } catch (JsonSyntaxException e) {
            System.out.println("File to be read is malformed JSON. Please specify a valid JSON file.");
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return metroLines;
    }

    /**
     * Parse a JSON file and create necessary objects.
     * <p>
     * Parse the JSON file creating MetroLine and Station objects as needed in the specified order.
     *
     * @param file
     *         Object to read JSON from.
     */
    private static void parseJSONFile(final BufferedReader file, HashMap<String, MetroLine> metroLines) throws JsonSyntaxException {
        // map to hold each metro line keyed by line name
        TRANSFERS.clear();

        JsonElement fileJsonParseTree = JsonParser.parseReader(file);

        if (fileJsonParseTree.isJsonNull()) {     // if we have a JsonNull value, the file read is empty
            return;
        }

        createLines(fileJsonParseTree.getAsJsonObject(), metroLines);

        // add any transfer points between the lines. Needs to be done after the lines
        // are created to ensure we have all the necessary station objects created.
        for (var transferFromStation : TRANSFERS.keySet()) {
            var transferLines = TRANSFERS.get(transferFromStation);
            transferLines.forEach((line, station) -> {
                Station transferToStation = metroLines.get(line).getStation(station);
                transferFromStation.setTransfers(transferToStation);
            });
        }
    }

    /**
     * Creates the map of lines and stations.
     * <p>
     * Goes through the parse tree of the file processing each line object and returning a map of each line with their
     * respective stations in the correct order.
     *
     * @param fileObject
     *         the JSON object holding the parse tree of the read file
     */
    private static void createLines(final JsonObject fileObject, HashMap<String, MetroLine> metroLines) {
        // iterate over each metro line in the file
        for (var metroLine : fileObject.entrySet()) {
            lineName = metroLine.getKey();
            var stations = metroLine.getValue();


            if (stations.isJsonObject()) {
                createLineFromJsonObject(stations.getAsJsonObject(), metroLines);
            }

            if (stations.isJsonArray()) {
                createLineFromJsonArray(stations.getAsJsonArray(), metroLines);
            }
        }
    }

    private static void createLineFromJsonObject(final JsonObject lineObject, HashMap<String, MetroLine> metroLines) {
        // map to hold the stations, sorts by ascending key value
        TreeMap<Integer, Station> stationTreeMap = new TreeMap<>();

        // iterate through each station, reading its specifications (name, transfer status, etc.)
        for (var stationEntry : lineObject.entrySet()) {
            // get details for the station
            int     stationNumber = Integer.parseInt(stationEntry.getKey());
            Station station       = createStation(stationEntry.getValue());
            stationTreeMap.put(stationNumber, station);
        }

        // update the stations with their previous and next stops
        stationTreeMap.forEach((key, val) -> {
            var prevStation = stationTreeMap.get(key - 1);
            if (prevStation != null) {
                val.setPrev(new LinkedList<>(List.of(prevStation)));
            }

            var nextStation = stationTreeMap.get(key + 1);
            if (nextStation != null) {
                val.setNext(new LinkedList<>(List.of(nextStation)));
            }
        });

        // get the first and last stations and create the line
        var head = stationTreeMap.firstEntry().getValue();
        var tail = stationTreeMap.lastEntry().getValue();
        metroLines.put(lineName, new MetroLine(lineName, head, tail));
    }

    private static void createLineFromJsonArray(final JsonArray lineArray, HashMap<String, MetroLine> metroLines) {
        LinkedHashMap<String, Station> stationLinkedHashMap = new LinkedHashMap<>();
        HashMap<String, JsonArray> nextStop = new HashMap<>();
        HashMap<String, JsonArray> prevStop = new HashMap<>();
        Station head = null;
        Station tail = null;

        for (var stationEntry : lineArray) {
            Station station = createStation(stationEntry);
            stationLinkedHashMap.put(station.getName(), station);
            nextStop.put(station.getName(), stationEntry.getAsJsonObject().get("next").getAsJsonArray());
            prevStop.put(station.getName(), stationEntry.getAsJsonObject().get("prev").getAsJsonArray());
            if (head == null) {
                head = station;
            }
            tail = station;
        }

        nextStop.forEach((stationName, nextStops) -> {
            LinkedList<Station> next = new LinkedList<>();
            nextStops.forEach(name -> next.add(stationLinkedHashMap.get(name.getAsString())));
            stationLinkedHashMap.get(stationName).setNext(next);
        });

        prevStop.forEach((stationName, prevStops) -> {
            LinkedList<Station> prev = new LinkedList<>();
            prevStops.forEach(name -> prev.add(stationLinkedHashMap.get(name.getAsString())));
            stationLinkedHashMap.get(stationName).setPrev(prev);
        });

        metroLines.put(lineName, new MetroLine(lineName, head, tail, stationLinkedHashMap));
    }

    /**
     * Create the Station object from a station entry.
     * <p>
     * Takes an entry from a line JSON object and creates the Station object from it. We take into account if the entry
     * is as simple as the station number with a name only, or a whole JSON object itself with name, transfers, and time
     * between stations.
     *
     * @param stationElement
     *         JSON element of station to create
     *
     * @return Station object
     */
    private static Station createStation(final JsonElement stationElement) {
        // the element is only a station number and station name; i.e. ("1": "Hammersmith")
        if (stationElement.isJsonPrimitive()) {
            return new Station(stationElement.getAsString(), lineName);
        }

        JsonObject stationDetails = stationElement.getAsJsonObject();
        String     stationName    = stationDetails.get("name").getAsString();
        JsonElement timeElement = stationDetails.has("time") ? stationDetails.get("time") : JsonNull.INSTANCE;
        Station station = timeElement.isJsonNull() ? new Station(stationName, lineName)
                : new Station(stationName, lineName, timeElement.getAsInt());
        addTransferStations(stationDetails.get("transfer"), station);
        return station;
    }

    /**
     * Add transfer stations (if any) to our map
     * <p>
     *
     * @param transferElement
     *         JSON element holding the transfers
     * @param station
     *         the station we're processing transfers for
     */
    private static void addTransferStations(final JsonElement transferElement, final Station station) {
        // the element is null, nothing to process
        if (transferElement.isJsonNull()) {
            return;
        }

        HashMap<String, String> transferLineNames = new HashMap<>();

        // the element is a single JSON object so add it to the transfers map
        if (transferElement.isJsonObject()) {
            transferLineNames.put(transferElement.getAsJsonObject().get("line").getAsString(),
                    transferElement.getAsJsonObject().get("station").getAsString());
        }

        if (transferElement.isJsonArray()) {
            JsonArray transferArray = transferElement.getAsJsonArray();

            // the array is empty, nothing to process
            if (transferArray.isEmpty()) {
                return;
            }

            // process all the elements in the array adding them to the transfers map
            for (var transfer : transferArray) {
                transferLineNames.put(transfer.getAsJsonObject().get("line").getAsString(),
                        transfer.getAsJsonObject().get("station").getAsString());
            }
        }

        // add any lines to the transfer map
        var transferFromStation = TRANSFERS.get(station);
        if (transferFromStation == null) {
            TRANSFERS.put(station, new HashMap<>(transferLineNames));
        } else {
            transferFromStation.putAll(transferLineNames);
        }
    }
}
