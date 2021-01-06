package uek.mh.algorithms;

import lombok.Getter;
import uek.mh.models.Node;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpData;

@Getter
public class GreedyAlgorithm {
    private final int noOfVehicles;
    private final Node[] nodes;
    private final double[][] distances;
    private final int numberOfCities;
    private final Vehicle[] vehicles;
    private int finalNumberOfUsedVehicles;

    private double cost;

    public GreedyAlgorithm(VrpData vrpData) {
        this.numberOfCities = vrpData.getNumberOfCities();
        this.noOfVehicles = vrpData.getVehicles();
        this.distances = vrpData.getDistance();
        this.cost = 0;

        nodes = new Node[numberOfCities];

        for (int i = 0; i < numberOfCities; i++) {
            nodes[i] = new Node(i, vrpData.getDemand()[i]);
        }

        this.vehicles = new Vehicle[this.noOfVehicles];

        for (int i = 0; i < this.noOfVehicles; i++) {
            vehicles[i] = new Vehicle(vrpData.getVehicleCapacity());
        }
    }

    private boolean unassignedCustomerExists(Node[] Nodes) {
        for (int i = 1; i < Nodes.length; i++) {
            if (!Nodes[i].isRouted)
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

            if (vehicles[vehIndex].stopPoints.isEmpty()) {
                vehicles[vehIndex].addStopPointToVehicle(nodes[0]);
            }

            for (int i = 0; i < numberOfCities; i++) {
                if (!nodes[i].isRouted) {
                    if (vehicles[vehIndex].checkIfCapacityFits(nodes[i].demand)) {
                        candidateCost = distances[vehicles[vehIndex].currentLocation][i];
                        if (minCost > candidateCost) {
                            minCost = candidateCost;
                            customerIndex = i;
                            candidate = nodes[i];
                        }
                    }
                }
            }

            if (candidate == null) {
                //Not a single Customer Fits
                if (vehIndex + 1 < vehicles.length) //We have more vehicles to assign
                {
                    if (vehicles[vehIndex].currentLocation != 0) {//End this route
                        endCost = distances[vehicles[vehIndex].currentLocation][0];
                        vehicles[vehIndex].addStopPointToVehicle(nodes[0]);
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
                vehicles[vehIndex].addStopPointToVehicle(candidate);//If a fitting Customer is Found
                nodes[customerIndex].isRouted = true;
                this.cost += minCost;
            }
        }

        endCost = distances[vehicles[vehIndex].currentLocation][0];
        vehicles[vehIndex].addStopPointToVehicle(nodes[0]);
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


