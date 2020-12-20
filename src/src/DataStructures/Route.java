package src;

import java.util.List;

public class Route {

    List<Node> route;
    Vehicle vehicle; // specify the vehicle

    public Route(List<Node> route, Vehicle vehicle) {
        this.route = route;
        this.vehicle = vehicle;
    }

    public String trace() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle ").append(vehicle.getK()).append("\n");
        for (Node node : route) {
            sb.append(node.getIndex()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vehicle ").append(vehicle.getK()).append(" with capacity ")
                .append(vehicle.getCapacity()).append("\n");
        for (Node node : route) {
            sb.append(node.toString()).append("\n");
        }
        return sb.toString();
    }
}
