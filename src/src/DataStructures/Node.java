package src.DataStructures;

/**
 * Represent one location with related information (load, time, etc.)
 */
public class Node {

    // values for the input values (related to what in .dat file)
    private int index;
    private int q;
    private int tw1;
    private int tw2;
    private int s;
    private int x;
    private int y;
    private int membership;
    // values for the solution
    private double Q;
    private double T;
    private double DL;

    public Node(int index, int q, int tw1, int tw2, int s, int x, int y, int membership, double Q, double T, double DL) {
        this.index = index;
        this.q = q;
        this.tw1 = tw1;
        this.tw2 = tw2;
        this.x = x;
        this.y = y;
        this.membership = membership;
        this.s = s;
        this.Q = Q;
    }

    public Node(int index, int q, int tw1, int tw2, int s) {
        this(index, q, tw1, tw2, s, -1, -1, -1, 0, 0,0);
    }

    public Node(int q, int tw1, int tw2, int s, int x, int y) {
        this(-1, q, tw1, tw2, s, x, y, -1, 0, 0, 0);
    }

    public Node(Node node) {
        this(node.index, node.q, node.tw1, node.tw2, node.s, node.x, node.y, node.membership, node.Q, node.T, node.DL);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tNode ").append(index).append("\n");
        sb.append("\t\tLoad ").append(Q).append("\n");
        sb.append("\t\tTime ").append(T).append("\n");
        sb.append("\t\tDelay ").append(DL).append("\n");
        sb.append("\t\t\tMembership ").append(membership).append("\n");
        sb.append("\t\t\tTime Window [").append(tw1)
                .append(", ")
                .append(tw2)
                .append("]").append("\n");
        sb.append("\t\t\tLoad at node ").append(q).append("\n");
        sb.append("\t\t\tService time at node ").append(s).append("\n");
        return sb.toString();
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMembership() {
        return membership;
    }

    public int getIndex() {
        return index;
    }

    public double getT() {
        return T;
    }

    public double getQ() {
        return Q;
    }

    public double getDL() {
        return DL;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setMembership(int membership) {
        this.membership = membership;
    }

    public void setQ(double Q) {
        this.Q = Q;
    }

    public void setT(double t) {
        T = t;
    }

    public void setDL(double DL) {
        this.DL = DL;
    }
}
