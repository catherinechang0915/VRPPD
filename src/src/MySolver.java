package src;

import javafx.util.Pair;
import src.DataStructures.*;

import javax.xml.bind.annotation.XmlInlineBinaryData;
import java.util.*;

public class MySolver {

    private InputParam inputParam;
    private String destroyOperator;
    private String constructOperator;

    public MySolver(InputParam inputParam, String destroyOperator, String constructOperator) {
        this.inputParam = inputParam;
        this.destroyOperator = destroyOperator;
        this.constructOperator = constructOperator;
    }

    public Solution solve() {
        int MAX_ITER = 10000;
        int q = (int) (0.1 * inputParam.getN());

        long startTime = System.currentTimeMillis();
        Solution sol = initialSolutionConstruction();
        Solution bestSol = sol;

        Random random = new Random(100);

        List<Integer> nodePair = null;
        for (int i = 0; i < MAX_ITER; i++) {
            int rand = (int)(random.nextDouble() * 53) + 1;
            // Destroy
            switch (destroyOperator) {
                case "random":
                    nodePair = generateNodePairRandom(q);
                    break;
                case "worst":
                    nodePair = generateNodePairWorst(q);
                    break;
                case "shaw":
                    nodePair = generateNodePairShaw(q);
                    break;
            }
//            System.out.println(i + " " + nodePair);
            destroy(sol, nodePair);
            validation(sol);

            // Construct
            switch (constructOperator) {
                case "best":
                    bestConstruct(sol, nodePair);
                    break;
                case "regret":
                    regretConstruct(sol, nodePair);
                    break;
            }
//            System.out.println(sol.getVehicleNumber());
            validation(sol);
        }
        sol.setTimeElapsed(System.currentTimeMillis() - startTime);
        return sol;
    }

    /**
     * Randomly choose requests to be removed
     * @param q number of requests to be removed each iteration
     * @return request set
     */
    private List<Integer> generateNodePairRandom(int q) {
        int N = inputParam.getN();
        List<Integer> nodePair = new LinkedList<>();
        for (int k = 0; k < q; k++) {
            int rand = (int)(Math.random() * N) + 1;
            while (nodePair.contains(rand)) {
                rand = (int)(Math.random() * N) + 1;
            }
            nodePair.add(rand);
        }
        return nodePair;
    }

    private List<Integer> generateNodePairWorst(int q) {
        return null;
    }

    private List<Integer> generateNodePairShaw(int q, int rand) {
        int N = inputParam.getN();
        Node[] nodes = inputParam.getNodes();
        List<Integer> nodePair = new LinkedList<>();
        nodePair.add(rand);
        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o ->  9 * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N]))
                            + 3 * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT()))
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq())
            ));
            for (int i = 1; i < N; i++) {
                if (!nodePair.contains(i)) pq.add(i);
            }
