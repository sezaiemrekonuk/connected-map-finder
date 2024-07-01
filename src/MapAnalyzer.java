import java.util.Locale;

/**
 * Main class of MapAnalyzing system, it is only used for creating a RoadMap object and analyzing it.
 */
public class MapAnalyzer {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length < 2) {
            System.out.println("Usage: java8 MapAnalyzer <input_file> <output_file>");
            System.exit(1);
        }

        String[] inputs = IOHandler.readFile(args[0], true, true);

        RoadMap roadMap = new RoadMap();

        assert inputs != null;
        roadMap.processInput(inputs);

        // whole process of creating and analyzing the road map
        roadMap.findShortestPath()
                .createBarelyConnectedRoads()
                .createBarelyConnections()
                .findBarelyShortestPath()
                .analyze();

        for (int i=0; i < RoadMap.logs.size(); i++) {
            IOHandler.writeToFile(args[1], RoadMap.logs.get(i), true, i != RoadMap.logs.size()-1);
        }
    }
}

