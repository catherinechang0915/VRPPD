package src.DataStructures;

/**
 * Equivalent to the information in .dat files
 */
public class InputParam {

    // number of nodes
    private int N;
    // number of vehicles
    private int K;
    // Vehicle and Node objects
    private Vehicle[] vehicles;
    private Node[] nodes;
    // distance
    private double[][] distanceMatrix;
    // Objective Function Weights
    private double alpha;
    private double beta;
    private double normalizeFactorLoad;
    private double normalizeFactorDis;
    private double normalizeFactorTime;

    public InputParam(int N, int K, Vehicle[] vehicles, Node[] nodes, double alpha, double beta) {
        this.N = N;
        this.K = K;
        this.vehicles = vehicles;
        this.nodes = nodes;
        this.distanceMatrix = calculateDistanceMatrix(nodes);
        this.alpha = alpha;
        this.beta = beta;
        this.normalizeFactorLoad = getMaxLoad();
        this.normalizeFactorTime = nodes[0].getTw2();
    }

    public InputParam(int N, int K, Vehicle[] vehicles, Node[] nodes) {
        this(N, K, vehicles, nodes, -1, -1);
    }

    private double getMaxLoad() {
        double max = -1;
        for (Node node : nodes) {
            max = Math.max(max, node.getq());
        }
        return max;
    }

//    private double[][] calculateDistMatrix(Node[] nodes) {
//        int len = nodes.length;
//        int n = (len - 2) / 2;
//        double[][] distanceMatrix = new double[len][len];
//        for (int i = 0; i < nodes.length; i++) {
//            for (int j = 0; j < nodes.length; j++) {
//                if (i != j) {
//                    if (i == 0) {
//                        if (j >= 1 && j <= n) distanceMatrix[i][j] = calculateDistance(nodes[i], nodes[j]);
//                        else distanceMatrix[i][j] = Double.MAX_VALUE;
//                    } else if (j == 2*n+1) {
//                        if (i > n && i <= 2*n) distanceMatrix[i][j] = calculateDistance(nodes[i], nodes[j]);
//                        else distanceMatrix[i][j] = Double.MAX_VALUE;
//                    } else {
//                        distanceMatrix[i][j] = calculateDistance(nodes[i], nodes[j]);
//                    }
//                }
//            }
//        }
//        distanceMatrix[0][len - 1] = calculateDistance(nodes[0], nodes[len - 1]);
//        return distanceMatrix;
//    }

    private double[][] calculateDistanceMatrix(Node[] nodes) {
        double maxDis = -1;
        double[][] realDistanceMatrix = new double[nodes.length][nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                realDistanceMatrix[i][j] = calculateDistance(nodes[i], nodes[j]);
                maxDis = Math.max(maxDis, realDistanceMatrix[i][j]);
            }
        }
        this.normalizeFactorDis = maxDis;
        return realDistanceMatrix;
    }

    public double calculateDistance(Node n1, Node n2) {
        return Math.sqrt( (n1.getX() - n2.getX()) *  (n1.getX() - n2.getX())
                + (n1.getY() - n2.getY()) * (n1.getY() - n2.getY()));
    }

    public int getN() {
        return N;
    }

    public int getK() {
        return K;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public Vehicle[] getVehicles() {
        return vehicles;
    }

    public double[][] getDistanceMatrix() {
        return distanceMatrix;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public double getNormalizeFactorDis() {
        return normalizeFactorDis;
    }

    public double getNormalizeFactorLoad() {
        return normalizeFactorLoad;
    }

    public double getNormalizeFactorTime() {
        return normalizeFactorTime;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }
}
