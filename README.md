# 🔗 SocialNetworkAnalyzer

A graph-based social network analysis tool built in Java, developed as a Discrete Mathematical Structures project at **Marwadi University**.

## 📖 Description

SocialNetworkAnalyzer models real-world social networks as mathematical graphs, where users are represented as **nodes** and their connections as **edges**. The project was built in six progressive phases, each introducing a new layer of graph theory and algorithm design, starting from basic graph structures all the way to an interactive visual interface.

The goal is to demonstrate how graph algorithms can be applied to analyze social relationships, identifying the most connected individuals, finding the shortest path between two people, and detecting communities or clusters within a network. The project also served as the foundation for an ongoing research paper applying these same techniques to **Liberia's inter-county road network**.

---

## 📌 Features

- **Graph Construction** — Build directed/undirected graphs representing social connections
- **BFS & DFS Traversal** — Explore network reachability and structure
- **Dijkstra's Shortest Path** — Find the shortest connection path between any two users
- **Centrality Analysis** — Identify the most influential nodes in the network
- **Community Detection** — Discover clusters and subgroups within the network
- **Interactive GUI** — Visualize and interact with the graph using Java Swing

---

## 🏗️ Project Structure

```
SocialNetworkAnalyzer/
├── src/
│   ├── Phase1_GraphStructures/       # Graph data structures (nodes, edges, adjacency)
│   ├── Phase2_Traversal/             # BFS and DFS implementations
│   ├── Phase3_Dijkstra/              # Shortest path algorithm
│   ├── Phase4_CentralityAnalysis/    # Degree, betweenness, closeness centrality
│   ├── Phase5_CommunityDetection/    # Community/cluster detection
│   └── Phase6_GUI/                   # Java Swing graphical interface
└── README.md
```

---

## 🧠 Algorithms Implemented

| Phase | Algorithm / Concept |
|-------|----------------------|
| 1 | Graph representation (adjacency list/matrix) |
| 2 | Breadth-First Search (BFS), Depth-First Search (DFS) |
| 3 | Dijkstra's Shortest Path Algorithm |
| 4 | Degree Centrality, Betweenness Centrality, Closeness Centrality |
| 5 | Community Detection (graph clustering) |
| 6 | Java Swing GUI for visualization and interaction |

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 11 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Run the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/Gaye-sam1/SocialNetworkAnalyzer.git
   ```

2. Open the project in IntelliJ IDEA

3. Navigate to the `Phase6_GUI` package and run the main GUI class

---

## 🖥️ GUI Overview

The Java Swing interface allows users to:
- Add and remove nodes (users) and edges (connections)
- Run traversal algorithms and visualize the order of visited nodes
- Find shortest paths between selected nodes
- View centrality scores for each node
- Detect and highlight communities within the network

---

## 📚 Academic Context

- **Course:** Discrete Mathematics & Structures (DMS) — Semester 4
- **Institution:** Marwadi University, Rajkot, Gujarat, India
- **Author:** Samuel B. Gaye

This project also inspired an ongoing research paper applying graph algorithms to **Liberia's inter-county road network**, exploring how computational graph theory can support real-world infrastructure and connectivity analysis in developing regions.

---

## 👤 Author

**Samuel B. Gaye**
CSE-AI Student | Marwadi University
GitHub: [@Gaye-sam1](https://github.com/Gaye-sam1)

---

## 📄 License

This project is for academic and educational purposes.
