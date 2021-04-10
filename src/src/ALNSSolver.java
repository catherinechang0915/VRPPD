package src;

import javafx.util.Pair;
import src.DataStructures.*;
import src.Operator.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class ALNSSolver extends Solver {

    private double r = 0.1;
    private int sigma1 = 33;
    private int sigma2 = 9;
    private int sigma3 = 13;
    private int noise;
//    private Solution solution;
    private double[] destructorWeights;
    private double[] constructorWeights;
    private double[] destructorNormalizedWeights;
    private double[] constructorNormalizedWeights;
    private double[] destructorScores;
    private double[] constructorScores;
    private int[] destructorCounts;
    private int[] constructorCounts;
    private Destructor[] destructors;
    private Constructor[] constructors;
    private int destructorsNum;
    private int constructorsNum;
    private double TStart;

    public ALNSSolver(int noise) {
        this(noise, -1, -1, -1, -1, -1);
    }

    public ALNSSolver(int noise, double TStart) {
        this(noise, TStart, -1, -1, -1, -1);
    }

    public ALNSSolver(int noise, double eta1, double eta2, double eta3, double eta4) {
        this(noise, -1, eta1, eta2, eta3, eta4);
    }

    // for threshold acceptance and Shaw parameter tuning
    public ALNSSolver(int noise, double TStart, double eta1, double eta2, double eta3, double eta4) {
        this.noise = noise;
        double percentLo = 0.2;
        double percentHi = 0.4;
        this.TStart = TStart == -1 ? 0.025 : TStart;
        if (eta1 == -1 || eta2 == -1 || eta3 == -1 || eta4 == -1) {
            destructors = new Destructor[] {
                new RandomDestructor(percentLo, percentHi),
                new WorstDestructor(percentLo, percentHi),
                    new ShawDestructor(percentLo, percentHi),
//                new ShawMemDestructor(percentLo, percentHi)
            };
        } else{
            destructors = new Destructor[] {
                new RandomDestructor(percentLo, percentHi),
                new WorstDestructor(percentLo, percentHi),
                    new ShawDestructor(percentLo, percentHi),
//                new ShawMemDestructor(percentLo, percentHi, eta1, eta2, eta3, eta4)
            };
        }

        if (noise == 0) {
            constructors = new Constructor[] {
                    new RegretConstructor(1, 0),
                    new RegretConstructor(2, 0),
                    new RegretConstructor(3, 0),
                    new RegretConstructor(4, 0),
                    new RegretConstructor(-1, 0)
            };
        } else {
            constructors = new Constructor[] {
                    new RegretConstructor(-1, 0),
                    new RegretConstructor(1, 0),
                    new RegretConstructor(2, 0),
                    new RegretConstructor(3, 0),
                    new RegretConstructor(4, 0),
                    new RegretConstructor(-1, 1),
                    new RegretConstructor(1, 1),
                    new RegretConstructor(2, 1),
                    new RegretConstructor(3, 1),
                    new RegretConstructor(4, 1)
            };
        }
        destructorsNum = destructors.length;
        constructorsNum = constructors.length;
        if (noise == 1) constructorsNum *= 2;
        destructorWeights = new double[destructorsNum];
        constructorWeights = new double[constructorsNum];
        destructorNormalizedWeights = new double[destructorsNum];
        constructorNormalizedWeights = new double[constructorsNum];
        Arrays.fill(destructorWeights, 1.0 / destructorsNum);
        Arrays.fill(constructorWeights, 1.0 / constructorsNum);
        Arrays.fill(destructorNormalizedWeights, 1.0 / destructorsNum);
        Arrays.fill(constructorNormalizedWeights, 1.0 / constructorsNum);
        destructorScores = new double[destructorsNum];
        constructorScores = new double[constructorsNum];
        destructorCounts = new int[destructorsNum];
        constructorCounts = new int[constructorsNum];
    }


    @Override
    public void solve(String dataFilePath, String resFilePath) {
        Arrays.fill(destructorWeights, 1.0 /destructorsNum);
        Arrays.fill(constructorWeights, 1.0 / constructorsNum);

        InputParam inputParam = Utils.readParam(dataFilePath);
        double alpha = inputParam.getAlpha(), beta = inputParam.getBeta();
        int MAX_ITER = 25000;
        int segments = 100;

        long startTime = System.currentTimeMillis();
        Pair<Solution, List<Integer>> temp = init(inputParam);
        Solution sol = temp.getKey();
        List<Integer> nodeNotProcessed = temp.getValue();
        if (nodeNotProcessed.size() != 0) throw new RuntimeException("No feasible initial solution");
//        validation(sol);

        HashSet<Integer> acceptedSolution = new HashSet<>();
        acceptedSolution.add(sol.hashCode());

        byte[] bestSol = Utils.serialize(sol);
        double bestObj = sol.getObjective(alpha, beta);

        double T = this.TStart;
        double coolingRate = T / MAX_ITER;

        boolean isWriteWeight = false, isWriteObjective = true;

        List<Integer> nodePair = null;
        Destructor destructor = null;
        Constructor constructor = null;

        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        StringBuilder bestObjectives = new StringBuilder(), currObjectives = new StringBuilder();

        double prevObj = -1;
        byte[] prevSol = null;

        // MAX_ITER / segments
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
                nodePair = destructor.destroy(inputParam, sol);
//                validation(sol);

                // Construct
                int constructorType = chooseConstructor();
                constructor = constructors[constructorType];
                constructor.construct(inputParam, sol, nodePair);

                if (nodePair.size() != 0) { // constructor does not find a feasible solution
                    sol = Utils.deserialize(prevSol);
                    continue;
                }
//                validation(sol);

                double currObj = sol.getObjective(alpha, beta);
                if (currObj < bestObj) {
                    bestObj = currObj;
                    bestSol = Utils.serialize(sol);
                    destructorScores[destructorType] += sigma1;
                    constructorScores[constructorType] += sigma1;
                    destructorCounts[destructorType]++;
                    constructorCounts[constructorType]++;
                    acceptedSolution.add(sol.hashCode());
                    continue;
                }

                if ((currObj - bestObj) / bestObj < T) {
                    // accepted
                    // only reward unvisited solution
                    if (!acceptedSolution.contains(sol.hashCode())) {
                        // accepted with improving objective
                        if (currObj < prevObj) {
                            destructorScores[destructorType] += sigma2;
                            constructorScores[constructorType] += sigma2;
                        // accepted with worse objective
                        } else {
                            destructorScores[destructorType] += sigma3;
                            constructorScores[constructorType] += sigma3;
                        }
                    }
                    destructorCounts[destructorType]++;
                    constructorCounts[constructorType]++;
                    acceptedSolution.add(sol.hashCode());
                } else {
                    // not accept if worse than global and not meeting criteria, rollback
                    sol = Utils.deserialize(prevSol);
                }
                T -= coolingRate;

                if (isWriteObjective) {
                    bestObjectives.append(bestObj).append("\n");
                    currObjectives.append(currObj).append("\n");
                }
            }
            // update weights
            updateweights(destructorWeights, destructorScores, destructorCounts);
            updateweights(constructorWeights, constructorScores, constructorCounts);
            normalizeWeights(destructorWeights, destructorNormalizedWeights);
            normalizeWeights(constructorWeights, constructorNormalizedWeights);

            if (isWriteWeight) {
                for (int k = 0; k < destructorNormalizedWeights.length; k++) {
                    sb1.append(destructorWeights[k]).append(" ");
                }
                sb1.append("\n");
                for (int k = 0; k < constructorNormalizedWeights.length; k++) {
                    sb2.append(constructorWeights[k]).append(" ");
                }
                sb2.append("\n");
            }
        }
        Solution bestToReturn = Utils.deserialize(bestSol);
        bestToReturn.setTimeElapsed(System.currentTimeMillis() - startTime);
        solution = bestToReturn;
        String salt = Utils.createSalt();
        writeToFile(resFilePath.replaceAll(".txt", "") + "_" + salt + ".txt");
        if (isWriteWeight) {
            Utils.writeToFile(sb1.toString(), resFilePath.replaceAll(".txt", "")
                    + "_" + salt + "_destructor_origin_weights.txt", false);
            Utils.writeToFile(sb2.toString(), resFilePath.replaceAll(".txt", "")
                    + "_" + salt + "_constructor_origin_weights.txt", false);
        }
        if (isWriteObjective) {
            Utils.writeToFile(bestObjectives.toString(), resFilePath.replaceAll(".txt", "")
                    + "_" + salt + "_best_objectives.txt", false);
            Utils.writeToFile(currObjectives.toString(), resFilePath.replaceAll(".txt", "")
                    + "_" + salt + "_curr_objectives.txt", false);
        }
    }

    private void normalizeWeights(double[] weights, double[] normalizedWeights) {
        double sum = 0;
        for (int k = 0; k < weights.length; k++) {
            sum += weights[k];
        }
        for (int k = 0; k < weights.length; k++) {
            normalizedWeights[k] = weights[k] / sum;
        }
    }
    
    /**
     * update weights for next segment based on the average score in this segment
     */
    private void updateweights(double[] weights, double[] scores, int[] counts) {
        for (int k = 0; k < weights.length; k++) {
            if (counts[k] == 0) {
                weights[k] = weights[k] * (1 - r);
            } else {
                weights[k] = weights[k] * (1 - r) + r * scores[k] / counts[k];
            }
        }
    }

    private int chooseDestructor() {
        double temp = 0;
        double rand = Math.random();
        for (int i = 0; i < destructorNormalizedWeights.length; i++) {
            temp += destructorNormalizedWeights[i];
            if (rand < temp) return i;
        }
        return -1;
    }

    private int chooseConstructor() {
        double temp = 0;
        double rand = Math.random();
        for (int i = 0; i < constructorNormalizedWeights.length; i++) {
            temp += constructorNormalizedWeights[i];
            if (rand < temp) return i;
        }
        return -1;
    }

    private boolean initForTW(InputParam inputParam) {
//        List<Route> routes = new LinkedList<>();
//        Vehicle[] vehicles = inputParam.getVehicles();
//        for (int routeCount = 0; routeCount < inputParam.getK(); routeCount++) {
//            routes.add(new Route(new LinkedList<>(), vehicles[routeCount]));
//        }
//        Solution solution = new Solution(routes, 0, 0);
//
//        Constructor initialConstructor = new InitialConstructor();
//        // store the unprocessed request pairs
//        List<Integer> nodePairNotProcessed = new LinkedList<>();
//        for (int i = 1; i <= inputParam.getN(); i++) {
//            nodePairNotProcessed.add(i);
//        }
        Pair<Solution, List<Integer>> temp = init(inputParam);
        Solution solution = temp.getKey();
        List<Integer> nodePairNotProcessed = temp.getValue();
         //initialConstructor.construct(inputParam, solution, nodePairNotProcessed);
        if (nodePairNotProcessed.size() == 0) return true;
//        Destructor destructor = new RandomDestructor(0.15, 0.4);
//        Constructor constructor = new RegretConstructor(1, 0);
//        for (int i = 0; i < 10; i++) {
//            List<Integer> nodePairTemp = destructor.destroy(inputParam, solution);
//            validation(solution);
//            for (int node : nodePairNotProcessed) {
//                if (nodePairTemp.contains(node)) continue;
//                nodePairTemp.add(node);
//            }
//            constructor.construct(inputParam, solution, nodePairTemp);
//            if (nodePairTemp.size() == 0) {
//                // System.out.println(i);
//                return true;
//            }
//            nodePairNotProcessed = nodePairTemp;
//        }
        return false;
    }

    public boolean check(String dataFilePath) {
        InputParam inputParam = Utils.readParam(dataFilePath);
        return initForTW(inputParam);
    }

    /**
     * For debug use, recalculate and compare the information along the route
     * @param sol Solution to be verified
     */
    private static void validation(Solution sol) {
        List<Route> routes = sol.getRoutes();
        if (routes.size() != 25) throw new RuntimeException("Wrong number of routes.");
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
                if (!Utils.doubleEqual(node.getQ(), load)) throw new RuntimeException("Wrong with load.");
                if (load < 0 || load > route.getVehicle().getCapacity()) throw new RuntimeException("Load Violation.");
                time = Math.max(node.getTw1(), prevNode.gets() + time + Utils.calculateDistance(prevNode, node));
                if (node.getMembership() == 1 && time > node.getTw2()) throw new RuntimeException("Time window violation.");
                if (!Utils.doubleEqual(time, node.getT())) throw new RuntimeException("Wrong with time.");
                double tempPenalty = Math.max(0, time - node.getTw2());
                if (!Utils.doubleEqual(tempPenalty, node.getDL())) throw new RuntimeException("Wrong penalty");
                penalty += tempPenalty;
                dist += Utils.calculateDistance(prevNode, node);
            }
            if (!Utils.doubleEqual(dist, route.getDist())) throw new RuntimeException("Wrong route distance");
//            System.out.println(penalty + " " + route.getPenalty());
            if (!Utils.doubleEqual(penalty, route.getPenalty())) throw new RuntimeException("Wrong route penalty");
            totalDist += dist;
            totalPenalty += penalty;
        }
        if (!Utils.doubleEqual(totalDist, sol.getTotalDist())) throw new RuntimeException("Wrong solution distance");
        if (!Utils.doubleEqual(totalPenalty, sol.getTotalPenalty())) throw new RuntimeException("Wrong solution penalty");
    }

    private void writeToFile(String filePath) {
        solution.writeToFile(filePath, false);
    }

    @Override
    public String toString() {
        return "ALNS";
    }
}
