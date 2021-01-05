package uek.mh.models;

public class Node {
    public int nodeId;
    public int demand;
    public boolean isRouted;

    public Node(int id, int demand) {
        this.nodeId = id;
        this.demand = demand;
        this.isRouted = false;
    }
}