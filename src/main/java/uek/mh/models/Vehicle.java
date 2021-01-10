package uek.mh.models;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class Vehicle {
    public ArrayList<City> stopPoints;
    private final int capacity;
    public int load;
    public int currentLocation;
    public int vehicleId;

    public Vehicle(int id, int capacity) {
        this.vehicleId = id;
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
        return (this.load + dem <= capacity);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "currentLocation=" + currentLocation +
                ", vehicleId=" + vehicleId +
                '}';
    }
}