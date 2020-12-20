package src;

import java.util.LinkedList;
import java.util.List;

public class Solution {

    List<Route> sol;
    long timeElapsed;

    public Solution() {
        sol = new LinkedList<>();
        timeElapsed = 0;
    }

    public Solution(List<Route> sol, long timeElapsed) {
        this.sol = sol;
        this.timeElapsed = timeElapsed;
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
        sb.append("Time Elasped: ").append(timeElapsed).append("\n");
        for (Route r : sol) {
            sb.append(r.toString()).append("\n");
        }
        return sb.toString();
    }
}
