package src;

import src.DataStructures.*;
import src.Operator.*;

import java.util.*;

public class MySolver extends Solver{

    private Destructor destructor;
    private Constructor constructor;

    public MySolver(int destructorIndex, int constructorIndex, int noise) {
        double percentLo = 0.2;
        double percentHi = 0.4;
        switch (destructorIndex) {
            case 0:
                this.destructor = new RandomDestructor(percentLo, percentHi);
                break;
            case 1:
                this.destructor = new ShawDestructor(percentLo, percentHi);
                break;
            case 2:
                this.destructor = new WorstDestructor(percentLo, percentHi);
                break;
            case 3:
                this.destructor = new WorstDelayDestructor(percentLo, percentHi);
                break;
        }
        switch (constructorIndex) {
            case 0:
                this.constructor = new RegretConstructor(-1, noise);
                break;
            case 1:
                this.constructor = new RegretConstructor(1, noise);
                break;
            case 2:
                this.constructor = new RegretConstructor(2, noise);
                break;
            case 3:
                this.constructor = new RegretConstructor(3, noise);
                break;
            case 4:
                this.constructor = new RegretConstructor(4, noise);
                break;
        }
    }

    @Override
    public void solve(String dataFilePath, String resFilePath) {

        InputParam inputParam = Utils.readParam(dataFilePath);
        double alpha = inputParam.getAlpha(), beta = inputParam.getBeta();
        int MAX_ITER = 150;

        long startTime = System.currentTimeMillis();
        Solution sol = init(inputParam);

        byte[] bestSol = Utils.serialize(sol);
        double bestObj = sol.getObjective(alpha, beta);

        double T = 0.025;
        double coolingRate = T / MAX_ITER;

        List<Integer> nodePair = null;

        byte[] prevSol = null;
        for (int i = 0; i < MAX_ITER; i++) {
            prevSol = Utils.serialize(sol);

            // Destroy
            nodePair = destructor.destroy(inputParam, sol);

            // Construct
            constructor.construct(inputParam, sol, nodePair);
            // validation(sol);
            double currObj = sol.getObjective(alpha, beta);

            if (currObj < bestObj) {
                bestObj = sol.getObjective(alpha, beta);
                bestSol = Utils.serialize(sol);
            }

            if (T < (currObj - bestObj) / bestObj) {
                // not accept if worse than global and not meeting criteria, rollback
                sol = Utils.deserialize(prevSol);
            }
            T -= coolingRate;
        }
        Solution bestToReturn = Utils.deserialize(bestSol);
        bestToReturn.setTimeElapsed(System.currentTimeMillis() - startTime);
        solution = bestToReturn;
        writeToFile(resFilePath);
    }

    @Override
    public Solution getSolverSolution() {
        return solution;
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

    @Override
    public String toString() {
        return destructor + "_" + constructor;
    }

}
