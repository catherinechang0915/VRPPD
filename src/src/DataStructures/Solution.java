package src.DataStructures;

import src.Utils;

import java.util.LinkedList;
import java.util.List;

public class Solution {

    private List<Route> sol;
    private long timeElapsed;
    private double totalDist;
    private double totalPenalty;

    public Solution() {
        sol = new LinkedList<>();
        timeElapsed = -1;
    }

    public Solution(List<Route> sol, double totalDist, double totalPenalty) {
        this(sol, -1, totalDist, totalPenalty);
    }

    public Solution(List<Route> sol, long timeElapsed, double totalDist, double totalPenalty) {
        this.sol = sol;
        this.timeElapsed = timeElapsed;
        this.totalDist = totalDist;
        this.totalPenalty = totalPenalty;
    }


    /**
     * Only output the node values of the solution
     * @return values for each route split by empty line
     */
    public String trace() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Route r : sol) {
            if (r.getNodes().size() != 2) {
                sb.append(r.trace()).append("\n");
                count++;
            }
        }
        sb.append("Vehicle used: ").append(count);
        return sb.toString();
    }

    public String objective() {
        StringBuilder sb = new StringBuilder();
//        sb.append("Objective Value: ").append(objective).append("\n");
        sb.append("Total Distance Traveled: ").append(totalDist).append("\n");
        sb.append("Total Penalty of Delay: ").append(totalPenalty).append("\n");
        sb.append("Time Elasped: ").append(timeElapsed).append("\n");
        return sb.toString();
    }

    /**
     *
     * @return detailed information about the solution routes
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
//        sb.append("Objective Value: ").append(objective).append("\n");
        sb.append("Total Distance Traveled: ").append(totalDist).append("\n");
        sb.append("Total Penalty of Delay: ").append(totalPenalty).append("\n");
        sb.append("Time Elasped: ").append(timeElapsed).append("\n");
        for (Route r : sol) {
            if (r.getNodes().size() != 2) {
                sb.append(r.toString());
                count++;
            }
        }
        sb.append("Vehicle used: ").append(count);
        return sb.toString();
    }

    public void writeToFile(String filename) {
        Utils.writeToFile(sol.toString(), filename);
    }

    public List<Route> getRoutes() {
        return sol;
    }

    public double getTotalDist() {
        return totalDist;
    }

    public double getTotalPenalty() {
        return totalPenalty;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public int getVehicleNumber() {
        int count = 0;
        for (Route r : sol) {
            if (r.getNodes().size() != 2) {
                count++;
            }
        }
        return count;
    }

    public void setTotalDist(double totalDist) {
        this.totalDist = totalDist;
    }

    public void setTotalPenalty(double totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public double getObjective(double alpha, double beta) {
        return alpha * totalDist + beta * totalPenalty;
    }
}
