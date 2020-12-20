package src;

import src.DataStructures.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Read from .dat file
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
                            Integer.parseInt(info[1]),
                            Integer.parseInt(info[2]),
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Incorrect format of .dat file. " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

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
                kMap.put(text[0] + ".txt", Integer.parseInt(text[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return kMap;
    }



    public static InputParam readDataFromFile(int n, int K, double memberPercent, String filePath) {

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
                int membership = Math.random() < memberPercent ? 1 : 0;
                unsortedNodes.add(new Node(Integer.parseInt(text[3]),
                        Integer.parseInt(text[4]),
                        Integer.parseInt(text[5]),
                        Integer.parseInt(text[6]),
                        Integer.parseInt(text[1]),
                        Integer.parseInt(text[2]),
                        membership));
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

            // internal pd-pairs
            int index = 1;
            for (int p : pdPair.keySet()) {
                nodes[index + N] = unsortedNodes.get(pdPair.get(p));
                nodes[index++] = unsortedNodes.get(p);
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
     *
     * @param inputParam generated input data
     * @param filePath
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
        try {
            bufferedWriter.write(sb.toString());
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return a list of string names in the directory, return null if dir not exist
     * @param dirPath
     * @return filenames in dirPath
     */
    public static List<String> fileList(String dirPath) {
        File folder = new File(dirPath);
        if (!folder.exists() || !folder.isDirectory()) return null;

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) return null;

        List<String> files = new LinkedList<>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                files.add(listOfFiles[i].getName());
            }
        }
        return files;
    }

    /**
     * create a directory if not exists
     * @param dirPath
     */
    public static void createDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }

    /**
     *
     * @param content String content to be saved
     * @param filename filepath
     */
    public static void writeToFile(String content, String filename) {
        File file = new File(filename);
        if (file.exists()) file.delete();
        BufferedWriter bufferedWriter = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamReader);

            bufferedWriter.write(content);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
