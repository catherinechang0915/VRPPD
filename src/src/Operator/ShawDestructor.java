package src.Operator;

import src.DataStructures.InputParam;
import src.DataStructures.Node;
import src.DataStructures.Solution;
import src.Utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class ShawDestructor extends Destructor{

    // TODO: may modify later
    private final int randomRange = 3;

    public ShawDestructor(InputParam inputParam, double percent) {
        super(inputParam, percent);
    }

    @Override
    public void destroy(Solution solution) {
        nodePair = generateNodePairShaw();
        destroy(solution, nodePair);
    }

    /**
     * Choose requests to be removed based on their relativity, one request is randomly selected from nodePair
     * each iteration, and found the closely related 3 requests, randomly add one to the request set
     * @return request set
     */
    private List<Integer> generateNodePairShaw() {
        int N = inputParam.getN();
        Node[] nodes = inputParam.getNodes();
        List<Integer> nodePair = new LinkedList<>();
        int rand = (int)(Math.random() * N) + 1;
        nodePair.add(rand);
        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o ->  9 * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N]))
                            + 3 * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT()))
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq())
            ));
            for (int i = 1; i <= N; i++) {
                if (!nodePair.contains(i)) pq.add(i);
            }
            double temp = (int)(Math.random() * randomRange);
            for (int i = 0; i < temp; i++) {
                pq.poll();
            }
            nodePair.add(pq.poll());
        }
        return nodePair;
    }

    /**
     * For debug use
     */
    private List<Integer> generateNodePairShaw(int q, int rand) {
        int N = inputParam.getN();
        Node[] nodes = inputParam.getNodes();
        List<Integer> nodePair = new LinkedList<>();
        nodePair.add(rand);
        while (nodePair.size() < q) {
            int nodeNum = nodePair.get((int)(Math.random() * nodePair.size()));
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingDouble(
                    o ->  9 * (Utils.calculateDistance(nodes[o], nodes[nodeNum])
                            + Utils.calculateDistance(nodes[o + N], nodes[nodeNum + N]))
                            + 3 * (Math.abs(nodes[o].getT() - nodes[nodeNum].getT())
                            + Math.abs(nodes[o + N].getT() - nodes[nodeNum + N].getT()))
                            + 2 * Math.abs(nodes[o].getq() - nodes[nodeNum].getq())
            ));
            for (int i = 1; i < N; i++) {
                if (!nodePair.contains(i)) pq.add(i);
            }
            nodePair.add(pq.poll());
        }
        return nodePair;
    }

    @Override
    public String toString() {
        return "Shaw Destructor";
    }
}
