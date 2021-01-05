package uek.mh.algorithms;

import uek.mh.VrpConfiguration;
import uek.mh.models.Node;
import uek.mh.VRPLibReader;
import uek.mh.VRPRunner;
import uek.mh.models.Vehicle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TabuSearchAlgorithm {
    private final double[][] distances;
    private final int numberOfVehicles;
    private final int tabuMemoryTime = 10;
    private final Vehicle[] bestSolution;
    private int iterations = 1000;
    private Vehicle[] vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchAlgorithm(VRPRunner jct) throws IOException {

        VRPLibReader reader = new VRPLibReader(new BufferedReader(new FileReader(VrpConfiguration.instance)));
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

    public TabuSearchAlgorithm solve() {
        //We use 1-0 exchange move
        ArrayList<Node> routesFrom;
        ArrayList<Node> routesTo;

        int currentNodeDemand = 0;

        int vehicleIndexFrom, vehicleIndexTo;
        double bestIterationCost, currentCost;

        int swapIndexA = -1, swapIndexB = -1, swapRouteFrom = -1, swapRouteTo = -1;

        int tabuMatrix[][] = new int[this.distances[1].length + 1][this.distances[1].length + 1];

        this.bestSolutionCost = this.cost;

        while (this.iterations-- > 0) {
            bestIterationCost = Double.MAX_VALUE;

            for (vehicleIndexFrom = 0; vehicleIndexFrom < this.vehicles.length; vehicleIndexFrom++) {
                routesFrom = this.vehicles[vehicleIndexFrom].routes;

                for (int i = 1; i < (routesFrom.size() - 1); i++) { //Not possible to move depot!
                    for (vehicleIndexTo = 0; vehicleIndexTo < this.vehicles.length; vehicleIndexTo++) {
                        routesTo = this.vehicles[vehicleIndexTo].routes;
                        for (int j = 0; (j < routesTo.size() - 1); j++) {//Not possible to move after last Depot!

                            currentNodeDemand = routesFrom.get(i).demand;

                            if ((vehicleIndexFrom == vehicleIndexTo) || this.vehicles[vehicleIndexTo].checkIfFits(currentNodeDemand)) {
                                //If we assign to a different route check capacity constrains
                                //if in the new route is the same no need to check for capacity

                                if (!((vehicleIndexFrom == vehicleIndexTo) && ((j == i) || (j == i - 1))))  // Not a move that Changes solution cost
                                {
                                    //Check if the move is a Tabu! - If it is Tabu break
                                    if ((tabuMatrix[routesFrom.get(i - 1).nodeId][routesFrom.get(i + 1).nodeId] != 0)
                                            || (tabuMatrix[routesTo.get(j).nodeId][routesFrom.get(i).nodeId] != 0)
                                            || (tabuMatrix[routesFrom.get(i).nodeId][routesTo.get(j + 1).nodeId] != 0)) {
                                        break;
                                    }

                                    double subtractedCosts = this.distances[routesFrom.get(i - 1).nodeId][routesFrom.get(i).nodeId]
                                            + this.distances[routesFrom.get(i).nodeId][routesFrom.get(i + 1).nodeId]
                                            + this.distances[routesTo.get(j).nodeId][routesTo.get(j + 1).nodeId];

                                    double addedCosts = this.distances[routesFrom.get(i - 1).nodeId][routesFrom.get(i + 1).nodeId]
                                            + this.distances[routesTo.get(j).nodeId][routesFrom.get(i).nodeId]
                                            + this.distances[routesFrom.get(i).nodeId][routesTo.get(j + 1).nodeId];

                                    currentCost = addedCosts - subtractedCosts;

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

            for (int i = 0; i < tabuMatrix[0].length; i++) {
                for (int j = 0; j < tabuMatrix[0].length; j++) {
                    if (tabuMatrix[i][j] > 0) {
                        tabuMatrix[i][j]--;
                    }
                }
            }

            routesFrom = this.vehicles[swapRouteFrom].routes;
            routesTo = this.vehicles[swapRouteTo].routes;
            this.vehicles[swapRouteFrom].routes = null;
            this.vehicles[swapRouteTo].routes = null;

            Node SwapNode = routesFrom.get(swapIndexA);

            int nodeIdBefore = routesFrom.get(swapIndexA - 1).nodeId;
            int nodeIdAfter = routesFrom.get(swapIndexA + 1).nodeId;
            int nodeId_F = routesTo.get(swapIndexB).nodeId;
            int nodeId_G = routesTo.get(swapIndexB + 1).nodeId;


            tabuMatrix[nodeIdBefore][SwapNode.nodeId] = this.tabuMemoryTime;
            tabuMatrix[SwapNode.nodeId][nodeIdAfter] = this.tabuMemoryTime;
            tabuMatrix[nodeId_F][nodeId_G] = this.tabuMemoryTime;

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
                        System.out.print(this.vehicles[j].routes.get(k).nodeId);
                    } else {
                        System.out.print(this.vehicles[j].routes.get(k).nodeId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.cost + "\n");
    }
}


