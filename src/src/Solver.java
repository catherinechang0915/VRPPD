package src;

import src.DataStructures.*;

import ilog.concert.*;
import ilog.opl.*;

import java.io.*;
import java.util.*;

public class Solver {

    private static final String WORKDIR = "src\\";
    private IloOplFactory oplF;
    private IloOplModel opl;
    private IloCplex cplex;

    InputParam inputParam;
    OutputParam outputParam;
    Solution solution;

    public Solver (InputParam inputParam, String dataDir, String dataFilename) {
        this(inputParam, WORKDIR, "VRP", dataDir, dataFilename);
    }

    /**
     * Set OPL solver with proper .mod and .dat file
     * @param inputParam parameter object read from the .dat file
     * @param modelDir .mod directory path
     * @param modelFilename .mod filename
     * @param dataDir .dat directory path
     * @param dataFilename .dat filename
     */
    public Solver (InputParam inputParam, String modelDir, String modelFilename,
                   String dataDir, String dataFilename) {
        this.inputParam = inputParam;
        try {
            IloOplFactory.setDebugMode(true);
            oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplModelSource modelSource = oplF.createOplModelSource(modelDir + modelFilename + ".mod");
            IloOplSettings settings = oplF.createOplSettings(errHandler);
            IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
            cplex = oplF.createCplex();
            cplex.setOut(null);
            opl = oplF.createOplModel(def, cplex);
            IloOplDataSource dataSource = oplF.createOplDataSource(dataDir + dataFilename + ".dat");
            opl.addDataSource(dataSource);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void solve() {
        int status = -1;
        try {
            long startTime = System.currentTimeMillis();
            opl.generate();
            if (cplex.solve()) {
                long elasped = System.currentTimeMillis() - startTime;
                outputParam = setParam();
                opl.postProcess();
                // calculate detailed routes solution
                solution = constructSolution(elasped);

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

    /**
     * Compare the value of objective by OPL and constructed solution calculation, save the result to file
     * @param resDir Dir path to save the result
     * @param dataFilename result file name
     */
    public void validateAndSaveToFile(String resDir, String dataFilename) {
        try {
            // save to file
            File file = new File(resDir + dataFilename + ".txt");
            opl.printSolution(new FileOutputStream(file));

            Utils.writeToFile(solution.toString(), resDir + dataFilename + "_calculated.txt");

            // Model validation, output related info to file (debug usage only)
            if (opl.getCplex().getObjValue() != solution.getObjective()) {
                String content = "OPL OBJECTIVE: " + opl.getCplex().getObjValue() + "\n"
                        + "SOL OBJECTIVE: " + solution.getObjective() + "\n"
                        + getTrace();
                Utils.writeToFile(content, resDir + dataFilename + "_diff.txt");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * This method reads the decision variables from the solved opl model
     */
    private OutputParam setParam() {
        try {
            int N = getN();
            int K = getK();

            IloIntMap xMap = opl.getElement("x").asIntMap();
            IloNumMap TMap = opl.getElement("T").asNumMap();
            IloNumMap QMap = opl.getElement("Q").asNumMap();
            IloNumMap DLMap = opl.getElement("DL").asNumMap();
            IloTupleSet edgeSet = opl.getElement("edge").asTupleSet();

            int[][][] x = new int[2 * N + 2][2 * N + 2][K];
            Iterator iter;
            for (int k = 1; k <= K; k++) {
                iter = edgeSet.iterator();
                while (iter.hasNext()) {
                    IloTuple tuple = (IloTuple) iter.next();
                    x[tuple.getIntValue(0)][tuple.getIntValue(1)][k - 1]
                            = xMap.getSub(tuple).get(k);
                }
            }

            double[][] T = new double[TMap.getSize()][K];
            for (int k = 1; k <= K; k++) {
                for (int i = 0; i < TMap.getSize(); i++) {
                    T[i][k - 1] = TMap.getSub(i).get(k);
                }
            }

            double[][] Q = new double[QMap.getSize()][K];
            for (int k = 1; k <= K; k++) {
                for (int i = 0; i < QMap.getSize(); i++) {
                    Q[i][k - 1] = QMap.getSub(i).get(k);
                }
            }

            double[][] DL = new double[DLMap.getSize()][K];
            for (int k = 1; k <= K; k++) {
                for (int i = 0; i < DLMap.getSize(); i++) {
                    DL[i][k - 1] = DLMap.getSub(i).get(k);
                }
            }
            return new OutputParam(x, T, Q, DL);
        } catch (ilog.concert.IloException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     *
     * @param timeElapsed Elapsed Time for OPL solver
     * @return set of routes along with separate & total objective
     */
    private Solution constructSolution(long timeElapsed) {
        Route tempRoute = null;
        double totalDist = 0;
        double totalPenalty = 0;
        List<Route> routes = new LinkedList<>();
        for (int k = 0; k < getK(); k++) {
            tempRoute = constructRoute(k);
            totalDist += tempRoute.getDist();
            totalPenalty += tempRoute.getPenalty();
            routes.add(tempRoute);
        }
        double objective = getAlpha() * totalDist + getBeta() * totalPenalty;
        return new Solution(routes, timeElapsed, totalDist, totalPenalty, objective);
    }

    /**
     * Find the consecutive path traversed by vehicle k,
     * calculate and update the node info along the route
     * @param k vehicle specified
     * @return Route consists of a list of nodes start from 0 and end at 2n + 1,
     * with traversed distance and delay penalty calculated on the route
     */
    private Route constructRoute(int k) {
        Vehicle vehicle = getVehicles()[k];
        Node[] nodes = getNodes();
        int N = getN();
        int[][][] x = getx();
        List<Node> route = new LinkedList<>();

        double dist = 0;
        double penalty = 0;
        double time = 0;
        double load = 0;
        double tempDL = -1;
        int curr = 0;
        while (true) {

            nodes[curr].setQ(load);
            nodes[curr].setT(time);
            tempDL = Math.max(0, time - nodes[curr].getTw2());
            nodes[curr].setDL(tempDL);
            penalty += tempDL;

            route.add(nodes[curr]);

            if (curr == 2 * N + 1) break;

            for (int i = 0; i < 2 * N + 2; i++) {
                if (x[curr][i][k] == 1) {
                    time = Math.max(getDistanceMatrix()[curr][i] + nodes[curr].gets() + time, nodes[i].getTw1());
                    dist += getDistanceMatrix()[curr][i];
                    load += nodes[i].getq();
                    curr = i; // vehicle k passes arc (curr, i)
                    break;
                }
            }
        }
        return new Route(route, vehicle, dist, penalty);
    }

    /**
     *
     * @return the trace of routes, only a list of node indexes without detailed information
     */
    public String getTrace() {
        return solution.trace();
    }


    public Solution getSolution() {
        return solution;
    }

    public int getK() {
        return inputParam.getK();
    }

    public int getN() {
        return inputParam.getN();
    }

    public Vehicle[] getVehicles() {
        return inputParam.getVehicles();
    }

    public Node[] getNodes() {
        return inputParam.getNodes();
    }

    public double[][] getDistanceMatrix() {
        return inputParam.getDistanceMatrix();
    }

    public double getAlpha() {
        return inputParam.getAlpha();
    }

    public double getBeta() {
        return inputParam.getBeta();
    }

    public double[][] getDL() {
        return outputParam.getDL();
    }

    public double[][] getQ() {
        return outputParam.getQ();
    }

    public double[][] getT() {
        return outputParam.getT();
    }

    public int[][][] getx() {
        return outputParam.getx();
    }
}
