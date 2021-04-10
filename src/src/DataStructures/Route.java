package src.DataStructures;

import src.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * A set of nodes traversed by the same vehicle in the solution
 */
public class Route implements Serializable {

    private List<Node> route;
    private Vehicle vehicle;
    private double dist;
    private double penalty;

    public Route(List<Node> route, Vehicle vehicle, double dist, double penalty) {
        this.route = route;
        this.vehicle = vehicle;
        this.dist = dist;
        this.penalty = penalty;
    }

    public Route(List<Node> route, Vehicle vehicle) {
        this(route, vehicle, 0, 0);
    }

    public String trace() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle ").append(vehicle.getK()).append("\n");
        for (Node node : route) {
            sb.append(node.getIndex()).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle ").append(vehicle.getK()).append(" with capacity ")
                .append(vehicle.getCapacity()).append("\n");
        for (int i = 0; i < route.size(); i++) {
            sb.append(route.get(i).toString());
            if (i != route.size() - 1) {
                sb.append("\tDistance between " + Utils.calculateDistance(route.get(i), route.get(i + 1))).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    public double getDist() {
        return dist;
    }

    public double getPenalty() {
        return penalty;
    }

    public List<Node> getNodes() {
        return route;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public double getObjective(double alpha, double beta) {
        return alpha * dist + beta * penalty;
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Route)) return false;

        Route route = (Route) other;
        if (!this.vehicle.equals(route.getVehicle())) return false;
        return this.route.equals(route.getNodes());
    }
}
