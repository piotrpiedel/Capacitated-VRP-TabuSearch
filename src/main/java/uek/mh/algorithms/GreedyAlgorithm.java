package uek.mh.algorithms;

import lombok.Getter;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpData;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GreedyAlgorithm {
    private final int noOfVehicles;
    private final List<City> cities;
    private final double[][] distances;
    private final int numberOfCities;
    private final List<Vehicle> vehicles;
    private int finalNumberOfUsedVehicles;

    private double cost;

    public GreedyAlgorithm(VrpData vrpData) {
        this.numberOfCities = vrpData.getNumberOfCities();
        this.noOfVehicles = vrpData.getVehicles();
        this.distances = vrpData.getDistance();
        this.cost = 0;

        cities = createCitiesWithDemandsFromFile(vrpData);
        vehicles = createVehiclesWithCapacitiesFromFile(vrpData);
    }

    private List<City> createCitiesWithDemandsFromFile(VrpData vrpData) {
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < numberOfCities; i++) {
            cities.add(new City(i, vrpData.getDemandForCity(i)));
        }
        return cities;
    }

    private List<Vehicle> createVehiclesWithCapacitiesFromFile(VrpData vrpData) {
        List<Vehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < this.noOfVehicles; i++) {
            vehicles.add(new Vehicle(vrpData.getVehicleCapacity()));
        }
        return vehicles;
    }

    private boolean isAnyCityUnassigned(List<City> cities) {
        for (int i = 1; i < cities.size(); i++) {
            if (!cities.get(i).isRouted)
                return true;
        }
        return false;
    }

    public GreedyAlgorithm runAlgorithm() {
        double endCost;
        double candidateCost;
        int currentVehicle = 0;

        while (isAnyCityUnassigned(cities)) {
            int customerIndex = 0;
            City candidate = null;
            double minCost = Double.MAX_VALUE;

            if (vehicles.get(currentVehicle).stopPoints.isEmpty()) {
                vehicles.get(currentVehicle).addStopPointToVehicle(cities.get(0));
            }

            for (int i = 0; i < numberOfCities; i++) {
                if (isCityRouted(i)) {
                    if (vehicles.get(currentVehicle).checkIfCapacityFits(cities.get(i).demand)) {
                        candidateCost = distances[vehicles.get(currentVehicle).currentLocation][i];
                        if (minCost > candidateCost) {
                            minCost = candidateCost;
                            customerIndex = i;
                            candidate = cities.get(i);
                        }
                    }
                }
            }

            if (candidate == null) {
                //Not a single Customer Fits
                if (currentVehicle + 1 < vehicles.size()) //We have more vehicles to assign
                {
                    if (vehicles.get(currentVehicle).currentLocation != 0) {//End this route
                        endCost = distances[vehicles.get(currentVehicle).currentLocation][0];
                        vehicles.get(currentVehicle).addStopPointToVehicle(cities.get(0));
                        this.cost += endCost;
                    }
                    currentVehicle = currentVehicle + 1; //Go to next Vehicle
                } else //We DO NOT have any more vehicle to assign. The problem is unsolved under these parameters
                {
                    System.out.println("\nThe rest customers do not fit in any Vehicle\n" +
                            "The problem cannot be resolved under these constrains");
                    System.exit(0);
                }
            } else {
                vehicles.get(currentVehicle).addStopPointToVehicle(candidate);//If a fitting Customer is Found
                cities.get(customerIndex).isRouted = true;
                this.cost += minCost;
            }
        }

        endCost = distances[vehicles.get(currentVehicle).currentLocation][0];
        vehicles.get(currentVehicle).addStopPointToVehicle(cities.get(0));
        this.cost += endCost;

        finalNumberOfUsedVehicles = currentVehicle;
        return this;
    }

    private boolean isCityRouted(int i) {
        return !cities.get(i).isRouted;
    }
}


