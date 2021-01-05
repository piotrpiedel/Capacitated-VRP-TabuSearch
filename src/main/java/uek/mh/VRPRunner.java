package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
        VRPRunner jct = new VRPRunner();
        new TabuSearchAlgorithm(jct)
                .solve()
                .print();
    }
}
