package src;

import src.DataStructures.*;

import java.util.*;

/**
 * This class generates test cases and write them in .dat files
 */
public class DataGenerator {

    /**
     *
     * @param n number of nodes (depots not included), total n + 2 nodes
     * @param memberPercent percentage of members
     */
    public DataGenerator(int n, double memberPercent, int alpha, int beta) {
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

        String inputDir = "raw_data\\" + "pdp_" + N + "\\";
        String outputDir = "data\\" + "pdp_" + n + "_mem_" + memberPercent + "\\" + alpha + "_" + beta + "\\";
        Utils.createDirectory(outputDir);
        List<String> filenames = Utils.fileList(inputDir);
        if (filenames == null || filenames.size() == 0) {
            System.out.println("No files in the directory " + inputDir + " . Data generation failed.");
            System.exit(1);
        }

//        Map<String, Integer> kMap = Utils.getVehicleNumber(inputDir + "vehicle.txt");
        for (String file : filenames) {
            if (!file.equals("vehicle.txt")) {
//                generateFile(n, kMap.get(file), memberPercent, alpha, beta,
//                        inputDir + file, outputDir + file.substring(0, file.length() - 4) + ".dat");
                generateFile(n, -1, memberPercent, alpha, beta,
                        inputDir + file, outputDir + file.substring(0, file.length() - 4) + ".dat");
            }
        }
    }

    public void generateFile(int n, int K, double memberPercent, int alpha, int beta,
                             String inputFileName, String outputFileName) {
        InputParam inputParam = Utils.readDataFromFile(n, K, memberPercent, inputFileName);
        inputParam.setAlpha(alpha);
        inputParam.setBeta(beta);
        Utils.writeDataToFile(inputParam, outputFileName);
    }
}
