package uek.mh.algorithms;

import lombok.Getter;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class GreedyAlgorithm {
    private final int numberOfVehicles;
    private final List<City> cities;
    private final double[][] distances;
    private final int numberOfCities;
    private final List<Vehicle> vehicles;
    private int finalNumberOfUsedVehicles;

    private double cost;

    public GreedyAlgorithm(VrpDataConfig vrpDataConfig) {
        this.numberOfCities = vrpDataConfig.getNumberOfCities();
        this.numberOfVehicles = vrpDataConfig.getVehicles();
        this.distances = vrpDataConfig.getDistance();
        this.cost = 0;

        cities = createCitiesWithDemandsFromFile(vrpDataConfig);
        vehicles = createVehiclesWithCapacitiesFromFile(vrpDataConfig);
    }

    private List<City> createCitiesWithDemandsFromFile(VrpDataConfig vrpDataConfig) {
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < numberOfCities; i++) {
            cities.add(new City(i, vrpDataConfig.getDemandForCity(i)));
        }
        return cities;
    }

    private List<Vehicle> createVehiclesWithCapacitiesFromFile(VrpDataConfig vrpDataConfig) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < this.numberOfVehicles; i++) {
            vehicles.add(new Vehicle(vrpDataConfig.getVehicleCapacity()));
        }
        return vehicles;
    }

    private boolean isAnyCityUnassignedToVehicle(List<City> cities) {
        return IntStream.range(1, cities.size()).anyMatch(i -> !cities.get(i).isRouted);
    }

    public void runAlgorithm() throws Exception {
        double newBestCost;
        int vehicleIndex = 0;

        while (isAnyCityUnassignedToVehicle(cities)) {
            Integer currentBestCityCandidateIndex = null;
            double currentBestCost = Double.MAX_VALUE;

            for (int cityIndex = 1; cityIndex < numberOfCities; cityIndex++) {
                if (isCityRouted(cityIndex)) {
                    if (vehicles.get(vehicleIndex).checkIfCapacityFits(getDemandForCityWithId(cityIndex))) {
                        newBestCost = distances[vehicles.get(vehicleIndex).currentLocation][cityIndex];
                        if (newBestCost < currentBestCost) {
                            currentBestCost = newBestCost;
                            currentBestCityCandidateIndex = cityIndex;
                        }
                    }
                }
            }

            if (currentBestCityCandidateIndex != null) {
                vehicles.get(vehicleIndex).addStopPointToVehicle(cities.get(currentBestCityCandidateIndex));
                cities.get(currentBestCityCandidateIndex).isRouted = true;
                this.cost += currentBestCost;
            } else {
                if (isVehicleInDepot(vehicleIndex)) {
                    addToCostsDistanceBetweenLastCityAndDepot(vehicleIndex);
                }
                vehicleIndex = getNextVehicle(vehicleIndex);
            }
        }

        finalNumberOfUsedVehicles = vehicleIndex;
    }

    private boolean isVehicleInDepot(int currentVehicle) {
        return vehicles.get(currentVehicle).currentLocation != 0;
    }

    private int getNextVehicle(int vehicle) throws Exception {
        int nextVehicle;
        if (vehicle + 1 < vehicles.size()) {
            nextVehicle = vehicle + 1;
        } else {
            throw new Exception("Not enough vehicles to solve this problem");
        }
        return nextVehicle;
    }

    private void addToCostsDistanceBetweenLastCityAndDepot(int currentVehicle) {
        vehicles.get(currentVehicle).addStopPointToVehicle(getDepot());
        double costBetweenLastCityAndDepot = distances[vehicles.get(currentVehicle).currentLocation][0];
        this.cost += costBetweenLastCityAndDepot;
    }

    private int getDemandForCityWithId(int i) {
        return cities.get(i).demand;
    }

    private City getDepot() {
        return cities.get(0);
    }

    private boolean isCityRouted(int i) {
        return !cities.get(i).isRouted;
    }

    public void print() {
        System.out.println("===============GREEDY ALGORITHM==============================");

        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            if (!vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1)  + " Load for vehicle " + vehicles.get(vehicleIndex).load + ": 0->");
                int routSize = vehicles.get(vehicleIndex).stopPoints.size();
                for (int k = 0; k < routSize; k++) {
                    if (k == routSize - 1) {
                        System.out.print(vehicles.get(vehicleIndex).stopPoints.get(k).cityId);
                    } else {
                        System.out.print(vehicles.get(vehicleIndex).stopPoints.get(k).cityId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.cost + "\n");
    }
}


