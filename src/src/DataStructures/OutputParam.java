package src.DataStructures;

/**
 * Equivalent to OPL output decision variables
 */
public class OutputParam {

    private int[][][] x;
    private double[][] T;
    private double[][] Q;
    private double[][] DL;

    public OutputParam(int[][][] x, double[][] T, double[][] Q, double[][] DL) {
        this.x = x;
        this.T = T;
        this.Q = Q;
        this.DL = DL;
    }

    public int[][][] getx() {
        return x;
    }

    public double[][] getT() {
        return T;
    }

    public double[][] getQ() {
        return Q;
    }

    public double[][] getDL() {
        return DL;
    }
}
