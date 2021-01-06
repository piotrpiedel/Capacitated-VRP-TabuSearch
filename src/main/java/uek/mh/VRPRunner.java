package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.models.VrpData;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
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

        VrpData vrpDataPolandCitiesFromMhProject = DataFromFileConverterToInitialVrpData.convert("datasets/supported_datasets/Mhprojekt.vrp");
        new TabuSearchAlgorithm(vrpDataPolandCitiesFromMhProject)
                .run()
                .printOnlyCalculatedCost();
    }
}
