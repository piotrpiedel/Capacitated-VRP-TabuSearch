package uek.mh.dataloader;

import uek.mh.algorithms.HaversineDistanceCalculator;
import uek.mh.models.Coordinates;
import uek.mh.models.VrpDataConfig;
import uek.mh.utils.FileFromResourcesReader;

import java.io.*;
import java.util.ArrayList;

public class DataFromFileConverterToInitialVrpData {

    private final BufferedReader reader;
    private final VrpDataConfig vrpDataConfig;
    private final HaversineDistanceCalculator haversineDistanceCalculator;

    /**
     * @param pathToFileFromResources the location of the file, relative resource folder
     *                                eg. datasets/big/Golden_20.vrp
     */
    public static VrpDataConfig convert(String pathToFileFromResources) throws IOException {
        DataFromFileConverterToInitialVrpData dataFromFileConverterToInitialVrpData = new DataFromFileConverterToInitialVrpData(pathToFileFromResources);
        return dataFromFileConverterToInitialVrpData.loadFileToVrpData();
    }

    private DataFromFileConverterToInitialVrpData(String pathToFileFromResources) throws FileNotFoundException {
        FileFromResourcesReader fileFromResourcesReader = new FileFromResourcesReader();
        File file = fileFromResourcesReader.loadFile(pathToFileFromResources);
        reader = new BufferedReader(new FileReader(file));
        vrpDataConfig = new VrpDataConfig();
        haversineDistanceCalculator = new HaversineDistanceCalculator();
    }

    private VrpDataConfig loadFileToVrpData() throws IOException {
        readHeader();
        readCoordinates();
        readDemand();
        convertCoordinatesToDistance();
        return vrpDataConfig;
    }

    private void readHeader() throws IOException {
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("COORDINATES"))) {
            String[] split = line.split(":");

            String key = split[0].trim();

            if (key.equalsIgnoreCase("DIMENSION")) {
                vrpDataConfig.numberOfCities = Integer.parseInt(split[1].trim());
            }

            if (key.equalsIgnoreCase("VEHICLE_CAPACITY")) {
                vrpDataConfig.vehicleCapacity = Integer.parseInt(split[1].trim());
            }

            if (key.equalsIgnoreCase("NUMBER_OF_VEHICLES")) {
                vrpDataConfig.vehicles = Integer.parseInt(split[1].trim());
            }
        }
    }

    private void readCoordinates() throws IOException {
        vrpDataConfig.coordinates = new ArrayList<>();
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("CITY_DEMAND"))) {
            parseRow(line);
        }
    }

    private void parseRow(String line) {
        String[] split = line.split("\\s+");
        double latitude = Double.parseDouble(split[1].trim());
        double longitude = Double.parseDouble(split[2].trim());
        vrpDataConfig.coordinates.add(new Coordinates(latitude, longitude));
    }

    private void readDemand() throws IOException {
        vrpDataConfig.demand = new ArrayList<>();
        String line;
        while (!((line = reader.readLine()).equalsIgnoreCase("DEPOT_COORDINATES"))) {
            String[] split = line.split("\\s+");
            vrpDataConfig.demand.add(Integer.valueOf(split[1].trim()));
        }
    }

    private void convertCoordinatesToDistance() {
        vrpDataConfig.distance = new double[vrpDataConfig.numberOfCities][vrpDataConfig.numberOfCities];

        for (int i = 0; i < vrpDataConfig.numberOfCities; i++) {
            for (int j = i; j < vrpDataConfig.numberOfCities; j++) {
                if (i != j) {
                    vrpDataConfig.distance[i][j] = haversineDistanceCalculator
                            .calculateDistance(vrpDataConfig.getCoordinates().get(i), vrpDataConfig.getCoordinates().get(j));

                    vrpDataConfig.distance[j][i] = vrpDataConfig.distance[i][j];
                }
            }
        }
    }
}
