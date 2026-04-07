import graph.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        // ── Build a network with 3 clear communities ──────
        Graph network = new Graph(false);

        // Community A — close friends (school group)
        Node alice = new Node("alice", "Alice", 22);
        Node bob   = new Node("bob",   "Bob",   24);
        Node carol = new Node("carol", "Carol", 21);
        Node dave  = new Node("dave",  "Dave",  25);

        // Community B — colleagues (work group)
        Node eve   = new Node("eve",   "Eve",   23);
        Node frank = new Node("frank", "Frank", 26);
        Node grace = new Node("grace", "Grace", 20);

        // Community C — neighbours (neighbourhood group)
        Node henry = new Node("henry", "Henry", 29);
        Node iris  = new Node("iris",  "Iris",  27);
        Node jack  = new Node("jack",  "Jack",  28);

        // Dense intra-community edges (strong bonds)
        network.addEdge(alice, bob,   1.0);
        network.addEdge(alice, carol, 1.0);
        network.addEdge(alice, dave,  1.0);
        network.addEdge(bob,   carol, 1.0);
        network.addEdge(bob,   dave,  1.0);

        network.addEdge(eve,   frank, 1.0);
        network.addEdge(eve,   grace, 1.0);
        network.addEdge(frank, grace, 1.0);

        network.addEdge(henry, iris,  1.0);
        network.addEdge(henry, jack,  1.0);
        network.addEdge(iris,  jack,  1.0);

        // Sparse inter-community edges (weak bridge links)
        network.addEdge(dave,  eve,   1.0);  // bridge A ↔ B
        network.addEdge(grace, henry, 1.0);  // bridge B ↔ C

        network.printGraph();

        // ── Run the full community detection report ────────
        CommunityDetector.fullReport(network);

        // ── Bonus: test Union-Find directly ───────────────
        System.out.println("\n=== Union-Find Direct Queries ===");
        UnionFind uf = new UnionFind(network.getAllNodes());
        for (Node node : network.getAllNodes()) {
            for (Edge edge : network.getNeighbors(node)) {
                uf.union(edge.getSource(), edge.getDestination());
            }
        }
        System.out.println("Alice & Bob same community?  " + uf.connected(alice, bob));
        System.out.println("Alice & Eve same community?  " + uf.connected(alice, eve));
        System.out.println("Alice & Henry same community?" + uf.connected(alice, henry));
    }
}