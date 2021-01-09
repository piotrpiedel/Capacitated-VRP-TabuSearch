package uek.mh.models;

import lombok.Data;

import java.util.List;

@Data
public class VrpDataConfig {
    public int numberOfVehicles;
    public int iterations = 100000;
    public int tabuHorizonSize = 10;

    public int numberOfCities;
    public int vehicleCapacity;
    public List<Coordinates> coordinates;
    public double[][] distance;
    public List<Integer> demand;

    public int getDemandForCity(int i) {
        return demand.get(i);
    }
}
