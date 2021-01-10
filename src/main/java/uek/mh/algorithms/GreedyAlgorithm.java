package uek.mh.algorithms;

import lombok.Getter;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;
import uek.mh.utils.SolutionPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class GreedyAlgorithm {
    private final List<City> cities;
    private final List<Vehicle> vehicles;

    private final int numberOfCities;
    private final int numberOfVehicles;

    private final double[][] distances;
    private int finalNumberOfUsedVehicles;

    private double totalRouteCost;

    public GreedyAlgorithm(VrpDataConfig vrpDataConfig) {
        cities = createCitiesWithDemandsFromFile(vrpDataConfig);
        vehicles = createVehiclesWithCapacitiesFromFile(vrpDataConfig);

        numberOfCities = vrpDataConfig.getNumberOfCities();
        numberOfVehicles = vrpDataConfig.getNumberOfVehicles();
        distances = vrpDataConfig.getDistance();
        totalRouteCost = 0;
    }

    private List<City> createCitiesWithDemandsFromFile(VrpDataConfig vrpDataConfig) {
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < vrpDataConfig.getNumberOfCities(); i++) {
            cities.add(new City(i, vrpDataConfig.getDemandForCity(i), vrpDataConfig.getCoordinates().get(i)));
        }
        return cities;
    }

    private List<Vehicle> createVehiclesWithCapacitiesFromFile(VrpDataConfig vrpDataConfig) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < vrpDataConfig.getNumberOfVehicles(); i++) {
            vehicles.add(new Vehicle(i, vrpDataConfig.getVehicleCapacity()));
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
        totalRouteCost += currentBestCost;
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
        totalRouteCost += costBetweenLastCityAndDepot;
    }

    private City getDepot() {
        return cities.get(0);
    }

    private boolean isCityRouted(int i) {
        return !cities.get(i).isRouted;
    }

    public void printResult() {
        System.out.println("===============GREEDY ALGORITHM==============================");
        SolutionPrinter solutionPrinter = new SolutionPrinter(numberOfVehicles, vehicles, totalRouteCost);
        solutionPrinter.print();
    }
}


