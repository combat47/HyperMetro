package metro;

import java.util.*;

public class Station {
    private final String name;
    private final String line;
    private final int    time;

    private LinkedList<Station> prev;
    private LinkedList<Station> next;

    private final List<Station> transfers = new LinkedList<>();

    Station(final String name, final String line) {
        this(name, line, 0);
    }

    Station(final String name, final String line, final int time) {
        this.name = name;
        this.line = line;
        this.time = time;
        this.next = null;
        this.prev = null;
    }

    String getName() {
        return name;
    }

    void setTransfers(final Station station) {
        transfers.add(station);
    }

    boolean hasTransfers() {
        return !transfers.isEmpty();
    }

    List<Station> getTransfers() {
        return transfers;
    }

    void setPrev(final LinkedList<Station> previous) {
        this.prev = previous;
    }

    LinkedList<Station> getPrev() {
        return prev;
    }

    void setNext(final LinkedList<Station> next) {
        this.next = next;
    }

    LinkedList<Station> getNext() {
        return next;
    }

    LinkedList<Station> getNeighbors() {
        LinkedList<Station> neighbors = new LinkedList<>(transfers);
        if (prev != null) {
            neighbors.addAll(prev);
        }
        if (next != null) {
            neighbors.addAll(next);
        }
        neighbors.removeIf(Objects::isNull);
        return neighbors;
    }

    String getLine() {
        return line;
    }

    int getTime() {
        return time;
    }
}
