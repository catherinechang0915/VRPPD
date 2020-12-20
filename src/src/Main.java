package src;

import src.DataStructures.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        int n = 20;
        int memberPercent = 1;
        int alpha = 1;
        int beta = 1;

//        DataGenerator gen = new DataGenerator(n, memberPercent, alpha, beta);

        String dataDir = "data\\pdp_" + n + "_mem_" + memberPercent + "\\" + alpha + "_" + beta + "\\";
        String resDirOPL = "res\\opl\\pdp_" + n + "_mem_" + memberPercent + "_validation\\" + alpha + "_" + beta + "\\";
        String resDirHEU = "res\\heuristics\\pdp_" + n + "_mem_" + memberPercent + "\\" + alpha + "_" + beta + "\\";

        List<String> files = Utils.fileList(dataDir);

        for (String filename : files) {
            InputParam inputParam = Utils.readParam(dataDir + filename);
            Solver solver = new Solver(inputParam, dataDir, filename);
            solver.solve();
            solver.validateAndSaveToFile(resDirOPL, filename);
            Visualizer visualizer = new Visualizer(solver);
        }
    }
}
