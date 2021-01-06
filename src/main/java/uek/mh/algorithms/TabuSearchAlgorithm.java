package uek.mh.algorithms;

import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabuSearchAlgorithm {
    private final double[][] distances;
    private final int numberOfVehicles;
    private final int tabuMemoryTime;
    private final Vehicle[] bestSolution;
    private final int totalIterations;
    private List<Vehicle> vehicles;
    private double cost;

    private double bestSolutionCost;

    public TabuSearchAlgorithm(VrpDataConfig vrpDataConfig) {
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(vrpDataConfig);
        greedyAlgorithm.runAlgorithm();
        greedyAlgorithm.print();

        this.distances = vrpDataConfig.getDistance();
        this.tabuMemoryTime = vrpDataConfig.getTabuHorizonSize();
        this.totalIterations = vrpDataConfig.getIterations();
        this.vehicles = greedyAlgorithm.getVehicles();
        this.cost = greedyAlgorithm.getCost();
        this.numberOfVehicles = greedyAlgorithm.getFinalNumberOfUsedVehicles();
        this.bestSolution = new Vehicle[this.numberOfVehicles];

        for (int i = 0; i < this.numberOfVehicles; i++) {
            this.bestSolution[i] = new Vehicle(vrpDataConfig.getVehicleCapacity());
        }
    }

    public TabuSearchAlgorithm run() {
        //We use 1-0 exchange move
        ArrayList<City> routeFrom;
        ArrayList<City> routeTo;

        int currentNodeDemand = 0;

        int vehicleIndexFrom;
        int vehicleIndexTo;
        double bestIterationCost;
        double currentCost;

        int swapIndexA = -1;
        int swapIndexB = -1;
        int swapRouteFrom = -1;
        int swapRouteTo = -1;

        int[][] tabuMatrix = new int[this.distances[1].length + 1][this.distances[1].length + 1];

        this.bestSolutionCost = this.cost;

        for (int iterations = this.totalIterations; iterations > 0; iterations--) {
            bestIterationCost = Double.MAX_VALUE;

            for (vehicleIndexFrom = 0; vehicleIndexFrom < this.vehicles.size(); vehicleIndexFrom++) {
                routeFrom = this.vehicles.get(vehicleIndexFrom).stopPoints;

                for (int i = 1; i < (routeFrom.size() - 1); i++) { // on first and last position is depot, which we cannot move
                    for (vehicleIndexTo = 0; vehicleIndexTo < this.vehicles.size(); vehicleIndexTo++) {
                        routeTo = this.vehicles.get(vehicleIndexTo).stopPoints;
                        for (int j = 0; (j < routeTo.size() - 1); j++) { // on last position is depot, which we cannot move

                            currentNodeDemand = routeFrom.get(i).demand;

                            if ((vehicleIndexFrom != vehicleIndexTo) && !this.vehicles.get(vehicleIndexTo).checkIfCapacityFits(currentNodeDemand)) {
                                // if we swap to different route and there's no enough capacity, it means we cannot make swap to that route
                                break;
                            }

                            if (((vehicleIndexFrom == vehicleIndexTo) && ((j == i) || (j == i - 1)))) {
                                // that swap would not change anything
                                continue;
                            }

                            if ((tabuMatrix[routeFrom.get(i - 1).cityId][routeFrom.get(i + 1).cityId] != 0)
                                    || (tabuMatrix[routeTo.get(j).cityId][routeFrom.get(i).cityId] != 0)
                                    || (tabuMatrix[routeFrom.get(i).cityId][routeTo.get(j + 1).cityId] != 0)) {
                                // checking if that move isn't in tabu
                                break;
                            }

                            double subtractedCosts = this.distances[routeFrom.get(i - 1).cityId][routeFrom.get(i).cityId]
                                    + this.distances[routeFrom.get(i).cityId][routeFrom.get(i + 1).cityId]
                                    + this.distances[routeTo.get(j).cityId][routeTo.get(j + 1).cityId];

                            double addedCosts = this.distances[routeFrom.get(i - 1).cityId][routeFrom.get(i + 1).cityId]
                                    + this.distances[routeTo.get(j).cityId][routeFrom.get(i).cityId]
                                    + this.distances[routeFrom.get(i).cityId][routeTo.get(j + 1).cityId];

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

            routeFrom = this.vehicles.get(swapRouteFrom).stopPoints;
            routeTo = this.vehicles.get(swapRouteTo).stopPoints;

            City swapCity = routeFrom.get(swapIndexA);

            int nodeIdBefore = routeFrom.get(swapIndexA - 1).cityId;
            int nodeIdAfter = routeFrom.get(swapIndexA + 1).cityId;
            int nodeId_F = routeTo.get(swapIndexB).cityId;
            int nodeId_G = routeTo.get(swapIndexB + 1).cityId;


            tabuMatrix[nodeIdBefore][swapCity.cityId] = this.tabuMemoryTime;
            tabuMatrix[swapCity.cityId][nodeIdAfter] = this.tabuMemoryTime;
            tabuMatrix[nodeId_F][nodeId_G] = this.tabuMemoryTime;

            routeFrom.remove(swapIndexA);

            if (swapRouteFrom == swapRouteTo) {
                if (swapIndexA < swapIndexB) {
                    routeTo.add(swapIndexB, swapCity);
                } else {
                    routeTo.add(swapIndexB + 1, swapCity);
                }
            } else {
                routeTo.add(swapIndexB + 1, swapCity);
            }

            this.vehicles.get(swapRouteFrom).stopPoints = routeFrom;
            this.vehicles.get(swapRouteFrom).load -= currentNodeDemand;

            this.vehicles.get(swapRouteTo).stopPoints = routeTo;
            this.vehicles.get(swapRouteTo).load += currentNodeDemand;

            this.cost += bestIterationCost;

            if (this.cost < this.bestSolutionCost) {
                this.saveBestSolution();
            }
        }

        this.vehicles = Arrays.asList(this.bestSolution);
        this.cost = this.bestSolutionCost;

        return this;
    }

    private void saveBestSolution() {
        this.bestSolutionCost = this.cost;
        for (int j = 0; j < this.numberOfVehicles; j++) {
            this.bestSolution[j].stopPoints.clear();
            if (!this.vehicles.get(j).stopPoints.isEmpty()) {
                int routSize = this.vehicles.get(j).stopPoints.size();
                for (int k = 0; k < routSize; k++) {
                    City n = this.vehicles.get(j).stopPoints.get(k);
                    this.bestSolution[j].stopPoints.add(n);
                }
            }
        }
    }

    public void printAll() {
        System.out.println("================TABU SEARCH=============================");

        for (int vehicleIndex = 0; vehicleIndex < this.numberOfVehicles; vehicleIndex++) {
            if (!this.vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1) + ":");
                int routSize = this.vehicles.get(vehicleIndex).stopPoints.size();
                for (int k = 0; k < routSize; k++) {
                    if (k == routSize - 1) {
                        System.out.print(this.vehicles.get(vehicleIndex).stopPoints.get(k).cityId);
                    } else {
                        System.out.print(this.vehicles.get(vehicleIndex).stopPoints.get(k).cityId + "->");
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


