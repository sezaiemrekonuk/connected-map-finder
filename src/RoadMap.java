import java.util.*;

/**
 * RoadMap class is used for creating a road map and analyzing it.
 */
public class RoadMap {
    private final List<Road> roads;
    private List<Road> barelyConnectedRoads;
    private final Set<String> points;
    private final Map<String, List<Road>> connections;
    private Map<String, List<Road>> barelyConnections;
    private List<Road> fastestPath;
    private List<Road> fastestBarelyConnectedPath;
    private String startLocation;
    private String endLocation;

    public static List<String> logs = new ArrayList<>();

    public RoadMap() {
        roads = new ArrayList<>();
        points = new HashSet<>();
        connections = new HashMap<>();
    }

    /**
     * Process the input and assigns all the values in place.
     * @param input inputs from the file
     */
    public void processInput(String[] input) {
        String[] startEndLocations = input[0].split("\t");
        setEndLocation(startEndLocations[0]);
        setStartLocation(startEndLocations[1]);

        for (int i = 1; i < input.length; i++) {
            String[] stringParts = input[i].split("\t");
            String from = stringParts[0];
            String to = stringParts[1];
            int distance = Integer.parseInt(stringParts[2]);
            int id = Integer.parseInt(stringParts[3]);

            Road road = new Road(id, distance, from, to);
            roads.add(road);

            points.add(from);
            points.add(to);

            // if there isn't any value for the key, create a new list and add the road
            connections.computeIfAbsent(from, k->new ArrayList<>()).add(road);
            connections.computeIfAbsent(to, k->new ArrayList<>()).add(road);
        }
    }

    /**
     * Finds the shortest path from start to end location using my variation of Dijkstra's algorithm.
     * @return RoadMap Returns it self for chaining with other methods.
     */
    public RoadMap findShortestPath() {
        Set<String> visited = new HashSet<>(); // used for memoization
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Road> previous = new HashMap<>();

        // used PriorityQueue for making the sorting job easier.
        PriorityQueue<Road> queue = new PriorityQueue<>(Comparator.comparingInt(Road::getDistance).thenComparing(Road::getId));

        for (String point : points) {
            distances.put(point, Integer.MAX_VALUE); // set every point's distance to infinity (pretend)
        }

        // putting the start location to the distances map with 0 distance.
        distances.put(getStartLocation(), 0);

        // also putting a road with 0 distance and -1 id, to make it the first element in the queue to process.
        queue.add(new Road(-1, 0, getStartLocation(), getStartLocation()));

        while (!queue.isEmpty()) {
            Road currentRoad = queue.poll();
            String currentPoint = currentRoad.getTo();

            // edge conditions
            if (!visited.add(currentPoint)) continue;
            if (currentPoint.equals(getEndLocation())) break;

            List<Road> roads = connections.get(currentPoint);
            if (roads == null) continue;

            // for each road in roads, calculate the new distance and update the distances map and previous map.
            for (Road road : roads) {
                String nextPoint = road.getFrom().equals(currentPoint) ? road.getTo() : road.getFrom();
                int newDistance = distances.get(currentPoint) + road.getDistance();

                if (newDistance < distances.get(nextPoint)) {
                    distances.put(nextPoint, newDistance);
                    previous.put(nextPoint, road);
                    queue.add(new Road(road.getId(), newDistance, currentPoint, nextPoint));
                }
            }
        }

        List<Road> path = new ArrayList<>();
        String step = getEndLocation();

        // adding the roads to the path list by using the previous map.
        while (previous.containsKey(step)) {
            Road road = previous.get(step);
            path.add(road);
            step = road.getFrom().equals(step) ? road.getTo() : road.getFrom();
        }


        int totalPathDistance = getTotalRoadDistance(Collections.singleton(path));

        logs.add(String.format("Fastest Route from %s to %s (%d KM):", getEndLocation(), getStartLocation(), totalPathDistance));
        for (Road road : path) {
            logs.add(String.format("%s\t%s\t%d\t%d", road.getFrom(), road.getTo(), road.getDistance(), road.getId()));
        }

        // reversing the path to make it from start to end location.
        Collections.reverse(path);
        setFastestPath(path);

        return this;
    }

