package src.Operator;

import javafx.util.Pair;
import src.DataStructures.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class RegretConstructor extends Constructor {

    private int size;

    public RegretConstructor(InputParam inputParam, int size) {
        super(inputParam);
        this.size = size;
    }

    /**
     * Find the best insertion place for best and regret construct operator
     * @param solution solution to be inserted
     * @param nodePair nodePair(request) to be inserted
     * @return best insertion position as InsertPosition object
     */
    private InsertPosition findInsertPosition(Solution solution, List<Integer> nodePair) {
        PriorityQueue<Pair<InsertPosition, Double>> nodePQ = new PriorityQueue<>(
                (o1, o2) -> {
                    if (!o1.getValue().equals(o2.getValue())) {
                        return - Double.compare(o1.getValue(), o2.getValue());
                    } else {
                        // tie breaking rule
                        return Double.compare(o1.getKey().getDistIncrease() * inputParam.getAlpha()
                                        + o1.getKey().getPenaltyIncrease() * inputParam.getBeta(),
                                o2.getKey().getDistIncrease() * inputParam.getAlpha()
                                        + o2.getKey().getPenaltyIncrease() * inputParam.getBeta());
                    }
                });

        int pqSize = Integer.MAX_VALUE;
        InsertPosition posWithLeastPossibleRoute = null;

        for (int nodeIndex : nodePair) {
            PriorityQueue<InsertPosition> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> inputParam.getAlpha() * o.getDistIncrease()
                            + inputParam.getBeta() * o.getPenaltyIncrease()));
            for (Route route : solution.getRoutes()) {
                for (int pIndex = 1; pIndex < route.getNodes().size(); pIndex++) {
                    for (int dIndex = pIndex; dIndex < route.getNodes().size(); dIndex++) {
                        InsertPosition pos = checkNodePairInsertion(route, nodeIndex, pIndex, dIndex);
                        if (pos == null) continue; // infeasible
                        pq.add(pos);
//                        if (pq.size() > size) pq.poll();
                    }
                }
            }
            int regretSize = size == -1 ? pq.size() : size;
            if (pq.size() >= regretSize) {
                InsertPosition bestPos = pq.peek();
                double bestRegretValue = bestPos.getDistIncrease() * inputParam.getAlpha()
                        + bestPos.getPenaltyIncrease() * inputParam.getBeta();
                double regretValue = 0;
                for (int i = 0; i < regretSize; i++) {
                    InsertPosition temp = pq.poll();
                    regretValue += temp.getDistIncrease() * inputParam.getAlpha()
                            + temp.getPenaltyIncrease() * inputParam.getBeta()
                            - bestRegretValue;
                }
                regretValue += (solution.getRoutes().size() - regretSize) * 1000;
                nodePQ.add(new Pair<>(bestPos, regretValue));
            } else if (pq.size() != 0) {
                if (pq.size() < pqSize) {
                    pqSize = pq.size();
                    posWithLeastPossibleRoute = pq.poll();
                }
            }
        }
        return posWithLeastPossibleRoute == null ? nodePQ.poll().getKey() : posWithLeastPossibleRoute;
    }

    /**
     * Construct Operator: insert at position with minimum difference in best and
     * second best objective increase
     * @param solution solution to be modified
     * @param nodePair node index pairs removed to be inserted
     */
    @Override
    public void construct(Solution solution, List<Integer> nodePair) {
        while (true) {
            InsertPosition pos = findInsertPosition(solution, nodePair);
            if (pos == null) {
                throw new NullPointerException("No feasible insertion");
            }
            nodeInsertion(pos);
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
