package src.DataStructures;

public class InsertPosition {
    private int pIndex;
    private int dIndex;
    private int nodeIndex;
    private Route route;
    private double distIncrease;
    private double penaltyIncrease;

    public InsertPosition(int pIndex, int dIndex, int nodeIndex, Route route, double distIncrease, double penaltyIncrease) {
        this.pIndex = pIndex;
        this.dIndex = dIndex;
        this.nodeIndex = nodeIndex;
        this.route = route;
        this.distIncrease = distIncrease;
        this.penaltyIncrease = penaltyIncrease;
    }

    public int getdIndex() {
        return dIndex;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public double getDistIncrease() {
        return distIncrease;
    }

    public double getPenaltyIncrease() {
        return penaltyIncrease;
    }

    public int getpIndex() {
        return pIndex;
    }

    public Route getRoute() {
        return route;
    }
}