    /**
     * Finds the shortest path from start to end location using barely connected roads.
     * I was planning to use the same method for each map,
     * but for chaining methods, I changed every method to use objects own values.
     * @return RoadMap Returns itself for chaining with other methods.
     */
    public RoadMap findBarelyShortestPath() {
        Set<String> visited = new HashSet<>();
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Road> previous = new HashMap<>();

        // same logic as findShortestPath method, but using barelyConnections map instead of connections.
        PriorityQueue<Road> queue = new PriorityQueue<>(Comparator.comparingInt(Road::getDistance).thenComparing(Road::getId));

        for (String point : points) {
            distances.put(point, Integer.MAX_VALUE);
        }

        distances.put(getStartLocation(), 0);

        queue.add(new Road(-1, 0, getStartLocation(), getStartLocation()));

        while (!queue.isEmpty()) {
            Road currentRoad = queue.poll();
            String currentPoint = currentRoad.getTo();

            if (!visited.add(currentPoint)) continue;
            if (currentPoint.equals(getEndLocation())) break;

            List<Road> roads = barelyConnections.get(currentPoint);
            if (roads == null) continue;

            for (Road road : roads) {
                String nextPoint = road.getFrom().equals(currentPoint) ? road.getTo() : road.getFrom();
                int newDistance = distances.get(currentPoint) + road.getDistance();

                if (newDistance < distances.get(nextPoint)) {
                    distances.put(nextPoint, newDistance);
                    previous.put(nextPoint, road);
                    queue.add(new Road(road.getId(), newDistance, currentPoint, nextPoint));
                }
            }
        }

        List<Road> path = new ArrayList<>();
        String step = getEndLocation();

        while (previous.containsKey(step)) {
            Road road = previous.get(step);
            path.add(road);
            step = road.getFrom().equals(step) ? road.getTo() : road.getFrom();
        }


        int totalPathDistance = getTotalRoadDistance(Collections.singleton(path));

        logs.add(String.format("Fastest Route from %s to %s on Barely Connected Map (%d KM):", getEndLocation(), getStartLocation(), totalPathDistance));

        for (Road road : path) {
            logs.add(String.format("%s\t%s\t%d\t%d", road.getFrom(), road.getTo(), road.getDistance(), road.getId()));
        }

        Collections.reverse(path);
        setFastestBarelyConnectedPath(path);

        return this;
    }

    /**
     * Creates barelyConnectedRoads list by using Kruskal's algorithm.
     * @return RoadMap Returns itself for chaining with other methods.
     */
    public RoadMap createBarelyConnectedRoads() {
        List<Road> barelyConnectedRoads = new ArrayList<>();
        List<Road> sortedRoads = new ArrayList<>(roads);
        Collections.sort(sortedRoads); // sorting the roads by distance using its own compareTo method.

        List<String> barelyConnectedPoints = new ArrayList<>(points);
        barelyConnectedPoints.sort(String.CASE_INSENSITIVE_ORDER); // using insensitive because of uppercase i like characters.

        /*
            * Kruskal's algorithm implementation for finding the minimum spanning tree.
            * The next 5 line of code is the implementation of the algorithm.
         */
        UnionFind uf = new UnionFind(points.size());
        Map<String, Integer> pointIndex = new HashMap<>();
        int index = 0;
        for (String point : barelyConnectedPoints) {
            pointIndex.put(point, index++);
        }

        for (Road road : sortedRoads) {
            int fromIndex = pointIndex.get(road.getFrom());
            int toIndex = pointIndex.get(road.getTo());

            // if the two points are not connected, connect them and add the road to the barelyConnectedRoads list.
            if (uf.find(fromIndex) != uf.find(toIndex)) {
                uf.union(fromIndex, toIndex);
                barelyConnectedRoads.add(road);

                // edge condition for breaking the algorithm.
                if (barelyConnectedRoads.size() == barelyConnectedPoints.size() - 1) {
                    break;
                }
            }
        }


        logs.add("Roads of Barely Connected Map is:");

        for (Road road : barelyConnectedRoads) {
            logs.add(String.format("%s\t%s\t%d\t%d", road.getFrom(), road.getTo(), road.getDistance(), road.getId()));
        }

        setBarelyConnectedRoads(barelyConnectedRoads);

        return this;
    }

