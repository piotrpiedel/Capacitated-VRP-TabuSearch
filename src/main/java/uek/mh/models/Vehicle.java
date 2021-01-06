package uek.mh.models;

import java.util.ArrayList;

public class Vehicle {
    public ArrayList<City> stopPoints;
    private final int capacity;
    public int load;
    public int currentLocation;

    public Vehicle(int capacity) {
        this.capacity = capacity;
        this.load = 0;
        this.currentLocation = 0; // this is starting point
        stopPoints = new ArrayList<>();
    }

    public void addStopPointToVehicle(City city) {
        stopPoints.add(city);
        this.load += city.getDemand();
        this.currentLocation = city.getCityId();
    }

    public boolean checkIfCapacityFits(int dem) {
        return load + dem <= capacity;
    }
}