//            double temp = (int)(Math.random() * );
//            for (int i = 0; i < temp; i++) {
//                pq.poll();
//            }
            nodePair.add(pq.poll());
        }
        return nodePair;
    }

    private List<Integer> generateNodePairShaw(int q) {
        int N = inputParam.getN();
        Node[] nodes = inputParam.getNodes();
        List<Integer> nodePair = new LinkedList<>();
        int rand = (int)(Math.random() * N) + 1;
        nodePair.add(rand);
        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o ->  9 * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N]))
                            + 3 * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT()))
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq())
            ));
            for (int i = 1; i < N; i++) {
                if (!nodePair.contains(i)) pq.add(i);
            }
            double temp = (int)(Math.random() * 3);
            for (int i = 0; i < temp; i++) {
                pq.poll();
            }
            nodePair.add(pq.poll());
        }
        return nodePair;
    }

    /**
     * For debug use, generate nodePair with random seed
     */
    private List<Integer> generateNodePairRandom(int q, int seed) {
        Random generator = new Random(seed);
        int N = inputParam.getN();
        List<Integer> nodePair = new LinkedList<>();
        for (int k = 0; k < q; k++) {
            int rand = (int)(generator.nextDouble() * N) + 1;
            while (nodePair.contains(rand)) {
                rand = (int)(generator.nextDouble() * N) + 1;
            }
            nodePair.add(rand);
        }
        return nodePair;
    }

    /**
     * Construct initial feasible set of routes from input param, the algorithm is greedily add the node
     * with minimum objective increase
     * @return initial solution
     */
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

    /**
     * Construct one feasible route and remove used nodes from nodeNotProcessed
     * @param routeCount route index in solution
     * @param nodeNotProcessed Set of nodes to be selected from for construction
     * @return one new feasible route
     */
    private Route initialRouteConstruction(int routeCount, Set<Integer> nodeNotProcessed) {
        double[][] distanceMatrix = inputParam.getDistanceMatrix();
        Node[] nodes = inputParam.getNodes();
        Vehicle vehicle = inputParam.getVehicles()[routeCount];

        // initialize route with depots
        List<Node> routeNodes = new LinkedList<>();
        routeNodes.add(new Node(nodes[0]));
        routeNodes.add(new Node(nodes[nodes.length - 1]));

        double dist = distanceMatrix[0][nodes.length - 1];
        double penalty = 0;

        Route route = new Route(routeNodes, vehicle);

        while (true) {

            InsertPosition minInsertPosition = null;
            double minObjectiveIncrease = Double.MAX_VALUE;


            for (int nodeIndex : nodeNotProcessed) {
                for (int pIndex = 1; pIndex < routeNodes.size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < routeNodes.size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        double objectiveIncrease = inputParam.getAlpha() * pos.distIncrease
                                + inputParam.getBeta() * pos.penaltyIncrease;
                        if (objectiveIncrease < minObjectiveIncrease) {
                            minObjectiveIncrease = objectiveIncrease;
                            minInsertPosition = pos;
                        }
                    }
                }
            }
            if (minInsertPosition == null) break; // no feasible position to insert on this route
            nodeInsertion(minInsertPosition);
            nodeNotProcessed.remove(minInsertPosition.nodeIndex);
            dist += minInsertPosition.distIncrease;
            penalty += minInsertPosition.penaltyIncrease;
        }
        route.setDist(dist);
        route.setPenalty(penalty);
        return route;
    }

    /**
     * Feasibility check
     * @param route insert request in this route
     * @param nodeIndex node index in inputParam.Node[], request pair is (index, index + N)
     * @param pIndex pickup node insert in route at position pIndex
     * @param dIndex delivery node insert in route at position dIndex
     * @return dist increase and penalty increase
     */
    private InsertPosition checkNodePairInsertion(Route route, int nodeIndex, int pIndex, int dIndex) {

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
            double distIncrease = 0;
            if (pIndex == dIndex) {
                distIncrease = distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]
                        + distanceMatrix[pNode.getIndex()][dNode.getIndex()]
                        + distanceMatrix[dNode.getIndex()][nextDNode.getIndex()]
                        - distanceMatrix[prevPNode.getIndex()][nextDNode.getIndex()];
            } else {
                distIncrease = distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]
                        + distanceMatrix[pNode.getIndex()][nextPNode.getIndex()]
                        + distanceMatrix[prevDNode.getIndex()][dNode.getIndex()]
                        + distanceMatrix[dNode.getIndex()][nextDNode.getIndex()]
                        - distanceMatrix[prevPNode.getIndex()][nextPNode.getIndex()]
                        - distanceMatrix[prevDNode.getIndex()][nextDNode.getIndex()];
            }

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
                prevNode = currNode;
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
                prevNode = currNode;
            }

            double penaltyIncrease = currPenalty - originalPenalty;
            return new InsertPosition(pIndex, dIndex, nodeIndex, route, distIncrease, penaltyIncrease);
    }

    /**
     * Insert node[nodeIndex] at pIndex, node[nodeIndex + N] at dIndex
     * Update load, time after insertion position
     * @param pos contains: index for node pair, pickup node insert in route at position pIndex,
     *            delivery node insert in route at position dIndex
     */
    private void nodeInsertion(InsertPosition pos) {
        Route route = pos.route;
        int nodeIndex = pos.nodeIndex;
        int pIndex = pos.pIndex;
        int dIndex = pos.dIndex;

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
    private void destroy(Solution sol, List<Integer> nodePair) {
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
        double totalDist = 0;
        double totalPenalty = 0;
        for (Route route : solution.getRoutes()) {
            updateRoute(route);
            totalDist += route.getDist();
            totalPenalty += route.getPenalty();
        }
        solution.setTotalDist(totalDist);
        solution.setTotalPenalty(totalPenalty);
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
     * Find the best insertion place for best and regret construct operator
     * @param solution solution to be inserted
     * @param nodePair nodePair(request) to be inserted
     * @param size 1: best 2: regret
     * @return best insertion position as InsertPosition object
     */
    private InsertPosition findInsertPosition(Solution solution, List<Integer> nodePair, int size) {
        PriorityQueue<Pair<InsertPosition, Double>> nodePQ = new PriorityQueue<>(
                Comparator.comparingDouble(Pair::getValue));

        for (int nodeIndex : nodePair) {
            PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> -inputParam.getAlpha() * o.distIncrease - inputParam.getBeta() * o.penaltyIncrease));
            for (Route route : solution.getRoutes()) {
                for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        pq.add(pos);
                        if (pq.size() > size) pq.poll();
                    }
                }
            }
            if (pq.size() >= size) {
                if (size == 1) {
                    InsertPosition bestPos = pq.poll();
                    nodePQ.add(new Pair<>(bestPos, inputParam.getAlpha() * bestPos.distIncrease
                            + inputParam.getBeta() * bestPos.penaltyIncrease));
                } else if (size == 2) {
                    InsertPosition secondBestPos = pq.poll();
                    InsertPosition bestPos = pq.poll();
                    nodePQ.add(new Pair<>(bestPos, inputParam.getAlpha() * (bestPos.distIncrease - secondBestPos.distIncrease)
                            + inputParam.getBeta() * (bestPos.penaltyIncrease - secondBestPos.penaltyIncrease)));
                }
            }
        }


        return nodePQ.poll().getKey();
    }

    /**
     * Construct Operator: insert at position with minimum objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    private void bestConstruct(Solution solution, List<Integer> nodePair) {
        while (true) {
            InsertPosition pos = findInsertPosition(solution, nodePair, 1);
            if (pos == null) {
                throw new NullPointerException("No feasible insertion");
            }
            nodeInsertion(pos);
            pos.route.setDist(pos.route.getDist() + pos.distIncrease);
            pos.route.setPenalty(pos.route.getPenalty() + pos.penaltyIncrease);
            solution.setTotalDist(solution.getTotalDist() + pos.distIncrease);
            solution.setTotalPenalty(solution.getTotalPenalty() + pos.penaltyIncrease);

            nodePair.remove((Integer) pos.nodeIndex);
            if (nodePair.size() == 0) break;
        }
    }

    /**
     * Construct Operator: insert at position with minimum difference in best and
     * second best objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    private void regretConstruct(Solution solution, List<Integer> nodePair) {
        while (true) {
            InsertPosition pos = findInsertPosition(solution, nodePair, 2);
            if (pos == null) {
                throw new NullPointerException("No feasible insertion");
            }
            nodeInsertion(pos);
            pos.route.setDist(pos.route.getDist() + pos.distIncrease);
            pos.route.setPenalty(pos.route.getPenalty() + pos.penaltyIncrease);
            solution.setTotalDist(solution.getTotalDist() + pos.distIncrease);
            solution.setTotalPenalty(solution.getTotalPenalty() + pos.penaltyIncrease);

            nodePair.remove((Integer) pos.nodeIndex);
            if (nodePair.size() == 0) break;
        }
    }

    /**
     * For debug use, recalculate and compare the information along the route
     * @param sol Solution to be verified
     */
    private void validation(Solution sol) {
        List<Route> routes = sol.getRoutes();
        double totalDist = 0;
        double totalPenalty = 0;
        for (Route route : routes) {
            double dist = 0;
            double penalty = 0;
            double time = 0;
            List<Node> routeNodes = route.getNodes();
            for (int i = 1; i < routeNodes.size(); i++) {
                Node prevNode = routeNodes.get(i - 1);
                Node node = routeNodes.get(i);
                time = Math.max(node.getTw1(), prevNode.gets() + time + Utils.calculateDistance(prevNode, node));
                if (!Utils.doubleEqual(time, node.getT())) throw new IllegalArgumentException("Wrong with time.");
                double tempPenalty = Math.max(0, time - node.getTw2());
                if (!Utils.doubleEqual(tempPenalty, node.getDL())) throw new IllegalArgumentException("Wrong penalty");
                penalty += tempPenalty;
                dist += Utils.calculateDistance(prevNode, node);
            }
            if (!Utils.doubleEqual(dist, route.getDist())) throw new IllegalArgumentException("Wrong route distance");
//            System.out.println(penalty + " " + route.getPenalty());
            if (!Utils.doubleEqual(penalty, route.getPenalty())) throw new IllegalArgumentException("Wrong route penalty");
            totalDist += dist;
            totalPenalty += penalty;
        }
        if (!Utils.doubleEqual(totalDist, sol.getTotalDist())) throw new IllegalArgumentException("Wrong solution distance");
        if (!Utils.doubleEqual(totalPenalty, sol.getTotalPenalty())) throw new IllegalArgumentException("Wrong solution penalty");
    }

    class InsertPosition {
        int pIndex;
        int dIndex;
        int nodeIndex;
        Route route;
        double distIncrease;
        double penaltyIncrease;

        InsertPosition(int pIndex, int dIndex, int nodeIndex, Route route, double distIncrease, double penaltyIncrease) {
            this.pIndex = pIndex;
            this.dIndex = dIndex;
            this.nodeIndex = nodeIndex;
            this.route = route;
            this.distIncrease = distIncrease;
            this.penaltyIncrease = penaltyIncrease;
        }
    }
}
