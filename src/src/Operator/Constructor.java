package src.Operator;

import src.DataStructures.*;

import java.util.List;

public abstract class Constructor {

    public Constructor() {
    }

    public abstract void construct(InputParam inputParam, Solution solution, List<Integer> nodePar);

    /**
     * Insert node[nodeIndex] at pIndex, node[nodeIndex + N] at dIndex
     * Update load, time after insertion position
     * @param pos contains: index for node pair, pickup node insert in route at position pIndex,
     *            delivery node insert in route at position dIndex
     */
    protected void nodeInsertion(InputParam inputParam, InsertPosition pos) {
        Route route = pos.getRoute();
        int nodeIndex = pos.getNodeIndex();
        int pIndex = pos.getpIndex();
        int dIndex = pos.getdIndex();

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
     * Feasibility check
     * @param route insert request in this route
     * @param nodeIndex node index in inputParam.Node[], request pair is (index, index + N)
     * @param pIndex pickup node insert in route at position pIndex
     * @param dIndex delivery node insert in route at position dIndex
     * @return dist increase and penalty increase
     */
    protected InsertPosition checkNodePairInsertion(InputParam inputParam, Route route, int nodeIndex, int pIndex, int dIndex) {

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

}
