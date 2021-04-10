package src;

import src.DataStructures.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Read from .dat file, for solver use
     * @param filePath filepath for .dat file
     * @return InputParam object
     */
    public static InputParam readParam(String filePath) {
        File file = new File(filePath);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String text;
            Pattern patten;
            Matcher matcher;

            int N;
            // set N
            text = bufferedReader.readLine();
            patten = Pattern.compile("N = (.*);");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                N = Integer.parseInt(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Wrong N.");
            }

            // set K
            int K;
            text = bufferedReader.readLine();
            patten = Pattern.compile("K = (.*);");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                K = Integer.parseInt(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Wrong K.");
            }

            // set capacity
            Vehicle[] vehicles = new Vehicle[K];
            text = bufferedReader.readLine();
            patten = Pattern.compile("capacity = \\[(.*)];");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                String[] c = matcher.group(1).split(",");
                for (int i = 0; i < K; i++) {
                    vehicles[i] = new Vehicle(i, Integer.parseInt(c[i]));
                }
            } else {
                throw new IllegalArgumentException("Wrong capacity.");
            }

            // set nodeInfo
            Node[] nodes = new Node[2 * N + 2];
            bufferedReader.readLine(); // line nodeInfo = [
            patten = Pattern.compile("<(.*)>");
            for (int i = 0; i < 2 * N + 2; i++) {
                text = bufferedReader.readLine();
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    String[] info = matcher.group(1).split(",");
                    nodes[i] = new Node(i, Integer.parseInt(info[0]),
                            Double.parseDouble(info[1]),
                            Double.parseDouble(info[2]),
                            Integer.parseInt(info[3]));
                } else {
                    throw new IllegalArgumentException("Wrong node info.");
                }
            }
            bufferedReader.readLine(); // line ];

            // set X
            int[] X = new int[2 * N + 2];
            text = bufferedReader.readLine();
            patten = Pattern.compile("X = \\[(.*)];");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                String[] xs = matcher.group(1).split(",");
                for (int i = 0; i < xs.length; i++) {
                    nodes[i].setX(Integer.parseInt(xs[i]));
                }
            } else {
                throw new IllegalArgumentException("Wrong X.");
            }

            // set Y
            text = bufferedReader.readLine();
            patten = Pattern.compile("Y = \\[(.*)];");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                String[] ys = matcher.group(1).split(",");
                for (int i = 0; i < ys.length; i++) {
                    nodes[i].setY(Integer.parseInt(ys[i]));
                }
            } else {
                throw new IllegalArgumentException("Wrong Y.");
            }


            text = bufferedReader.readLine();
            patten = Pattern.compile("membership = \\[(.*)]");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                String[] mem = matcher.group(1).split(",");
                for (int i = 0; i < mem.length; i++) {
                    nodes[i].setMembership(Integer.parseInt(mem[i]));
                }
            } else {
                throw new IllegalArgumentException("Wrong membership.");
            }

            double alpha;
            // set alpha
            text = bufferedReader.readLine();
            patten = Pattern.compile("alpha = (.*);");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                alpha = Double.parseDouble(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Wrong alpha.");
            }

            double beta;
            // set N
            text = bufferedReader.readLine();
            patten = Pattern.compile("beta = (.*);");
            matcher = patten.matcher(text);
            if (matcher.find()) {
                beta = Double.parseDouble(matcher.group(1));
            } else {
                throw new IllegalArgumentException("Wrong beta.");
            }

            return new InputParam(N, K, vehicles, nodes, alpha, beta);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Incorrect format of .dat file. " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * For debug use, the correspondence between test instances and optimal number of vehicles used are stored
     * in filePath, read and return the correspondence
     * @param filePath complete filepath for correspondence
     * @return a map stores the correspondence
     */
    public static Map<String, Integer> getVehicleNumber(String filePath) {
        Map<String, Integer> kMap = new HashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = null;
            String[] text = null;
            while ((line = bufferedReader.readLine()) != null) {
                text = line.split("\\s+");
                kMap.put(text[0], Integer.parseInt(text[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return kMap;
    }

    /**
     * For debug use, the correspondence between test instances and optimal objective are stored
     * in filePath, read and return the correspondence
     * @param filePath complete filepath for correspondence
     * @return a map stores the correspondence
     */
    public static Map<String, Double> getOptimalObj(String filePath) {
        Map<String, Double> objMap = new HashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = null;
            String[] text = null;
            while ((line = bufferedReader.readLine()) != null) {
                text = line.split("\\s+");
                objMap.put(text[0], Double.parseDouble(text[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return objMap;
    }

    /**
         * Read for raw test instances, used for data generator,
         * and save result as input parameters in our problem setting
         * @param n total number of nodes except depots (= 2 * number of requests)
         * @param K number of vehicles, -1 means sufficient large (original number in the text file),
         *          otherwise set to be value of input argument
         * @param memberPercent the percent of nodes to have hard time windows
         * @param filePath complete filepath for raw data
         * @return InputParam object modified from raw data
         */
    public static InputParam readDataFromFile(int n, int K, double memberPercent, String filePath, double shrinkPercent) {

        List<Node> unsortedNodes = new LinkedList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();
            String[] text = line.split("\\s+");

            int capacity = Integer.parseInt(text[1]);

            // if K != -1, we input K as the optimal number of vehicles for model validation
            // if K == -1, we use heuristic for route construction and recalculate K later
            if (K == -1) {
                K = Integer.parseInt(text[0]);
            }

            Vehicle[] vehicles = new Vehicle[K];
            for (int i = 0; i < vehicles.length; i++) {
                vehicles[i] = new Vehicle(i, capacity);
            }

            Map<Integer, Integer> pdPair = new HashMap<>();

            // First read every line and store info in nodes
            while ((line = bufferedReader.readLine()) != null) {
                text = line.split("\\s+");
                int index = Integer.parseInt(text[0]);
                double tw1 = Double.parseDouble(text[4]);
                double tw2 = Double.parseDouble(text[5]);
                if (index != 0) {
                    tw1 += shrinkPercent * (tw2 - tw1);
                    tw2 -= shrinkPercent * (tw2 - tw1);
                } else {
                    // tw1 += shrinkPercent * (tw2 - tw1) * 0.1;
                    tw2 -= shrinkPercent * (tw2 - tw1) * 0.1;
                }
                unsortedNodes.add(new Node(Integer.parseInt(text[3]),
                        tw1,
                        tw2,
                        Integer.parseInt(text[6]),
                        Integer.parseInt(text[1]),
                        Integer.parseInt(text[2])));
                // int p = Integer.parseInt(text[7]);
                int d = Integer.parseInt(text[8]);
                if (d != 0) {
                    pdPair.put(index, d);
                }
            }

            if (n == 100 || n == 200 || n == 400 || n == 600 || n == 600 || n == 1000) {
                n = unsortedNodes.size() - 1; // remove one depot from calculation, n = 2 * N, total nodes n + 2
            }

            int N = n / 2;

            Node[] nodes = new Node[2 * N + 2];

            // depots
            nodes[0] = unsortedNodes.get(0);
            nodes[2 * N + 1] = new Node(unsortedNodes.get(0));
            nodes[0].setMembership(1);
            nodes[2 * N + 1].setMembership(1);

            // internal pd-pairs
            int index = 1;
            for (int p : pdPair.keySet()) {
                int membership = Math.random() < memberPercent ? 1 : 0;
                nodes[index + N] = unsortedNodes.get(pdPair.get(p));
                nodes[index] = unsortedNodes.get(p);
                nodes[index + N].setMembership(membership);
                nodes[index].setMembership(membership);
                index++;
                if (index == N + 1) break; // when not all pairs are used
            }

            // update node indexes
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setIndex(i);
            }

            return new InputParam(N, K, vehicles, nodes);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    /**
     * Write input parameters to .dat file, used for data generator
     * @param inputParam generated input data
     * @param filePath complete filepath
     */
    public static void writeDataToFile(InputParam inputParam, String filePath) {
        File fileData = new File(filePath);
        BufferedWriter bufferedWriter = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileData);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("N = ").append(inputParam.getN()).append(";\n");
        sb.append("K = ").append(inputParam.getK()).append(";\n");

        Vehicle[] vehicles = inputParam.getVehicles();
        sb.append("capacity = [");
        for (int i = 0; i < inputParam.getK(); i++) {
            sb.append(vehicles[i].getCapacity());
            if (i != inputParam.getK() - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }

        Node[] nodes = inputParam.getNodes();
        sb.append("nodeInfo = [\n");
        for (int i = 0; i < 2 * inputParam.getN() + 2; i++) {
            Node node = nodes[i];
            sb.append("<").append(node.getq()).append(",")
                    .append(node.getTw1()).append(",")
                    .append(node.getTw2()).append(",")
                    .append(node.gets()).append(">\n");

        }
        sb.append("];\n");

        sb.append("X = [");
        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i].getX());
            if (i != nodes.length- 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }

        sb.append("Y = [");
        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i].getY());
            if (i != nodes.length - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }
        sb.append("membership = [");
        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i].getMembership());
            if (i != nodes.length - 1) {
                sb.append(",");
            } else {
                sb.append("];\n");
            }
        }

        sb.append("alpha = ").append(inputParam.getAlpha()).append(";\n");
        sb.append("beta = ").append(inputParam.getBeta()).append(";\n");

        try {
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return a list of string names in the directory, return null if dir not exist
     * @param dirPath path for directory
     * @return filenames in dirPath
     */
    public static List<String> fileList(String dirPath) {
        File folder = new File(dirPath);
        List<String> files = new LinkedList<>();
        try {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i].getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return files;
    }

    /**
     * Read filenames in directory, system exit if no such directory
     * @param dirPath path for directory
     * @return filename without extension in a directory
     */
    public static List<String> fileListNoExtension(String dirPath) {
        File folder = new File(dirPath);
        List<String> files = new LinkedList<>();
        try {
            File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i].getName().split("\\.")[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return files;
    }

    /**
     * create a directory if not exists
     * @param dirPath path for directory
     */
    public static void createDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    /**
     * create file and recursively create parent directories if not exist
     * @param filePath complete path for file
     * @return newly created file
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);
        try {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return file;
    }

    /**
     * Save String to file
     * @param content String content to be saved
     * @param filename filepath
     */
    public static void writeToFile(String content, String filename, boolean isAppend) {
        File file = new File(filename);
        if (!file.exists()) createFile(filename);
//        if (file.exists()) file.delete();
        BufferedWriter bufferedWriter = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, isAppend);
            OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamReader);

            bufferedWriter.write(content);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read lines in specified file
     * @param file file to be read
     * @param skipLine specify head lines length to be skipped
     * @return array of string, each representing a line
     */
    public static List<String> readLines(File file, int skipLine) {
        List<String> lines = new LinkedList<>();
        String line = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            for (int i = 0; i < skipLine; i++) bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) lines.add(line);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return lines;
    }

    /**
     * For deep copy, serialize the object as byte array
     * @param object object to be serialized
     * @return byte array
     */
    public static byte[] serialize(Solution object) {
        byte[] byteData = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            bos.close();
            byteData = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return byteData;
    }

    /**
     * For deep copy, deserialize the object
     * @param byteData serialized object
     * @return deserialized object
     */
    public static Solution deserialize(byte[] byteData) {
        Solution object = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            object = (Solution) new ObjectInputStream(bais).readObject();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return object;
    }

    public static void generateAggregationFileHeader(String filePath) {
        String header = String.format("%-15s%-15s%-15s%-15s%-15s%-15s%-15s\n", "Test Case", "Vehicle",
                "Objective", "Distance", "Delay", "Time", "Fail Num");
        Utils.writeToFile(header, filePath, false);
    }

    /**
     * Formulate an entry for result display
     */
    public static void generateAggregationFile(String filepath, String filename, double vehicle, double objective, double distance, double delay, long time, int fail) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s",filename)).append(String.format("%-15f", vehicle)).append(String.format("%-15f", objective))
                .append(String.format("%-15f", distance)).append(String.format("%-15f", delay))
                .append(String.format("%-15f", (double) (time / 1000.0))).append(String.format("%-15d", fail)).append("\n");
        Utils.writeToFile(sb.toString(), filepath, true);
    }

    public static void generateGapFileHeader(String filePath) {
        String header = String.format("%-15s%-15s%-15s\n", "Test Case", "Vehicle", "Objective");
        Utils.writeToFile(header, filePath, false);
    }

    public static void generateGapFile(String filepath, String filename, double vehicle, double objective) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s",filename)).append(String.format("%-15f", vehicle))
                .append(String.format("%-15f", objective)).append("\n");
        Utils.writeToFile(sb.toString(), filepath, true);
    }

    /**
     * Calculate the euclidean distance between two nodes using their (x, y) coordinates
     * @param n1 node1
     * @param n2 node2
     * @return distance
     */
    public static double calculateDistance(Node n1, Node n2) {
        return Math.sqrt((n1.getX() - n2.getX()) * (n1.getX() - n2.getX())
                + (n1.getY() - n2.getY()) * (n1.getY() - n2.getY()));
    }

    public static double findMaxDistance(double[][] distanceMatrix) {
        double maxDis = 0;
        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix[0].length; j++) {
                maxDis = Math.max(maxDis, distanceMatrix[i][j]);
            }
        }
        return maxDis;
    }

    /**
     * Check if two double are equal within tolerance
     * @param n1 node1
     * @param n2 node2
     * @return boolean isEqual
     */
    public static boolean doubleEqual(double n1, double n2) {
        return Math.abs(n1 - n2) < Math.pow(10, -5);
    }

    /**
     * @return system dependent separator
     */
    public static String separator() {
        return File.separator;
    }

    /** create unique identifier for file suffix **/
    public static String createSalt() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
