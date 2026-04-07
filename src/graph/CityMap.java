package graph;

import java.util.*;

public class CityMap {

    private Graph  map;
    private Map<String, Node> locationIndex; // quick lookup by name

    public CityMap() {
        this.map           = new Graph(false); // undirected — roads go both ways
        this.locationIndex = new HashMap<>();
    }

    // Add a location (intersection / landmark)
    public Node addLocation(String id, String name) {
        Node loc = new Node(id, name, 0);
        map.addNode(loc);
        locationIndex.put(id, loc);
        return loc;
    }

    // Add a road between two locations with a distance
    public void addRoad(String fromId, String toId, double distanceKm) {
        Node from = locationIndex.get(fromId);
        Node to   = locationIndex.get(toId);
        if (from == null || to == null) {
            System.out.println("Location not found!");
            return;
        }
        map.addEdge(from, to, distanceKm);
        System.out.println("Road added: " + from.getName()
                + " ↔ " + to.getName()
                + " (" + distanceKm + " km)");
    }

    // Navigate from one location to another — the GPS feature!
    public void navigate(String fromId, String toId) {
        Node from = locationIndex.get(fromId);
        Node to   = locationIndex.get(toId);

        if (from == null || to == null) {
            System.out.println("Invalid location ID.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║         GPS NAVIGATION STARTED       ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  From : " + padRight(from.getName(), 28) + "║");
        System.out.println("║  To   : " + padRight(to.getName(),   28) + "║");
        System.out.println("╚══════════════════════════════════════╝");

        // Run Dijkstra
        DijkstraAlgorithm.Result result = DijkstraAlgorithm.dijkstra(map, from);

        // Get and display the path
        List<Node> path = DijkstraAlgorithm.getPath(result, from, to);
        double totalDist = result.distances.get(to);

        if (path.isEmpty()) {
            System.out.println("No route found!");
            return;
        }

        System.out.println("\n🗺  ROUTE DIRECTIONS:");
        System.out.println("─".repeat(40));
        for (int i = 0; i < path.size() - 1; i++) {
            Node curr = path.get(i);
            Node next = path.get(i + 1);
            double segDist = getEdgeWeight(curr, next);
            System.out.printf("  Step %d: %-12s → %-12s  (%.1f km)%n",
                    i + 1, curr.getName(), next.getName(), segDist);
        }
        System.out.println("─".repeat(40));
        System.out.printf("  Total Distance: %.1f km%n", totalDist);
        System.out.printf("  Stops: %d%n", path.size() - 1);
        System.out.println("─".repeat(40));
    }

    // Helper — get weight of edge between two nodes
    private double getEdgeWeight(Node a, Node b) {
        for (Edge e : map.getNeighbors(a)) {
            if (e.getDestination().equals(b)) return e.getWeight();
        }
        return 0;
    }

    public Graph getMap() { return map; }

    public void printAllRoutes(String fromId) {
        Node from = locationIndex.get(fromId);
        if (from == null) return;
        DijkstraAlgorithm.Result result = DijkstraAlgorithm.dijkstra(map, from);
        DijkstraAlgorithm.printDistanceTable(result, from);
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }
}