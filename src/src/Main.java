package src;

public class Main {

    public static void main(String[] args) {

        String filename = "toyp";
//        DataGenerator generator = new DataGenerator("toy", "lc201.txt", filename + ".dat");
        Solver solver = new Solver("VRP.mod", filename + ".dat");
	    solver.displaySolution(filename + ".txt");
        Visualizer visualizer = new Visualizer(solver);
    }
}
