package uek.mh.models;

import lombok.Data;

@Data
public class City {
    public int cityId;
    public int demand;
    public boolean isRouted;
    Coordinates coordinates;
    String name;

    public City(int id, int demand, Coordinates coordinates) {
        this.cityId = id;
        this.demand = demand;
        this.isRouted = false;
        this.coordinates = coordinates;
        this.name = coordinates.getName();
    }


}