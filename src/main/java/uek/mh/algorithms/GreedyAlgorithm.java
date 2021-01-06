package uek.mh.algorithms;

import lombok.Getter;
import uek.mh.models.Node;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpData;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GreedyAlgorithm {
    private final int noOfVehicles;
    private final List<Node> nodes;
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

        nodes = new ArrayList<>();

        for (int i = 0; i < numberOfCities; i++) {
            nodes.add(new Node(i, vrpData.getDemand()[i]));
        }

        this.vehicles = new ArrayList<>();

        for (int i = 0; i < this.noOfVehicles; i++) {
            vehicles.add(new Vehicle(vrpData.getVehicleCapacity()));
        }
    }

    private boolean unassignedCustomerExists(List<Node> nodes) {
        for (int i = 1; i < nodes.size(); i++) {
            if (!nodes.get(i).isRouted)
                return true;
        }
        return false;
    }

    public GreedyAlgorithm run() {
        double candidateCost, endCost;
        int vehIndex = 0;

        while (unassignedCustomerExists(nodes)) {
            int customerIndex = 0;
            Node candidate = null;
            double minCost = Double.MAX_VALUE;

            if (vehicles.get(vehIndex).stopPoints.isEmpty()) {
                vehicles.get(vehIndex).addStopPointToVehicle(nodes.get(0));
            }

            for (int i = 0; i < numberOfCities; i++) {
                if (!nodes.get(i).isRouted) {
                    if (vehicles.get(vehIndex).checkIfCapacityFits(nodes.get(i).demand)) {
                        candidateCost = distances[vehicles.get(vehIndex).currentLocation][i];
                        if (minCost > candidateCost) {
                            minCost = candidateCost;
                            customerIndex = i;
                            candidate = nodes.get(i);
                        }
                    }
                }
            }

            if (candidate == null) {
                //Not a single Customer Fits
                if (vehIndex + 1 < vehicles.size()) //We have more vehicles to assign
                {
                    if (vehicles.get(vehIndex).currentLocation != 0) {//End this route
                        endCost = distances[vehicles.get(vehIndex).currentLocation][0];
                        vehicles.get(vehIndex).addStopPointToVehicle(nodes.get(0));
                        this.cost += endCost;
                    }
                    vehIndex = vehIndex + 1; //Go to next Vehicle
                } else //We DO NOT have any more vehicle to assign. The problem is unsolved under these parameters
                {
                    System.out.println("\nThe rest customers do not fit in any Vehicle\n" +
                            "The problem cannot be resolved under these constrains");
                    System.exit(0);
                }
            } else {
                vehicles.get(vehIndex).addStopPointToVehicle(candidate);//If a fitting Customer is Found
                nodes.get(customerIndex).isRouted = true;
                this.cost += minCost;
            }
        }

        endCost = distances[vehicles.get(vehIndex).currentLocation][0];
        vehicles.get(vehIndex).addStopPointToVehicle(nodes.get(0));
        this.cost += endCost;

        finalNumberOfUsedVehicles = vehIndex;
        return this;
    }

//    public void print() {
//        System.out.println("=========================================================");
//
//        for (int j = 0; j < noOfVehicles; j++) {
//            if (!vehicles[j].routes.isEmpty()) {
//                System.out.print("Vehicle " + j + ":");
//                int RoutSize = vehicles[j].routes.size();
//                for (int k = 0; k < RoutSize; k++) {
//                    if (k == RoutSize - 1) {
//                        System.out.print(vehicles[j].routes.get(k).nodeId);
//                    } else {
//                        System.out.print(vehicles[j].routes.get(k).nodeId + "->");
//                    }
//                }
//                System.out.println();
//            }
//        }
//        System.out.println("\nBest Value: " + this.cost + "\n");
//    }
}


