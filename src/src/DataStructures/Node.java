package src;

public class Node {

    // values for the input values (related to what in .dat file)
    private int index;
    private int q;
    private int tw1;
    private int tw2;
    private int s;
    // values for the solution
    private double Q;
    private double T;
    private double DL;

    public Node(int index, int q, int tw1, int tw2, int s) {
        this(index, q, tw1, tw2, s, -1, -1,-1);
    }

    public Node(int index, int q, int tw1, int tw2, int s, double Q, double T, double DL) {
        this.index = index;
        this.q = q;
        this.tw1 = tw1;
        this.tw2 = tw2;
        this.s = s;
        this.Q = Q;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    }

    public int getq() {
        return q;
    }

    public int getTw1() {
        return tw1;
    }

    public int getTw2() {
        return tw2;
    }

    public int gets() {
        return s;
    }

    public int getIndex() {
        return index;
    }

    public double getT() {
        return T;
    }

    public int getQ() {
        return q;
    }

    public double getDL() {
        return DL;
    }
}
