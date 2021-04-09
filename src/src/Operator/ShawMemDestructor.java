package src.Operator;

import src.DataStructures.*;
import src.Utils;

import javafx.util.Pair;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

public class ShawMemDestructor extends ShawDestructor{

    public ShawMemDestructor(double percentLo, double percentHi) {
        super(percentLo, percentHi);
    }

    protected List<Integer> generateNodePairShaw(InputParam inputParam, Solution solution) {
        int N = inputParam.getN();
        Node[] nodes = inputParam.getNodes();
        List<Integer> nodePair = new LinkedList<>();
        int rand = (int)(Math.random() * N) + 1;
        nodePair.add(rand);

        Function<Pair<Node, Node>, Double> sameRouteFactor = pair -> {
            Node node1 = pair.getKey();
            Node node2 = pair.getValue();
            for (Route route : solution.getRoutes()) {
                if (route.getNodes().contains(node1) && route.getNodes().contains(node2)) {
                    return 0.8;
                } else if (route.getNodes().contains(node1) || route.getNodes().contains(node2)) {
                    return 1.0;
                }
            }
            return 1.0;
        };

        Function<Pair<Node, Node>, Double> memFactor = pair -> {
            Node node1 = pair.getKey();
            Node node2 = pair.getValue();
            if (node1.getMembership() == 0 && node2.getMembership() == 0) return 0.5;
            if (node1.getMembership() == 1 && node2.getMembership() == 1) return 0.8;
            else return 1.0;
        };


        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> 9 * sameRouteFactor.apply(new Pair(nodes[o], nodes[nodeNum]))
                            * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N])) / inputParam.getNormalizeFactorDis()
                            + 3 * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT())) / inputParam.getNormalizeFactorTime()
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq()) / inputParam.getNormalizeFactorLoad()
                            + memFactor.apply(new Pair(nodes[o], nodes[nodeNum]))
            )); // smaller is similar -> top ele better
            for (int i = 1; i <= N; i++) {
                if (!nodePair.contains(i) && i != 0 && i != 2 * N + 1) pq.add(i);
            }

            for (int i = 0; i < getRandomPos(p, pq.size()); i++) {
                pq.poll();
            }
            nodePair.add(pq.poll());
        }
        return nodePair;
    }
}
