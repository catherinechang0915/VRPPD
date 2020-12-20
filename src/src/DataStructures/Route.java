package src.DataStructures;

import javafx.util.Pair;

import java.util.List;

/**
 * A set of nodes traversed by the same vehicle in the solution
 */
public class Route {

    private List<Node> route;
    private Vehicle vehicle;
    private double dist;
    private double penalty;

    public Route(List<Node> route, Vehicle vehicle, double dist, double penalty) {
        this.route = route;
        this.vehicle = vehicle;
    }

//    public void updateObjective(double[][] distanceMatrix) {
//        for (int i = 0; i < route.size(); i++) {
//            if (i != route.size() - 1) {
//                dist += distanceMatrix[route.get(i).getIndex()][route.get(i + 1).getIndex()];
//            }
//            penalty += route.get(i).getDL();
//        }
//    }

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
        for (Node node : route) {
            sb.append(node.toString());
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
}
