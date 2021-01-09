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
        double alpha = inputParam.getAlpha(), beta = inputParam.getBeta();
        int MAX_ITER = 10000;
        int q = (int) (0.1 * inputParam.getN());

        long startTime = System.currentTimeMillis();
        Solution sol = initialSolutionConstruction();
        Solution bestSol = sol;

        double T = 0.025;
        double coolingRate = T / MAX_ITER;

        List<Integer> nodePair = null;
        List<InsertPosition> positions = null;
        Pair<List<Integer>, List<InsertPosition>> temp = null;
        for (int i = 0; i < MAX_ITER; i++) {
            double prevObjective = sol.getObjective(alpha, beta);
            // Destroy
            switch (destroyOperator) {
                case "random":
                    nodePair = generateNodePairRandom(q);
                    positions = destroy(sol, nodePair, true);
                    break;
                case "worst":
                    // For this operator, request is removed one by one
                    temp = worstDestroy(q, sol);
                    nodePair = temp.getKey();
                    positions = temp.getValue();
                    break;
                case "shaw":
                    nodePair = generateNodePairShaw(q);
                    positions = destroy(sol, nodePair, true);
                    break;
            }
            validation(sol);

            // Construct
            switch (constructOperator) {
                case "best":
                    regretConstruct(sol, nodePair, 1);
                    break;
                case "regret2":
                    regretConstruct(sol, nodePair, 2);
                    break;
                case "regret3":
                    regretConstruct(sol, nodePair, 3);
                    break;
                case "regret4":
                    regretConstruct(sol, nodePair, 4);
                    break;
                case "regretM":
                    regretConstruct(sol, nodePair, -1);
            }
            validation(sol);

            if (sol.getObjective(alpha, beta) < bestSol.getObjective(alpha, beta)) {
                bestSol = sol;
            }
//            if (sol.getObjective(alpha, beta) >= bestSol.getObjective(alpha, beta)
//                    && T < (sol.getObjective(alpha, beta) - prevObjective) / prevObjective) {
            if (T < (sol.getObjective(alpha, beta) - bestSol.getObjective(alpha, beta)) / bestSol.getObjective(alpha, beta)) {
                // not accept if worse than global and not meeting criteria, rollback
                destroy(sol, nodePair, false);
                recover(sol, nodePair, positions);
                System.out.println("not accepted");
            }
            T -= coolingRate;
        }
        sol.setTimeElapsed(System.currentTimeMillis() - startTime);
        return bestSol;
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

    /**
     * Remove requests that would result in maximum objective decrease, and update the solution
     * @param q number of requests to be removed each iteration
     * @param solution solution contains requests to be removed
     * @return removed request set and corresponding positions
     */
    private Pair<List<Integer>, List<InsertPosition>> worstDestroy(int q, Solution solution) {
        List<Integer> nodePair = new LinkedList<>();
        List<InsertPosition> positions = new LinkedList<>();
        while (nodePair.size() < q) {
            InsertPosition pos = findDestroyPosition(solution, 5);
            if (pos == null) {
                throw new NullPointerException("Wrong code. Should always have feasible "
                        + "removal position in worst destroy operator");
            }
            positions.add(0, pos);
            nodeRemove(pos);
            pos.route.setDist(pos.route.getDist() + pos.distIncrease);
            pos.route.setPenalty(pos.route.getPenalty() + pos.penaltyIncrease);
            solution.setTotalDist(solution.getTotalDist() + pos.distIncrease);
            solution.setTotalPenalty(solution.getTotalPenalty() + pos.penaltyIncrease);

            nodePair.add(pos.nodeIndex);
        }
        return new Pair<>(nodePair, positions);
    }

    /**
     * For debug use
     */
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
            nodePair.add(pq.poll());
        }
        return nodePair;
    }

    /**
     * Choose requests to be removed based on their relativity, one request is randomly selected from nodePair
     * each iteration, and found the closely related 3 requests, randomly add one to the request set
     * @param q number of requests to be removed each iteration
     * @return request set
     */
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
     * Calculate objective decrease
     * @param route remove request in this route
     * @param pIndex pickup node to be removed
     * @return dist increase and penalty increase
     */
    private InsertPosition checkNodePairDestroy(Route route, int pIndex) {
        List<Node> routeNodes = route.getNodes();
        int nodeIndex = routeNodes.get(pIndex).getIndex();
        if (nodeIndex > inputParam.getN()) return null;

        double[][] distanceMatrix = inputParam.getDistanceMatrix();
        Node[] nodes = inputParam.getNodes();

        int dIndex = -1;
        for (int i = pIndex; i < routeNodes.size(); i++) {
            if (routeNodes.get(i).getIndex() == nodeIndex + inputParam.getN()) {
                dIndex = i;
                break;
            }
        }

        Node pNode = nodes[nodeIndex];
        Node dNode = nodes[nodeIndex + inputParam.getN()];

        double distIncrease = 0;

        Node prevPNode = routeNodes.get(pIndex - 1);
        Node nextDNode = routeNodes.get(dIndex + 1);
        Node nextPNode = null;
        Node prevDNode = null;

        // case 1: p-d pair not consecutive
        // Got prev and next nodes for pickup and delivery nodes
        if (dIndex != pIndex + 1) {
            nextPNode = routeNodes.get(pIndex + 1);
            prevDNode = routeNodes.get(dIndex - 1);
            distIncrease = - distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]
                    - distanceMatrix[pNode.getIndex()][nextPNode.getIndex()]
                    - distanceMatrix[prevDNode.getIndex()][dNode.getIndex()]
                    - distanceMatrix[dNode.getIndex()][nextDNode.getIndex()]
                    + distanceMatrix[prevPNode.getIndex()][nextPNode.getIndex()]
                    + distanceMatrix[prevDNode.getIndex()][nextDNode.getIndex()];
        } else {
        // case 2: p-d pair consecutive
            nextPNode = dNode;
            prevDNode = pNode;
            distIncrease = - distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]
                    - distanceMatrix[pNode.getIndex()][dNode.getIndex()]
                    - distanceMatrix[dNode.getIndex()][nextDNode.getIndex()]
                    + distanceMatrix[prevPNode.getIndex()][nextDNode.getIndex()];
        }

        Node prevNode = null;
        Node currNode = null;

        // initialization to be the value before the node to be inserted
        double load = prevPNode.getQ();
        double time = prevPNode.getT();

        // calculate along iterating the nodes after, before insertion
        double originalPenalty = 0;
        double currPenalty = 0;

        // check the current removed pickup node
        load += pNode.getq();
        if (load < 0 || load > route.getVehicle().getCapacity()) return null;
        time = Math.max(pNode.getTw1(), prevPNode.gets() + time
                + distanceMatrix[prevPNode.getIndex()][pNode.getIndex()]);
        if (time > pNode.getTw2() && pNode.getMembership() == 1) return null;
        currPenalty += Math.max(time - pNode.getTw2(), 0);

        // check the following nodes
        prevNode = pNode;
        for (int currIndex = pIndex; currIndex < routeNodes.size(); currIndex++) {

            currNode = routeNodes.get(currIndex);
            // calculate original delay first
            originalPenalty += currNode.getDL();
            if (currIndex != pIndex && currIndex != dIndex) {
                time = Math.max(currNode.getTw1(), prevNode.gets() + time
                        + distanceMatrix[prevNode.getIndex()][currNode.getIndex()]);
                currPenalty += Math.max(time - currNode.getTw2(), 0);
                prevNode = currNode;
            }
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
     * Remove requests pair (pos.nodeIndex, pos.nodeIndex + N) and update the following node information
     * @param pos contains route, index information about node to be removed
     */
    private void nodeRemove(InsertPosition pos) {
        Route route = pos.route;
        int pIndex = pos.pIndex;
        int dIndex = pos.dIndex;

        List<Node> routeNodes = route.getNodes();
        double[][] distanceMatrix = inputParam.getDistanceMatrix();

        Node prevNode = routeNodes.get(pIndex - 1);

        double load = prevNode.getQ();
        double time = prevNode.getT();

        routeNodes.remove(dIndex);
        routeNodes.remove(pIndex);

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
     * @return a copy of the original solution
     */
    private List<InsertPosition> destroy(Solution sol, List<Integer> nodePair, boolean update) {
        Node[] nodes = inputParam.getNodes();
        List<Route> routes = sol.getRoutes();
        List<InsertPosition> positions = new LinkedList<>();
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            List<Node> routeNodes = route.getNodes();

            for (Integer nodeIndex : nodePair) {
                Node pNode = nodes[nodeIndex];
                Node dNode = nodes[nodeIndex + inputParam.getN()];
                if (routeNodes.contains(pNode)) {
                    positions.add(0, new InsertPosition(routeNodes.indexOf(pNode), routeNodes.indexOf(dNode),
                            nodeIndex, route, -1, -1));
                    routeNodes.remove(pNode);
                    routeNodes.remove(dNode);
                }
            }
        }
        if (update) updateSolution(sol);
        return positions;
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
                (o1, o2) -> {
                    if (!o1.getValue().equals(o2.getValue())) {
                        return - Double.compare(o1.getValue(), o2.getValue());
                    } else {
                        // tie breaking rule
                        return Double.compare(o1.getKey().distIncrease * inputParam.getAlpha()
                                + o1.getKey().penaltyIncrease * inputParam.getBeta(),
                                o2.getKey().distIncrease * inputParam.getAlpha()
                                + o2.getKey().penaltyIncrease * inputParam.getBeta());
                    }
                });

        int pqSize = Integer.MAX_VALUE;
        InsertPosition posWithLeastPossibleRoute = null;

        for (int nodeIndex : nodePair) {
            PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> inputParam.getAlpha() * o.distIncrease + inputParam.getBeta() * o.penaltyIncrease));
            for (Route route : solution.getRoutes()) {
                for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        pq.add(pos);
//                        if (pq.size() > size) pq.poll();
                    }
                }
            }
            int regretSize = size == -1 ? pq.size() : size;
            if (pq.size() >= regretSize) {
                InsertPosition bestPos = pq.peek();
                double bestRegretValue = bestPos.distIncrease * inputParam.getAlpha()
                        + bestPos.penaltyIncrease * inputParam.getBeta();
                double regretValue = 0;
                for (int i = 0; i < regretSize; i++) {
                    InsertPosition temp = pq.poll();
                    regretValue += temp.distIncrease * inputParam.getAlpha()
                            + temp.penaltyIncrease * inputParam.getBeta()
                            - bestRegretValue;
                }
                regretValue += (solution.getRoutes().size() - regretSize) * 1000;
                nodePQ.add(new Pair<>(bestPos, regretValue));
            } else if (pq.size() != 0) {
                if (pq.size() < pqSize) {
                    pqSize = pq.size();
                    posWithLeastPossibleRoute = pq.poll();
                }
            }
        }
        return posWithLeastPossibleRoute == null ? nodePQ.poll().getKey() : posWithLeastPossibleRoute;
    }

    /**
     * Find the best destroy place (maximum reduce for objective) for worst destroy construct operator
     * @param solution solution to be inserted
     * @param size determine the randomness
     * @return random choice among best size destroy position as InsertPosition object
     */
    private InsertPosition findDestroyPosition(Solution solution, int size) {
        PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                o -> -inputParam.getAlpha() * o.distIncrease - inputParam.getBeta() * o.penaltyIncrease));
        for (Route route : solution.getRoutes()) {
            for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                InsertPosition pos = checkNodePairDestroy(route, pIndex);
                if (pos == null) continue; // infeasible
                pq.add(pos);
                if (pq.size() > size) pq.poll();
            }
        }
        int rand = (int)(Math.random() * pq.size());
        for (int i = 0; i < rand; i++) {
            pq.poll();
        }
        return pq.poll();
    }

    /**
     * Construct Operator: insert at position with minimum difference in best and
     * second best objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    private void regretConstruct(Solution solution, List<Integer> nodePair, int size) {
        while (true) {
            InsertPosition pos = findInsertPosition(solution, nodePair, size);
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
     * Reinsert the removed requests back to their original position, the order of InsertionPosition and
     * the order to insert pNode and dNode are very important
     * @param solution solution with requests removed
     * @param nodePair requests to be inserted back
     * @param positions original request positions in the route
     */
    private void recover(Solution solution, List<Integer> nodePair, List<InsertPosition> positions) {
        Node[] nodes = inputParam.getNodes();
        int N = inputParam.getN();
        for (InsertPosition pos : positions) {
            List<Node> routeNodes = pos.route.getNodes();
            int pIndex = pos.pIndex;
            int dIndex = pos.dIndex;
            int nodeIndex = pos.nodeIndex;
            routeNodes.add(pIndex, nodes[nodeIndex]);
            routeNodes.add(dIndex, nodes[nodeIndex + N]);
        }
        updateSolution(solution);
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
