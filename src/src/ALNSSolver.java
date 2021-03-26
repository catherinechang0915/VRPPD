package src;

import src.DataStructures.*;
import src.Operator.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
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
    private int destructorsNum = 3;
    private int constructorsNum = 5;

    public ALNSSolver(int noise) {
        this.noise = noise;
        double percentLo = 0.2;
        double percentHi = 0.4;
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
        destructors = new Destructor[] {
                new RandomDestructor(percentLo, percentHi),
                new ShawDestructor(percentLo, percentHi),
                new WorstDestructor(percentLo, percentHi)
                //new WorstDelayDestructor(percentLo, percentHi)
        };
        if (noise == 0) {
            constructors = new Constructor[] {
                new RegretConstructor(-1, 0),
                new RegretConstructor(1, 0),
                new RegretConstructor(2, 0),
                new RegretConstructor(3, 0),
                new RegretConstructor(4, 0)
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
        Solution sol = init(inputParam);

        HashSet<Integer> acceptedSolution = new HashSet<>();
        acceptedSolution.add(sol.hashCode());

        byte[] bestSol = Utils.serialize(sol);
        double bestObj = sol.getObjective(alpha, beta);

        double T = 0.025;
        double coolingRate = T / MAX_ITER;

        List<Integer> nodePair = null;
        Destructor destructor = null;
        Constructor constructor = null;

        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();

        double prevObj = -1;
        byte[] prevSol = null;

        String bestObjs = "";
        String currObjs = "";

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
                //validation(sol);

                // Construct
                int constructorType = chooseConstructor();
                constructor = constructors[constructorType];
                constructor.construct(inputParam, sol, nodePair);
                //validation(sol);

                double currObj = sol.getObjective(alpha, beta);
                if (currObj < bestObj) {
                    bestObj = currObj;
                    bestSol = Utils.serialize(sol);
                    destructorScores[destructorType] += sigma1;
                    constructorScores[constructorType] += sigma1;
                }

                if (T < (currObj - bestObj) / bestObj) {
                    // not accept if worse than global and not meeting criteria, rollback
                    sol = Utils.deserialize(prevSol);
                } else {
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

                    acceptedSolution.add(sol.hashCode());
                }
                destructorCounts[destructorType]++;
                constructorCounts[constructorType]++;


                T -= coolingRate;
//                bestObjs += bestObj + "\n";
//                currObjs += currObj + "\n";
            }
            // update weights
            updateweights(destructorWeights, destructorScores, destructorCounts);
            updateweights(constructorWeights, constructorScores, constructorCounts);
            normalizeWeights(destructorWeights, destructorNormalizedWeights);
            normalizeWeights(constructorWeights, constructorNormalizedWeights);

            for (int k = 0; k < destructorNormalizedWeights.length; k++) {
                sb1.append(destructorWeights[k]).append(" ");
            }
            sb1.append("\n");
            for (int k = 0; k < constructorNormalizedWeights.length; k++) {
                sb2.append(constructorWeights[k]).append(" ");
            }
            sb2.append("\n");

        }
        Solution bestToReturn = Utils.deserialize(bestSol);
        bestToReturn.setTimeElapsed(System.currentTimeMillis() - startTime);
        solution = bestToReturn;
//        Utils.writeToFile(sb1.toString(), "src\\destructor_origin_weights.txt", false);
//        Utils.writeToFile(sb2.toString(), "src\\constructor_origin_weights.txt", false);
        Utils.writeToFile(sb1.toString(), resFilePath.replaceAll(".txt", "") + "_destructor_origin_weights.txt", false);
        Utils.writeToFile(sb2.toString(), resFilePath.replaceAll(".txt", "") + "_constructor_origin_weights.txt", false);
        writeToFile(resFilePath);
//        Utils.writeToFile(bestObjs, "src\\bestObjs.txt", false);
//        Utils.writeToFile(currObjs, "src\\objs.txt", false);
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

    public boolean check(String dataFilePath) {
        InputParam inputParam = Utils.readParam(dataFilePath);
        Solution sol = init(inputParam);
        if (sol.size() != inputParam.getNodes().length) return false;
        return true;
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
        return "ALNS";
    }
}
