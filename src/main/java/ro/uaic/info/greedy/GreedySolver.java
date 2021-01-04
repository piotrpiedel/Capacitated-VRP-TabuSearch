package ro.uaic.info.greedy;

import ro.uaic.info.Node;
import ro.uaic.info.VRPLibReader;
import ro.uaic.info.VRPRunner;
import ro.uaic.info.Vehicle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GreedySolver {
    private final int noOfVehicles;
    private final Node[] nodes;
    private final double[][] distances;
    private final int noOfCustomers;
    private final Vehicle[] vehicles;

    private double cost;

    public GreedySolver(VRPRunner jct) throws IOException {
        VRPLibReader reader = new VRPLibReader(new BufferedReader(new FileReader(jct.instance)));
        this.noOfCustomers = reader.getDimension();
        this.noOfVehicles = reader.getDimension();
        this.distances = reader.getDistance();
        this.cost = 0;

        nodes = new Node[noOfCustomers];

        for (int i = 0; i < noOfCustomers; i++) {
            nodes[i] = new Node(i, reader.getDemand()[i]);
        }

        this.vehicles = new Vehicle[this.noOfVehicles];

        for (int i = 0; i < this.noOfVehicles; i++) {
            vehicles[i] = new Vehicle(reader.getVehicleCapacity());
        }
    }

    private boolean unassignedCustomerExists(Node[] Nodes) {
        for (int i = 1; i < Nodes.length; i++) {
            if (!Nodes[i].isRouted)
                return true;
        }
        return false;
    }

    public GreedySolver solve() {
        double candidateCost, endCost;
        int vehIndex = 0;

        while (unassignedCustomerExists(nodes)) {
            int custIndex = 0;
            Node candidate = null;
            double minCost = (float) Double.MAX_VALUE;

            if (vehicles[vehIndex].routes.isEmpty()) {
                vehicles[vehIndex].addNode(nodes[0]);
            }

            for (int i = 0; i < noOfCustomers; i++) {
                if (!nodes[i].isRouted) {
                    if (vehicles[vehIndex].CheckIfFits(nodes[i].demand)) {
                        candidateCost = distances[vehicles[vehIndex].currentLocation][i];
                        if (minCost > candidateCost) {
                            minCost = candidateCost;
                            custIndex = i;
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
                        vehicles[vehIndex].addNode(nodes[0]);
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
                vehicles[vehIndex].addNode(candidate);//If a fitting Customer is Found
                nodes[custIndex].isRouted = true;
                this.cost += minCost;
            }
        }

        endCost = distances[vehicles[vehIndex].currentLocation][0];
        vehicles[vehIndex].addNode(nodes[0]);
        this.cost += endCost;

        return this;
    }

    public void print() {
        System.out.println("=========================================================");

        for (int j = 0; j < noOfVehicles; j++) {
            if (!vehicles[j].routes.isEmpty()) {
                System.out.print("Vehicle " + j + ":");
                int RoutSize = vehicles[j].routes.size();
                for (int k = 0; k < RoutSize; k++) {
                    if (k == RoutSize - 1) {
                        System.out.print(vehicles[j].routes.get(k).nodeId);
                    } else {
                        System.out.print(vehicles[j].routes.get(k).nodeId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.cost + "\n");
    }

    public Vehicle[] getVehicles() {
        return vehicles;
    }

    public double getCost() {
        return cost;
    }
}


