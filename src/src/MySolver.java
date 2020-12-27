package src;

import javafx.util.Pair;
import src.DataStructures.*;

import java.util.*;

public class MySolver {

    private InputParam inputParam;

    public MySolver(InputParam inputParam) {
        this.inputParam = inputParam;
    }

    public Solution solve() {
        int MAX_ITER = 1000;

        int N = inputParam.getN();
        int q = (int) (0.1 * N);

        Solution sol = initialSolutionConstruction();

        for (int i = 0; i < MAX_ITER; i++) {
            Set<Integer> nodePair = new HashSet<>();
            for (int k = 0; k < q; k++) {
                int rand = (int)(Math.random() * N) + 1;
                while (nodePair.contains(rand)) {
                    rand = (int)(Math.random() * N) + 1;
                }
                nodePair.add(rand);
            }
            randomDestroy(sol, nodePair);
            bestConstruct(sol, nodePair);
        }
        return sol;
    }

    private Solution initialSolutionConstruction() {
        List<Route> routes = new LinkedList<>();
        double totalDistance = 0;
        double totalDelay = 0;
        int N = inputParam.getN();

        // store the unprocessed request pairs
        Set<Integer> nodePairNotProcessed = new HashSet<>();
        for (int i = 1; i <= N; i++) {
            nodePairNotProcessed.add(i);
        }

        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
            Route route = initialRouteConstruction(routeCount, nodePairNotProcessed);
            routes.add(route);
            totalDistance += route.getDist();
            totalDelay += route.getPenalty();
        }

        return new Solution(routes, totalDistance, totalDelay);
    }

    private Route initialRouteConstruction(int routeCount, Set<Integer> nodeNotProcessed) {
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

        Route route = new Route(routeNodes, vehicle);

        while (true) {

            int nodeIndexToInsert = -1;
            int pIndexToInsert = -1;
            int dIndexToInsert = -1;
            double minObjectiveIncrease = Double.MAX_VALUE;
            double minDistIncrease = Double.MAX_VALUE;
            double minPenaltyIncrease = Double.MAX_VALUE;

            for (int nodeIndex : nodeNotProcessed) {
                for (int pIndex = 1; pIndex < routeNodes.size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < routeNodes.size(); dIndex++) {
                        Pair<Double, Double> objectivePairIncrease
                                 = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                        if (objectivePairIncrease == null) continue; // infeasible

                        if (alpha * objectivePairIncrease.getKey() + beta * objectivePairIncrease.getValue()
                                < minObjectiveIncrease) {
                            minDistIncrease = objectivePairIncrease.getKey();
                            minPenaltyIncrease = objectivePairIncrease.getValue();
                            pIndexToInsert = pIndex;
                            dIndexToInsert = dIndex;
                            nodeIndexToInsert = nodeIndex;
                        }
                    }
                }
            }
            if (nodeIndexToInsert == -1) break; // no feasible position to insert on this route
            nodeInsertion(route, nodeIndexToInsert, pIndexToInsert, dIndexToInsert);
            nodeNotProcessed.remove(nodeIndexToInsert);
            dist += minDistIncrease;
            penalty += minPenaltyIncrease;
        }
        route.setDist(dist);
        route.setPenalty(penalty);
        return route;
    }

