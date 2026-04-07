package graph;

import java.util.*;

public class CommunityDetector {

    // ════════════════════════════════════════════════════
    //  METHOD 1 — UNION-FIND COMMUNITY DETECTION
    //  Process every edge → union the two endpoints
    //  Connected components = natural communities
    // ════════════════════════════════════════════════════
    public static Map<Node, List<Node>> detectByUnionFind(Graph graph) {
        System.out.println("\n=== Community Detection: Union-Find ===");

        UnionFind uf = new UnionFind(graph.getAllNodes());
        int step = 1;

        // Process every edge — union the endpoints
        for (Node node : graph.getAllNodes()) {
            for (Edge edge : graph.getNeighbors(node)) {
                Node a = edge.getSource();
                Node b = edge.getDestination();
                boolean merged = uf.union(a, b);
                if (merged) {
                    System.out.printf("  Step %2d: union(%s, %s) → merged into one community%n",
                            step++, a.getName(), b.getName());
                } else {
                    System.out.printf("  Step %2d: union(%s, %s) → already in same community%n",
                            step++, a.getName(), b.getName());
                }
            }
        }

        Map<Node, List<Node>> communities = uf.getCommunities();
        printCommunities(communities, "Union-Find");
        return communities;
    }

    // ════════════════════════════════════════════════════
    //  METHOD 2 — GREEDY GRAPH COLORING
    //  Assign the smallest available color to each node
    //  such that no two adjacent nodes share a color.
    //  The colors become the community labels.
    // ════════════════════════════════════════════════════
    public static Map<Node, Integer> detectByGraphColoring(Graph graph) {
        System.out.println("\n=== Community Detection: Graph Coloring ===");

        Map<Node, Integer> colors      = new HashMap<>();
        List<Node>         nodeList    = new ArrayList<>(graph.getAllNodes());
        String[]           colorNames  = {"Red","Blue","Green","Yellow",
                "Purple","Orange","Pink","Teal"};

        for (Node node : nodeList) {
            // Collect colors already used by neighbors
            Set<Integer> usedColors = new HashSet<>();
            for (Edge edge : graph.getNeighbors(node)) {
                Node neighbor = edge.getDestination();
                if (colors.containsKey(neighbor)) {
                    usedColors.add(colors.get(neighbor));
                }
            }

            // Find the smallest non-conflicting color
            int color = 0;
            while (usedColors.contains(color)) color++;

            colors.put(node, color);
            String name = (color < colorNames.length) ? colorNames[color] : "Color-"+color;
            System.out.printf("  Assigned %-10s → %-8s (color %d)%n",
                    node.getName(), name, color);
        }

        // Group nodes by color → these are communities
        Map<Integer, List<Node>> colorGroups = new HashMap<>();
        for (Map.Entry<Node, Integer> e : colors.entrySet()) {
            colorGroups.computeIfAbsent(e.getValue(),
                    k -> new ArrayList<>()).add(e.getKey());
        }

        int chromaticNumber = colorGroups.size();
        System.out.println("\n  Chromatic number χ(G) = " + chromaticNumber);
        System.out.println("  (Minimum colors needed so no neighbors share a color)");

        // Print color groups
        System.out.println("\n  Color groups (potential communities):");
        for (Map.Entry<Integer, List<Node>> entry : colorGroups.entrySet()) {
            String cname = (entry.getKey() < colorNames.length)
                    ? colorNames[entry.getKey()] : "Color-"+entry.getKey();
            System.out.print("  [" + cname + "] ");
            entry.getValue().forEach(n -> System.out.print(n.getName() + " "));
            System.out.println();
        }
        return colors;
    }

    // ════════════════════════════════════════════════════
    //  METHOD 3 — EDGE BETWEENNESS / BRIDGE REMOVAL
    //  Girvan-Newman inspired:
    //  1. Find all bridge-like edges (high betweenness)
    //  2. Remove them iteratively
    //  3. What's left are tight communities
    // ════════════════════════════════════════════════════
    public static List<List<Node>> detectByEdgeBetweenness(
            Graph graph, int targetCommunities) {
        System.out.println("\n=== Community Detection: Edge Betweenness ===");
        System.out.println("  Target: " + targetCommunities + " communities");

        // Work on a copy of edge data so we don't destroy the real graph
        // We'll track which edges are "removed" via a blacklist
        Set<String> removedEdges = new HashSet<>();
        List<List<Node>> communities;

        int iteration = 0;

        while (true) {
            // Find connected components (current communities)
            communities = getConnectedComponents(graph, removedEdges);
            if (communities.size() >= targetCommunities) break;

            // Find the edge with highest betweenness (the bridge)
            String bridgeEdge = findHighestBetweennessEdge(graph, removedEdges);
            if (bridgeEdge == null) break;

            removedEdges.add(bridgeEdge);
            System.out.printf("  Iteration %d: removed bridge edge [%s]%n",
                    ++iteration, bridgeEdge);
        }

        System.out.println("\n  Communities after bridge removal:");
        for (int i = 0; i < communities.size(); i++) {
            System.out.print("  Community " + (i+1) + ": ");
            communities.get(i).forEach(n -> System.out.print(n.getName() + " "));
            System.out.println();
        }
        return communities;
    }

