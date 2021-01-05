package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;
import uek.mh.models.VrpData;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
        VrpData vrpDataForFirstRun = FileConverterToInitialVrpData.convert("datasets/big/Golden_20.vrp");
        new TabuSearchAlgorithm(vrpDataForFirstRun)
                .solve()
                .print();
    }
}
