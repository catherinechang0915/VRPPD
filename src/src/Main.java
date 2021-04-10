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
                || mode == 1 && args.length != 5
                || mode == 2 && args.length != 6
                || mode == 3 && args.length != 8) {
            exit("Incorrect number of arguments.");
        }


        // Debug with input data file
        if (mode == 4) {
            int type = -1;
            try {
                type = Integer.parseInt(args[2]);
            } catch (Exception e) {
                exit("Wrong format of debug solver type.");
            }

            Solver debugSolver = null;
            int noise = -1;
            if (type == 2 || type == 3) {
                noise = Integer.parseInt(args[3]);
            }
            if (type == 3) {
                int destructorType = -1, constructorType = -1;
                try {
                    destructorType = Integer.parseInt(args[4]);
                    constructorType = Integer.parseInt(args[5]);
                } catch (Exception e) {
                    exit("Wrong format of operator type.");
                }
                debugSolver = new MySolver(destructorType, constructorType, noise);
            } else if (type == 2) {
                debugSolver = new ALNSSolver(noise, 0.025);
            } else if (type == 1) {
                debugSolver = new OPLSolver();
            } else {
                exit("Wrong solver type.");
            }
            debugSolver.solve(args[1], "src" + Utils.separator() + "debug.txt");
            return;
        }

        int n = Integer.parseInt(args[1]);
        double memberPercent = Double.parseDouble(args[2]);
        double alpha = Double.parseDouble(args[3]);
        double beta = Double.parseDouble(args[4]);

        // Data Generator
        if (mode == 0) {
            int optimalVehicle = Integer.parseInt(args[5]);
            new DataGenerator(n, memberPercent, alpha, beta, optimalVehicle);
            System.out.println("Data generation finished.");
            return;
        }

        String optimalVehicleOn = mode == 1 ? "_optimalVehicle" : "";
        String sp = Utils.separator();
        String dataDir = "data" + sp + "pdp_" + n + "_mem_" + memberPercent + optimalVehicleOn + sp + alpha + "_" + beta + sp;
        String resDir = null;
        if (mode == 1) {
            resDir = "res" + sp + "opl" + sp + "pdp_" + n + "_mem_" + memberPercent + sp + alpha + "_" + beta + sp;
        } else if (mode == 2) {
            resDir = "res" + sp + "alns" + sp + "pdp_" + n + "_mem_" + memberPercent + sp + alpha + "_" + beta + sp;
        } else if (mode == 3) {
            resDir = "res" + sp + "heu" + sp + "pdp_" + n + "_mem_" + memberPercent + sp + alpha + "_" + beta + sp;
        }

        List<String> files = Utils.fileListNoExtension(dataDir);
        if (files.size() == 0) {
            exit("No data in data directory.");
        }

        Solver solver = null;
        Solution solution;

        if (mode == 1) {
            solver = new OPLSolver();
            Utils.generateAggregationFileHeader(resDir + "aggregation.txt");
            for (String filename : files) {
                solver.solve(dataDir + filename + ".dat", resDir + filename + ".txt");
                solution = solver.getSolverSolution();
                int fail = solution == null ? 1 : 0;
                Utils.generateAggregationFile(resDir + "aggregation.txt", filename, solution.getVehicleNumber(),
                        solver.getSolverObjective(alpha, beta), solution.getTotalDist(), solution.getTotalPenalty(),
                        solution.getTimeElapsed(), fail);
            }
            return;
        }

        if (mode == 2) {
            int noise = -1;
            try {
                noise = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                exit("Wrong noise flag.");
            }
            solver = new ALNSSolver(noise);
        }

        if (mode == 3) {
            int destructorType = -1, constructorType = -1, noise = -1;
            try {
                noise = Integer.parseInt(args[5]);
                destructorType = Integer.parseInt(args[6]);
                constructorType = Integer.parseInt(args[7]);
            } catch (Exception e) {
                exit("Wrong format of operator type.");
            }
            solver = new MySolver(destructorType, constructorType, noise);
        }
        int iteration = 3;
        runSolver(solver, files, dataDir, resDir, n, memberPercent, alpha, beta, iteration);
    }

    private static void runSolver(Solver solver, List<String> files, String dataDir, String resDir, int n, double memberPercent, double alpha, double beta, int iteration) {
                                          
        String sp = Utils.separator();
        String resAggregationFilename = resDir + sp + "aggregation_" + solver + ".txt";
        String resGapFilename = resDir + "gap_" + solver + ".txt";  

        double totalAvgVehicleGap = 0;
        double totalAvgObjGap = 0;
        Solution solution = null;
//        Map<String, Integer> kMap = null;
//        Map<String, Double> objMap = null;

        int totalFail = 0;
        long totalAvgTime = 0;
        Utils.writeToFile(solver.toString(), resAggregationFilename, false);
        Utils.generateAggregationFileHeader(resAggregationFilename);
//        if (memberPercent == 1.0) {
//            kMap = Utils.getVehicleNumber("raw_data" + sp + "pdp_" + n + sp + "optimal.txt");
//            objMap = Utils.getOptimalObj("raw_data" + sp + "pdp_" + n + sp + "optimal.txt");
//            generateGapFileHeader(resGapFilename);
//        }

        for (String filename : files) {
            int fail = 0;
            double avgVehicle = 0;
            double avgObj = 0;
            double avgDistance = 0;
            double avgPenalty = 0;
            long avgTime = 0;
            for (int i = 0; i < iteration; i++) {
                
                try {
                    solver.solve(dataDir + filename + ".dat", resDir + filename + ".txt");
                    solution = solver.getSolverSolution();
                    avgVehicle += solution.getVehicleNumber();
                    avgObj += solver.getSolverObjective(alpha, beta);
                    avgDistance += solution.getTotalDist();
                    avgPenalty += solution.getTotalPenalty();
                    avgTime += solution.getTimeElapsed();
                } catch (Exception e) {
                    e.printStackTrace();
                    fail++;
                }
            }
            avgVehicle = avgVehicle / iteration;
            avgObj = avgObj / iteration;
            avgDistance = avgDistance / iteration;
            avgPenalty = avgPenalty / iteration;
            avgTime = avgTime / iteration;

            totalAvgTime += avgTime;
            totalFail += fail;

//            if (memberPercent == 1.0) {
//                double vehicleNum = kMap.get(filename);
//                double obj = objMap.get(filename);
//                double vehicleGap = Math.abs(avgVehicle - vehicleNum) / vehicleNum;
//                double objGap = Math.abs(avgObj - obj) / obj;
//                totalAvgObjGap += vehicleGap;
//                totalAvgObjGap += objGap;
//                generateGapFile(resGapFilename, filename, vehicleGap, objGap);
//            }

            Utils.generateAggregationFile(resAggregationFilename, filename, avgVehicle, avgObj,
                    avgDistance, avgPenalty, avgTime, fail);
        }
        Utils.writeToFile("Average Time: " + (totalAvgTime / 1000.0 / files.size()) + "\n",
                resAggregationFilename, true);
        Utils.writeToFile("Fail percentage: " + (totalFail / (3 * files.size())) + "\n",
                resAggregationFilename, true);
        if (memberPercent == 1.0) {
            Utils.writeToFile("Average Vehicle Gap: " + (totalAvgVehicleGap / files.size()) + "\n",
                    resGapFilename, true);
            Utils.writeToFile("Average Objective Gap: " + (totalAvgObjGap / files.size()) + "\n",
                    resGapFilename, true);
        }
    }

    private static void exit(String msg) {
        String instruction = "mode: 0 Data Generator\n\t: 1 OPL Solver\n\t: 2 ALNS Solver\n\t: 3 Heuristic\n\t: 4 Debug\n\n"
                + "For mode 0\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [K]\n"
                + "\tK: 0 using vehicle number in raw data text file\n\t : 1 using vehicle number in optimal solution\n\n"
                + "For mode 1\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta]\n\n"
                + "For mode 2\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [noise flag]\n\n"
                + "For mode 3\n"
                + "args: [mode] [n] [memberPercent] [alpha] [beta] [noise flag] [destructor type] [constructor type]\n\n"
                + "For mode 4\n"
                + "args: [mode] [.dat file path] [solver type] ([noise flag] [destructor type] [constructor type])\n\n"
                + "Destructor Type: \n\t0: Random\n\t1: Worst\n\t2: Shaw\n\t3:Shaw with Priority"
                + "Constructor Type: \n\t0: Regret-M\n\t1: Regret-1\n\t2: Regret-2\n\t3: Regret-3\n\t4: Regret-4";

        System.out.println(msg);
        System.out.println("Please follow the instruction below.\n");
        System.out.println(instruction);
        System.exit(1);
    }

}
