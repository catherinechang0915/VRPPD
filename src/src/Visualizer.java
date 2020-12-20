package src;

import src.DataStructures.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.util.List;

public class Visualizer{

    public Visualizer(Solver solver) {
        JFrame frame = new MyFrame(solver);
        frame.setVisible(true);
    }

    public class MyFrame extends JFrame {

        Solver solver;
        Solution solution;

        public MyFrame(Solver solver) {
            this.solver = solver;
            this.solution = solver.getSolution();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setTitle("Routes Visualization");
            setSize(1000,1000);
            setLocation(new Point(0,0));
            setVisible(true);
            setBackground(Color.black);
        }

        public void paint(Graphics g) {
            List<Route> routes = solution.getRoutes();
            for (int k = 0; k < routes.size(); k++) {
                paintRoute(g, routes.get(k),
                        new Color((int)(Math.random()*256), (int)(Math.random()*256), (int)(Math.random()*256)),
                        8);
            }
        }

        /**
         * Plot one route by connecting nodes in List<Integer> route, with random color
         * @param g graphics
         * @param route list of nodes on one route
         * @param color random color
         * @param scale enlarge/shrink the distance between nodes
         */
        private void paintRoute(Graphics g, Route route, Color color, int scale) {
            List<Node> nodes = route.getNodes();
            g.setColor(color);
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                g.fillRect(node.getX()*scale - 1, node.getY()*scale - 1, 2, 2);
                if (i != nodes.size() - 1) {
                    g.drawLine(node.getX()*scale, node.getY()*scale,
                            nodes.get(i+1).getX()*scale, nodes.get(i+1).getY()*scale);
                }
            }
        }
    }
}