    /**
     * Creates barelyConnections map by using barelyConnectedRoads list.
     * This method is used for creating a new HashMap for the fastestPath algorithm.
     * @return RoadMap Returns it self for chaining with other methods.
     */
    public RoadMap createBarelyConnections() {
        Map<String, List<Road>> barelyConnections = new HashMap<>();
        for (Road road : barelyConnectedRoads) {
            // same logic as processInput method, but using barelyConnections map instead of connections.
            barelyConnections.computeIfAbsent(road.getFrom(), k->new ArrayList<>()).add(road);
            barelyConnections.computeIfAbsent(road.getTo(), k->new ArrayList<>()).add(road);
        }

        setBarelyConnections(barelyConnections);

        return this;
    }

    /**
     * Analyzes the road map by calculating the ratio of construction material usage and the ratio of the fastest route.
     */
    public void analyze() {
        int totalRoadDistance = getTotalRoadDistance(connections.values());
        int totalBarelyConnectedRoadDistance = getTotalRoadDistance(barelyConnections.values());

        int totalPathDistance = getTotalRoadDistance(Collections.singleton(fastestPath));
        int totalBarelyConnectedPathDistance = getTotalRoadDistance(Collections.singleton(fastestBarelyConnectedPath));

        logs.add("Analysis:");
        logs.add(String.format("Ratio of Construction Material Usage Between Barely Connected and Original Map: %.2f", (double) totalBarelyConnectedRoadDistance / totalRoadDistance));
        logs.add(String.format("Ratio of Fastest Route Between Barely Connected and Original Map: %.2f", (double) totalBarelyConnectedPathDistance / totalPathDistance));
    }

    /**
     * Calculates the total distance of the roads in the list.
     * @param roadList List of roads as a nested collection.
     *                 If you are going to use with single list, you can use Collections.singleton(list) method.
     * @return int Returns the total distance of the given roads.
     */
    private int getTotalRoadDistance(Collection<List<Road>> roadList) {
        return roadList.stream().flatMap(List::stream).mapToInt(Road::getDistance).sum();
    }

    /* Getter and Setter methods */

    public List<Road> getRoads() {
        return roads;
    }

    public Set<String> getPoints() {
        return points;
    }

    public Map<String, List<Road>> getConnections() {
        return connections;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Map<String, List<Road>> getBarelyConnections() {
        return barelyConnections;
    }

    public void setBarelyConnections(Map<String, List<Road>> barelyConnections) {
        this.barelyConnections = barelyConnections;
    }

    public List<Road> getBarelyConnectedRoads() {
        return barelyConnectedRoads;
    }

    public void setBarelyConnectedRoads(List<Road> barelyConnectedRoads) {
        this.barelyConnectedRoads = barelyConnectedRoads;
    }

    public List<Road> getFastestPath() {
        return fastestPath;
    }

    public void setFastestPath(List<Road> fastestPath) {
        this.fastestPath = fastestPath;
    }

    public List<Road> getFastestBarelyConnectedPath() {
        return fastestBarelyConnectedPath;
    }

    public void setFastestBarelyConnectedPath(List<Road> fastestBarelyConnectedPath) {
        this.fastestBarelyConnectedPath = fastestBarelyConnectedPath;
    }
}
