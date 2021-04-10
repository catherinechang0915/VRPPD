package src.Operator;

import src.Utils;
import src.DataStructures.*;

import javafx.util.Pair;
import java.util.*;

public class RegretConstructor extends Constructor {

    protected static final double ita = 0.025;
    protected int size;
    protected double noiseRange;
    protected double noise;

    public RegretConstructor(int size, int noise) {
        this.size = size;
        this.noiseRange = -1;
        this.noise = noise;
    }

    /**
     * Find the best insertion place for best and regret construct operator
     * @param solution solution to be inserted
     * @param nodePair nodePair(request) to be inserted
     * @return best insertion position as InsertPosition object
     */
    protected InsertPosition findInsertPosition(InputParam inputParam, Solution solution, List<Integer> nodePair,
                                                Map<Integer, PriorityQueue<InsertPosition>> pqMap, Route updateRoute) {
        PriorityQueue<Pair<InsertPosition, Double>> nodePQ = new PriorityQueue<>(
                (o1, o2) -> {
                    if (!o1.getValue().equals(o2.getValue())) {
                        return - Double.compare(o1.getValue(), o2.getValue()); // larger regret better
                    } else {
                        // tie breaking rule
                        return Double.compare(o1.getKey().getDistIncrease() * inputParam.getAlpha()
                                        + o1.getKey().getPenaltyIncrease() * inputParam.getBeta(),
                                o2.getKey().getDistIncrease() * inputParam.getAlpha()
                                        + o2.getKey().getPenaltyIncrease() * inputParam.getBeta()); // smaller cost increase better
                    }
                }
        );

        if (this.noiseRange == -1) {
            this.noiseRange = Utils.findMaxDistance(inputParam.getDistanceMatrix()) * ita;
        }

        for (int nodeIndex : nodePair) {
            PriorityQueue<InsertPosition> pq = pqMap.get(nodeIndex); // one request one pq, save all value for all routes
            if (updateRoute == null) {
                for (Route route : solution.getRoutes()) {
                    InsertPosition minPos = findMinPosOnRoute(route, inputParam, nodeIndex);
                    if (minPos != null) pq.add(minPos);
                }
            } else {
                pq.removeIf(o -> o.getRoute().getVehicle().equals(updateRoute.getVehicle()));
                InsertPosition minPos = findMinPosOnRoute(updateRoute, inputParam, nodeIndex);
                if (minPos != null) pq.add(minPos);
            }
            List<InsertPosition> removedPos = new LinkedList<>();
            int regretSize = size == -1 ? solution.getRoutes().size() : size;
            if (pq.size() >= regretSize) {
                InsertPosition bestPos = pq.poll();
                removedPos.add(bestPos);
                double bestRegretValue = bestPos.getDistIncrease() * inputParam.getAlpha()
                        + bestPos.getPenaltyIncrease() * inputParam.getBeta();
                double regretValue = 0;
                for (int i = 1; i < regretSize; i++) {
                    InsertPosition temp = pq.poll();
                    removedPos.add(temp);
                    regretValue += temp.getDistIncrease() * inputParam.getAlpha()
                            + temp.getPenaltyIncrease() * inputParam.getBeta()
                            - bestRegretValue;
                }
                if (this.noise == 0) {
                    nodePQ.add(new Pair<>(bestPos, regretValue));
                } else {
                    double noiseVal = (Math.random() * 2 * this.noiseRange) - this.noiseRange;
                    nodePQ.add(new Pair<>(bestPos, Math.max(0, regretValue + noiseVal)));
                }
            } else if (pq.size() != 0) {
                double regretValue = 0;
                regretValue += (regretSize - pq.size()) * 1000;
                InsertPosition bestPos = pq.poll();
                removedPos.add(bestPos);
                double bestRegretValue = bestPos.getDistIncrease() * inputParam.getAlpha()
                        + bestPos.getPenaltyIncrease() * inputParam.getBeta();
                while (!pq.isEmpty()) {
                    InsertPosition temp = pq.poll();
                    removedPos.add(temp);
                    regretValue += temp.getDistIncrease() * inputParam.getAlpha()
                            + temp.getPenaltyIncrease() * inputParam.getBeta()
                            - bestRegretValue;
                }
                if (this.noise == 0) {
                    nodePQ.add(new Pair<>(bestPos, regretValue));
                } else {
                    double noiseVal = (Math.random() * 2 * this.noiseRange) - this.noiseRange;
                    nodePQ.add(new Pair<>(bestPos, Math.max(0, regretValue + noiseVal)));
                }
            }
            pq.addAll(removedPos);
        }
        return nodePQ.isEmpty() ? null : nodePQ.poll().getKey();
    }

    /**
     * For fixed route, request, find position with minimum insertion cost
     */
    private InsertPosition findMinPosOnRoute(Route route, InputParam inputParam, int nodeIndex) {
        InsertPosition minPos = null;
        for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
            for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                InsertPosition pos = checkNodePairInsertion(inputParam, route, nodeIndex, pIndex, dIndex);
                if (pos == null) continue; // infeasible
                if (minPos == null ||
                        pos.getDistIncrease() * inputParam.getAlpha()
                                + pos.getPenaltyIncrease() * inputParam.getBeta()
                                < minPos.getDistIncrease() * inputParam.getAlpha()
                                + minPos.getPenaltyIncrease() * inputParam.getBeta()) {
                    minPos = pos; // minPos is min on current route
                }
            }
        }
        return minPos;
    }

    /**
     * Construct Operator: insert at position with minimum difference in best and
     * second best objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    @Override
    public void construct(InputParam inputParam, Solution solution, List<Integer> nodePair) {
        Map<Integer, PriorityQueue<InsertPosition>> pqMap = new HashMap<>();
        for (int nodeIndex : nodePair) {
            pqMap.put(nodeIndex, new PriorityQueue<>(Comparator.comparingDouble(
                    o -> inputParam.getAlpha() * o.getDistIncrease()
                            + inputParam.getBeta() * o.getPenaltyIncrease())));
        }
        Route updateRoute = null;
        while (true) {
            InsertPosition pos = findInsertPosition(inputParam, solution, nodePair, pqMap, updateRoute);
            if (pos == null) {
                System.out.println(nodePair.size());
                //System.out.println("Regret construct no feasible solution");
                return;
            }
            nodeInsertion(inputParam, pos);
            pos.getRoute().setDist(pos.getRoute().getDist() + pos.getDistIncrease());
            pos.getRoute().setPenalty(pos.getRoute().getPenalty() + pos.getPenaltyIncrease());
            updateRoute = pos.getRoute();
            solution.setTotalDist(solution.getTotalDist() + pos.getDistIncrease());
            solution.setTotalPenalty(solution.getTotalPenalty() + pos.getPenaltyIncrease());
            nodePair.remove((Integer) pos.getNodeIndex());
            if (nodePair.size() == 0) break;
        }
    }

    @Override
    public String toString() {
        if (size == -1) return "Regret-M";
        return "Regret-" + size;
    }
}
