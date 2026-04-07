package graph;

import java.util.Objects;

public class Node {

    private String id;       // Unique identifier e.g. "alice"
    private String name;     // Display name e.g. "Alice"
    private int age;         // Optional extra data

    // Constructor
    public Node(String id, String name, int age) {
        this.id   = id;
        this.name = name;
        this.age  = age;
    }

    // Getters
    public String getId()   { return id;   }
    public String getName() { return name; }
    public int    getAge()  { return age;  }

    // Two nodes are equal if their IDs match
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (id=" + id + ", age=" + age + ")";
    }
}