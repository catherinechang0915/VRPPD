package src;

public class Node {
    private int q;
    private int tw1;
    private int tw2;
    private int s;

    public Node(int q, int tw1, int tw2, int s) {
        this.q = q;
        this.tw1 = tw1;
        this.tw2 = tw2;
        this.s = s;
    }

    public int getQ() {
        return q;
    }

    public int getTw1() {
        return tw1;
    }

    public int getTw2() {
        return tw2;
    }

    public int getS() {
        return s;
    }
}
