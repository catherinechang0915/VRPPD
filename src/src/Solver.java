package src;

import ilog.concert.*;
import ilog.opl.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Solver {

    private final String WORKDIR = "C:\\Users\\cathe\\opl\\VRP";
    private IloOplFactory oplF;
    private IloOplModel opl;

    private int N;
    private int K;
    private int[] capacity;
    private Node[] nodes;
    private int[] X;
    private int[] Y;
    private int[] membership;

    private int[][][] x;
    private double[][] T;
    private double[][] Q;
    private double[][] DL;

    public Solver (String modelFilePath, String dataFilePath) {
        /**
         * @Param modelFilePath specifies the file name for .mod file (including .mod)
         * @Param dataFilePath specifies the file name for .dat file (including .dat)
         * @Param N is the number of pickup-delivery pairs
         * @Param K is the number of vehicles
         * Solve the model with OPL
         */
        readParam(WORKDIR + "\\" + dataFilePath);
        int status = 127;
        try {
            IloOplFactory.setDebugMode(true);
            oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplModelSource modelSource = oplF.createOplModelSource(WORKDIR + "\\" + modelFilePath);
            IloOplSettings settings = oplF.createOplSettings(errHandler);
            IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
            IloCplex cplex = oplF.createCplex();
            cplex.setOut(null);
            opl = oplF.createOplModel(def, cplex);
            IloOplDataSource dataSource = oplF.createOplDataSource(WORKDIR + "\\" + dataFilePath);
            opl.addDataSource(dataSource);
            opl.generate();
            if (cplex.solve()) {
                // print objective function value
                System.out.println("OBJECTIVE: " + opl.getCplex().getObjValue());
                setParam();
                opl.postProcess();
                // save to file
                File file =  new File("VRP.txt");
                opl.printSolution(new FileOutputStream(file));
            } else {
                System.out.println("No solution!");
            }
            oplF.end();
            status = 0;
        } catch (IloOplException ex) {
            System.err.println("### OPL exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 2;
        } catch (IloException ex) {
            System.err.println("### CONCERT exception: " + ex.getMessage());
            ex.printStackTrace();
            status = 3;
        } catch (Exception ex) {
            System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
            ex.printStackTrace();
            status = 4;
        }
        if (status != 0) {
            System.exit(status);
        }
    }

    private void readParam(String filePath) {
        File file = new File(filePath);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String text;
                Pattern patten;
                Matcher matcher;

                // set N
                text = bufferedReader.readLine();
                patten = Pattern.compile("n = (.*);");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    N = Integer.parseInt(matcher.group(1));
                } else {
                    throw new IllegalArgumentException("Wrong N.");
                }

                // set K
                text = bufferedReader.readLine();
                patten = Pattern.compile("K = (.*);");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    K = Integer.parseInt(matcher.group(1));
                } else {
                    throw new IllegalArgumentException("Wrong K.");
                }

                // set capacity
                capacity = new int[K];
                text = bufferedReader.readLine();
                patten = Pattern.compile("capacity = \\[(.*)];");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    String[] c = matcher.group(1).split(",");
                    for (int i = 0; i < K; i++) {
                        capacity[i] = Integer.parseInt(c[i]);
                    }
                } else {
                    throw new IllegalArgumentException("Wrong capacity.");
                }

                // set nodeInfo
                nodes = new Node[2 * N + 2];
                bufferedReader.readLine(); // line nodeInfo = [
                patten = Pattern.compile("<(.*)>");
                for (int i = 0; i < 2 * N + 2; i++) {
                    text = bufferedReader.readLine();
                    matcher = patten.matcher(text);
                    if (matcher.find()) {
                        String[] info = matcher.group(1).split("\\s+");
                        nodes[i] = new Node(Integer.parseInt(info[0]),
                                Integer.parseInt(info[1]),
                                Integer.parseInt(info[2]),
                                Integer.parseInt(info[3]));
                    } else {
                        throw new IllegalArgumentException("Wrong node info.");
                    }
                }
                bufferedReader.readLine(); // line ];

                // set X
                X = new int[2 * N + 2];
                text = bufferedReader.readLine();
                patten = Pattern.compile("X = \\[(.*)];");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                     String[] xs = matcher.group(1).split(",");
                     for (int i = 0; i < xs.length; i++) {
                         X[i] = Integer.parseInt(xs[i]);
                     }
                } else {
                    throw new IllegalArgumentException("Wrong X.");
                }

                // set Y
                Y = new int[2 * N + 2];
                text = bufferedReader.readLine();
                patten = Pattern.compile("Y = \\[(.*)];");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    String[] ys = matcher.group(1).split(",");
                    for (int i = 0; i < ys.length; i++) {
                        Y[i] = Integer.parseInt(ys[i]);
                    }
                } else {
                    throw new IllegalArgumentException("Wrong Y.");
                }

                // set membership
                membership = new int[2 * N + 2];
                text = bufferedReader.readLine();
                patten = Pattern.compile("membership = \\[(.*)]");
                matcher = patten.matcher(text);
                if (matcher.find()) {
                    String[] mem = matcher.group(1).split(",");
                    for (int i = 0; i < mem.length; i++) {
                        membership[i] = Integer.parseInt(mem[i]);
                    }
                } else {
                    throw new IllegalArgumentException("Wrong membership.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Incorrect format of .dat file. " + e.getMessage());
            }
        }
    }

    private void setParam() throws IloException {
        /**
         * This method reads the decision variables from the solved opl model
         */
        IloIntMap xMap = opl.getElement("x").asIntMap();
        IloNumMap TMap = opl.getElement("T").asNumMap();
        IloNumMap QMap = opl.getElement("Q").asNumMap();
        IloNumMap DLMap = opl.getElement("DL").asNumMap();
        IloTupleSet edgeSet = opl.getElement("edge").asTupleSet();

        x = new int[2 * N + 2][2 * N + 2][K];
        Iterator iter;
        for (int k = 1; k <= K; k++) {
            iter = edgeSet.iterator();
            while (iter.hasNext()) {
                IloTuple tuple = (IloTuple) iter.next();
                x[tuple.getIntValue(0)][tuple.getIntValue(1)][k - 1]
                        = xMap.getSub(tuple).get(k);
            }
        }

        T = new double[TMap.getSize()][K];
        for (int k = 1; k <= K; k++) {
            for (int i = 0; i < TMap.getSize(); i++) {
                T[i][k - 1] = TMap.getSub(i).get(k);
            }
        }

        Q = new double[QMap.getSize()][K];
        for (int k = 1; k <= K; k++) {
            for (int i = 0; i < QMap.getSize(); i++) {
                Q[i][k - 1] = QMap.getSub(i).get(k);
            }
        }

        DL = new double[DLMap.getSize()][K];
        for (int k = 1; k <= K; k++) {
            for (int i = 0; i < DLMap.getSize(); i++) {
                DL[i][k - 1] = DLMap.getSub(i).get(k);
            }
        }
    }

    public List<List<Integer>> constructRoute() {
        List<List<Integer>> routes = new LinkedList<>();
        for (int k = 0; k < K; k++) {
            routes.add(constructRoute(k));
        }
        return routes;
    }

    private List<Integer> constructRoute(int k) {
        /**
         * Find the consecutive path traversed by vehicle k
         * @Return A list of nodes start from 0 and end at 2n +
         */
        List<Integer> route = new LinkedList<>();
        int curr = 0;
        while (curr != 2 * N + 1) {
            route.add(curr);
            for (int i = 0; i < 2 * N + 2; i++) {
                if (x[curr][i][k] == 1) {
                    curr = i; // vehicle k passes arc (curr, i)
                    break;
                }
            }
        }
        route.add(2 * N + 1);
        return route;
    }

    public void displaySolution(String filename) {
        List<List<Integer>> routes = constructRoute();
        File file = new File(filename);
        BufferedWriter bufferedWriter = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamReader = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamReader);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < routes.size(); k++) {
            sb.append("Vehicle ").append(k).append("\n");
            List<Integer> route = routes.get(k);
            for (int i = 0; i < route.size(); i++) {
                int node = route.get(i);
                sb.append("\tNode ").append(node).append("\n");
                sb.append("\t\tLoad ").append(Q[node][k]).append("\n");
                sb.append("\t\tTime ").append(T[node][k]).append("\n");
                sb.append("\t\tDelay ").append(DL[node][k]).append("\n");
                sb.append("\t\t\tTime Window [").append(nodes[node].getTw1())
                        .append(", ")
                        .append(nodes[node].getTw2())
                        .append("]").append("\n");
                sb.append("\t\t\tLoad at node ").append(nodes[node].getQ()).append("\n");
                sb.append("\t\t\tService time at node ").append(nodes[node].getS()).append("\n");
                if (i != route.size() - 1) {
                    sb.append("\tCost between ").append(distance(node, route.get(i + 1))).append("\n");
                }
            }
            System.out.println(sb.toString());
            try {
                bufferedWriter.write(sb.toString());
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double distance(int from, int to) {
        return Math.sqrt((X[from] - X[to]) * (X[from] - X[to]) + (Y[from] - Y[to]) * (Y[from] - Y[to]));
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public int[] getCapacity() {
        return capacity;
    }

    public Node[] getNodes() {
        return nodes;
    }

    public int[] getX() {
        return X;
    }

    public int[] getY() {
        return Y;
    }

    public int[] getMembership() {
        return membership;
    }

    public double[][] getDL() {
        return DL;
    }

    public double[][] getQ() {
        return Q;
    }

    public double[][] getT() {
        return T;
    }

    public int[][][] getx() {
        return x;
    }
}
