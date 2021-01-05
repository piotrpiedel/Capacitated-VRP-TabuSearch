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
import java.util.Random;

public class TabuSearchSolver {
    private final double[][] distances;
    private final int noOfVehicles;
    private final int tabuHorizon;
    private final int iterations;
    private final Vehicle[] bestSolutionVehicles;

    private Vehicle[] vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchSolver(VRPRunner jct) throws IOException {

        VRPLibReader reader = new VRPLibReader(new BufferedReader(new FileReader(jct.instance)));
        this.noOfVehicles = reader.getDimension();
        this.tabuHorizon = jct.tabuHorizon;
        this.distances = reader.getDistance();
        this.iterations = jct.iterations;

        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(jct);
        greedyAlgorithm.solve();
        this.vehicles = greedyAlgorithm.getVehicles();
        this.cost = greedyAlgorithm.getCost();

        this.bestSolutionVehicles = new Vehicle[this.noOfVehicles];

        for (int i = 0; i < this.noOfVehicles; i++) {
            this.bestSolutionVehicles[i] = new Vehicle(reader.getVehicleCapacity());
        }
    }

    public TabuSearchSolver solve() {
        //We use 1-0 exchange move
        ArrayList<Node> routesFrom;
        ArrayList<Node> routesTo;

        int movingNodeDemand = 0;

        int vehIndexFrom, vehIndexTo;
        double bestNCost, neighborCost;

        int swapIndexA = -1, swapIndexB = -1, swapRouteFrom = -1, swapRouteTo = -1;
        int iteration_number = 0;

        int dimensionCustomer = this.distances[1].length;
        int tabuMatrix[][] = new int[dimensionCustomer + 1][dimensionCustomer + 1];

        this.bestSolutionCost = this.cost;

        while (true) {
            bestNCost = Double.MAX_VALUE;

            for (vehIndexFrom = 0; vehIndexFrom < this.vehicles.length; vehIndexFrom++) {
                routesFrom = this.vehicles[vehIndexFrom].routes;
                int RoutFromLength = routesFrom.size();

                for (int i = 1; i < (RoutFromLength - 1); i++) { //Not possible to move depot!
                    for (vehIndexTo = 0; vehIndexTo < this.vehicles.length; vehIndexTo++) {
                        routesTo = this.vehicles[vehIndexTo].routes;
                        int RouteToLength = routesTo.size();
                        for (int j = 0; (j < RouteToLength - 1); j++) {//Not possible to move after last Depot!

                            movingNodeDemand = routesFrom.get(i).demand;

                            //If we assign to a different route check capacity constrains
                            //if in the new route is the same no need to check for capacity
                            if ((vehIndexFrom == vehIndexTo) || this.vehicles[vehIndexTo].checkIfFits(movingNodeDemand)) {


                                if (!((vehIndexFrom == vehIndexTo) && ((j == i) || (j == i - 1))))  // Not a move that Changes solution cost
                                {
                                    double minusCost1 = this.distances[routesFrom.get(i - 1).nodeId][routesFrom.get(i).nodeId];
                                    double minusCost2 = this.distances[routesFrom.get(i).nodeId][routesFrom.get(i + 1).nodeId];
                                    double minusCost3 = this.distances[routesTo.get(j).nodeId][routesTo.get(j + 1).nodeId];

                                    double addedCost1 = this.distances[routesFrom.get(i - 1).nodeId][routesFrom.get(i + 1).nodeId];
                                    double addedCost2 = this.distances[routesTo.get(j).nodeId][routesFrom.get(i).nodeId];
                                    double addedCost3 = this.distances[routesFrom.get(i).nodeId][routesTo.get(j + 1).nodeId];

                                    //Check if the move is a Tabu! - If it is Tabu break
                                    if ((tabuMatrix[routesFrom.get(i - 1).nodeId][routesFrom.get(i + 1).nodeId] != 0)
                                            || (tabuMatrix[routesTo.get(j).nodeId][routesFrom.get(i).nodeId] != 0)
                                            || (tabuMatrix[routesFrom.get(i).nodeId][routesTo.get(j + 1).nodeId] != 0)) {
                                        break;
                                    }

                                    neighborCost = addedCost1 + addedCost2 + addedCost3
                                            - minusCost1 - minusCost2 - minusCost3;

                                    if (neighborCost < bestNCost) {
                                        bestNCost = neighborCost;
                                        swapIndexA = i;
                                        swapIndexB = j;
                                        swapRouteFrom = vehIndexFrom;
                                        swapRouteTo = vehIndexTo;
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

            int NodeIDBefore = routesFrom.get(swapIndexA - 1).nodeId;
            int NodeIDAfter = routesFrom.get(swapIndexA + 1).nodeId;
            int NodeID_F = routesTo.get(swapIndexB).nodeId;
            int NodeID_G = routesTo.get(swapIndexB + 1).nodeId;

            Random TabuRan = new Random();
            int randomDelay1 = TabuRan.nextInt(5);
            int randomDelay2 = TabuRan.nextInt(5);
            int randomDelay3 = TabuRan.nextInt(5);

            tabuMatrix[NodeIDBefore][SwapNode.nodeId] = this.tabuHorizon + randomDelay1;
            tabuMatrix[SwapNode.nodeId][NodeIDAfter] = this.tabuHorizon + randomDelay2;
            tabuMatrix[NodeID_F][NodeID_G] = this.tabuHorizon + randomDelay3;

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
            this.vehicles[swapRouteFrom].load -= movingNodeDemand;

            this.vehicles[swapRouteTo].routes = routesTo;
            this.vehicles[swapRouteTo].load += movingNodeDemand;

            this.cost += bestNCost;

            if (this.cost < this.bestSolutionCost) {
                iteration_number = 0;
                this.saveBestSolution();
            } else {
                iteration_number++;
            }

            if (iterations == iteration_number) {
                break;
            }
        }

        this.vehicles = this.bestSolutionVehicles;
        this.cost = this.bestSolutionCost;

        return this;
    }

    private void saveBestSolution() {
        this.bestSolutionCost = this.cost;
        for (int j = 0; j < this.noOfVehicles; j++) {
            this.bestSolutionVehicles[j].routes.clear();
            if (!this.vehicles[j].routes.isEmpty()) {
                int routSize = this.vehicles[j].routes.size();
                for (int k = 0; k < routSize; k++) {
                    Node n = this.vehicles[j].routes.get(k);
                    this.bestSolutionVehicles[j].routes.add(n);
                }
            }
        }
    }

    public void print() {
        System.out.println("=========================================================");

        for (int j = 0; j < this.noOfVehicles; j++) {
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


