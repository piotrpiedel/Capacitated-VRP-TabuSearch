package uek.mh.algorithms;

import org.graphstream.graph.Graph;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;
import uek.mh.utils.GraphBuilder;
import uek.mh.utils.SolutionPrinter;

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
            bestSolution.add(new Vehicle(i, vrpDataConfig.getVehicleCapacity()));
        }
    }

    private GreedyAlgorithm getBestSolutionFromGreedyAlgorithm(VrpDataConfig vrpDataConfig) throws Exception {
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(vrpDataConfig);
        greedyAlgorithm.runAlgorithm();
        greedyAlgorithm.printResult();
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

                Vehicle vehicleFrom = vehicles.get(vehicleIndexFrom);
                routeFrom = vehicleFrom.stopPoints;

                for (int i = 1; i < (routeFrom.size() - 1); i++) {// on first and last position is depot, which we cannot move
                    City cityFrom = routeFrom.get(i);

                    for (vehicleIndexTo = 0; vehicleIndexTo < vehicles.size(); vehicleIndexTo++) {
                        Vehicle vehicleTo = vehicles.get(vehicleIndexTo);
                        routeTo = vehicleTo.stopPoints;
                        for (int j = 0; (j < routeTo.size() - 1); j++) { // on last position is depot, which we cannot move

                            currentNodeDemand = cityFrom.demand;

                            if ((vehicleIndexFrom != vehicleIndexTo) &&
                                    !vehicleTo.checkIfCapacityFits(currentNodeDemand)) {
                                // if we swap to different route and there's no enough capacity, it means we cannot make swap to that route
                                break;
                            }

                            if (((vehicleIndexFrom == vehicleIndexTo) && ((j == i) || (j == i - 1)))) {
                                // that swap would not change anything
                                continue;
                            }

                            City cityTo = routeTo.get(j);
                            City previousToCityFrom = routeFrom.get(i - 1);
                            if ((tabuMatrix[previousToCityFrom.cityId][routeFrom.get(i + 1).cityId] != 0)
                                    || (tabuMatrix[cityTo.cityId][cityFrom.cityId] != 0)
                                    || (tabuMatrix[cityFrom.cityId][routeTo.get(j + 1).cityId] != 0)) {
                                // checking if that move isn't in tabu
                                continue;
                            }

                            double subtractedCosts = distances[previousToCityFrom.cityId][cityFrom.cityId]
                                    + distances[cityFrom.cityId][routeFrom.get(i + 1).cityId]
                                    + distances[cityTo.cityId][routeTo.get(j + 1).cityId];

                            double addedCosts = distances[previousToCityFrom.cityId][routeFrom.get(i + 1).cityId]
                                    + distances[cityTo.cityId][cityFrom.cityId]
                                    + distances[cityFrom.cityId][routeTo.get(j + 1).cityId];

                            currentCost = addedCosts - subtractedCosts;

                            if (currentCost < bestIterationCost) {
                                bestIterationCost = currentCost;
                                swapIndexA = i;
                                swapIndexB = j;
                                swapRouteFrom = vehicleIndexFrom;
                                City fromSwap = vehicles.get(swapRouteFrom).getStopPoints().get(i);
                                swapRouteTo = vehicleIndexTo;
                                City toSwap = vehicles.get(swapRouteTo).getStopPoints().get(j);

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

            Vehicle vehicleFrom = vehicles.get(swapRouteFrom);
            routeFrom = vehicleFrom.stopPoints;
            Vehicle vehicleTo = vehicles.get(swapRouteTo);
            routeTo = vehicleTo.stopPoints;

            City swapCity = routeFrom.get(swapIndexA);


            City city = routeFrom.get(swapIndexA - 1);
            int nodeIdBefore = city.cityId;
            City city1 = routeFrom.get(swapIndexA + 1);
            int nodeIdAfter = city1.cityId;

            City city2 = routeTo.get(swapIndexB);
            int nodeId_F = city2.cityId;
            City city3 = routeTo.get(swapIndexB + 1);
            int nodeId_G = city3.cityId;


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

            vehicleFrom.stopPoints = routeFrom;
            vehicleFrom.load -= swapCity.demand;

            vehicleTo.stopPoints = routeTo;
            vehicleTo.load += swapCity.demand;

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

    public void printResult() {
        SolutionPrinter solutionPrinter = new SolutionPrinter(numberOfVehicles, vehicles, cost);
        solutionPrinter.print();

        displayGraphWithResult();
    }

    private void displayGraphWithResult() {
        GraphBuilder graphBuilder = new GraphBuilder(numberOfVehicles, vehicles, cities);
        Graph graph = graphBuilder.buildGraph();
        graph.display().disableAutoLayout();
    }
}


