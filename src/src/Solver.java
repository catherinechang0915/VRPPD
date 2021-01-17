package src;

import src.DataStructures.Solution;

public interface Solver {

    public void solve(String resFilePath);

    public Solution getSolverSolution();

    public double getSolverObjective();
}
