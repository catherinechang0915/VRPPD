package src;

import java.io.*;
import java.util.*;

/**
 * This class generates test cases and write them in .dat files
 */
public class DataGenerator {

    private final String WORKDIR = "C:\\Users\\cathe\\opl\\VRP\\";

    /**
     *
     * @param type determine whether the test case depends on benchmark or nor
     * @param inputFileName filename for benchmark to be modified
     * @param outputFileName output filename for .dat file
     */
    public DataGenerator(String type, String inputFileName, String outputFileName) {
        switch (type) {
            case "fixed": // fixed multi-vehicle example for debug use
                generateFixed(outputFileName);
            case "toy":
                generateToy(outputFileName);
                break;
            case "benchmark":
                // TODO: modification of solomon benchmark for PD problem
                break;
        }
    }

    public void generateToy(String outputFileName) {
        // TODO
    }

    public void generateFixed(String outputFileName) {
        int n = 4;
        int K = 3;
        int[] X = new int[2 * n + 2];
        int[] Y = new int[2 * n + 2];
        // depots
        X[0] = 20; Y[0] = 20;
        X[2 * n + 1] = 20; Y[2 * n + 1] = 20;
        // route 1
        X[1] = 21; Y[1] = 21;
        X[1 + n] = 21; Y[1 + n] = 19;
        // route 2
        X[2] = 17; Y[2] = 21;
        X[2 + n] = 21; Y[2 + n] = 23;
        // route 3
        X[3] = 18; Y[3] = 19;
        X[4] = 19; Y[4] = 18;
        X[3 + n] = 21; Y[3 + n] = 17;
        X[4 + n] = 23; Y[4 + n] = 19;
        double[] capacity = new double[] {10, 10, 10};
        int[] membership = new int[2 * n + 2];
        Arrays.fill(membership, 1);
        Node[] nodes = new Node[2 * n + 2];
        nodes[0] = new Node(0, 0, 100, 0);
        nodes[2 * n + 1] = new Node(0, 0, 100, 0);
        for (int i = 1; i <= n; i++) {
            int load = (int)(Math.random()*10);
            nodes[i] = new Node(load, 0, 100, (int)(Math.random()*5));
            nodes[i + n] = new Node(-load, 0, 100, (int)(Math.random()*5));
        }
        writeToFile(outputFileName, n, K, capacity, nodes, X, Y, membership);
    }

    public void writeToFile(String outputFileName, int n, int K, double[] capacity, Node[] nodes,
                            int[] X, int[] Y, int[] membership) {
        File fileOPL = new File(WORKDIR + outputFileName);
        File fileData = new File("data\\" + outputFileName);
        BufferedWriter bufferedWriter1 = null, bufferedWriter2 = null;
        try {
            FileOutputStream fileOutputStream1 = new FileOutputStream(fileOPL);
            FileOutputStream fileOutputStream2 = new FileOutputStream(fileData);
            bufferedWriter1 = new BufferedWriter(new OutputStreamWriter(fileOutputStream1));
            bufferedWriter2 = new BufferedWriter(new OutputStreamWriter(fileOutputStream2));
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("n = ").append(n).append(";\n");
        sb.append("K = ").append(K).append(";\n");
        sb.append("capacity = [");
        for (int i = 0; i < K; i++) {
            sb.append(capacity[i]);
            if (i != K - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }
        sb.append("nodeInfo = [\n");
        for (int i = 0; i < 2 * n + 2; i++) {
            Node node = nodes[i];
            sb.append("<").append(node.getQ()).append(",")
                    .append(node.getTw1()).append(",")
                    .append(node.getTw2()).append(",")
                    .append(node.getS()).append(">\n");

        }
        sb.append("];\n");
        sb.append("X = [");
        for (int i = 0; i < X.length; i++) {
            sb.append(X[i]);
            if (i != X.length- 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }
        sb.append("Y = [");
        for (int i = 0; i < Y.length; i++) {
            sb.append(Y[i]);
            if (i != Y.length - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }
        sb.append("membership = [");
        for (int i = 0; i < membership.length; i++) {
            sb.append(membership[i]);
            if (i != membership.length - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }
        try {
            bufferedWriter1.write(sb.toString());
            bufferedWriter1.flush();
            bufferedWriter2.write(sb.toString());
            bufferedWriter2.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
