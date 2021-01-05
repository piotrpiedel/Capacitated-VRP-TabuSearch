package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
        VrpData vrpDataForFirstRun = FileUtils.loadFileFromPathToVrpData("datasets/big/Golden_20.vrp");
        new TabuSearchAlgorithm(vrpDataForFirstRun)
                .solve()
                .print();
    }
}
