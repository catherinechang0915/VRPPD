package src;

public class InputParam {

    private int N;
    private int K;
    private int[] capacity;
    private Node[] nodes;
    private int[] X;
    private int[] Y;
    private int[] membership;

    public InputParam(int N, int K, int[] capacity, Node[] nodes, int[] X, int[] Y, int[] membership) {
        this.N = N;
        this.K = K;
        this.capacity = capacity;
        this.nodes = nodes;
        this.X = X;
        this.Y = Y;
        this.membership = membership;
    }

    public int getN() {
        return N;
    }

    public int getK() {
        return K;
    }

    public int[] getX() {
        return X;
    }

    public int[] getY() {
        return Y;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int[] getMembership() {
        return membership;
    }

    public int[] getCapacity() {
        return capacity;
    }
}
