package src.DataStructures;

import java.io.Serializable;

public class Vehicle implements Serializable {

    private int k; // index for the vehicle
    private int capacity;

    public Vehicle(int k, int capacity) {
        this.k = k;
        this.capacity = capacity;
    }

    public int getK() {
        return k;
    }

    public int getCapacity() {
        return capacity;
    }
}
