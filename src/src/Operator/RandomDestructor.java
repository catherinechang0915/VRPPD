package src.Operator;

import src.DataStructures.InputParam;
import src.DataStructures.Solution;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RandomDestructor extends Destructor{

    public RandomDestructor(InputParam inputParam, double percent) {
        super(inputParam, percent);
    }

    @Override
    public void destroy(Solution solution) {
        nodePair = generateNodePairRandom();
        destroy(solution, nodePair);
    }

    /**
     * Randomly choose requests to be removed
     * @return request set
     */
    private List<Integer> generateNodePairRandom() {
        int N = inputParam.getN();
        List<Integer> nodePair = new LinkedList<>();
        for (int k = 0; k < q; k++) {
            int rand = (int)(Math.random() * N) + 1;
            while (nodePair.contains(rand)) {
                rand = (int)(Math.random() * N) + 1;
            }
            nodePair.add(rand);
        }
        return nodePair;
    }

    /**
     * For debug use, generate nodePair with random seed
     */
    private List<Integer> generateNodePairRandom(int q, int seed) {
        Random generator = new Random(seed);
        int N = inputParam.getN();
        List<Integer> nodePair = new LinkedList<>();
        for (int k = 0; k < q; k++) {
            int rand = (int)(generator.nextDouble() * N) + 1;
            while (nodePair.contains(rand)) {
                rand = (int)(generator.nextDouble() * N) + 1;
            }
            nodePair.add(rand);
        }
        return nodePair;
    }

    @Override
    public String toString() {
        return "Random Destructor";
    }
}
