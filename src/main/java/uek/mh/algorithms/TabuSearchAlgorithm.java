package uek.mh.algorithms;

import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;

import java.util.ArrayList;
import java.util.List;

public class TabuSearchAlgorithm {
    private final double[][] distances;
    private final List<City> cities;
    private final List<Vehicle> vehicles;
    private final List<Vehicle> bestSolution;
    private double cost;

    private final int numberOfVehicles;
    private final int tabuMemoryTime;
    private final int totalIterations;

    private double bestSolutionCost;

    public TabuSearchAlgorithm(VrpDataConfig vrpDataConfig) throws Exception {
        GreedyAlgorithm greedyAlgorithm = getBestSolutionFromGreedyAlgorithm(vrpDataConfig);
        cities = greedyAlgorithm.getCities();

        distances = vrpDataConfig.getDistance();
        tabuMemoryTime = vrpDataConfig.getTabuHorizonSize();
        totalIterations = vrpDataConfig.getIterations();
        vehicles = greedyAlgorithm.getVehicles();
        cost = greedyAlgorithm.getTotalRouteCost();
        numberOfVehicles = greedyAlgorithm.getFinalNumberOfUsedVehicles();
        bestSolution = new ArrayList<>();

        createStartupVehicles(vrpDataConfig);
    }

    private void createStartupVehicles(VrpDataConfig vrpDataConfig) {
        for (int i = 0; i < numberOfVehicles; i++) {
            bestSolution.add(new Vehicle(vrpDataConfig.getVehicleCapacity()));
        }
    }

    private GreedyAlgorithm getBestSolutionFromGreedyAlgorithm(VrpDataConfig vrpDataConfig) throws Exception {
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(vrpDataConfig);
        greedyAlgorithm.runAlgorithm();
        greedyAlgorithm.print();
        return greedyAlgorithm;
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
        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            ArrayList<City> stopPoints = bestSolution.get(vehicleIndex).getStopPoints();
            stopPoints.clear();
            Vehicle vehicle = vehicles.get(vehicleIndex);
            ArrayList<City> vehicleStopPoints = vehicle.getStopPoints();
            if (!vehicleStopPoints.isEmpty()) {
                stopPoints.addAll(vehicleStopPoints);
            }
        }
    }

    public void printAll() {
        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            ArrayList<City> stopPointsForVehicle = vehicles.get(vehicleIndex).stopPoints;
            if (!stopPointsForVehicle.isEmpty()) {
                System.out.print("Vehicle " + (vehicleIndex + 1) + " Load for vehicle " + vehicles.get(vehicleIndex).load + ":");
                int routSize = stopPointsForVehicle.size();

                for (int k = 0; k < routSize; k++) {
                    if (k == routSize - 1) {
                        System.out.print(stopPointsForVehicle.get(k).getName());
                    } else {
                        City city = stopPointsForVehicle.get(k);
                        System.out.print(city.getName() + "->");
                    }
                }
                System.out.println();
            }
        }
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


