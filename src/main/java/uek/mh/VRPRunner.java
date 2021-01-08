package uek.mh;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.dataloader.DataFromFileConverterToInitialVrpData;
import uek.mh.models.City;
import uek.mh.models.Vehicle;
import uek.mh.models.VrpDataConfig;
import uek.mh.utils.GraphBuilder;

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

            VrpDataConfig vrpDataConfigPolandCitiesFromMhProject = DataFromFileConverterToInitialVrpData
                    .convert("datasets/supported_datasets/Mhprojekt.vrp");
            TabuSearchAlgorithm tabuSearchAlgorithm = new TabuSearchAlgorithm(vrpDataConfigPolandCitiesFromMhProject);
            tabuSearchAlgorithm.run();
            tabuSearchAlgorithm.printAll();

            int numberOfVehicles = tabuSearchAlgorithm.getNumberOfVehicles();
            List<Vehicle> vehicles = tabuSearchAlgorithm.getVehicles();
            List<City> cities = tabuSearchAlgorithm.getCities();

            GraphBuilder graphBuilder = new GraphBuilder(numberOfVehicles, vehicles, cities);
            Graph graph = graphBuilder.buildGraph();
            Viewer display = graph.display();
            display.disableAutoLayout();
    }
}
