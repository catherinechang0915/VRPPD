package src.Operator;

import src.DataStructures.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class InitialConstructor extends Constructor {

    /**
     * Construct initial feasible set of routes from input param, the algorithm is greedily add the node
     * with minimum objective increase
     */
    @Override
    public void construct(InputParam inputParam, Solution solution, List<Integer> nodePair) {
        List<Route> routes = solution.getRoutes();
        double totalDistance = 0;
        double totalDelay = 0;
        int N = inputParam.getN();

        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
            Route route = routes.get(routeCount);
            initialRouteConstruction(inputParam, route, nodePair);
            totalDistance += route.getDist();
            totalDelay += route.getPenalty();
        }

        solution.setTotalDist(totalDistance);
        solution.setTotalPenalty(totalDelay);
    }

    /**
     * Construct one feasible route and remove used nodes from nodeNotProcessed
     * @param route route to be initialized
     * @param nodePair Set of remaining nodes to be selected from for construction
     */
    private void initialRouteConstruction(InputParam inputParam, Route route, List<Integer> nodePair) {
        double[][] distanceMatrix = inputParam.getDistanceMatrix();
        Node[] nodes = inputParam.getNodes();

        // initialize route with depots
        List<Node> routeNodes = route.getNodes();
        routeNodes.add(new Node(nodes[0]));
        routeNodes.add(new Node(nodes[nodes.length - 1]));

        double dist = distanceMatrix[0][nodes.length - 1];
        double penalty = 0;

        while (true) {

            InsertPosition minInsertPosition = null;
            double minObjectiveIncrease = Double.MAX_VALUE;

            for (int nodeIndex : nodePair) {
                for (int pIndex = 1; pIndex < routeNodes.size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < routeNodes.size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(inputParam, route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        double objectiveIncrease = inputParam.getAlpha() * pos.getDistIncrease()
                                + inputParam.getBeta() * pos.getPenaltyIncrease();
                        if (objectiveIncrease < minObjectiveIncrease) {
                            minObjectiveIncrease = objectiveIncrease;
                            minInsertPosition = pos;
                        }
                    }
                }
            }
            if (minInsertPosition == null) break; // no feasible position to insert on this route
            nodeInsertion(inputParam, minInsertPosition);
            nodePair.remove((Integer) minInsertPosition.getNodeIndex());
            dist += minInsertPosition.getDistIncrease();
            penalty += minInsertPosition.getPenaltyIncrease();
        }
        route.setDist(dist);
        route.setPenalty(penalty);
    }
}
