package src;

import src.DataStructures.*;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            exit("No argument input.");
        }

        int mode = -1;
        try {
            mode = Integer.parseInt(args[0]);
            if (mode < 0 || mode > 4) throw new IllegalArgumentException("Mode arg our of range");
        } catch (Exception e) {
            exit("Wrong format of mode.");
        }


        if (mode == 0 && args.length != 6
                || (mode == 1 || mode == 2) && args.length != 5
                || mode == 3 && args.length != 7) {
            exit("Incorrect number of arguments.");
        }


        // Debug with input data file
        if (mode == 4) {
            int type = -1;
            try {
                type = Integer.parseInt(args[1]);
            } catch (Exception e) {
                exit("Wrong format of debug solver type.");
            }

            Solver debugSolver = null;
            if (type == 3) {
                int destructorType = -1, constructorType = -1;
                try {
                    destructorType = Integer.parseInt(args[3]);
                    constructorType = Integer.parseInt(args[4]);
                } catch (Exception e) {
                    exit("Wrong format of operator type.");
                }
                debugSolver = new MySolver(args[2], destructorType, constructorType);
            } else if (type == 2) {
                // debugSolver = new ALNSSolver();
            } else if (type == 1) {
                debugSolver = new OPLSolver(args[2]);
            } else {
                exit("Wrong solver type.");
            }
            debugSolver.solve("src" + Utils.separator() + "debug.txt");
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
        String sp = Utils.separator();
        String dataDir = "data" + sp + "pdp_" + n + "_mem_" + memberPercent + optimalVehicleOn + sp + alpha + "_" + beta + sp;
        String resDirOPL = "res" + sp + "opl" + sp + "pdp_" + n + "_mem_" + memberPercent + sp + alpha + "_" + beta + sp;
        String resDirHEU = "res" + sp + "heu" + sp + "pdp_" + n + "_mem_" + memberPercent + sp + alpha + "_" + beta + sp;
        String resFile = "res" + sp + "pdp_" + n + "_mem_" + memberPercent + "_" + alpha + "_" + beta;

        List<String> files = Utils.fileListNoExtension(dataDir);
        if (files.size() == 0) {
            exit("No data in data directory.");
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

        if (mode == 2) {

        }

        // TODO: how many times to repeat for the same test cases, may change to arg
        int iter = 3;
        if (mode == 3) {

            double totalAvgVehicleGap = 0;
            double totalAvgObjGap = 0;
            Map<String, Integer> kMap = null;
            Map<String, Double> objMap = null;

            int destructorType = -1, constructorType = -1;
            try {
                destructorType = Integer.parseInt(args[5]);
                constructorType = Integer.parseInt(args[6]);
            } catch (Exception e) {
                exit("Wrong format of operator type.");
            }

            String resAggregationFilename = resDirHEU + "aggregation_" + destructorType + "_"
                    + constructorType + ".txt";
            String resGapFilename = resDirHEU + "gap_" + destructorType + "_"
                    + constructorType + ".txt";

            int totalFail = 0;
            long totalAvgTime = 0;
            generateAggregationFileHeader(resAggregationFilename);
            if (memberPercent == 1.0) {
                kMap = Utils.getVehicleNumber("raw_data" + sp + "pdp_" + n + sp + "optimal.txt");
                objMap = Utils.getOptimalObj("raw_data" + sp + "pdp_" + n + sp + "optimal.txt");
                generateGapFileHeader(resGapFilename);
            }

            for (String filename : files) {
                int fail = 0;
                double avgVehicle = 0;
                double avgObj = 0;
                double avgDistance = 0;
                double avgPenalty = 0;
                long avgTime = 0;
                for (int i = 0; i < iter; i++) {
                    solver = new MySolver(dataDir + filename + ".dat", destructorType, constructorType);
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

                totalAvgTime += avgTime;
                totalFail += fail;

                if (memberPercent == 1.0) {
                    double vehicleNum = kMap.get(filename);
                    double obj = objMap.get(filename);
                    double vehicleGap = Math.abs(avgVehicle - vehicleNum) / vehicleNum;
                    double objGap = Math.abs(avgObj - obj) / obj;
                    totalAvgObjGap += vehicleGap;
                    totalAvgObjGap += objGap;
                    generateGapFile(resGapFilename, filename, vehicleGap, objGap);
                }

                generateAggregationFile(resAggregationFilename, filename, avgVehicle, avgObj,
                        avgDistance, avgPenalty, avgTime, fail);
            }
            Utils.writeToFile("Average Time: " + (totalAvgTime / files.size()),
                    resAggregationFilename, true);
            Utils.writeToFile("Fail percentage: " + (totalFail / (3 * files.size())),
                    resAggregationFilename, true);
            if (memberPercent == 1.0) {
                Utils.writeToFile("Average Vehicle Gap: " + (totalAvgVehicleGap / files.size()),
                        resGapFilename, true);
                Utils.writeToFile("Average Objective Gap: " + (totalAvgObjGap / files.size()),
                        resGapFilename, true);
            }
        }
    }

    private static void exit(String msg) {
        String instruction = "mode: 0 Data Generator\n\t: 1 OPL Solver\n\t: 2 ALNS Solver\n\t: 3 Heuristic\n\t: 4 Debug\n\n"
                + "For mode 0\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [K]\n"
                + "\tK: 0 using vehicle number in raw data text file\n\t : 1 using vehicle number in optimal solution\n\n"
                + "For mode 1, 2\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta]\n\n"
                + "For mode 3\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [destructor type] [constructor type]\n\n"
                + "For mode 4\n"
                + "args: [mode] [solver type] [.dat file path] ([destructor type] [constructor type])\n\n"
                + "Destructor Type: \n\t0: Random\n\t1: Worst\n\t2: Shaw\n"
                + "Construcor Type: \n\t0: Regret-M\n\t1: Regret-1\n\t2: Regret-2\n\t3: Regret-3\n\t4: Regret-4";

        System.out.println(msg);
        System.out.println("Please follow the instruction below.\n");
        System.out.println(instruction);
        System.exit(1);
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

    private static void generateGapFileHeader(String filePath) {
        String header = String.format("%-15s%-15s%-15s\n", "Test Case", "Vehicle", "Objective");
        Utils.writeToFile(header, filePath, false);
    }

    private static void generateGapFile(String filepath, String filename, double vehicle, double objective) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s",filename)).append(String.format("%-15f", vehicle))
                .append(String.format("%-15f", objective)).append("\n");
        Utils.writeToFile(sb.toString(), filepath, true);
    }

}
