package uek.mh.utils;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import uek.mh.models.City;
import uek.mh.models.Vehicle;

import java.util.List;

public class GraphBuilder {
    private int numberOfVehicles;
    private List<Vehicle> vehicles;
    private List<City> cities;

    public GraphBuilder(int numberOfVehicles, List<Vehicle> vehicles, List<City> cities) {
        this.numberOfVehicles = numberOfVehicles;
        this.vehicles = vehicles;
        this.cities = cities;
    }

    public Graph buildGraph() {
        Graph graph = new SingleGraph("CVRP visualization");
        for (City city : cities) {
            graph.addNode(String.valueOf(city.cityId))
                    .setAttribute("xy", city.getCoordinates().getLatitude(), city.getCoordinates().getLongitude());

        }
        for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {
            if (!vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                int routSize = vehicles.get(vehicleIndex).stopPoints.size();
                for (int k = 0; k < routSize - 1; k++) {
                    graph.addEdge(String.valueOf(vehicles.get(vehicleIndex).stopPoints.get(k).cityId) + vehicles.get(vehicleIndex).stopPoints.get(k + 1).cityId,
                            String.valueOf(vehicles.get(vehicleIndex).stopPoints.get(k).cityId), String.valueOf(vehicles.get(vehicleIndex).stopPoints.get(k + 1).cityId));

                }
            }
        }
        return graph;
    }
}
