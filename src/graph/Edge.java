package graph;

public class Edge {

    private Node   source;      // Who the edge starts from
    private Node   destination; // Who it goes to
    private double weight;      // Strength of connection / distance

    // Constructor
    public Edge(Node source, Node destination, double weight) {
        this.source      = source;
        this.destination = destination;
        this.weight      = weight;
    }

    // Getters
    public Node   getSource()      { return source;      }
    public Node   getDestination() { return destination; }
    public double getWeight()      { return weight;      }

    @Override
    public String toString() {
        return source.getName() + " --[" + weight + "]--> " + destination.getName();
    }
}