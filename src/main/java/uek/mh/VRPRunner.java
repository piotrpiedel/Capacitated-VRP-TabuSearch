package uek.mh;

import com.beust.jcommander.JCommander;
import uek.mh.algorithms.TabuSearchAlgorithm;

import java.io.IOException;

public class VRPRunner {
    public String instance = "C:\\Users\\habzd\\Documents\\projectocnversion\\src\\main\\resources\\datasets\\big\\Golden_20.vrp";
    public int iterations = 100;
    public Integer tabuHorizon = 10;

    public static void main(String[] args) throws IOException {
        VRPRunner jct = new VRPRunner();
        JCommander jCommander = new JCommander(jct, args);
        new TabuSearchAlgorithm(jct)
                .solve()
                .print();
    }
}
