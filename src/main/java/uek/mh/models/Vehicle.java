package uek.mh.models;

import java.util.ArrayList;

public class Vehicle {
    public ArrayList<City> stopPoints = new ArrayList<>();
    private final int capacity;
    public int load;
    public int currentLocation;

    public Vehicle(int capacity) {
        this.capacity = capacity;
        this.load = 0;
        this.currentLocation = 0; //In depot Initially
        this.stopPoints.clear();
    }

    public void addStopPointToVehicle(City city)
    {
        stopPoints.add(city);
        this.load += city.demand;
        this.currentLocation = city.cityId;
    }

    public boolean checkIfCapacityFits(int dem) {
        return load + dem <= capacity;
    }
}