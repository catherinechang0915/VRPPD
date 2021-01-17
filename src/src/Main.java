package src;

import src.DataStructures.*;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String instruction = "mode: 0 Data Generator\n\t: 1 OPL Solver\n\t: 2 Heuristic\n\t: 3 Validation\n\t: 4 Debug\n\n"
                + "For mode 0\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [K]\n"
                + "\tK: 0 using vehicle number in raw data text file\n\t : 1 using vehicle number in optimal solution\n\n"
                + "For mode 1, 2, 3\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta]\n\n"
                + "For mode 4\n"
                + "args: [mode] [.dat file path]";

        if (args.length == 0) {
            System.out.println("No argument input. Please follow the instruction below.\n");
            System.out.println(instruction);
            System.exit(1);
        }

        int mode = Integer.parseInt(args[0]);

        if (mode == 0 && args.length != 6 || mode == 4 && args.length != 2 || mode != 0 && mode != 4 && args.length != 5) {
            System.out.println("Incorrect number of arguments. Please format as following instruction.\n");
            System.out.println(instruction);
            System.exit(1);
        }

        // Debug with input data file
        if (mode == 4) {
            Solver debugSolver = new MySolver(args[1]);
            debugSolver.solve("src\\debug.txt");
            return;
        }

        int n = Integer.parseInt(args[1]);
        double memberPercent = Double.parseDouble(args[2]);
        double alpha = Double.parseDouble(args[3]);
        double beta = Double.parseDouble(args[4]);

        // Data Generator
        if (mode == 0) {
            int optimalVehicle = Integer.parseInt(args[5]);
            DataGenerator gen = new DataGenerator(n, memberPercent, alpha, beta, optimalVehicle);
            System.out.println("Data generation finished.");
            return;
        }

        String optimalVehicleOn = mode == 1 ? "_optimalVehicle" : "";
        String dataDir = "data\\pdp_" + n + "_mem_" + memberPercent + optimalVehicleOn + "\\" + alpha + "_" + beta + "\\";
        String resDirOPL = "res\\opl\\pdp_" + n + "_mem_" + memberPercent + "\\" + alpha + "_" + beta + "\\";
        String resDirHEU = "res\\heu\\pdp_" + n + "_mem_" + memberPercent + "\\" + alpha + "_" + beta + "\\";
        String resFile = "res\\pdp_" + n + "_mem_" + memberPercent + "_" + alpha + "_" + beta;

        List<String> files = Utils.fileListNoExtension(dataDir);
        if (files.size() == 0) {
            System.out.println("No data in data directory. Please use mode 0 to generate suitable data first.");
            System.out.println(instruction);
            System.exit(1);
        }

        Solver solver = null;
        Solution solution;

        if (mode == 1) {
            generateAggregationFileHeader(resDirOPL + "aggregation.txt");
            for (String filename : files) {
                solver = new OPLSolver(dataDir + filename + ".dat");
                solver.solve(resDirOPL + filename + ".txt");
                solution = solver.getSolverSolution();
                int fail = solution == null ? 1 : 0;
                generateAggregationFile(resDirOPL + "aggregation.txt", filename, solution.getVehicleNumber(),
                        solver.getSolverObjective(), solution.getTotalDist(), solution.getTotalPenalty(),
                        solution.getTimeElapsed(), fail);
            }
            return;
        }

        int iter = 3;

        if (mode == 2) {
            generateAggregationFileHeader(resDirHEU + "aggregation.txt");
            for (String filename : files) {
                int fail = 0;
                double avgVehicle = 0;
                double avgObj = 0;
                double avgDistance = 0;
                double avgPenalty = 0;
                long avgTime = 0;
                for (int i = 0; i < iter; i++) {
                    solver = new MySolver(dataDir + filename + ".dat");
                    try {
                        solver.solve(resDirHEU + filename + ".txt");
                        solution = solver.getSolverSolution();
                        avgVehicle += solution.getVehicleNumber();
                        avgObj += solver.getSolverObjective();
                        avgDistance += solution.getTotalDist();
                        avgPenalty += solution.getTotalPenalty();
                        avgTime += solution.getTimeElapsed();
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail++;
                    }
                }
                avgVehicle = avgVehicle / iter;
                avgObj = avgObj / iter;
                avgDistance = avgDistance / iter;
                avgPenalty = avgPenalty / iter;
                avgTime = avgTime / iter;

                generateAggregationFile(resDirHEU + "aggregation.txt", filename, avgVehicle, avgObj,
                        avgDistance, avgPenalty, avgTime, fail);
            }
            return;
        }

        if (mode == 3) {
            generateAggregationDiffFile(resDirOPL + "aggregation.txt",
                    resDirHEU + "aggregation.txt", resFile);
        }
    }

    private static void generateAggregationFileHeader(String filePath) {
        String header = String.format("%-15s%-15s%-15s%-15s%-15s%-15s%-15s\n", "Test Case", "Vehicle",
                "Objective", "Distance", "Penalty", "Time", "Fail Num");
        Utils.writeToFile(header, filePath, false);
    }

    private static void generateAggregationFile(String filepath, String filename, double vehicle, double objective, double distance, double penalty, long time, int fail) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s",filename)).append(String.format("%-15f", vehicle)).append(String.format("%-15f", objective))
                .append(String.format("%-15f", distance)).append(String.format("%-15f", penalty))
                .append(String.format("%-15f", (double) (time / 1000.0))).append(String.format("%-15d", fail)).append("\n");
        Utils.writeToFile(sb.toString(), filepath, true);
    }

    private static void generateAggregationDiffFile(String filepathOPL, String filepathHEU, String filePath) {
        File fileOPL = new File(filepathOPL);
        if (!fileOPL.exists()) {
            System.out.println("No aggregation file for OPL solver. Please use mode 1 to generate it first.");
            System.exit(1);
        }
        File fileHEU = new File(filepathHEU);
        if (!fileHEU.exists()) {
            System.out.println("No aggregation file for heuristic solver. Please use mode 2 to generate it first.");
            System.exit(1);
        }

        List<String> linesOPL = Utils.readLines(fileOPL, 1);
        List<String> linesHEU = Utils.readLines(fileHEU, 1);

        generateAggregationFileHeader(filePath);

        int numberOfInstances = Math.min(linesOPL.size(), linesHEU.size());

        double totalVehicleGap = 0;
        double totalObjectiveGap = 0;
        double totalDistanceGap = 0;
        double totalPenaltyGap = 0;
        double totalTimeOPL = 0;
        double totalTimeHEU = 0;
        int totalFail = 0;

        for (int i = 0; i < numberOfInstances; i++) {
            String[] lineOPL = linesOPL.get(i).split("\\s+");
            String[] lineHEU = linesHEU.get(i).split("\\s+");
            if (!lineOPL[0].equals(lineHEU[0])) {
                System.out.println("Wrong order in aggregation file. Please check or regenerate.");
                System.exit(1);
            }
            double vehicleDiff = (Double.parseDouble(lineHEU[1]) - Double.parseDouble(lineOPL[1])) / Double.parseDouble(lineOPL[1]);
            totalVehicleGap += vehicleDiff;

            double objectiveDiff = Double.parseDouble(lineHEU[2]) - Double.parseDouble(lineOPL[2]);
            totalObjectiveGap += objectiveDiff / Double.parseDouble(lineOPL[2]);

            double distanceDiff = (Double.parseDouble(lineHEU[3]) - Double.parseDouble(lineOPL[3]))  / Double.parseDouble(lineOPL[3]);
            totalDistanceGap += distanceDiff;

            double penaltyDiff = (Double.parseDouble(lineHEU[4]) - Double.parseDouble(lineOPL[4])) / Double.parseDouble(lineOPL[4]);
            totalPenaltyGap += penaltyDiff;

            totalTimeOPL += Double.parseDouble(lineOPL[5]);
            totalTimeHEU += Double.parseDouble(lineHEU[5]);
            totalFail += Integer.parseInt(lineHEU[6]);

            generateAggregationFile(filePath, lineOPL[0], vehicleDiff, objectiveDiff, distanceDiff, penaltyDiff,
                    -1L, Integer.parseInt(lineHEU[6]));
        }

        generateAggregationFile(filePath, "Summary", totalVehicleGap / numberOfInstances,
                totalObjectiveGap / numberOfInstances, totalDistanceGap / numberOfInstances,
                totalPenaltyGap / numberOfInstances, -1L, totalFail);

        String avgTime = "OPL Average Time " + totalTimeOPL / numberOfInstances
                + "\n Heuristic Average Time " + totalTimeHEU / numberOfInstances;
        Utils.writeToFile(avgTime, filePath, true);
    }
}
