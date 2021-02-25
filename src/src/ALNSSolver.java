package src;

import src.DataStructures.*;
import src.Operator.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ALNSSolver implements Solver {

    private InputParam inputParam;
    private double r = 0.1;
    private int sigma1 = 33;
    private int sigma2 = 9;
    private int sigma3 = 13;
    private Solution solution;
    private double[] destructorWeights;
    private double[] constructorWeights;
    private int[] destructorScores;
    private int[] constructorScores;
    private int[] destructorCounts;
    private int[] constructorCounts;
    private Destructor[] destructors;
    private Constructor[] constructors;

    public ALNSSolver(String dataFilePath) {
        this.inputParam = Utils.readParam(dataFilePath);
        double percent = 0.15;
        int destructorsNum = 4;
        int constructorsNum = 5;
        destructorWeights = new double[destructorsNum];
        constructorWeights = new double[constructorsNum];
        Arrays.fill(destructorWeights, 1.0 / destructorsNum);
        Arrays.fill(constructorWeights, 1.0 / constructorsNum);
        destructorScores = new int[destructorsNum];
        constructorScores = new int[constructorsNum];
        destructorCounts = new int[destructorsNum];
        constructorCounts = new int[constructorsNum];
        destructors = new Destructor[] {
                new RandomDestructor(inputParam, percent),
                new WorstDestructor(inputParam, percent),
                new ShawDestructor(inputParam, percent),
                new WorstDelayDestructor(inputParam, percent)
        };
        constructors = new Constructor[] {
                new RegretConstructor(inputParam, -1),
                new RegretConstructor(inputParam, 1),
                new RegretConstructor(inputParam, 2),
                new RegretConstructor(inputParam, 3),
                new RegretConstructor(inputParam, 4)
        };
    }
    @Override
    public void solve(String resFilePath) {
        double alpha = inputParam.getAlpha(), beta = inputParam.getBeta();
        int MAX_ITER = 10000;
        int segments = 100;

        long startTime = System.currentTimeMillis();
        Solution sol = init();

        HashSet<Integer> acceptedSolution = new HashSet<>();
        acceptedSolution.add(sol.hashCode());

        byte[] bestSol = Utils.serialize(sol);
        double bestObj = sol.getObjective(alpha, beta);

        double T = 0.025;
        double coolingRate = T / MAX_ITER;

        List<Integer> nodePair = null;
        Destructor destructor = null;
        Constructor constructor = null;

        double prevObj = -1;
        byte[] prevSol = null;

        for (int i = 0; i < MAX_ITER / segments; i++) {
            Arrays.fill(destructorScores, 0);
            Arrays.fill(constructorScores, 0);
            Arrays.fill(destructorCounts, 0);
            Arrays.fill(constructorCounts, 0);
            for (int j = 0; j < segments; j++) {
                prevObj = sol.getObjective(alpha, beta);
                prevSol = Utils.serialize(sol);

                // Destroy
                int destructorType = chooseDestructor();
                destructor = destructors[destructorType];
                destructor.destroy(sol);
                nodePair = destructor.getNodePair();
                //            validation(sol);

                // Construct
                int constructorType = chooseConstructor();
                constructor = constructors[constructorType];
                constructor.construct(sol, nodePair);
                //            validation(sol);

                if (sol.getObjective(alpha, beta) < bestObj) {
                    bestObj = sol.getObjective(alpha, beta);
                    bestSol = Utils.serialize(sol);
                    destructorScores[destructorType] += sigma1;
                    constructorScores[constructorType] += sigma1;
                }

                if (T < (sol.getObjective(alpha, beta) - bestObj) / bestObj) {
                    // not accept if worse than global and not meeting criteria, rollback
                    sol = Utils.deserialize(prevSol);
//                    System.out.println("not accepted");
                } else {
                    // accepted
                    destructorCounts[destructorType]++;
                    constructorCounts[constructorType]++;
                    // only reward unaccepted solution
                    if (!acceptedSolution.contains(sol.hashCode())) {
                        // accepted with improving objective
                        if (sol.getObjective(alpha, beta) < prevObj) {
                            destructorScores[destructorType] += sigma2;
                            constructorScores[constructorType] += sigma2;
                            // accepted with worse objective
                        } else {
                            destructorScores[destructorType] += sigma3;
                            constructorScores[constructorType] += sigma3;
                        }
                    }
                    acceptedSolution.add(sol.hashCode());
                }
                T -= coolingRate;
            }
            // update weights
            for (int k = 0; k < destructorWeights.length; k++) {
                destructorWeights[k] = destructorWeights[k] * (1 - r) + r * destructorScores[k] / destructorCounts[k];
            }
            for (int k = 0; k < constructorWeights.length; k++) {
                constructorWeights[k] = constructorWeights[k] * (1 - r) + r * constructorScores[k] / constructorCounts[k];
            }
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

    private int chooseDestructor() {
        double sum = 0;
        for (double weight : destructorWeights) {
            sum += weight;
        }
        double temp = 0;
        double rand = Math.random();
        for (int i = 0; i < destructorWeights.length; i++) {
            temp += destructorWeights[i] / sum;
            if (rand < temp) return i;
        }
        return -1;
    }

    private int chooseConstructor() {
        double sum = 0;
        for (double weight : constructorWeights) {
            sum += weight;
        }
        double temp = 0;
        double rand = Math.random();
        for (int i = 0; i < constructorWeights.length; i++) {
            temp += constructorWeights[i] / sum;
            if (rand < temp) return i;
        }
        return -1;
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
