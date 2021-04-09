package src;

import javafx.util.Pair;
import src.DataStructures.*;
import java.util.*;
import src.Operator.*;

public abstract class Solver {

    protected Solution solution = null;

    public abstract void solve(String dataFilePath, String resFilePath);

    /**
     * Initialize the solution by calling InitialConstructor using greedy insertion
     * @return initial solution
     */
    public Pair<Solution, List<Integer>> init(InputParam inputParam) {
        Node[] nodes = inputParam.getNodes();
        List<Route> routes = new LinkedList<>();
        Vehicle[] vehicles = inputParam.getVehicles();
        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
            List<Node> routeNodes = new LinkedList<>();
            routeNodes.add(new Node(nodes[0]));
            routeNodes.add(new Node(nodes[2 * inputParam.getN() + 1]));
            routes.add(new Route(routeNodes, vehicles[routeCount]));
        }
        Solution solution = new Solution(routes, 0, 0);

        Constructor initialConstructor = new RegretConstructor(1, 0);
        // store the unprocessed request pairs
        List<Integer> nodePairNotProcessed = new LinkedList<>();
        for (int i = 1; i <= inputParam.getN(); i++) {
            nodePairNotProcessed.add(i);
        }
        initialConstructor.construct(inputParam, solution, nodePairNotProcessed);
        return new Pair<>(solution, nodePairNotProcessed);
    }

    public Solution getSolverSolution() {
        return solution;
    }

    public double getSolverObjective(double alpha, double beta) {
        return solution.getObjective(alpha, beta);
    }
}
