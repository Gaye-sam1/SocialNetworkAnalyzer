package graph;

import java.util.*;

public class DijkstraAlgorithm {

    // Result object — holds distances AND the full path to any node
    public static class Result {
        public Map<Node, Double>  distances;  // shortest dist from source to every node
        public Map<Node, Node>    previous;   // previous node on shortest path

        public Result(Map<Node, Double> distances, Map<Node, Node> previous) {
            this.distances = distances;
            this.previous  = previous;
        }
    }

    // ─────────────────────────────────────────────────────────
    // Run Dijkstra from a source node
    // Returns a Result containing dist[] and prev[] for ALL nodes
    // ─────────────────────────────────────────────────────────
    public static Result dijkstra(Graph graph, Node source) {
        Map<Node, Double>  dist     = new HashMap<>();
        Map<Node, Node>    prev     = new HashMap<>();
        Set<Node>          settled  = new HashSet<>();

        // Priority queue — orders nodes by their current known distance
        // PriorityQueue<Node> sorted by dist[node] ascending
        PriorityQueue<Node> pq = new PriorityQueue<>(
                Comparator.comparingDouble(n -> dist.getOrDefault(n, Double.MAX_VALUE))
        );

        // Step 1: Initialize all distances to infinity
        for (Node node : graph.getAllNodes()) {
            dist.put(node, Double.MAX_VALUE);
            prev.put(node, null);
        }

        // Step 2: Distance to source is 0
        dist.put(source, 0.0);
        pq.add(source);

        System.out.println("\n=== Dijkstra from: " + source.getName() + " ===");
        System.out.printf("%-12s %-10s %-15s%n", "Node", "Distance", "Via");
        System.out.println("-".repeat(38));

        // Step 3: Main loop
        while (!pq.isEmpty()) {
            // Pick the unvisited node with the smallest known distance
            Node u = pq.poll();

            if (settled.contains(u)) continue; // skip if already finalized
            settled.add(u);

            System.out.printf("Finalized: %-10s dist=%-8.1f via=%s%n",
                    u.getName(),
                    dist.get(u),
                    prev.get(u) == null ? "START" : prev.get(u).getName()
            );

            // Step 4: Relax all neighbors
            for (Edge edge : graph.getNeighbors(u)) {
                Node   v         = edge.getDestination();
                double newDist   = dist.get(u) + edge.getWeight();

                if (!settled.contains(v) && newDist < dist.get(v)) {
                    // Found a shorter path to v!
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(v); // re-add with updated priority
                    System.out.printf("  Relaxed: %-10s new dist=%.1f%n",
                            v.getName(), newDist);
                }
            }
        }
        return new Result(dist, prev);
    }

    // ─────────────────────────────────────────────────────────
    // Get the shortest path from source to a specific target
    // Uses the prev[] map from dijkstra() result
    // ─────────────────────────────────────────────────────────
    public static List<Node> getPath(Result result, Node source, Node target) {
        LinkedList<Node> path = new LinkedList<>();
        Node current = target;

        // Walk backwards from target to source
        while (current != null) {
            path.addFirst(current);
            current = result.previous.get(current);
        }

        // If path doesn't start at source, no path exists
        if (path.isEmpty() || !path.getFirst().equals(source)) {
            System.out.println("No path from " + source.getName()
                    + " to " + target.getName());
            return new ArrayList<>();
        }
        return path;
    }

    // ─────────────────────────────────────────────────────────
    // Print a full distance table (like a GPS summary screen)
    // ─────────────────────────────────────────────────────────
    public static void printDistanceTable(Result result, Node source) {
        System.out.println("\n=== Distance Table from " + source.getName() + " ===");
        System.out.printf("%-15s %-12s %-20s%n", "Destination", "Distance", "Shortest Path");
        System.out.println("-".repeat(50));

        for (Map.Entry<Node, Double> entry : result.distances.entrySet()) {
            Node   dest     = entry.getKey();
            double distance = entry.getValue();
            String distStr  = (distance == Double.MAX_VALUE) ? "UNREACHABLE" : distance + " km";

            // Build path string
            List<Node> path = getPath(result, source, dest);
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                pathStr.append(path.get(i).getName());
                if (i < path.size() - 1) pathStr.append("→");
            }
            System.out.printf("%-15s %-12s %-20s%n",
                    dest.getName(), distStr, pathStr);
        }
    }
}