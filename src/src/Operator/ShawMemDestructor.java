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

    private double eta1, eta2, eta3, eta4;

    public ShawMemDestructor(double percentLo, double percentHi) {
        this(percentLo, percentHi, 0.8, 0.5, 0.8, 1);
    }

    public ShawMemDestructor(double percentLo, double percentHi, double eta1, double eta2, double eta3, double eta4) {
        super(percentLo, percentHi);
        this.eta1 = eta1;
        this.eta2 = eta2;
        this.eta3 = eta3;
        this.eta4 = eta4;
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
                    return this.eta1;
                } else if (route.getNodes().contains(node1) || route.getNodes().contains(node2)) {
                    return 1.0;
                }
            }
            return 1.0;
        };

        Function<Pair<Node, Node>, Double> memFactor = pair -> {
            Node node1 = pair.getKey();
            Node node2 = pair.getValue();
            if (node1.getMembership() == 0 && node2.getMembership() == 0) return this.eta2;
            if (node1.getMembership() == 0 || node2.getMembership() == 0) return this.eta3;
            else return 1.0;
        };

        Function<Pair<Node, Node>, Double> sameMem = pair -> {
            Node node1 = pair.getKey();
            Node node2 = pair.getValue();
            if (node1.getMembership() == node2.getMembership()) return 0.0;
            else return 1.0;
        };


        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o -> 9 * sameRouteFactor.apply(new Pair(nodes[o], nodes[nodeNum]))
                            * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N])) / inputParam.getNormalizeFactorDis()
                            + 3 * memFactor.apply(new Pair(nodes[o], nodes[nodeNum]))
                            * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT())) / inputParam.getNormalizeFactorTime()
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq()) / inputParam.getNormalizeFactorLoad()
                            + this.eta4 * sameMem.apply(new Pair(nodes[o], nodes[nodeNum]))
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
