package metro;

import java.util.HashMap;
import java.util.Map;

public class Station {
    private final String name;
    private final Map<String, String> transfer = new HashMap<>();
    private int time;

    public Station(String name) {
        this.name = name;

    }

    public String getStationName() {
        return name;
    }

    public void addTransfer(String lineName, String stationName) {
        transfer.put(lineName, stationName);
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String printTransfer() {
        String line = transfer.keySet().toString().replaceAll("[\\[\\]]", "");
        String station = transfer.values().toString().replaceAll("[\\[\\]]", "");
        return String.format("%s (%s)", station, line);
    }

    @Override
    public String toString() {
        String print;
        if (transfer.isEmpty()) {
            print = getStationName();
        } else {
            print = getStationName() + " - " + printTransfer();
        }
        return print;
    }
}
