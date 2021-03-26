package src;

import src.DataStructures.*;

import ilog.concert.*;
import ilog.opl.*;

import java.io.*;
import java.util.*;

public class OPLSolver extends Solver{

    private IloOplFactory oplF;
    private IloOplModel opl;
    private IloCplex cplex;

    private InputParam inputParam;
    private OutputParam outputParam;

    public OPLSolver () {
        this("src" + Utils.separator() + "VRP.mod");
    }

    /**
     * Set OPL solver with proper .mod and .dat file
     * @param modelFilePath .mod file path
     */
    public OPLSolver (String modelFilePath) {
        try {
            IloOplFactory.setDebugMode(true);
            oplF = new IloOplFactory();
            IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
            IloOplModelSource modelSource = oplF.createOplModelSource(modelFilePath);
            IloOplSettings settings = oplF.createOplSettings(errHandler);
            IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
            cplex = oplF.createCplex();
            cplex.setOut(null);
            opl = oplF.createOplModel(def, cplex);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Use OPL interface to solve the model
     * @param resFilePath output file path
     */
    @Override
    public void solve(String dataFilePath, String resFilePath) {
        this.inputParam = Utils.readParam(dataFilePath);
        IloOplDataSource dataSource = oplF.createOplDataSource(dataFilePath);
        opl.addDataSource(dataSource);
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
                writeToFile(resFilePath);
                oplF.end();
            } else {
                System.out.println("No solution!");
            }
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

    @Override
    public Solution getSolverSolution() {
        return solution;
    }

    /**
     * Save opl and calculated solution to file
     * @param filePath complete output filepath
     */
    private void writeToFile(String filePath) {
        try {
            // Write opl solution
            File file = Utils.createFile(filePath);
//            opl.printSolution(new FileOutputStream(file));

            double calcObjective = solution.getObjective(inputParam.getAlpha(), inputParam.getBeta());
            // Model validation, output related info to file (debug usage only)
            if (!Utils.doubleEqual(opl.getCplex().getObjValue(), calcObjective)) {
                String content = "OPL OBJECTIVE: " + opl.getCplex().getObjValue() + "\n"
                        + "SOL OBJECTIVE: " + calcObjective + "\n"
                        + solution.trace();
                Utils.writeToFile(content, filePath, false);
                solution.writeToFile(filePath, true);
            } else {
                solution.writeToFile(filePath, false);
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
        return new Solution(routes, timeElapsed, totalDist, totalPenalty);
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
        int[][][] x = outputParam.getx();
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

    private int getK() {
        return inputParam.getK();
    }

    private int getN() {
        return inputParam.getN();
    }

    private Vehicle[] getVehicles() {
        return inputParam.getVehicles();
    }

    private Node[] getNodes() {
        return inputParam.getNodes();
    }

    private double[][] getDistanceMatrix() {
        return inputParam.getDistanceMatrix();
    }

}
