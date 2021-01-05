package uek.mh;

import lombok.Data;

@Data
public class VrpData {

    public int dimension;
    public int vehicleCapacity;
    public double[][] coordinates;
    public double[][] distance;
    public int[] demand;
}
