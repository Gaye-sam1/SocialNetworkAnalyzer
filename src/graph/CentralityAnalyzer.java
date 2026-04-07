package graph;

import java.util.*;

public class CentralityAnalyzer {

    // ════════════════════════════════════════════════════════
    //  1. DEGREE CENTRALITY
    //     Score = number of direct connections / (n - 1)
    //     Higher = more directly connected to others
    // ════════════════════════════════════════════════════════
    public static Map<Node, Double> degreeCentrality(Graph graph) {
        Map<Node, Double> scores = new HashMap<>();
        Set<Node> allNodes = graph.getAllNodes();
        int n = allNodes.size();

        if (n <= 1) return scores;

        for (Node node : allNodes) {
            double degree = graph.getDegree(node);
            // Normalize: divide by max possible connections
            scores.put(node, degree / (n - 1));
        }
        return scores;
    }

    // ════════════════════════════════════════════════════════
    //  2. BETWEENNESS CENTRALITY
    //     How many shortest paths pass THROUGH this node?
    //     Uses Brandes' algorithm (BFS-based)
    // ════════════════════════════════════════════════════════
    public static Map<Node, Double> betweennessCentrality(Graph graph) {
        Set<Node>          allNodes = graph.getAllNodes();
        List<Node>         nodeList = new ArrayList<>(allNodes);
        Map<Node, Double>  between  = new HashMap<>();

        // Initialize all scores to 0
        for (Node n : nodeList) between.put(n, 0.0);

        // For every source node, run BFS to find all shortest paths
        for (Node source : nodeList) {

            Stack<Node>              stack    = new Stack<>();
            Map<Node, List<Node>>    pred     = new HashMap<>(); // predecessors
            Map<Node, Double>        sigma    = new HashMap<>(); // # shortest paths
            Map<Node, Double>        dist     = new HashMap<>(); // BFS distance
            Map<Node, Double>        delta    = new HashMap<>(); // dependency

            // Initialize
            for (Node n : nodeList) {
                pred.put(n, new ArrayList<>());
                sigma.put(n, 0.0);
                dist.put(n,  -1.0);
                delta.put(n,  0.0);
            }
            sigma.put(source, 1.0);
            dist.put(source,  0.0);

            Queue<Node> queue = new LinkedList<>();
            queue.add(source);

            // BFS phase — find shortest paths and count them
            while (!queue.isEmpty()) {
                Node v = queue.poll();
                stack.push(v);

                for (Edge edge : graph.getNeighbors(v)) {
                    Node w = edge.getDestination();

                    // First time visiting w?
                    if (dist.get(w) < 0) {
                        queue.add(w);
                        dist.put(w, dist.get(v) + 1);
                    }
                    // Is this a shortest path to w via v?
                    if (dist.get(w) == dist.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        pred.get(w).add(v);
                    }
                }
            }

            // Back-propagation phase — accumulate dependencies
            while (!stack.isEmpty()) {
                Node w = stack.pop();
                for (Node v : pred.get(w)) {
                    double coeff = (sigma.get(v) / sigma.get(w))
                            * (1.0 + delta.get(w));
                    delta.put(v, delta.get(v) + coeff);
                }
                if (!w.equals(source)) {
                    between.put(w, between.get(w) + delta.get(w));
                }
            }
        }

        // Normalize by (n-1)(n-2) for undirected graphs
        int n = nodeList.size();
        double norm = (n > 2) ? ((n - 1.0) * (n - 2.0) / 2.0) : 1.0;
        for (Node node : nodeList) {
            between.put(node, between.get(node) / norm);
        }
        return between;
    }

    // ════════════════════════════════════════════════════════
    //  3. CLOSENESS CENTRALITY
    //     Score = (n-1) / sum of distances to all other nodes
    //     Higher = you can reach everyone faster
    // ════════════════════════════════════════════════════════
    public static Map<Node, Double> closenessCentrality(Graph graph) {
        Set<Node>         allNodes = graph.getAllNodes();
        Map<Node, Double> scores   = new HashMap<>();
        int n = allNodes.size();

        for (Node source : allNodes) {
            // BFS to find distances to all reachable nodes
            Map<Node, Integer> dist    = new HashMap<>();
            Queue<Node>        queue   = new LinkedList<>();
            dist.put(source, 0);
            queue.add(source);

            while (!queue.isEmpty()) {
                Node current = queue.poll();
                for (Edge edge : graph.getNeighbors(current)) {
                    Node neighbor = edge.getDestination();
                    if (!dist.containsKey(neighbor)) {
                        dist.put(neighbor, dist.get(current) + 1);
                        queue.add(neighbor);
                    }
                }
            }

            // Sum of all distances from this source
            double sumDist = 0;
            for (int d : dist.values()) sumDist += d;

            // Closeness = (reachable nodes - 1) / sum of distances
            int reachable = dist.size();
            if (sumDist > 0 && reachable > 1) {
                // Normalize for disconnected graphs (Wasserman-Faust)
                double closeness = ((reachable - 1.0) / (n - 1.0))
                        * ((reachable - 1.0) / sumDist);
                scores.put(source, closeness);
            } else {
                scores.put(source, 0.0);
            }
        }
        return scores;
    }