//    private double calcNodePairBestInsertion(Route route, int nodeIndex) {
//
//    }

    /**
     * Feasibility check
     * @param route insert request in this route
     * @param nodeIndex node index in inputParam.Node[], request pair is (index, index + N)
     * @param pIndex pickup node insert in route at position pIndex
     * @param dIndex delivery node insert in route at position dIndex
     * @return dist increase and penalty increase
     */
    private Pair<Double, Double> checkNodePairInsertion(Route route, int nodeIndex, int pIndex, int dIndex) {

            List<Node> routeNodes = route.getNodes();

            double[][] distanceMatrix = inputParam.getDistanceMatrix();
            Node[] nodes = inputParam.getNodes();

            // case 1: p-d pair not consecutive
            // Got prev and next nodes for pickup and delivery nodes
            Node pNode = nodes[nodeIndex];
            Node dNode = nodes[nodeIndex + inputParam.getN()];
            Node prevPNode = routeNodes.get(pIndex - 1);
            Node nextPNode = routeNodes.get(pIndex);
            Node prevDNode = routeNodes.get(dIndex - 1);
            Node nextDNode = routeNodes.get(dIndex);

            // case 2: p-d pair consecutive
            if (pIndex == dIndex) {
                nextPNode = dNode;
                prevDNode = pNode;
            }

            Node prevNode = null;
            Node currNode = null;

            // calculate the increase distance
            double distIncrease = distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]
                    + distanceMatrix[pNode.getIndex()][nextPNode.getIndex()]
                    + distanceMatrix[prevDNode.getIndex()][dNode.getIndex()]
                    + distanceMatrix[dNode.getIndex()][nextDNode.getIndex()]
                    - distanceMatrix[prevPNode.getIndex()][nextPNode.getIndex()]
                    - distanceMatrix[prevDNode.getIndex()][nextDNode.getIndex()];

            // initialization to be the value before the node to be inserted
            double load = prevPNode.getQ();
            double time = prevPNode.getT();

            // calculate along iterating the nodes after, before insertion
            double originalPenalty = 0;
            double currPenalty = 0;

            // check the current insert pickup node
            load += pNode.getq();
            if (load < 0 || load > route.getVehicle().getCapacity()) return null;
            time = Math.max(pNode.getTw1(), prevPNode.gets() + time
                    + distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]);
            if (time > pNode.getTw2() && pNode.getMembership() == 1) return null;
            currPenalty += Math.max(time - pNode.getTw2(), 0);

            // check the following nodes
            prevNode = pNode;
            for (int currIndex = pIndex; currIndex < dIndex; currIndex++) {
                currNode = routeNodes.get(currIndex);
                // calculate original delay first
                originalPenalty += Math.max(currNode.getT() - currNode.getTw2(), 0);
                // check load
                load += currNode.getq();
                if (load < 0 || load > route.getVehicle().getCapacity()) return null;
                time = Math.max(currNode.getTw1(), prevNode.gets() + time
                        + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
                if (time > currNode.getTw2() && currNode.getMembership() == 1) return null;
                currPenalty += Math.max(time - currNode.getTw2(), 0);
            }

            // check the current insert delivery node
            load += dNode.getq();
            if (load < 0 || load > route.getVehicle().getCapacity()) return null;
            time = Math.max(dNode.getTw1(), prevDNode.gets() + time
                    + distanceMatrix[prevDNode.getIndex()][dNode.getIndex()]);
            if (time > dNode.getTw2() && dNode.getMembership() == 1) return null;
            currPenalty += Math.max(time - dNode.getTw2(), 0);

            // check the following nodes
            prevNode = dNode;
            for (int currIndex = dIndex; currIndex < routeNodes.size(); currIndex++) {
                currNode = routeNodes.get(currIndex);
                // calculate original delay first
                originalPenalty += Math.max(currNode.getT() - currNode.getTw2(), 0);
                // check load
                load += currNode.getq();
                if (load < 0 || load > route.getVehicle().getCapacity()) return null;
                time = Math.max(currNode.getTw1(), prevNode.gets() + time
                        + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
                if (time > currNode.getTw2() && currNode.getMembership() == 1) return null;
                currPenalty += Math.max(time - currNode.getTw2(), 0);
            }

            double penaltyIncrease = currPenalty - originalPenalty;
            return new Pair<>(distIncrease, penaltyIncrease);
    }

    /**
     * Insert node[nodeIndex] at pIndex, node[nodeIndex + N] at dIndex
     * Update load, time after insertion position
     * @param route route to be inserted
     * @param nodeIndex index for node pair
     * @param pIndex pickup node insert in route at position pIndex
     * @param dIndex delivery node insert in route at position dIndex
     */
    private void nodeInsertion(Route route, int nodeIndex, int pIndex, int dIndex) {
        List<Node> routeNodes = route.getNodes();
        Node[] nodes = inputParam.getNodes();
        double[][] distanceMatrix = inputParam.getDistanceMatrix();

        Node pNode = nodes[nodeIndex];
        Node dNode = nodes[nodeIndex + inputParam.getN()];
        Node prevNode = routeNodes.get(pIndex - 1);

        double load = prevNode.getQ();
        double time = prevNode.getT();

        routeNodes.add(dIndex, dNode);
        routeNodes.add(pIndex, pNode);

        Node currNode;
        for (int currIndex = pIndex; currIndex < routeNodes.size(); currIndex++) {
            currNode = routeNodes.get(currIndex);
            load += currNode.getq();
            currNode.setQ(load);
            time = Math.max(currNode.getTw1(), prevNode.gets() + time
                    + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
            double delay = Math.max(time - currNode.getTw2(), 0);
            currNode.setDL(delay);
            currNode.setT(time);
            prevNode = currNode;
        }
    }

    /**
     * Destroy Operator: remove nodes in nodePair
     * @param sol solution to be operated on
     * @param nodePair request set to be removed
     */
    private void randomDestroy(Solution sol, Set<Integer> nodePair) {
        List<Route> routes = sol.getRoutes();
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            List<Node> routeNodes = route.getNodes();
            for (Node node : new LinkedList<>(routeNodes)) {
                if (nodePair.contains(node.getIndex())
                        || nodePair.contains(node.getIndex() - inputParam.getN())) {
                    routeNodes.remove(node);
                }
            }
        }
        updateSolution(sol);
    }

    /**
     * Recalculate load, time and objective for each route in solution
     * @param solution solution to be updated
     */
    private void updateSolution(Solution solution) {
        for (Route route : solution.getRoutes()) {
            updateRoute(route);
        }
    }

    /**
     * After destroy, update the load and time at each node
     * Recalculate and update the objective for each route
     * @param route route to be updated
     */
    private void updateRoute(Route route) {
        List<Node> nodes = route.getNodes();
        Node prevNode;
        Node currNode;
        double load = 0;
        double time = 0;
        double delay = 0;
        double dist = 0;
        double penalty = 0;
        for (int i = 1; i < nodes.size(); i++) {
            prevNode = nodes.get(i - 1);
            currNode = nodes.get(i);
            // load
            load += currNode.getq();
            currNode.setQ(load);
            // time
            time = Math.max(prevNode.gets() + Utils.calculateDistance(prevNode, currNode) + time, currNode.getTw1());
            currNode.setT(time);
            // delay
            delay = Math.max(0, time - currNode.getTw2());
            currNode.setDL(delay);
            penalty += delay;
            // dist
            dist += Utils.calculateDistance(prevNode, currNode);
        }
        route.setDist(dist);
        route.setPenalty(penalty);
    }

    /**
     * Construct Operator: insert at position with minimum objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    private void bestConstruct(Solution solution, Set<Integer> nodePair) {
        try {
            for (int nodeIndex : nodePair) {
                Route minRoute = null;
                int pIndexToInsert = -1;
                int dIndexToInsert = -1;
                double minObjectiveIncrease = Double.MAX_VALUE;
                double minDistIncrease = Double.MAX_VALUE;
                double minPenaltyIncrease = Double.MAX_VALUE;
                for (Route route : solution.getRoutes()) {
                    for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                        for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                            Pair<Double, Double> objectivePairIncrease
                                    = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                            if (objectivePairIncrease == null) continue; // infeasible
                            if (inputParam.getAlpha() * objectivePairIncrease.getKey()
                                    + inputParam.getBeta() * objectivePairIncrease.getValue()
                                    < minObjectiveIncrease) {
                                minRoute = route;
                                minDistIncrease = objectivePairIncrease.getKey();
                                minPenaltyIncrease = objectivePairIncrease.getValue();
                                pIndexToInsert = pIndex;
                                dIndexToInsert = dIndex;
                            }
                        }
                    }
                }
                if (minRoute == null) {
                    throw new NullPointerException("No feasible insertion");
                }
                nodeInsertion(minRoute, nodeIndex, pIndexToInsert, dIndexToInsert);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void regretConstruct(Solution solution, Set<Integer> nodePair) {

    }
}
