package src.Operator;

import src.Utils;
import src.DataStructures.*;

import javafx.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class RegretConstructor extends Constructor {

    private static final double ita = 0.025;
    private int size;
    private double noiseRange;

    public RegretConstructor(int size, int noise) {
        this.size = size;
        this.noiseRange = -1;
    }

    /**
     * Find the best insertion place for best and regret construct operator
     * @param solution solution to be inserted
     * @param nodePair nodePair(request) to be inserted
     * @return best insertion position as InsertPosition object
     */
    private InsertPosition findInsertPosition(InputParam inputParam, Solution solution, List<Integer> nodePair) {
        if (this.noiseRange == -1) this.noiseRange = Utils.findMaxDistance(inputParam.getDistanceMatrix()) * ita;
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
                });

        for (int nodeIndex : nodePair) {
            PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> inputParam.getAlpha() * o.getDistIncrease()
                            + inputParam.getBeta() * o.getPenaltyIncrease()));
            for (Route route : solution.getRoutes()) {
                for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(inputParam, route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        pq.add(pos);
//                        if (pq.size() > size) pq.poll();
                    }
                }
            }
            int regretSize = size == -1 ? solution.getRoutes().size() : size;
            if (pq.size() >= regretSize) {
                InsertPosition bestPos = pq.peek();
                double bestRegretValue = bestPos.getDistIncrease() * inputParam.getAlpha()
                        + bestPos.getPenaltyIncrease() * inputParam.getBeta();
                double regretValue = 0;
                for (int i = 1; i < regretSize; i++) {
                    InsertPosition temp = pq.poll();
                    regretValue += temp.getDistIncrease() * inputParam.getAlpha()
                            + temp.getPenaltyIncrease() * inputParam.getBeta()
                            - bestRegretValue;
                }
                if (this.noiseRange == -1) {
                    nodePQ.add(new Pair<>(bestPos, regretValue));
                } else {
                    double noiseVal = (Math.random() * 2 * this.noiseRange) - this.noiseRange;
                    nodePQ.add(new Pair<>(bestPos, Math.max(0, regretValue + noiseVal)));
                }
            } else if (pq.size() != 0) {
                double regretValue = 0;
                regretValue += (regretSize - pq.size()) * 1000;
                InsertPosition bestPos = pq.peek();
                double bestRegretValue = bestPos.getDistIncrease() * inputParam.getAlpha()
                        + bestPos.getPenaltyIncrease() * inputParam.getBeta();
                while (!pq.isEmpty()) {
                    InsertPosition temp = pq.poll();
                    regretValue += temp.getDistIncrease() * inputParam.getAlpha()
                            + temp.getPenaltyIncrease() * inputParam.getBeta()
                            - bestRegretValue;
                }
                if (this.noiseRange == -1) {
                    nodePQ.add(new Pair<>(bestPos, regretValue));
                } else {
                    double noiseVal = (Math.random() * 2 * this.noiseRange) - this.noiseRange;
                    nodePQ.add(new Pair<>(bestPos, Math.max(0, regretValue + noiseVal)));
                }
            }
        }
        return nodePQ.isEmpty() ? null : nodePQ.poll().getKey();
    }

    /**
     * Construct Operator: insert at position with minimum difference in best and
     * second best objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    @Override
    public void construct(InputParam inputParam, Solution solution, List<Integer> nodePair) {
        while (true) {
            InsertPosition pos = findInsertPosition(inputParam, solution, nodePair);
            if (pos == null) {
                throw new NullPointerException("No feasible insertion");
            }
            nodeInsertion(inputParam, pos);
            pos.getRoute().setDist(pos.getRoute().getDist() + pos.getDistIncrease());
            pos.getRoute().setPenalty(pos.getRoute().getPenalty() + pos.getPenaltyIncrease());
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
