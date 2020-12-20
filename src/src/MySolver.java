package src;

import javafx.util.Pair;
import src.DataStructures.*;

import java.util.*;

public class MySolver {

    private InputParam inputParam;

    public MySolver(InputParam inputParam) {
        this.inputParam = inputParam;
    }

    public Solution initialSolutionConstruction() {
        List<Route> routes = new LinkedList<>();
        double totalDistance = 0;
        double totalDelay = 0;
        int n = inputParam.getN() * 2 + 2;
        Set<Integer> nodeNotProcessed = new HashSet<>();
        for (int i = 1; i < n - 1; i++) {
            nodeNotProcessed.add(i);
        }

        int routeCount = 0;

        while (nodeNotProcessed.size() != 0) {
            Route route = initialRouteConstruction(routeCount, inputParam, nodeNotProcessed);
            totalDistance += route.getDist();
            totalDelay += route.getPenalty();
            routeCount++;
        }

        double objective = inputParam.getAlpha() * totalDistance + inputParam.getBeta() * totalDelay;
        return new Solution(routes, totalDistance, totalDelay, objective);
    }

    private Route initialRouteConstruction(int routeCount, InputParam inputParam, Set<Integer> nodeNotProcessed) {
        double[][] distanceMatrix = inputParam.getDistanceMatrix();
        Node[] nodes = inputParam.getNodes();
        Vehicle vehicle = inputParam.getVehicles()[routeCount];

        // initialize route with depots
        List<Node> routeNodes = new LinkedList<>();
        routeNodes.add(nodes[0]);
        routeNodes.add(nodes[nodes.length - 1]);

        double alpha = inputParam.getAlpha();
        double beta = inputParam.getBeta();
        double dist = distanceMatrix[0][nodes.length - 1];
        double penalty = 0;
        double objective = alpha * dist + beta * penalty;

        while (true) {
            int nodeIndexToInsert = -1;
            int indexToInsert = -1;
            double minObjectiveIncrease = Double.MAX_VALUE;
            for (int nodeIndex : nodeNotProcessed) {
                for (int index = 1; index < routeNodes.size(); index++) {
                    double objectiveIncrease
                            = checkNodeInsertion(routeNodes, nodeIndex, index, inputParam, vehicle);
                    if (objectiveIncrease < 0) continue; // infeasible
                    if (objectiveIncrease < minObjectiveIncrease) {
                        minObjectiveIncrease = objectiveIncrease;
                        indexToInsert = index;
                        nodeIndexToInsert = nodeIndex;
                    }
                }
            }
            if (indexToInsert == -1) break; // no feasible position to insert on this route
            nodeInsertion(routeNodes, nodeIndexToInsert, indexToInsert, inputParam);
            nodeNotProcessed.remove(nodeIndexToInsert);
            objective += minObjectiveIncrease;
        }

        Pair<Double, Double> pairs = calculateObjective(routeNodes, distanceMatrix);
        System.out.println(pairs.getKey() + " " + pairs.getValue());
        dist = pairs.getKey();
        penalty = pairs.getValue();
        Route route = new Route(routeNodes, vehicle, dist, penalty);
        return route;
    }

    public double checkNodeInsertion(List<Node> routeNodes, int nodeIndex, int index, InputParam inputParam, Vehicle vehicle) {
        double[][] distanceMatrix = inputParam.getDistanceMatrix();
        Node[] nodes = inputParam.getNodes();

        // First check pick and delivery order
        int N = inputParam.getN();
        int pairNodeIndex = nodeIndex < N ? nodeIndex + N : nodeIndex - N;
        int pairIndex = routeNodes.indexOf(nodes[pairNodeIndex]);
        if (pairIndex != -1 && (pairIndex - index) * (pairNodeIndex - nodeIndex) < 0) {
            return -1;
        }

        // calculate the increase distance
        Node node = nodes[nodeIndex];
        Node prevNode = routeNodes.get(index - 1);
        Node nextNode = routeNodes.get(index);
        double distIncrease = distanceMatrix[prevNode.getIndex()][node.getIndex()]
                + distanceMatrix[node.getIndex()][nextNode.getIndex()]
                - distanceMatrix[prevNode.getIndex()][nextNode.getIndex()];

        // initialization to be the value before the node to be inserted
        double load = prevNode.getQ();
        double time = prevNode.getT();

        // calculate along iterating the nodes after, before insertion
        double originalPenalty = 0;
        double currPenalty = 0;

        // check the current insert node
        load += node.getq();
        if (load < 0 || load > vehicle.getCapacity()) return -1;
        time = Math.max(node.getTw1(), prevNode.gets() + time
                + distanceMatrix[prevNode.getIndex()][node.getIndex()]);
        if (time > node.getTw2() && node.getMembership() == 1) return -1;
        currPenalty += Math.max(time - node.getTw2(), 0);

        // check the node after
        originalPenalty += Math.max(nextNode.getT() - nextNode.getTw2(), 0);

        load += nextNode.getq();
        if (load < 0 || load > vehicle.getCapacity()) return -1;
        time = Math.max(nextNode.getTw1(), node.gets() + time
                + distanceMatrix[node.getIndex()][nextNode.getIndex()]);
        if (time > nextNode.getTw2() && nextNode.getMembership() == 1) return -1;
        currPenalty += Math.max(time - nextNode.getTw2(), 0);

        // check the following nodes
        Node currNode;
        for (int currIndex = index + 1; currIndex < routeNodes.size(); currIndex++) {
            prevNode = routeNodes.get(currIndex - 1);
            currNode = routeNodes.get(currIndex);
            // calculate original delay first
            originalPenalty += Math.max(currNode.getT() - currNode.getTw2(), 0);
            // check load
            load += currNode.getq();
            if (load < 0 || load > vehicle.getCapacity()) return -1;
            time = Math.max(currNode.getTw1(), prevNode.gets() + time
                    + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
            if (time > currNode.getTw2() && currNode.getMembership() == 1) return -1;
            currPenalty += Math.max(time - currNode.getTw2(), 0);
        }
        double penaltyIncrease = currPenalty - originalPenalty;
        return inputParam.getAlpha() * distIncrease + inputParam.getBeta() * penaltyIncrease;
    }

    public void nodeInsertion(List<Node> routeNodes, int nodeIndex, int index, InputParam inputParam) {
        Node[] nodes = inputParam.getNodes();
        double[][] distanceMatrix = inputParam.getDistanceMatrix();

        Node node = nodes[nodeIndex];
        Node prevNode = routeNodes.get(index - 1);

        double load = prevNode.getQ();
        double time = prevNode.getT();

        routeNodes.add(index, node);

        Node currNode;
        for (int currIndex = index; currIndex < routeNodes.size(); currIndex++) {
            currNode = routeNodes.get(currIndex);
            load += currNode.getq();
            node.setQ(load);
            time = Math.max(currNode.getTw1(), prevNode.gets() + time
                    + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
            double delay = Math.max(time - currNode.getTw2(), 0);
            currNode.setDL(delay);
            currNode.setT(time);
            prevNode = currNode;
        }
    }

    private Pair<Double, Double> calculateObjective(List<Node> routeNodes, double[][] distanceMatrix) {
        double dist = 0;
        double penalty = 0;
        for (int i = 0; i < routeNodes.size(); i++) {
            if (i != routeNodes.size() - 1) {
                dist += distanceMatrix[routeNodes.get(i).getIndex()][routeNodes.get(i + 1).getIndex()];
            }
            penalty += routeNodes.get(i).getDL();
        }
        return new Pair<>(dist, penalty);
    }
}
