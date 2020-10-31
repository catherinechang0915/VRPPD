package src;

public class Main {

    public static void main(String[] args) {
	    Solver solver = new Solver("VRP.mod", "VRP.dat");
	    solver.displaySolution();
    }
}
