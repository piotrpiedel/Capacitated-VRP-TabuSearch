package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.dataloader.DataFromFileConverterToInitialVrpData;
import uek.mh.models.VrpDataConfig;

public class VRPRunner {

    public static void main(String[] args) throws Exception {
            System.setProperty("org.graphstream.ui", "swing");

            VrpDataConfig vrpDataConfigPolandCitiesFromMhProject = DataFromFileConverterToInitialVrpData
                    .convert("Mhprojekt.vrp");
            TabuSearchAlgorithm tabuSearchAlgorithm = new TabuSearchAlgorithm(vrpDataConfigPolandCitiesFromMhProject);
            tabuSearchAlgorithm.run();
            tabuSearchAlgorithm.printResult();
    }
}
