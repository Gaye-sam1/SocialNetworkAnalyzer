package graph;

import java.util.*;

public class DFSTraversal {

    // ─────────────────────────────────────────────────────
    // DFS from a start node — goes deep before backtracking
    // ─────────────────────────────────────────────────────
    public static List<Node> dfs(Graph graph, Node start) {
        List<Node> visitOrder = new ArrayList<>();
        Set<Node>  visited    = new HashSet<>();

        System.out.println("\n=== DFS Traversal from: " + start.getName() + " ===");
        dfsRecursive(graph, start, visited, visitOrder, 0);
        System.out.println("DFS order: " + nodeNames(visitOrder));
        return visitOrder;
    }

    // Internal recursive helper
    private static void dfsRecursive(Graph graph, Node current,
                                     Set<Node> visited,
                                     List<Node> visitOrder,
                                     int depth) {
        visited.add(current);
        visitOrder.add(current);

        // Indent to show depth visually in console
        String indent = "  ".repeat(depth);
        System.out.println(indent + "↓ Visiting: " + current.getName()
                + " (depth=" + depth + ")");

        for (Edge edge : graph.getNeighbors(current)) {
            Node neighbor = edge.getDestination();
            if (!visited.contains(neighbor)) {
                dfsRecursive(graph, neighbor, visited, visitOrder, depth + 1);
            }
        }
        System.out.println(indent + "↑ Backtrack from: " + current.getName());
    }

    // ─────────────────────────────────────────────────────
    // CYCLE DETECTION using DFS
    // Returns true if the graph has a cycle
    // ─────────────────────────────────────────────────────
    public static boolean hasCycle(Graph graph) {
        Set<Node>  visited  = new HashSet<>();
        Set<Node>  recStack = new HashSet<>();  // nodes in current DFS path

        for (Node node : graph.getAllNodes()) {
            if (!visited.contains(node)) {
                if (dfsHasCycle(graph, node, visited, recStack, null)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean dfsHasCycle(Graph graph, Node current,
                                       Set<Node> visited,
                                       Set<Node> recStack,
                                       Node parent) {
        visited.add(current);
        recStack.add(current);

        for (Edge edge : graph.getNeighbors(current)) {
            Node neighbor = edge.getDestination();

            // Skip the parent node (undirected graphs have back-edges to parent)
            if (neighbor.equals(parent)) continue;

            if (!visited.contains(neighbor)) {
                if (dfsHasCycle(graph, neighbor, visited, recStack, current))
                    return true;
            } else if (recStack.contains(neighbor)) {
                System.out.println("Cycle detected involving: "
                        + current.getName() + " → " + neighbor.getName());
                return true;
            }
        }
        recStack.remove(current);
        return false;
    }

    // ─────────────────────────────────────────────────────
    // CONNECTED COMPONENTS — groups of connected people
    // ─────────────────────────────────────────────────────
    public static List<List<Node>> findConnectedComponents(Graph graph) {
        Set<Node>         visited    = new HashSet<>();
        List<List<Node>>  components = new ArrayList<>();

        for (Node node : graph.getAllNodes()) {
            if (!visited.contains(node)) {
                // Found an unvisited node — start a new component
                List<Node> component = new ArrayList<>();
                dfsRecursive(graph, node, visited, component, 0);
                components.add(component);
            }
        }
        return components;
    }

    private static String nodeNames(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodes.get(i).getName());
            if (i < nodes.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }
}