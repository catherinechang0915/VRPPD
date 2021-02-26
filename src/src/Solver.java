package src;

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
    public Solution init(InputParam inputParam) {
        List<Route> routes = new LinkedList<>();
        Vehicle[] vehicles = inputParam.getVehicles();
        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
            routes.add(new Route(new LinkedList<>(), vehicles[routeCount]));
        }
        Solution solution = new Solution(routes, 0, 0);

        Constructor initialConstructor = new InitialConstructor();
        // store the unprocessed request pairs
        List<Integer> nodePairNotProcessed = new LinkedList<>();
        for (int i = 1; i <= inputParam.getN(); i++) {
            nodePairNotProcessed.add(i);
        }
        initialConstructor.construct(inputParam, solution, nodePairNotProcessed);
        return solution;
    }

    public Solution getSolverSolution() {
        return solution;
    }

    public double getSolverObjective(double alpha, double beta) {
        return solution.getObjective(alpha, beta);
    }
}