    // ════════════════════════════════════════════════════════
    //  4. PAGERANK
    //     Iteratively computes influence based on neighbors'
    //     importance — the algorithm behind Google Search
    //     PR(v) = (1-d)/n + d × Σ PR(u)/deg(u)
    // ════════════════════════════════════════════════════════
    public static Map<Node, Double> pageRank(Graph graph,
                                             double dampingFactor,
                                             int    iterations) {
        Set<Node>         allNodes = graph.getAllNodes();
        int               n        = allNodes.size();
        Map<Node, Double> rank     = new HashMap<>();
        Map<Node, Double> newRank  = new HashMap<>();

        // Initialize everyone with equal rank
        for (Node node : allNodes) rank.put(node, 1.0 / n);

        double base = (1.0 - dampingFactor) / n;

        System.out.println("\n=== PageRank Iterations ===");

        // Iterate until convergence (or max iterations)
        for (int iter = 0; iter < iterations; iter++) {
            for (Node v : allNodes) newRank.put(v, base);

            for (Node u : allNodes) {
                List<Edge> neighbors = graph.getNeighbors(u);
                if (neighbors.isEmpty()) continue;

                double contribution = dampingFactor * rank.get(u) / neighbors.size();
                for (Edge edge : neighbors) {
                    Node dest = edge.getDestination();
                    newRank.put(dest, newRank.get(dest) + contribution);
                }
            }

            // Check convergence (total change < threshold)
            double totalChange = 0;
            for (Node node : allNodes) {
                totalChange += Math.abs(newRank.get(node) - rank.get(node));
            }
            rank = new HashMap<>(newRank);

            if (iter < 3 || iter == iterations - 1) {
                System.out.printf("  Iter %2d — total change: %.6f%n",
                        iter + 1, totalChange);
            }
            if (totalChange < 1e-6) {
                System.out.println("  Converged at iteration " + (iter + 1));
                break;
            }
        }
        return rank;
    }

    // ════════════════════════════════════════════════════════
    //  INFLUENCER RADAR — combine all 4 metrics into a
    //  single composite influence score + ranked leaderboard
    // ════════════════════════════════════════════════════════
    public static void printInfluencerRadar(Graph graph) {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║              INFLUENCER RADAR  📡                     ║");
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.printf("║  %-12s %-10s %-12s %-11s %-10s║%n",
                "Name","Degree","Betweeness","Closeness","PageRank");
        System.out.println("╠═══════════════════════════════════════════════════════╣");

        // Compute all metrics
        Map<Node, Double> deg    = degreeCentrality(graph);
        Map<Node, Double> bet    = betweennessCentrality(graph);
        Map<Node, Double> clo    = closenessCentrality(graph);
        Map<Node, Double> pr     = pageRank(graph, 0.85, 100);

        // Build composite score (simple average of normalized metrics)
        Map<Node, Double> composite = new HashMap<>();
        for (Node node : graph.getAllNodes()) {
            double score = (deg.getOrDefault(node, 0.0)
                    + bet.getOrDefault(node, 0.0)
                    + clo.getOrDefault(node, 0.0)
                    + pr.getOrDefault(node,  0.0)) / 4.0;
            composite.put(node, score);
        }

        // Sort by composite score descending
        List<Node> ranked = new ArrayList<>(graph.getAllNodes());
        ranked.sort((a, b) -> Double.compare(composite.get(b), composite.get(a)));

        // Print table
        for (Node node : ranked) {
            System.out.printf("║  %-12s %-10.3f %-12.3f %-11.3f %-10.4f║%n",
                    node.getName(),
                    deg.getOrDefault(node, 0.0),
                    bet.getOrDefault(node, 0.0),
                    clo.getOrDefault(node, 0.0),
                    pr.getOrDefault(node, 0.0));
        }

        System.out.println("╚═══════════════════════════════════════════════════════╝");

        // Crown the top influencer
        Node topInfluencer = ranked.get(0);
        System.out.println("\n👑 TOP INFLUENCER: " + topInfluencer.getName().toUpperCase()
                + "  (composite score: "
                + String.format("%.4f", composite.get(topInfluencer)) + ")");
        System.out.println("   Reason: Highest combined degree, bridge position,");
        System.out.println("           reach speed, and peer-influence score.");

        // Print top 3 podium
        System.out.println("\n🏆 PODIUM:");
        for (int i = 0; i < Math.min(3, ranked.size()); i++) {
            String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : "🥉";
            System.out.printf("   %s  %s  (%.4f)%n",
                    medal, ranked.get(i).getName(),
                    composite.get(ranked.get(i)));
        }
    }
}