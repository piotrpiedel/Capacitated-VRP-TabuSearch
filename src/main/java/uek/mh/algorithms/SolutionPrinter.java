package uek.mh.algorithms;

import uek.mh.models.City;
import uek.mh.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class SolutionPrinter {
    private final int numberOfVehicles;
    private final List<Vehicle> vehicles;
    private final double totalRouteCost;

    public SolutionPrinter(int numberOfVehicles, List<Vehicle> vehicles, double totalRouteCost) {
        this.numberOfVehicles = numberOfVehicles;
        this.vehicles = vehicles;
        this.totalRouteCost = totalRouteCost;
    }

    public void print() {
        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            ArrayList<City> stopPoints = vehicles.get(vehicleIndex).stopPoints;
            if (!stopPoints.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1) + " Load for vehicle " + vehicles.get(vehicleIndex).load + ":");
                int routSize = stopPoints.size();

                for (int k = 0; k < routSize; k++) {
                    City city = stopPoints.get(k);
                    if (k == routSize - 1) {
                        System.out.print(city.cityId);
                    } else {
                        System.out.print(city.cityId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + totalRouteCost + "\n");
    }
}