    // ─────────────────────────────────────────────────────
    // Helper — find edge with the most shortest paths through it
    // ─────────────────────────────────────────────────────
    private static String findHighestBetweennessEdge(
            Graph graph, Set<String> removed) {
        Map<String, Double> edgeBet = new HashMap<>();
        List<Node> nodeList = new ArrayList<>(graph.getAllNodes());

        // For every source, BFS and count paths through each edge
        for (Node source : nodeList) {
            Stack<Node>           stack  = new Stack<>();
            Map<Node,List<Node>>  pred   = new HashMap<>();
            Map<Node,Double>      sigma  = new HashMap<>();
            Map<Node,Integer>     dist   = new HashMap<>();
            Map<Node,Double>      delta  = new HashMap<>();

            for (Node n : nodeList) {
                pred.put(n, new ArrayList<>());
                sigma.put(n, 0.0);
                dist.put(n, -1);
                delta.put(n, 0.0);
            }
            sigma.put(source, 1.0);
            dist.put(source, 0);

            Queue<Node> queue = new LinkedList<>();
            queue.add(source);

            while (!queue.isEmpty()) {
                Node v = queue.poll();
                stack.push(v);
                for (Edge edge : graph.getNeighbors(v)) {
                    String eKey = edgeKey(v, edge.getDestination());
                    if (removed.contains(eKey)) continue;
                    Node w = edge.getDestination();
                    if (dist.get(w) < 0) {
                        queue.add(w);
                        dist.put(w, dist.get(v) + 1);
                    }
                    if (dist.get(w) == dist.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        pred.get(w).add(v);
                    }
                }
            }

            while (!stack.isEmpty()) {
                Node w = stack.pop();
                for (Node v : pred.get(w)) {
                    double c = (sigma.get(v) / sigma.get(w)) * (1 + delta.get(w));
                    delta.put(v, delta.get(v) + c);
                    String key = edgeKey(v, w);
                    if (!removed.contains(key)) {
                        edgeBet.merge(key, c, Double::sum);
                    }
                }
            }
        }

        // Return edge with max betweenness
        return edgeBet.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // ─────────────────────────────────────────────────────
    // Helper — find connected components ignoring removed edges
    // ─────────────────────────────────────────────────────
    private static List<List<Node>> getConnectedComponents(
            Graph graph, Set<String> removed) {
        Set<Node>        visited    = new HashSet<>();
        List<List<Node>> components = new ArrayList<>();

        for (Node start : graph.getAllNodes()) {
            if (visited.contains(start)) continue;
            List<Node>  comp  = new ArrayList<>();
            Queue<Node> queue = new LinkedList<>();
            queue.add(start);
            visited.add(start);
            while (!queue.isEmpty()) {
                Node curr = queue.poll();
                comp.add(curr);
                for (Edge e : graph.getNeighbors(curr)) {
                    String key = edgeKey(curr, e.getDestination());
                    if (!removed.contains(key)
                            && !visited.contains(e.getDestination())) {
                        visited.add(e.getDestination());
                        queue.add(e.getDestination());
                    }
                }
            }
            components.add(comp);
        }
        return components;
    }

    // Canonical edge key — always smaller ID first so A-B == B-A
    private static String edgeKey(Node a, Node b) {
        String ia = a.getId(), ib = b.getId();
        return (ia.compareTo(ib) < 0) ? ia + "-" + ib : ib + "-" + ia;
    }

    // ─────────────────────────────────────────────────────
    // Pretty-print any community map
    // ─────────────────────────────────────────────────────
    private static void printCommunities(
            Map<Node, List<Node>> communities, String method) {
        System.out.println("\n  Communities detected by " + method + ":");
        System.out.println("  Total communities: " + communities.size());
        int i = 1;
        for (List<Node> group : communities.values()) {
            System.out.print("  Community " + i++ + " [size=" + group.size() + "]: ");
            group.forEach(n -> System.out.print(n.getName() + " "));
            System.out.println();
        }
    }

    // ════════════════════════════════════════════════════
    //  FULL COMMUNITY REPORT — runs all 3 methods and
    //  prints a clean summary with analysis
    // ════════════════════════════════════════════════════
    public static void fullReport(Graph graph) {
        System.out.println("""
        ╔════════════════════════════════════════════════╗
        ║         COMMUNITY DETECTION REPORT  🏘          ║
        ╚════════════════════════════════════════════════╝""");

        // Method 1
        Map<Node, List<Node>> ufResult  = detectByUnionFind(graph);

        // Method 2
        Map<Node, Integer>    colResult = detectByGraphColoring(graph);

        // Method 3
        List<List<Node>> gbResult = detectByEdgeBetweenness(graph, 3);

        // Summary
        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║                   SUMMARY                      ║");
        System.out.println("╠════════════════════════════════════════════════╣");
        System.out.printf("║  Union-Find communities    : %-5d              ║%n",
                ufResult.size());
        System.out.printf("║  Graph coloring χ(G)       : %-5d              ║%n",
                colResult.values().stream().mapToInt(x->x).max().orElse(0) + 1);
        System.out.printf("║  Edge-betweenness groups   : %-5d              ║%n",
                gbResult.size());
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}