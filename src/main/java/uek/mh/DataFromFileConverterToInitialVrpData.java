package uek.mh;

import uek.mh.algorithms.HaversineDistanceCalculator;
import uek.mh.models.Coordinates;
import uek.mh.models.VrpData;
import uek.mh.utils.FileFromResourcesReader;

import java.io.*;
import java.util.ArrayList;

public class DataFromFileConverterToInitialVrpData {

    private final BufferedReader reader;
    private final VrpData vrpData;
    private final HaversineDistanceCalculator haversineDistanceCalculator;

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. datasets/big/Golden_20.vrp
     */
    public static VrpData convert(String pathToFileFromResources) throws IOException {
        DataFromFileConverterToInitialVrpData dataFromFileConverterToInitialVrpData = new DataFromFileConverterToInitialVrpData(pathToFileFromResources);
        return dataFromFileConverterToInitialVrpData.loadFileToVrpData();
    }

    private DataFromFileConverterToInitialVrpData(String pathToFileFromResources) throws FileNotFoundException {
        FileFromResourcesReader fileFromResourcesReader = new FileFromResourcesReader();
        File file = fileFromResourcesReader.loadFile(pathToFileFromResources);
        reader = new BufferedReader(new FileReader(file));
        vrpData = new VrpData();
        haversineDistanceCalculator = new HaversineDistanceCalculator();
    }

    private VrpData loadFileToVrpData() throws IOException {
        readHeader();
        readCoordinates();
        readDemand();
        convertCoordinatesToDistance();
        return vrpData;
    }

    private void readHeader() throws IOException {
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("COORDINATES"))) {
            String[] split = line.split(":");

            String key = split[0].trim();

            if (key.equalsIgnoreCase("DIMENSION")) {
                vrpData.numberOfCities = Integer.parseInt(split[1].trim());
            }

            if (key.equalsIgnoreCase("VEHICLE_CAPACITY")) {
                vrpData.vehicleCapacity = Integer.parseInt(split[1].trim());
            }

            if (key.equalsIgnoreCase("NUMBER_OF_VEHICLES")) {
                vrpData.vehicles = Integer.parseInt(split[1].trim());
            }
        }
    }

    private void readCoordinates() throws IOException {
        vrpData.coordinates = new ArrayList<>();
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("CITY_DEMAND"))) {
            parseRow(line);
        }
    }

    private void parseRow(String line) {
        String[] split = line.split("\\s+");
        double latitude = Double.valueOf(split[1].trim());
        double longitude = Double.valueOf(split[2].trim());
        vrpData.coordinates.add(new Coordinates(latitude, longitude));
    }

    private void readDemand() throws IOException {
        vrpData.demand = new ArrayList<>(vrpData.numberOfCities);
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("DEPOT_COORDINATES"))) {
            String[] split = line.split("\\s+");
            vrpData.demand.add(Integer.valueOf(split[1].trim()));
        }
    }

    private void convertCoordinatesToDistance() {
        vrpData.distance = new double[vrpData.numberOfCities][vrpData.numberOfCities];

        for (int i = 0; i < vrpData.numberOfCities; i++) {
            for (int j = i; j < vrpData.numberOfCities; j++) {
                if (i != j) {
                    vrpData.distance[i][j] = haversineDistanceCalculator
                            .calculateDistance(vrpData.getCoordinates().get(i), vrpData.getCoordinates().get(j));

                    vrpData.distance[j][i] = vrpData.distance[i][j];
                }
            }
        }
    }
}
