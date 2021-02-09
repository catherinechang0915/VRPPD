package src.Operator;

import src.DataStructures.*;
import src.MySolver;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class WorstDestructor extends Destructor {

    // TODO: may modify later
    private final int randomRange = 3;

    public WorstDestructor(InputParam inputParam, double percent) {
        super(inputParam, percent);
    }

    /**
     * Remove requests that would result in maximum objective decrease, and update the solution
     * @param solution solution contains requests to be removed
     */
    @Override
    public void destroy(Solution solution) {
        nodePair = new LinkedList<>();
        while (nodePair.size() < q) {
            InsertPosition pos = findDestroyPosition(solution, randomRange);
            if (pos == null) {
                throw new NullPointerException("Wrong code. Should always have feasible "
                        + "removal position in worst destroy operator");
            }
            nodeRemove(pos);
            solution.setTotalDist(solution.getTotalDist() + pos.getDistIncrease());
            solution.setTotalPenalty(solution.getTotalPenalty() + pos.getPenaltyIncrease());

            nodePair.add(pos.getNodeIndex());
        }
    }

    /**
     * Find the best destroy place (maximum reduce for objective) for worst destroy construct operator
     * @param solution solution to be inserted
     * @param size determine the randomness
     * @return random choice among best size destroy position as InsertPosition object
     */
    private InsertPosition findDestroyPosition(Solution solution, int size) {
        PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                o -> - inputParam.getAlpha() * o.getDistIncrease() - inputParam.getBeta() * o.getPenaltyIncrease()));
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

        // check the following nodes
        prevNode = prevPNode;
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
     * Remove requests pair (pos.nodeIndex, pos.nodeIndex + N) and update the following node information
     * @param pos contains route, index information about node to be removed
     */
    private void nodeRemove(InsertPosition pos) {
        Route route = pos.getRoute();
        int pIndex = pos.getpIndex();
        int dIndex = pos.getdIndex();

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
        route.setDist(route.getDist() + pos.getDistIncrease());
        route.setPenalty(route.getPenalty() + pos.getPenaltyIncrease());
    }

    @Override
    public String toString() {
        return "Worst Destructor";
    }
}
