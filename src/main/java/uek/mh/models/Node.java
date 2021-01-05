package uek.mh.models;

public class Node {
    public int nodeId;
    public int demand; //Node Demand if Customer
    public boolean isRouted;

    public Node(int id, int demand) //Cunstructor for Customers
    {
        this.nodeId = id;
        this.demand = demand;
        this.isRouted = false;
    }
}