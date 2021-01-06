package uek.mh.algorithms;

import uek.mh.models.Node;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpData;

import java.io.IOException;
import java.util.ArrayList;

public class TabuSearchAlgorithm {
    private final double[][] distances;
    private final int numberOfVehicles;
    private final int tabuMemoryTime;
    private final Vehicle[] bestSolution;
    private final int totalIterations;
    private Vehicle[] vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchAlgorithm(VrpData vrpData) throws IOException {

        this.distances = vrpData.getDistance();
        this.tabuMemoryTime = vrpData.getTabuHorizonSize();
        this.totalIterations = vrpData.getIterations();

        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(vrpData);
        greedyAlgorithm.run();
        this.vehicles = greedyAlgorithm.getVehicles();
        this.cost = greedyAlgorithm.getCost();
        this.numberOfVehicles = greedyAlgorithm.getFinalNumberOfUsedVehicles();
        this.bestSolution = new Vehicle[this.numberOfVehicles];

        for (int i = 0; i < this.numberOfVehicles; i++) {
            this.bestSolution[i] = new Vehicle(vrpData.getVehicleCapacity());
        }
    }

    public TabuSearchAlgorithm run() {
        //We use 1-0 exchange move
        ArrayList<Node> routeFrom;
        ArrayList<Node> routeTo;

        int currentNodeDemand = 0;

        int vehicleIndexFrom;
        int vehicleIndexTo;
        double bestIterationCost;
        double currentCost;

        int swapIndexA = -1;
        int swapIndexB = -1;
        int swapRouteFrom = -1;
        int swapRouteTo = -1;

        int tabuMatrix[][] = new int[this.distances[1].length + 1][this.distances[1].length + 1];

        this.bestSolutionCost = this.cost;

        for (int iterations = this.totalIterations; iterations > 0; iterations--) {
            bestIterationCost = Double.MAX_VALUE;

            for (vehicleIndexFrom = 0; vehicleIndexFrom < this.vehicles.length; vehicleIndexFrom++) {
                routeFrom = this.vehicles[vehicleIndexFrom].stopPoints;

                for (int i = 1; i < (routeFrom.size() - 1); i++) { // on first and last position is depot, which we cannot move
                    for (vehicleIndexTo = 0; vehicleIndexTo < this.vehicles.length; vehicleIndexTo++) {
                        routeTo = this.vehicles[vehicleIndexTo].stopPoints;
                        for (int j = 0; (j < routeTo.size() - 1); j++) { // on last position is depot, which we cannot move

                            currentNodeDemand = routeFrom.get(i).demand;

                            if ((vehicleIndexFrom != vehicleIndexTo) && !this.vehicles[vehicleIndexTo].checkIfCapacityFits(currentNodeDemand)) {
                                // if we swap to different route and there's no enough capacity, it means we cannot make swap to that route
                                break;
                            }

                            if (((vehicleIndexFrom == vehicleIndexTo) && ((j == i) || (j == i - 1)))) {
                                // that swap would not change anything
                                continue;
                            }

                            if ((tabuMatrix[routeFrom.get(i - 1).nodeId][routeFrom.get(i + 1).nodeId] != 0)
                                    || (tabuMatrix[routeTo.get(j).nodeId][routeFrom.get(i).nodeId] != 0)
                                    || (tabuMatrix[routeFrom.get(i).nodeId][routeTo.get(j + 1).nodeId] != 0)) {
                                // checking if that move isn't in tabu
                                break;
                            }

                            double subtractedCosts = this.distances[routeFrom.get(i - 1).nodeId][routeFrom.get(i).nodeId]
                                    + this.distances[routeFrom.get(i).nodeId][routeFrom.get(i + 1).nodeId]
                                    + this.distances[routeTo.get(j).nodeId][routeTo.get(j + 1).nodeId];

                            double addedCosts = this.distances[routeFrom.get(i - 1).nodeId][routeFrom.get(i + 1).nodeId]
                                    + this.distances[routeTo.get(j).nodeId][routeFrom.get(i).nodeId]
                                    + this.distances[routeFrom.get(i).nodeId][routeTo.get(j + 1).nodeId];

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

            for (int i = 0; i < tabuMatrix[0].length; i++) {
                for (int j = 0; j < tabuMatrix[0].length; j++) {
                    if (tabuMatrix[i][j] > 0) {
                        tabuMatrix[i][j]--;
                    }
                }
            }

            routeFrom = this.vehicles[swapRouteFrom].stopPoints;
            routeTo = this.vehicles[swapRouteTo].stopPoints;

            Node SwapNode = routeFrom.get(swapIndexA);

            int nodeIdBefore = routeFrom.get(swapIndexA - 1).nodeId;
            int nodeIdAfter = routeFrom.get(swapIndexA + 1).nodeId;
            int nodeId_F = routeTo.get(swapIndexB).nodeId;
            int nodeId_G = routeTo.get(swapIndexB + 1).nodeId;


            tabuMatrix[nodeIdBefore][SwapNode.nodeId] = this.tabuMemoryTime;
            tabuMatrix[SwapNode.nodeId][nodeIdAfter] = this.tabuMemoryTime;
            tabuMatrix[nodeId_F][nodeId_G] = this.tabuMemoryTime;

            routeFrom.remove(swapIndexA);

            if (swapRouteFrom == swapRouteTo) {
                if (swapIndexA < swapIndexB) {
                    routeTo.add(swapIndexB, SwapNode);
                } else {
                    routeTo.add(swapIndexB + 1, SwapNode);
                }
            } else {
                routeTo.add(swapIndexB + 1, SwapNode);
            }

            this.vehicles[swapRouteFrom].stopPoints = routeFrom;
            this.vehicles[swapRouteFrom].load -= currentNodeDemand;

            this.vehicles[swapRouteTo].stopPoints = routeTo;
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
            this.bestSolution[j].stopPoints.clear();
            if (!this.vehicles[j].stopPoints.isEmpty()) {
                int routSize = this.vehicles[j].stopPoints.size();
                for (int k = 0; k < routSize; k++) {
                    Node n = this.vehicles[j].stopPoints.get(k);
                    this.bestSolution[j].stopPoints.add(n);
                }
            }
        }
    }

    public void print() {
        System.out.println("=========================================================");

        for (int j = 0; j < this.numberOfVehicles; j++) {
            if (!this.vehicles[j].stopPoints.isEmpty()) {
                System.out.print("Vehicle " + j + ":");
                int RoutSize = this.vehicles[j].stopPoints.size();
                for (int k = 0; k < RoutSize; k++) {
                    if (k == RoutSize - 1) {
                        System.out.print(this.vehicles[j].stopPoints.get(k).nodeId);
                    } else {
                        System.out.print(this.vehicles[j].stopPoints.get(k).nodeId + "->");
                    }
                }
                System.out.println();
            }
        }
        System.out.println("\nBest Value: " + this.cost + "\n");
    }

    public void printOnlyCalculatedCost() {
        System.out.println("=========================================================");
        System.out.println("\nBest Value: " + this.cost + "\n");
    }
}


