package ro.uaic.info.tabu;

import ro.uaic.info.Node;
import ro.uaic.info.VRPLibReader;
import ro.uaic.info.VRPRunner;
import ro.uaic.info.Vehicle;
import ro.uaic.info.greedy.GreedySolver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class TabuSearchSolver {
    private final double[][] distances;
    private final int noOfVehicles;
    private final int TABU_Horizon;
    private final int iterations;
    private final Vehicle[] bestSolutionVehicles;

    private Vehicle[] vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchSolver(VRPRunner jct) throws IOException {

        VRPLibReader reader = new VRPLibReader(new BufferedReader(new FileReader(jct.instance)));
        this.noOfVehicles = reader.getDimension();
        this.TABU_Horizon = jct.TabuHorizon;
        this.distances = reader.getDistance();
        this.iterations = jct.iterations;

        GreedySolver greedySolver = new GreedySolver(jct);
        greedySolver.solve();
        this.vehicles = greedySolver.getVehicles();
        this.cost = greedySolver.getCost();

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

        int SwapIndexA = -1, SwapIndexB = -1, SwapRouteFrom = -1, SwapRouteTo = -1;
        int iteration_number = 0;

        int DimensionCustomer = this.distances[1].length;
        int TABU_Matrix[][] = new int[DimensionCustomer + 1][DimensionCustomer + 1];

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
                                    if ((TABU_Matrix[routesFrom.get(i - 1).nodeId][routesFrom.get(i + 1).nodeId] != 0)
                                            || (TABU_Matrix[routesTo.get(j).nodeId][routesFrom.get(i).nodeId] != 0)
                                            || (TABU_Matrix[routesFrom.get(i).nodeId][routesTo.get(j + 1).nodeId] != 0)) {
                                        break;
                                    }

                                    neighborCost = addedCost1 + addedCost2 + addedCost3
                                            - minusCost1 - minusCost2 - minusCost3;

                                    if (neighborCost < bestNCost) {
                                        bestNCost = neighborCost;
                                        SwapIndexA = i;
                                        SwapIndexB = j;
                                        SwapRouteFrom = vehIndexFrom;
                                        SwapRouteTo = vehIndexTo;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (int o = 0; o < TABU_Matrix[0].length; o++) {
                for (int p = 0; p < TABU_Matrix[0].length; p++) {
                    if (TABU_Matrix[o][p] > 0) {
                        TABU_Matrix[o][p]--;
                    }
                }
            }

            routesFrom = this.vehicles[SwapRouteFrom].routes;
            routesTo = this.vehicles[SwapRouteTo].routes;
            this.vehicles[SwapRouteFrom].routes = null;
            this.vehicles[SwapRouteTo].routes = null;

            Node SwapNode = routesFrom.get(SwapIndexA);

            int NodeIDBefore = routesFrom.get(SwapIndexA - 1).nodeId;
            int NodeIDAfter = routesFrom.get(SwapIndexA + 1).nodeId;
            int NodeID_F = routesTo.get(SwapIndexB).nodeId;
            int NodeID_G = routesTo.get(SwapIndexB + 1).nodeId;

            Random TabuRan = new Random();
            int randomDelay1 = TabuRan.nextInt(5);
            int randomDelay2 = TabuRan.nextInt(5);
            int randomDelay3 = TabuRan.nextInt(5);

            TABU_Matrix[NodeIDBefore][SwapNode.nodeId] = this.TABU_Horizon + randomDelay1;
            TABU_Matrix[SwapNode.nodeId][NodeIDAfter] = this.TABU_Horizon + randomDelay2;
            TABU_Matrix[NodeID_F][NodeID_G] = this.TABU_Horizon + randomDelay3;

            routesFrom.remove(SwapIndexA);

            if (SwapRouteFrom == SwapRouteTo) {
                if (SwapIndexA < SwapIndexB) {
                    routesTo.add(SwapIndexB, SwapNode);
                } else {
                    routesTo.add(SwapIndexB + 1, SwapNode);
                }
            } else {
                routesTo.add(SwapIndexB + 1, SwapNode);
            }

            this.vehicles[SwapRouteFrom].routes = routesFrom;
            this.vehicles[SwapRouteFrom].load -= movingNodeDemand;

            this.vehicles[SwapRouteTo].routes = routesTo;
            this.vehicles[SwapRouteTo].load += movingNodeDemand;

            this.cost += bestNCost;

            if (this.cost < this.bestSolutionCost) {
                iteration_number = 0;
                this.SaveBestSolution();
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

    private void SaveBestSolution() {
        this.bestSolutionCost = this.cost;
        for (int j = 0; j < this.noOfVehicles; j++) {
            this.bestSolutionVehicles[j].routes.clear();
            if (!this.vehicles[j].routes.isEmpty()) {
                int RoutSize = this.vehicles[j].routes.size();
                for (int k = 0; k < RoutSize; k++) {
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


