/**
 * Road class represents a road between two cities.
 */
public class Road implements Comparable<Road> {
    private final int id;
    private final Integer distance;
    private final String from;
    private final String to;

    public Road(int id, int distance, String from, String to) {
        this.id = id;
        this.distance = distance;
        this.from = from;
        this.to = to;
    }

    /**
     * Compare two roads by their distances and ids.
     * @param r the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Road r) {
        if (this.distance.equals(r.distance)) {
            return Integer.compare(this.id, r.id);
        }
        return Integer.compare(this.distance, r.distance);
    }

    /**
     * Returns a string representation of the road.
     * It is used for testing purposes.
     * @return String representation of the road object.
     */
    @Override
    public String toString() {
        return "Road{" +
                "id=" + id +
                ", distance=" + distance +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getDistance() {
        return distance;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}