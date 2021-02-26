package src.Operator;

import src.DataStructures.InputParam;
import src.DataStructures.Node;
import src.DataStructures.Route;
import src.DataStructures.Solution;
import src.Utils;

import java.util.LinkedList;
import java.util.List;

public abstract class Destructor {

    protected int q;
    protected double percentLo;
    protected double percentHi;
    protected List<Integer> nodePair;

    public Destructor(double percentLo, double percentHi) {
        this.percentLo = percentLo;
        this.percentHi = percentHi;
        this.q = -1;
    }

    public List<Integer> destroy(InputParam inputParam, Solution solution) {
        setRandomQ(inputParam.getN());
        destroyNodePair(inputParam, solution);
        return nodePair;
    }

    protected abstract void destroyNodePair(InputParam inputParam, Solution solution);

    /**
     * Destroy Operator: remove nodes in nodePair
     * @param sol solution to be operated on
     * @param nodePair request set to be removed
     */
    protected void destroy(int N, Solution sol, List<Integer> nodePair) {
        List<Node> routeNodes = null;
        for (Route route : sol.getRoutes()) {
            routeNodes = route.getNodes();
            for (Node node : new LinkedList<>(routeNodes)) {
                if (nodePair.contains(node.getIndex()) || nodePair.contains(node.getIndex() - N)) {
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
     * Introduce y^p for randomness
     * @return index to remove in request set
     */
    protected int getRandomPos(int p, int remainSize) {
        double y = Math.random();
        return (int)(remainSize * Math.pow(y, p));
    }

    /**
     * Random select q (number of requests to remove) between lower and upper threshold
     */
    public void setRandomQ(int N) {
        double percent = Math.random() * (this.percentHi - this.percentLo) + this.percentLo;
        this.q = (int) (N * percent);
    }
}
