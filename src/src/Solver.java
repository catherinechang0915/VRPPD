package src;

import src.DataStructures.Solution;

public interface Solver {

    public void solve(String resDir, String resFilename);

    public Solution getSolverSolution();

    public double getSolverObjective();
}
