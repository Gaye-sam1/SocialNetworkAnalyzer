package graph;

import java.util.*;

public class BFSTraversal {
    // ─────────────────────────────────────────────────────
    // BFS from a start node — visits all reachable nodes
    // Returns nodes in the order they were visited
    // ─────────────────────────────────────────────────────
    public static List<Node> bfs(Graph graph, Node start) {
        List<Node>  visitOrder = new ArrayList<>();
        Set<Node>   visited    = new HashSet<>();
        Queue<Node> queue      = new LinkedList<>();

        // Seed the queue with the start node
        queue.add(start);
        visited.add(start);

        System.out.println("\n=== BFS Traversal from: " + start.getName() + " ===");

        while (!queue.isEmpty()) {
            Node current = queue.poll();   // dequeue front
            visitOrder.add(current);
            System.out.print("Visiting: " + current.getName() + "  →  Neighbors: ");

            // Explore all neighbors
            for (Edge edge : graph.getNeighbors(current)) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);   // enqueue for later
                    System.out.print(neighbor.getName() + " ");
                }
            }
            System.out.println();
        }
        System.out.println("BFS order: " + nodeNames(visitOrder));
        return visitOrder;
    }

    // ─────────────────────────────────────────────────────
    // BFS PATH FINDER — find shortest path between two nodes
    // Returns the path as a list of nodes, or empty if none
    // ─────────────────────────────────────────────────────
    public static List<Node> findShortestPath(Graph graph, Node start, Node end) {
        // parent map: tracks how we reached each node
        Map<Node, Node> parent  = new HashMap<>();
        Set<Node>       visited = new HashSet<>();
        Queue<Node>     queue   = new LinkedList<>();

        queue.add(start);
        visited.add(start);
        parent.put(start, null);  // start has no parent

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            // Found the destination — reconstruct the path
            if (current.equals(end)) {
                return reconstructPath(parent, start, end);
            }

            for (Edge edge : graph.getNeighbors(current)) {
                Node neighbor = edge.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // No path found
        System.out.println("No path found between "
                + start.getName() + " and " + end.getName());
        return new ArrayList<>();
    }

    // ─────────────────────────────────────────────────────
    // Rebuilds path from end → start using the parent map
    // ─────────────────────────────────────────────────────
    private static List<Node> reconstructPath(Map<Node,Node> parent,
                                              Node start, Node end) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = end;

        // Walk backwards from end to start
        while (current != null) {
            path.addFirst(current);  // prepend so path is start→end
            current = parent.get(current);
        }
        return path;
    }

    // Helper: extract names for printing
    private static String nodeNames(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodes.get(i).getName());
            if (i < nodes.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }
}