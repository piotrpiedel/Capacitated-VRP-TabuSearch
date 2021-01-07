package uek.mh;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.dataloader.DataFromFileConverterToInitialVrpData;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;

import java.util.List;

public class VRPRunner {

    public static void main(String[] args) throws Exception {
            System.setProperty("org.graphstream.ui", "swing");
//        VrpData vrpDataForFirstRun = DataFromFileConverterToInitialVrpData.convert("datasets/supported_datasets/Golden_20.vrp");
//        new TabuSearchAlgorithm(vrpDataForFirstRun)
//                .run()
//                .printOnlyCalculatedCost();
//
//        VrpData vrpDataForSecondRun = DataFromFileConverterToInitialVrpData.convert("datasets/supported_datasets/Golden_19.vrp");
//        new TabuSearchAlgorithm(vrpDataForSecondRun)
//                .run()
//                .printOnlyCalculatedCost();
//
//        VrpData vrpDataForThirdRun = DataFromFileConverterToInitialVrpData.convert("datasets/supported_datasets/Golden_18.vrp");
//        new TabuSearchAlgorithm(vrpDataForThirdRun)
//                .run()
//                .printOnlyCalculatedCost();

            VrpDataConfig vrpDataConfigPolandCitiesFromMhProject = DataFromFileConverterToInitialVrpData.convert("datasets/supported_datasets/Mhprojekt.vrp");
            TabuSearchAlgorithm tabuSearchAlgorithm = new TabuSearchAlgorithm(vrpDataConfigPolandCitiesFromMhProject);
            tabuSearchAlgorithm.run();
            tabuSearchAlgorithm.printAll();
            int numberOfVehicles = tabuSearchAlgorithm.getNumberOfVehicles();
            List<Vehicle> vehicles = tabuSearchAlgorithm.getVehicles();
            List<City> cities = tabuSearchAlgorithm.getCities();

            Graph graph = new SingleGraph("CVRP visualization");
            for (City city : cities) {
                    graph.addNode(String.valueOf(city.cityId));
            }
//            graph.addNode("A");
//            graph.addNode("B");
//            graph.addNode("C");
//            graph.addEdge("AB", "A", "B");
//            graph.addEdge("BC", "B", "C");
//            graph.addEdge("CA", "C", "A");

            graph.display();

            int i = 0;

            for (int vehicleIndex = 0; vehicleIndex < numberOfVehicles; vehicleIndex++) {

                    if (!vehicles.get(vehicleIndex).stopPoints.isEmpty()) {
                            System.out.print("Vehicle " + (vehicleIndex + 1) + " Load for vehicle " + vehicles.get(vehicleIndex).load + ":");
                            int routSize = vehicles.get(vehicleIndex).stopPoints.size();
                            for (int k = 0; k < routSize; k++) {
                                    if (k == routSize - 1) {
                                            System.out.print(vehicles.get(vehicleIndex).stopPoints.get(k).cityId);
                                    } else {
                                            System.out.print(vehicles.get(vehicleIndex).stopPoints.get(k).cityId + "->");
                                    }
                            }
                            System.out.println();
                    }
            }

    }
}
