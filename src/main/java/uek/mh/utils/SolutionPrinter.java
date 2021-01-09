package uek.mh.utils;

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
            ArrayList<City> stopPointsForVehicle = vehicles.get(vehicleIndex).stopPoints;
            if (!stopPointsForVehicle.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1) + " Load for vehicle " + vehicles.get(vehicleIndex).load + ": ");
                int routSize = stopPointsForVehicle.size();

                for (int k = 0; k < routSize; k++) {
                    City city = stopPointsForVehicle.get(k);
                    if (k == routSize - 1) {
                        System.out.print(city.getName());
                    } else {
                        System.out.print(city.getName() + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + totalRouteCost + "\n");
    }
}