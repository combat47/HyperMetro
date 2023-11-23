package metro;

import java.util.*;

public class MetroLine {
    private final String lineName;

    private Station head;
    private Station tail;

    LinkedHashMap<String, Station> stations = new LinkedHashMap<>();

    MetroLine(final String lineName, final Station firstStation, final Station lastStation) {
        this.lineName = lineName;
        this.head = firstStation;
        this.tail = lastStation;

        Deque<Station> stationDeque = new ArrayDeque<>();
        stationDeque.add(head);
        while (!stationDeque.isEmpty()) {
            Station current = stationDeque.remove();
            stations.put(current.getName(), current);
            if (current.getNext() != null) {
                stationDeque.addAll(current.getNext());
            }
        }
    }

    MetroLine(final String lineName, final Station firstStation, final Station lastStation,
              final LinkedHashMap<String, Station> stationLinkedHashMap) {
        this.lineName = lineName;
        this.head = firstStation;
        this.tail = lastStation;
        this.stations = stationLinkedHashMap;
    }

    /**
     * Output the stations of the line
     * <p>
     * Print the stations of the line in order, with depot's at the beginning and end. Each line lists the station name
     * followed by which line you can transfer to if applicable.
     */
    void printStations() {
        if (head == null) { // if head is null, there are no stations on the line
            return;
        }

        ArrayDeque<Station> stationDeque = new ArrayDeque<>();
        stationDeque.add(head);
        System.out.println("depot");
        while (!stationDeque.isEmpty()) {
            Station current = stationDeque.remove();
            System.out.print(current.getName());
            if (current.hasTransfers()) {
                var transfer = current.getTransfers();
                for (var entry : transfer) {
                    System.out.printf(" - %s (%s)", entry.getName(), entry.getLine());
                }
            }
            System.out.println();
            if (current.getNext() != null) {
                stationDeque.addAll(current.getNext());
            }
        }
        System.out.println("depot");
    }

    void addHead(final String stationName, final int time) {
        if (stationName != null && !stationName.isEmpty()) {
            Station newStation = new Station(stationName, lineName, time);
            newStation.setNext(new LinkedList<>(List.of(head)));
            head.setPrev(new LinkedList<>(List.of(newStation)));
            head = newStation;
            stations.put(stationName, newStation);
        }
    }

    void append(final String stationName, final int time) {
        if (stationName != null && !stationName.isEmpty()) {
            Station newStation = new Station(stationName, lineName, time);
            newStation.setPrev(new LinkedList<>(List.of(tail)));
            tail.setNext(new LinkedList<>(List.of(newStation)));
            tail = newStation;
            stations.put(stationName, newStation);
        }
    }

    void remove(final String stationName) {
        if (stationName != null && !stationName.isEmpty()) {
            Station             toRemove = stations.get(stationName);
            LinkedList<Station> previous = toRemove.getPrev();
            LinkedList<Station> next     = toRemove.getNext();
            if (previous != null) {
                previous.forEach(station -> station.setNext(next));
            }
            if (next != null) {
                next.forEach(station -> station.setPrev(previous));
            }
            if (toRemove == head) {
                head = next.get(0);
            }
            if (toRemove == tail) {
                tail = previous.get(0);
            }
            stations.remove(stationName);
        }
    }

    Station getStation(final String station) {
        if (stations.containsKey(station)) {
            return stations.get(station);
        }

        System.out.printf("No station %s on the %s line.", station, lineName);
        return null;
    }
}
