package graph;

import java.util.*;

public class Graph {

    // Every node maps to its list of edges going OUT from it
    private Map<Node, List<Edge>> adjacencyList;
    private boolean isDirected; // true = directed, false = undirected

    // Constructor
    public Graph(boolean isDirected) {
        this.adjacencyList = new HashMap<>();
        this.isDirected    = isDirected;
    }

    // ─────────────────────────────────────────
    // ADD a node to the graph
    // ─────────────────────────────────────────
    public void addNode(Node node) {
        // Only add if not already present
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    // ─────────────────────────────────────────
    // REMOVE a node (and all its edges)
    // ─────────────────────────────────────────
    public void removeNode(Node node) {
        adjacencyList.remove(node);

        // Also remove all edges pointing TO this node
        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(e -> e.getDestination().equals(node));
        }
    }

    // ─────────────────────────────────────────
    // ADD an edge (friendship) between two nodes
    // ─────────────────────────────────────────
    public void addEdge(Node source, Node destination, double weight) {
        // Auto-add nodes if they don't exist yet
        addNode(source);
        addNode(destination);

        // Add edge from source → destination
        adjacencyList.get(source).add(new Edge(source, destination, weight));

        // If undirected, also add the reverse edge
        if (!isDirected) {
            adjacencyList.get(destination).add(new Edge(destination, source, weight));
        }
    }

    // ─────────────────────────────────────────
    // REMOVE an edge
    // ─────────────────────────────────────────
    public void removeEdge(Node source, Node destination) {
        List<Edge> edges = adjacencyList.get(source);
        if (edges != null) {
            edges.removeIf(e -> e.getDestination().equals(destination));
        }
        // If undirected, remove the reverse too
        if (!isDirected) {
            List<Edge> reverseEdges = adjacencyList.get(destination);
            if (reverseEdges != null) {
                reverseEdges.removeIf(e -> e.getDestination().equals(source));
            }
        }
    }

    // ─────────────────────────────────────────
    // GET all neighbors of a node
    // ─────────────────────────────────────────
    public List<Edge> getNeighbors(Node node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    // ─────────────────────────────────────────
    // GET all nodes in the graph
    // ─────────────────────────────────────────
    public Set<Node> getAllNodes() {
        return adjacencyList.keySet();
    }

    // ─────────────────────────────────────────
    // DEGREE of a node (number of connections)
    // ─────────────────────────────────────────
    public int getDegree(Node node) {
        List<Edge> edges = adjacencyList.get(node);
        return (edges == null) ? 0 : edges.size();
    }

    // ─────────────────────────────────────────
    // PRINT the entire adjacency list
    // ─────────────────────────────────────────
    public void printGraph() {
        System.out.println("\n=== Graph Adjacency List ===");
        for (Map.Entry<Node, List<Edge>> entry : adjacencyList.entrySet()) {
            System.out.print(entry.getKey().getName() + "  →  ");
            if (entry.getValue().isEmpty()) {
                System.out.print("(no connections)");
            } else {
                for (Edge e : entry.getValue()) {
                    System.out.print("[" + e.getDestination().getName()
                            + ", w=" + e.getWeight() + "]  ");
                }
            }
            System.out.println();
        }
        System.out.println("============================\n");
    }
}