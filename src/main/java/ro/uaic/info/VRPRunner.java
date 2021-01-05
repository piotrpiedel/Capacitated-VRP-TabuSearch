package ro.uaic.info;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ro.uaic.info.tabu.TabuSearchSolver;

import java.io.IOException;

public class VRPRunner {
    public String instance = "C:\\Users\\habzd\\Documents\\projectocnversion\\src\\main\\resources\\datasets\\big\\Golden_20.vrp";
    public int iterations = 100;
    public Integer tabuHorizon = 10;

    public static void main(String[] args) throws IOException {
        VRPRunner jct = new VRPRunner();
        JCommander jCommander = new JCommander(jct, args);
        new TabuSearchSolver(jct)
                .solve()
                .print();
    }
}
