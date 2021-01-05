package uek.mh;

import uek.mh.algorithms.TabuSearchAlgorithm;

import java.io.IOException;

public class VRPRunner {

    public static void main(String[] args) throws IOException {
        new TabuSearchAlgorithm()
                .solve()
                .print();
    }
}
