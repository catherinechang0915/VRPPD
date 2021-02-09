package src;

import src.DataStructures.*;
import src.Operator.*;

import java.util.*;

public class MySolver implements Solver{

    private InputParam inputParam;
    private Solution solution;
    private Destructor destructor;
    private Constructor constructor;

    public MySolver(String dataFilePath, int destructorIndex, int constructorIndex) {
        this.inputParam = Utils.readParam(dataFilePath);
        double percent = 0.15;
        switch (destructorIndex) {
            case 0:
                this.destructor = new RandomDestructor(inputParam, percent);
                break;
            case 1:
                this.destructor = new WorstDestructor(inputParam, percent);
                break;
            case 2:
                this.destructor = new ShawDestructor(inputParam, percent);
                break;
        }
        switch (constructorIndex) {
            case 0:
                this.constructor = new RegretConstructor(inputParam, -1);
                break;
            case 1:
                this.constructor = new RegretConstructor(inputParam, 1);
                break;
            case 2:
                this.constructor = new RegretConstructor(inputParam, 2);
                break;
            case 3:
                this.constructor = new RegretConstructor(inputParam, 3);
                break;
            case 4:
                this.constructor = new RegretConstructor(inputParam, 4);
                break;
        }
    }

    @Override
    public void solve(String resFilePath) {

        double alpha = inputParam.getAlpha(), beta = inputParam.getBeta();
        int MAX_ITER = 10000;

        long startTime = System.currentTimeMillis();
        Solution sol = init();

        byte[] bestSol = Utils.serialize(sol);
        double bestObj = sol.getObjective(alpha, beta);

        double T = 0.025;
        double coolingRate = T / MAX_ITER;

        List<Integer> nodePair = null;

        byte[] prevSol = null;
        for (int i = 0; i < MAX_ITER; i++) {
            prevSol = Utils.serialize(sol);

            // Destroy
            destructor.destroy(sol);
            nodePair = destructor.getNodePair();

            // Construct
            constructor.construct(sol, nodePair);
//            validation(sol);

            if (sol.getObjective(alpha, beta) < bestObj) {
                bestObj = sol.getObjective(alpha, beta);
                bestSol = Utils.serialize(sol);
            }

            if (T < (sol.getObjective(alpha, beta) - bestObj) / bestObj) {
                // not accept if worse than global and not meeting criteria, rollback
                sol = Utils.deserialize(prevSol);
//                System.out.println("not accepted");
            }
            T -= coolingRate;
        }
        Solution bestToReturn = Utils.deserialize(bestSol);
        bestToReturn.setTimeElapsed(System.currentTimeMillis() - startTime);
        solution = bestToReturn;
        writeToFile(resFilePath);
    }

    /**
     * Initialize the solution by calling InitialConstructor using greedy insertion
     * @return initial solution
     */
    private Solution init() {
        List<Route> routes = new LinkedList<>();
        Vehicle[] vehicles = inputParam.getVehicles();
        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
            routes.add(new Route(new LinkedList<>(), vehicles[routeCount]));
        }
        Solution solution = new Solution(routes, 0, 0);

        Constructor initialConstructor = new InitialConstructor(inputParam);
        // store the unprocessed request pairs
        List<Integer> nodePairNotProcessed = new LinkedList<>();
        for (int i = 1; i <= inputParam.getN(); i++) {
            nodePairNotProcessed.add(i);
        }
        initialConstructor.construct(solution, nodePairNotProcessed);
        return solution;
    }

    @Override
    public Solution getSolverSolution() {
        return solution;
    }

    @Override
    public double getSolverObjective() {
        return solution.getObjective(inputParam.getAlpha(), inputParam.getBeta());
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
            double load = 0;
            List<Node> routeNodes = route.getNodes();
            for (int i = 1; i < routeNodes.size(); i++) {
                Node prevNode = routeNodes.get(i - 1);
                Node node = routeNodes.get(i);
                load += node.getq();
                if (!Utils.doubleEqual(node.getQ(), load)) throw new IllegalArgumentException("Wrong with load.");
                if (load < 0 || load > route.getVehicle().getCapacity()) throw new IllegalArgumentException("Load Violation.");
                time = Math.max(node.getTw1(), prevNode.gets() + time + Utils.calculateDistance(prevNode, node));
                if (node.getMembership() == 1 && time > node.getTw2()) throw new IllegalArgumentException("Time window violation.");
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

    private void writeToFile(String filePath) {
        solution.writeToFile(filePath, false);
    }

}
