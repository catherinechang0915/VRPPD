package src;

import src.DataStructures.Solution;

import java.util.List;

public class TATuning {
    public static void main(String[] args) {
        String sp = Utils.separator();
        double alpha = 1, beta = 1;
        String dataDir = "data" + sp + "pdp_100_mem_0.5" + sp + alpha + "_" + beta + sp;
        List<String> files = Utils.fileListNoExtension(dataDir);

        double TMAX = 0.1, TMIN = 0.1;
        int iteration = 5;
        Solution solution = null;

        for (double T = TMAX; T >= TMIN; T -= 0.01) {

            String resDir = "res" + sp + "TATuning" + sp + T + sp;
            String aggregationFilename = resDir + "aggregation.txt";
            Utils.generateAggregationFileHeader(aggregationFilename);
            Solver solver = new ALNSSolver(0, T);

            for (String filename : files) {
                int totalFail = 0;
                double totalObjective = 0, totalDistance = 0, totalPenalty = 0, totalVehicle = 0;
                long totalTime = 0;
                for (int i = 0; i < iteration; i++) {
                    solver.solve(dataDir + filename + ".dat", resDir + filename + ".txt");
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
}
