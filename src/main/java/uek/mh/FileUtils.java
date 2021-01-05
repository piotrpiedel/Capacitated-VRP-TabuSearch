package uek.mh;

import java.io.*;

public class FileUtils {

    private BufferedReader reader;
    private VrpData vrpData;

    private FileUtils(String pathToFileFromResources) throws IOException {
        loadFileToMemory(pathToFileFromResources);
    }

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. datasets/big/Golden_20.vrp
     */
    public static VrpData loadFileFromPathToVrpData(String pathToFileFromResources) throws IOException {
        FileUtils fileUtils = new FileUtils(pathToFileFromResources);
        return fileUtils.loadFileToVrpData();
    }

    private static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double xDistance = Math.abs(x1 - x2);
        double yDistance = Math.abs(y1 - y2);

        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
    }

    private VrpData loadFileToVrpData() throws IOException {
        vrpData = new VrpData();
        readHeader();
        readCoordinates();
        readDemand();
        convertCoordinatesToDistance();
        return vrpData;
    }

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. datasets/big/Golden_20.vrp
     */
    private void loadFileToMemory(String pathToFileFromResources) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(pathToFileFromResources).getFile());
        reader = new BufferedReader(new FileReader(file));
    }

    private void readHeader() throws IOException {
        String line = reader.readLine();

        while (!line.equalsIgnoreCase("NODE_COORD_SECTION")) {
            String[] split = line.split(":");

            String key = split[0].trim();

            if (key.equalsIgnoreCase("DIMENSION")) {
                vrpData.dimension = Integer.parseInt(split[1].trim());
            }

            if (key.equalsIgnoreCase("CAPACITY")) {
                vrpData.vehicleCapacity = Integer.parseInt(split[1].trim());
            }

            line = reader.readLine();

            if (line == null) {
                break;
            }
        }
    }

    private void readCoordinates() throws IOException {
        vrpData.coordinates = new double[vrpData.dimension][2];

        String line = reader.readLine();
        while (!line.equalsIgnoreCase("DEMAND_SECTION")) {
            parseRow(line, vrpData.coordinates);

            line = reader.readLine();
        }
    }

    private void parseRow(String line, double[][] coordinates) {
        String[] split = line.split("\\s+");

        int i = Integer.valueOf(split[0].trim()) - 1;
        coordinates[i][0] = Double.valueOf(split[1].trim());
        coordinates[i][1] = Double.valueOf(split[2].trim());
    }

    private void readDemand() throws IOException {
        vrpData.demand = new int[vrpData.dimension];

        String line = reader.readLine();
        while (!line.equalsIgnoreCase("DEPOT_SECTION")) {

            String[] split = line.split("\\s+");

            int i = Integer.valueOf(split[0].trim()) - 1;
            vrpData.demand[i] = Integer.valueOf(split[1].trim());

            line = reader.readLine();
        }
    }

    private void convertCoordinatesToDistance() {
        vrpData.distance = new double[vrpData.dimension][vrpData.dimension];

        for (int i = 0; i < vrpData.dimension; i++) {
            for (int j = i; j < vrpData.dimension; j++) {
                if (i != j) {
                    double x1 = vrpData.coordinates[i][0];
                    double y1 = vrpData.coordinates[i][1];
                    double x2 = vrpData.coordinates[j][0];
                    double y2 = vrpData.coordinates[j][1];

                    vrpData.distance[i][j] = euclideanDistance(x1, y1, x2, y2);
                    vrpData.distance[j][i] = vrpData.distance[i][j];
                }
            }
        }
    }
}
