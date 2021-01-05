package ro.uaic.info.tabu;

import ro.uaic.info.Node;
import ro.uaic.info.VRPLibReader;
import ro.uaic.info.VRPRunner;
import ro.uaic.info.Vehicle;
import ro.uaic.info.greedy.GreedyAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TabuSearchSolver {
    private final double[][] distances;
    private final int numberOfVehicles;
    private final int TabuMemoryTime = 10;
    private int iterations = 1000;
    private final Vehicle[] bestSolution;

    private Vehicle[] vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchSolver(VRPRunner jct) throws IOException {

        VRPLibReader reader = new VRPLibReader(new BufferedReader(new FileReader(jct.instance)));
        this.numberOfVehicles = reader.getDimension();
        this.distances = reader.getDistance();

        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(jct);
        greedyAlgorithm.solve();
        this.vehicles = greedyAlgorithm.getVehicles();
        this.cost = greedyAlgorithm.getCost();

        this.bestSolution = new Vehicle[this.numberOfVehicles];

        for (int i = 0; i < this.numberOfVehicles; i++) {
            this.bestSolution[i] = new Vehicle(reader.getVehicleCapacity());
        }
    }

    public TabuSearchSolver solve() {
        //We use 1-0 exchange move
        ArrayList<Node> routesFrom;
        ArrayList<Node> routesTo;

        int currentNodeDemand = 0;

        int vehicleIndexFrom, vehicleIndexTo;
        double bestIterationCost, currentCost;

        int swapIndexA = -1, swapIndexB = -1, swapRouteFrom = -1, swapRouteTo = -1;

        int DimensionCustomer = this.distances[1].length;
        int tabuMatrix[][] = new int[DimensionCustomer + 1][DimensionCustomer + 1];

        this.bestSolutionCost = this.cost;

        while (this.iterations-- > 0) {
            bestIterationCost = Double.MAX_VALUE;

            for (vehicleIndexFrom = 0; vehicleIndexFrom < this.vehicles.length; vehicleIndexFrom++) {
                routesFrom = this.vehicles[vehicleIndexFrom].routes;

                for (int i = 1; i < (routesFrom.size() - 1); i++) { //Not possible to move depot!
                    for (vehicleIndexTo = 0; vehicleIndexTo < this.vehicles.length; vehicleIndexTo++) {
                        routesTo = this.vehicles[vehicleIndexTo].routes;
                        int RouteToLength = routesTo.size();
                        for (int j = 0; (j < RouteToLength - 1); j++) {//Not possible to move after last Depot!

                            currentNodeDemand = routesFrom.get(i).demand;

                            if ((vehicleIndexFrom == vehicleIndexTo) || this.vehicles[vehicleIndexTo].CheckIfFits(currentNodeDemand)) {
                                //If we assign to a different route check capacity constrains
                                //if in the new route is the same no need to check for capacity

                                if (!((vehicleIndexFrom == vehicleIndexTo) && ((j == i) || (j == i - 1))))  // Not a move that Changes solution cost
                                {
                                    //Check if the move is a Tabu! - If it is Tabu break
                                    if ((tabuMatrix[routesFrom.get(i - 1).NodeId][routesFrom.get(i + 1).NodeId] != 0)
                                            || (tabuMatrix[routesTo.get(j).NodeId][routesFrom.get(i).NodeId] != 0)
                                            || (tabuMatrix[routesFrom.get(i).NodeId][routesTo.get(j + 1).NodeId] != 0)) {
                                        break;
                                    }

                                    double subtractedCosts = this.distances[routesFrom.get(i - 1).NodeId][routesFrom.get(i).NodeId]
                                        + this.distances[routesFrom.get(i).NodeId][routesFrom.get(i + 1).NodeId]
                                        + this.distances[routesTo.get(j).NodeId][routesTo.get(j + 1).NodeId];

                                    double addedCosts = this.distances[routesFrom.get(i - 1).NodeId][routesFrom.get(i + 1).NodeId]
                                        + this.distances[routesTo.get(j).NodeId][routesFrom.get(i).NodeId]
                                        + this.distances[routesFrom.get(i).NodeId][routesTo.get(j + 1).NodeId];

                                    currentCost = addedCosts - subtractedCosts

                                    if (currentCost < bestIterationCost) {
                                        bestIterationCost = currentCost;
                                        swapIndexA = i;
                                        swapIndexB = j;
                                        swapRouteFrom = vehicleIndexFrom;
                                        swapRouteTo = vehicleIndexTo;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int o = 0; o < tabuMatrix[0].length; o++) {
                for (int p = 0; p < tabuMatrix[0].length; p++) {
                    if (tabuMatrix[o][p] > 0) {
                        tabuMatrix[o][p]--;
                    }
                }
            }

            routesFrom = this.vehicles[swapRouteFrom].routes;
            routesTo = this.vehicles[swapRouteTo].routes;
            this.vehicles[swapRouteFrom].routes = null;
            this.vehicles[swapRouteTo].routes = null;

            Node SwapNode = routesFrom.get(swapIndexA);

            int NodeIDBefore = routesFrom.get(swapIndexA - 1).NodeId;
            int NodeIDAfter = routesFrom.get(swapIndexA + 1).NodeId;
            int NodeID_F = routesTo.get(swapIndexB).NodeId;
            int NodeID_G = routesTo.get(swapIndexB + 1).NodeId;


            tabuMatrix[NodeIDBefore][SwapNode.NodeId] = this.TabuMemoryTime;
            tabuMatrix[SwapNode.NodeId][NodeIDAfter] = this.TabuMemoryTime;
            tabuMatrix[NodeID_F][NodeID_G] = this.TabuMemoryTime;

            routesFrom.remove(swapIndexA);

            if (swapRouteFrom == swapRouteTo) {
                if (swapIndexA < swapIndexB) {
                    routesTo.add(swapIndexB, SwapNode);
                } else {
                    routesTo.add(swapIndexB + 1, SwapNode);
                }
            } else {
                routesTo.add(swapIndexB + 1, SwapNode);
            }

            this.vehicles[swapRouteFrom].routes = routesFrom;
            this.vehicles[swapRouteFrom].load -= currentNodeDemand;

            this.vehicles[swapRouteTo].routes = routesTo;
            this.vehicles[swapRouteTo].load += currentNodeDemand;

            this.cost += bestIterationCost;

            if (this.cost < this.bestSolutionCost) {
                this.saveBestSolution();
            }
        }

        this.vehicles = this.bestSolution;
        this.cost = this.bestSolutionCost;

        return this;
    }

    private void saveBestSolution() {
        this.bestSolutionCost = this.cost;
        for (int j = 0; j < this.numberOfVehicles; j++) {
            this.bestSolution[j].routes.clear();
            if (!this.vehicles[j].routes.isEmpty()) {
                int routSize = this.vehicles[j].routes.size();
                for (int k = 0; k < routSize; k++) {
                    Node n = this.vehicles[j].routes.get(k);
                    this.bestSolution[j].routes.add(n);
                }
            }
        }
    }

    public void print() {
        System.out.println("=========================================================");

        for (int j = 0; j < this.numberOfVehicles; j++) {
            if (!this.vehicles[j].routes.isEmpty()) {
                System.out.print("Vehicle " + j + ":");
                int RoutSize = this.vehicles[j].routes.size();
                for (int k = 0; k < RoutSize; k++) {
                    if (k == RoutSize - 1) {
                        System.out.print(this.vehicles[j].routes.get(k).NodeId);
                    } else {
                        System.out.print(this.vehicles[j].routes.get(k).NodeId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.cost + "\n");
    }
}


