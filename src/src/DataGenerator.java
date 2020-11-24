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
            case "toy":
                generateSmall(inputFileName, outputFileName, 5, 3);
                break;
            case "benchmarkSmall":
                generateSmall(inputFileName, outputFileName, 51, 3);
                break;
            default:
                System.out.println("No such type.");
        }
    }

    public void generateSmall(String inputFileName, String outputFileName, int n, int K) {
        String DIR = "C:\\Users\\cathe\\Desktop\\FYP\\TSP\\Li and Lim test cases\\pdp_100\\";

        Node[] nodes = new Node[2 * n + 2];
        int[] X = new int[2 * n + 2];
        int[] Y = new int[2 * n + 2];
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(DIR + inputFileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            int Q = Integer.parseInt(bufferedReader.readLine().split("\\s+")[1]);
            int[] capacity = new int[K];
            Arrays.fill(capacity, Q);

            String[] depot = bufferedReader.readLine().split("\\s+");
            X[0] = Integer.parseInt(depot[1]);
            Y[0] = Integer.parseInt(depot[2]);
            nodes[0] = new Node(Integer.parseInt(depot[3]),
                    Integer.parseInt(depot[4]),
                    Integer.parseInt(depot[5]),
                    Integer.parseInt(depot[6]));
            X[2 * n + 1] = Integer.parseInt(depot[1]);
            Y[2 * n + 1] = Integer.parseInt(depot[2]);
            nodes[2 * n + 1] = new Node(Integer.parseInt(depot[3]),
                    Integer.parseInt(depot[4]),
                    Integer.parseInt(depot[5]),
                    Integer.parseInt(depot[6]));

            String[] nodeInfo = new String[102 + 1]; // nodeInfo 0 dummy
            for (int i = 1; i <= 102; i++) {
                nodeInfo[i] = bufferedReader.readLine();
            }
            String[] node;
            int p, d, sib, x1, y1, x2, y2;
            for (int i = 1; i <= n; i++) {
                node = nodeInfo[i].split("\\s+");
                Node node1 = new Node(Integer.parseInt(node[3]),
                        Integer.parseInt(node[4]),
                        Integer.parseInt(node[5]),
                        Integer.parseInt(node[6]));
                x1 = Integer.parseInt(node[1]);
                y1 = Integer.parseInt(node[2]);
                p = Integer.parseInt(node[7]);
                d = Integer.parseInt(node[8]);
                sib = p == 0 ? d : p;
                node = nodeInfo[sib].split("\\s+");
                Node node2 = new Node(Integer.parseInt(node[3]),
                        Integer.parseInt(node[4]),
                        Integer.parseInt(node[5]),
                        Integer.parseInt(node[6]));
                y2 = Integer.parseInt(node[2]);
                x2 = Integer.parseInt(node[1]);
                if (p == 0) {
                    nodes[i] = node1;
                    nodes[i + n] = node2;
                    X[i] = x1;
                    Y[i] = y1;
                    X[i + n] = x2;
                    Y[i + n] = y2;
                } else {
                    nodes[i + n] = node1;
                    nodes[i] = node2;
                    X[i] = x2;
                    Y[i] = y2;
                    X[i + n] = x1;
                    Y[i + n] = y1;
                }
            }
            int[] membership = new int[2 * n + 2];
            Arrays.fill(membership, 1);
            writeToFile(outputFileName, n, K, capacity, nodes, X, Y, membership);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        int[] capacity = new int[] {10, 10, 10};
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

    public void writeToFile(String outputFileName, int n, int K, int[] capacity, Node[] nodes,
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
