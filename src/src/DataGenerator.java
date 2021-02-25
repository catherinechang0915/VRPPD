package src;

import src.DataStructures.*;

import java.util.*;

/**
 * This class generates test cases and write them in .dat files
 */
public class DataGenerator {

    public DataGenerator(int n, double memberPercent, double alpha, double beta, int optimalVehicle) {
        String sp = Utils.separator();
        int N = -1;
        if (n % 2 != 0) {
            System.out.println("Number of nodes cannot form pairs.");
            System.exit(1);
        }
        // choose the least N that is bigger than n to generate data
        if (n <= 100) N = 100;
        else if (n <= 200) N = 200;
        else if (n <= 400) N = 400;
        else if (n <= 600) N = 600;
        else if (n <= 800) N = 800;
        else if (n <= 1000) N = 1000;
        else {
            System.out.println("Input number of nodes larger than 1000.");
            System.exit(1);
        }

        String inputDir = "raw_data" + sp + "pdp_" + N + sp;
        List<String> filenames = Utils.fileListNoExtension(inputDir);
        if (filenames.size() == 0) {
            System.out.println("No files in the directory " + inputDir + " . Data generation failed.");
            System.exit(1);
        }

        String optimalVehicleOn = optimalVehicle == 1 ? "_optimalVehicle" : "";
        String outputDir = "data" + sp + "pdp_" + n + "_mem_" + memberPercent + optimalVehicleOn + sp
                + alpha + "_" + beta + "\\";
        Utils.createDirectory(outputDir);

        if (optimalVehicle == 0) {
            for (String file : filenames) {
                if (!file.equals("optimal")) {
                    generateFile(n, -1, memberPercent, alpha, beta,
                            inputDir + file + ".txt", outputDir + file + ".dat");
                }
            }
        } else {
            Map<String, Integer> kMap = Utils.getVehicleNumber(inputDir + "optimal.txt");
            for (String file : filenames) {
                if (!file.equals("optimal")) {
                    generateFile(n, kMap.get(file), memberPercent, alpha, beta,
                            inputDir + file + ".txt", outputDir + file + ".dat");
                }
            }
        }
    }

    public void generateFile(int n, int K, double memberPercent, double alpha, double beta,
                             String inputFileName, String outputFileName) {
        InputParam inputParam = Utils.readDataFromFile(n, K, memberPercent, inputFileName);
        inputParam.setAlpha(alpha);
        inputParam.setBeta(beta);
        Utils.writeDataToFile(inputParam, outputFileName);
    }
}
