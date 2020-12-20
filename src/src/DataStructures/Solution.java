package src.DataStructures;

import java.util.LinkedList;
import java.util.List;

public class Solution {

    List<Route> sol;
    long timeElapsed;
    double totalDist;
    double totalPenalty;
    double objective;

    public Solution() {
        sol = new LinkedList<>();
        timeElapsed = 0;
    }

    public Solution(List<Route> sol, long timeElapsed, double totalDist, double totalPenalty, double objective) {
        this.sol = sol;
        this.timeElapsed = timeElapsed;
        this.totalDist = totalDist;
        this.totalPenalty = totalPenalty;
        this.objective = objective;
    }


    /**
     * Only output the node values of the solution
     * @return values for each route split by empty line
     */
    public String trace() {
        StringBuilder sb = new StringBuilder();
        for (Route r : sol) {
            sb.append(r.trace()).append("\n");
        }
        return sb.toString();
    }

    /**
     *
     * @return detailed information about the solution routes
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Objective Value: ").append(objective).append("\n");
        sb.append("Total Distance Traveled: ").append(totalDist).append("\n");
        sb.append("Total Penalty of Delay: ").append(totalPenalty).append("\n");
        sb.append("Time Elasped: ").append(timeElapsed).append("\n");
        for (Route r : sol) {
            sb.append(r.toString());
        }
        return sb.toString();
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

    public double getObjective() {
        return objective;
    }
}
