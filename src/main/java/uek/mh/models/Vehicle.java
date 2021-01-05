package uek.mh.models;

import java.util.ArrayList;

public class Vehicle {
    public ArrayList<Node> stopPoints = new ArrayList<>();
    private int capacity;
    public int load;
    public int currentLocation;

    public Vehicle(int cap) {
        this.capacity = cap;
        this.load = 0;
        this.currentLocation = 0; //In depot Initially
        this.stopPoints.clear();
    }

    public void addNode(Node customer)//Add Customer to Vehicle routes
    {
        stopPoints.add(customer);
        this.load += customer.demand;
        this.currentLocation = customer.nodeId;
    }

    public boolean checkIfFits(int dem) //Check if we have Capacity Violation
    {
        return load + dem <= capacity;
    }
}