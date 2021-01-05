package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.models.VrpData;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
        VrpData vrpDataForFirstRun = FileConverterToInitialVrpData.convert("datasets/big/Golden_20.vrp");
        new TabuSearchAlgorithm(vrpDataForFirstRun)
                .run()
                .printOnlyCalculatedCost();

        VrpData vrpDataForSecondRun = FileConverterToInitialVrpData.convert("datasets/big/Golden_19.vrp");
        new TabuSearchAlgorithm(vrpDataForSecondRun)
                .run()
                .printOnlyCalculatedCost();

        VrpData vrpDataForThirdRun = FileConverterToInitialVrpData.convert("datasets/big/Golden_18.vrp");
        new TabuSearchAlgorithm(vrpDataForThirdRun)
                .run()
                .printOnlyCalculatedCost();
    }
}
