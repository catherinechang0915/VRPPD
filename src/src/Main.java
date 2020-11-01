package src;

public class Main {

    public static void main(String[] args) {
        String filename = "fixedSmallNoRestriction";
//        DataGenerator generator = new DataGenerator("fixed", null, filename + ".dat");
	    Solver solver = new Solver("VRP.mod", filename + ".dat");
	    solver.displaySolution(filename + ".txt");
        Visualizer visualizer = new Visualizer(solver);
    }
}
