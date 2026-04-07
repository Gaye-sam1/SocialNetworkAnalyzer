package graph;

import java.util.*;

public class UnionFind {

    // parent[x] = the representative (root) of x's set
    private Map<Node, Node>    parent;
    // rank[x]   = tree height — keeps the structure flat
    private Map<Node, Integer> rank;

    public UnionFind(Set<Node> nodes) {
        parent = new HashMap<>();
        rank   = new HashMap<>();
        // Initially every node is its own community (singleton set)
        for (Node node : nodes) {
            parent.put(node, node);  // x is its own parent
            rank.put(node, 0);
        }
    }

    // ─────────────────────────────────────────────────────
    // FIND — returns the root representative of node's set
    // Path compression: flattens the tree on every lookup
    // ─────────────────────────────────────────────────────
    public Node find(Node node) {
        if (!parent.get(node).equals(node)) {
            // Path compression: point directly to root
            parent.put(node, find(parent.get(node)));
        }
        return parent.get(node);
    }

    // ─────────────────────────────────────────────────────
    // UNION — merge the sets of two nodes
    // Union by rank: attach smaller tree under larger
    // ─────────────────────────────────────────────────────
    public boolean union(Node a, Node b) {
        Node rootA = find(a);
        Node rootB = find(b);

        // Already in the same set — adding this edge creates a cycle
        if (rootA.equals(rootB)) return false;

        // Attach smaller rank tree under larger rank tree
        if (rank.get(rootA) < rank.get(rootB)) {
            parent.put(rootA, rootB);
        } else if (rank.get(rootA) > rank.get(rootB)) {
            parent.put(rootB, rootA);
        } else {
            // Equal rank — pick one as root and increase its rank
            parent.put(rootB, rootA);
            rank.put(rootA, rank.get(rootA) + 1);
        }
        return true; // merge happened
    }

    // ─────────────────────────────────────────────────────
    // GET COMMUNITIES — group nodes by their root
    // Returns a map: root → list of nodes in that community
    // ─────────────────────────────────────────────────────
    public Map<Node, List<Node>> getCommunities() {
        Map<Node, List<Node>> communities = new HashMap<>();
        for (Node node : parent.keySet()) {
            Node root = find(node);
            communities.computeIfAbsent(root, k -> new ArrayList<>()).add(node);
        }
        return communities;
    }

    // ─────────────────────────────────────────────────────
    // Are two nodes in the same community?
    // ─────────────────────────────────────────────────────
    public boolean connected(Node a, Node b) {
        return find(a).equals(find(b));
    }
}