package uek.mh.algorithms;

import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;

import java.util.ArrayList;
import java.util.List;

public class TabuSearchAlgorithm {
    private final double[][] distances;
    private final int numberOfVehicles;
    private final int tabuMemoryTime;
    private final Vehicle[] bestSolution;
    private final int totalIterations;
    private List<Vehicle> vehicles;
    private double cost;
    private List<City> cities;

    private double bestSolutionCost;

    private HaversineDistanceCalculator haversineDistanceCalculator;

    public TabuSearchAlgorithm(VrpDataConfig vrpDataConfig) throws Exception {
        haversineDistanceCalculator = new HaversineDistanceCalculator();

        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(vrpDataConfig);
        greedyAlgorithm.runAlgorithm();
        greedyAlgorithm.print();
        cities = greedyAlgorithm.getCities();

        distances = vrpDataConfig.getDistance();
        tabuMemoryTime = vrpDataConfig.getTabuHorizonSize();
        totalIterations = vrpDataConfig.getIterations();
        vehicles = greedyAlgorithm.getVehicles();
        cost = greedyAlgorithm.getTotalRouteCost();
        numberOfVehicles = greedyAlgorithm.getFinalNumberOfUsedVehicles();
        bestSolution = new Vehicle[numberOfVehicles];

        for (int i = 0; i < numberOfVehicles; i++) {
            bestSolution[i] = new Vehicle(vrpDataConfig.getVehicleCapacity());
        }
    }

    public void run() {
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

        int[][] tabuMatrix = new int[distances[1].length][distances[1].length];

        bestSolutionCost = cost;

        for (int iterations = totalIterations; iterations > 0; iterations--) {
            bestIterationCost = Double.MAX_VALUE;

            for (vehicleIndexFrom = 0; vehicleIndexFrom < vehicles.size(); vehicleIndexFrom++) {
                routeFrom = vehicles.get(vehicleIndexFrom).stopPoints;

                for (int i = 1; i < (routeFrom.size() - 1); i++) { // on first and last position is depot, which we cannot move
                    for (vehicleIndexTo = 0; vehicleIndexTo < vehicles.size(); vehicleIndexTo++) {
                        routeTo = vehicles.get(vehicleIndexTo).stopPoints;
                        for (int j = 0; (j < routeTo.size() - 1); j++) { // on last position is depot, which we cannot move

                            currentNodeDemand = routeFrom.get(i).demand;

                            if ((vehicleIndexFrom != vehicleIndexTo) &&
                                    !vehicles.get(vehicleIndexTo).checkIfCapacityFits(currentNodeDemand)) {
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
                                continue;
                            }

                            double subtractedCosts = distances[routeFrom.get(i - 1).cityId][routeFrom.get(i).cityId]
                                    + distances[routeFrom.get(i).cityId][routeFrom.get(i + 1).cityId]
                                    + distances[routeTo.get(j).cityId][routeTo.get(j + 1).cityId];

                            double addedCosts = distances[routeFrom.get(i - 1).cityId][routeFrom.get(i + 1).cityId]
                                    + distances[routeTo.get(j).cityId][routeFrom.get(i).cityId]
                                    + distances[routeFrom.get(i).cityId][routeTo.get(j + 1).cityId];

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

            routeFrom = vehicles.get(swapRouteFrom).stopPoints;
            routeTo = vehicles.get(swapRouteTo).stopPoints;

            City swapCity = routeFrom.get(swapIndexA);

            int nodeIdBefore = routeFrom.get(swapIndexA - 1).cityId;
            int nodeIdAfter = routeFrom.get(swapIndexA + 1).cityId;
            int nodeId_F = routeTo.get(swapIndexB).cityId;
            int nodeId_G = routeTo.get(swapIndexB + 1).cityId;


            tabuMatrix[nodeIdBefore][swapCity.cityId] = tabuMemoryTime;
            tabuMatrix[swapCity.cityId][nodeIdAfter] = tabuMemoryTime;
            tabuMatrix[nodeId_F][nodeId_G] = tabuMemoryTime;

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

            vehicles.get(swapRouteFrom).stopPoints = routeFrom;
            vehicles.get(swapRouteFrom).load -= swapCity.demand;

            vehicles.get(swapRouteTo).stopPoints = routeTo;
            vehicles.get(swapRouteTo).load += swapCity.demand;

            cost += bestIterationCost;

            if (cost < bestSolutionCost) {
                saveBestSolution();
            }
        }

        cost = bestSolutionCost;
    }

    private void saveBestSolution() {
        bestSolutionCost = cost;
        for (int j = 0; j < numberOfVehicles; j++) {
            bestSolution[j].stopPoints.clear();
            if (!vehicles.get(j).stopPoints.isEmpty()) {
                int routSize = vehicles.get(j).stopPoints.size();
                for (int k = 0; k < routSize; k++) {
                    City n = vehicles.get(j).stopPoints.get(k);
                    bestSolution[j].stopPoints.add(n);
                }
            }
        }
    }

    public void printAll() {
        double totalDistanceToAllVehicles = 0;
        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            if (!vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1) + " Load for vehicle " + vehicles.get(vehicleIndex).load + ":");
                int routSize = vehicles.get(vehicleIndex).stopPoints.size();
                double distanceSizeForVehicle = 0;

                for (int k = 0; k < routSize; k++) {
                    if (k == routSize - 1) {
                        System.out.print(vehicles.get(vehicleIndex).stopPoints.get(k).getName());
                    } else {
                        City city = vehicles.get(vehicleIndex).stopPoints.get(k);
                        distanceSizeForVehicle += haversineDistanceCalculator.calculateDistance(city.getCoordinates(), vehicles.get(vehicleIndex).stopPoints.get(k + 1).getCoordinates());
                        System.out.print(city.getName() + "->");
                    }
                }
                totalDistanceToAllVehicles += distanceSizeForVehicle;
                System.out.println();
            }
        }
        System.out.println("\nDistance from all vehicles: " + totalDistanceToAllVehicles + "\n");
        System.out.println("\nBest Value: " + cost + "\n");
    }

    public void printOnlyCalculatedCost() {
        System.out.println("=========================================================");
        System.out.println("\nBest Value: " + cost + "\n");
    }

    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<City> getCities() {
        return cities;
    }
}


