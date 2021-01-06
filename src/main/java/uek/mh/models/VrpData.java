package uek.mh.models;

import lombok.Data;

import java.util.List;

@Data
public class VrpData {
    public int vehicles;
    public int iterations = 10000;
    public int tabuHorizonSize = 10;

    public int numberOfCities;
    public int vehicleCapacity;
    public double[][] coordinates;
    public double[][] distance;
    public List<Integer> demand;

    public int getDemandForCity(int i) {
        return demand.get(i);
    }
}
