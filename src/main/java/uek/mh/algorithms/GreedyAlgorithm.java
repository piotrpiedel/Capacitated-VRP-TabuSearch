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

    private double totalRouteCost;
    public GreedyAlgorithm(VrpDataConfig vrpDataConfig) {


        this.numberOfCities = vrpDataConfig.getNumberOfCities();
        this.numberOfVehicles = vrpDataConfig.getVehicles();
        this.distances = vrpDataConfig.getDistance();
        this.totalRouteCost = 0;

        cities = createCitiesWithDemandsFromFile(vrpDataConfig);
        vehicles = createVehiclesWithCapacitiesFromFile(vrpDataConfig);
    }

    private List<City> createCitiesWithDemandsFromFile(VrpDataConfig vrpDataConfig) {
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < numberOfCities; i++) {
            cities.add(new City(i, vrpDataConfig.getDemandForCity(i),vrpDataConfig.getCoordinates().get(i)));
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

    public void runAlgorithm() throws Exception {
        double newBestCost;
        int vehicleIndex = 0;

        while (isAnyCityUnassignedToVehicle(cities)) {
            Integer currentBestCityCandidateIndex = null;
            double currentBestCost = Double.MAX_VALUE;

            if (vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                vehicles.get(vehicleIndex).addStopPointToVehicle(cities.get(0));
            }

            for (int cityId = 1; cityId < numberOfCities; cityId++) {
                if (isCityRouted(cityId)) {
                    Vehicle vehicle = vehicles.get(vehicleIndex);
                    if (isVehicleAbleToAddDemandForCity(vehicle, cityId)) {
                        newBestCost = distances[vehicle.currentLocation][cityId];
                        if (newBestCost < currentBestCost) {
                            currentBestCost = newBestCost;
                            currentBestCityCandidateIndex = cityId;
                        }
                    }
                }
            }

            if (currentBestCityCandidateIndex != null) {
                addCityToVehicleRouteForbidFromRoutingAgain(vehicleIndex, currentBestCityCandidateIndex, currentBestCost);
            } else {
                if (!isVehicleInDepot(vehicleIndex)) {
                    addToCostsDistanceBetweenLastCityAndDepot(vehicleIndex);
                }
                vehicleIndex = getNextVehicle(vehicleIndex);
            }
        }
        addToCostsDistanceBetweenLastCityAndDepot(vehicleIndex);
        finalNumberOfUsedVehicles = vehicleIndex+1;
    }

    private boolean isVehicleAbleToAddDemandForCity(Vehicle vehicle, int cityIndex) {
        return vehicle.checkIfCapacityFits(getDemandForCityWithId(cityIndex));
    }

    private int getDemandForCityWithId(int i) {
        return cities.get(i).demand;
    }

    private boolean isAnyCityUnassignedToVehicle(List<City> cities) {
        return IntStream.range(1, cities.size()).anyMatch(i -> !cities.get(i).isRouted);
    }

    private void addCityToVehicleRouteForbidFromRoutingAgain(int vehicleIndex, Integer currentBestCityCandidateIndex, double currentBestCost) {
        City bestCandidateCity = cities.get(currentBestCityCandidateIndex);
        Vehicle vehicle = vehicles.get(vehicleIndex);
        vehicle.addStopPointToVehicle(bestCandidateCity);
        bestCandidateCity.isRouted = true;
        this.totalRouteCost += currentBestCost;
    }

    private boolean isVehicleInDepot(int currentVehicle) {
        return vehicles.get(currentVehicle).currentLocation == 0;
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
        double costBetweenLastCityAndDepot = distances[vehicles.get(currentVehicle).currentLocation][0];
        vehicles.get(currentVehicle).addStopPointToVehicle(getDepot());
        this.totalRouteCost += costBetweenLastCityAndDepot;
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
                System.out.print("Vehicle " + (vehicleIndex + 1)  + " Load for vehicle " + vehicles.get(vehicleIndex).load + ":");
                int routSize = vehicles.get(vehicleIndex).stopPoints.size();

                for (int k = 0; k < routSize; k++) {
                    City city = vehicles.get(vehicleIndex).stopPoints.get(k);
                    if (k == routSize - 1) {
                        System.out.print(city.cityId);
                    } else {
                        System.out.print(city.cityId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.totalRouteCost + "\n");
    }
}


