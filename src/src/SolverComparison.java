package src;

import src.DataStructures.Solution;

import java.util.List;

public class SolverComparison {
    public static void main(String[] args) {
        String sp = Utils.separator();
        double alpha = 1;
        double beta = 1;

        int size = 10;
        String dataDir = "data" + sp + "comparison" + sp + size + sp;

        List<String> files = Utils.fileListNoExtension(dataDir);
//        String[] files = new String[] {"src\\toy.dat"};

        int iteration = 1;
        Solution solution = null;

        boolean isALNS = false;

        String resDir = "res" + sp + "comparison" + sp + (isALNS ? "ALNS" : "OPL") + sp;
        String aggregationFilename = resDir + "aggregation" + size + ".txt";
        Utils.generateAggregationFileHeader(aggregationFilename);

        for (String filename : files) {
            Solver solver = isALNS ? new ALNSSolver(1) : new OPLSolver();
            int totalFail = 0;
            double totalObjective = 0, totalDistance = 0, totalPenalty = 0, totalVehicle = 0;
            long totalTime = 0;
            for (int i = 0; i < iteration; i++) {
                solver.solve(dataDir + filename + ".dat", resDir + filename + ".txt");
//                solver.solve(filename, resDir + filename + ".txt");
                solution = solver.getSolverSolution();
                int fail = solution == null ? 1 : 0;
                totalFail += fail;
                if (fail == 1) {
                    System.out.println(filename + " fail");
                    Utils.generateAggregationFile(aggregationFilename, filename, 0,
                            0, 0, 0, 0, fail);
                } else {
                    int vehicle = solution.getVehicleNumber();
                    double objective = solver.getSolverObjective(alpha, beta);
                    double distance = solution.getTotalDist();
                    double penalty = solution.getTotalPenalty();
                    long time = solution.getTimeElapsed();
                    totalVehicle += vehicle;
                    totalObjective += objective;
                    totalDistance += distance;
                    totalPenalty += penalty;
                    totalTime += time;
                    Utils.generateAggregationFile(aggregationFilename, filename, vehicle,
                            objective, distance, penalty, time, fail);
                }
            }
            Utils.generateAggregationFile(aggregationFilename, "avg",
                    totalVehicle / iteration, totalObjective / iteration,
                    totalDistance / iteration, totalPenalty / iteration,
                    totalTime / iteration, totalFail);
        }
    }
}
