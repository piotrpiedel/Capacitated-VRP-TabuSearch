package uek.mh.models;

import java.util.ArrayList;

public class Vehicle {
    public ArrayList<Node> stopPoints = new ArrayList<>();
    public int load;
    public int currentLocation;
    private int capacity;

    public Vehicle(int capacity) {
        this.capacity = capacity;
        this.load = 0;
        this.currentLocation = 0; //In depot Initially
        this.stopPoints.clear();
    }

    public void addStopPointToVehicle(Node customer) {
        stopPoints.add(customer);
        this.load += customer.demand;
        this.currentLocation = customer.nodeId;
    }

    public boolean checkIfCapacityFits(int dem) //Check if we have Capacity Violation
    {
        return load + dem <= capacity;
    }
}