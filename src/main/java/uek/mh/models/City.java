package uek.mh.models;

import lombok.Data;

@Data
public class City {
    public int cityId;
    public int demand;
    public boolean isRouted;

    public City(int id, int demand) {
        this.cityId = id;
        this.demand = demand;
        this.isRouted = false;
    }


}