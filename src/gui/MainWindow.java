package gui;

import graph.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private GraphPanel graphPanel;
    private Graph      graph;
    private JLabel     statusBar;

    public MainWindow() {
        super("Social Network Analyzer — Graph Visualizer");
        graph = new Graph(false);
        initUI();
        loadSampleGraph();
    }

    // ─────────────────────────────────────────────────────
    //  Build the full window layout
    // ─────────────────────────────────────────────────────
    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        graphPanel = new GraphPanel(graph);
        add(graphPanel,          BorderLayout.CENTER);
        add(buildToolbar(),      BorderLayout.NORTH);
        add(buildControlPanel(), BorderLayout.EAST);

        // Status bar at the bottom
        statusBar = new JLabel("  Ready.");
        statusBar.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusBar.setBorder(new EmptyBorder(4, 8, 4, 8));
        statusBar.setOpaque(true);
        statusBar.setBackground(UIManager.getColor("Panel.background"));
        add(statusBar, BorderLayout.SOUTH);

        // Wire graph panel → status bar
        graphPanel.setStatusCallback(msg ->
                SwingUtilities.invokeLater(() -> statusBar.setText("  " + msg)));
    }

    // ─────────────────────────────────────────────────────
    //  Top toolbar: mode selector + utility buttons
    // ─────────────────────────────────────────────────────
    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBorder(new EmptyBorder(5, 8, 5, 8));

        String[] modes = {"Add Node", "Add Edge", "Move Node", "Select"};
        JComboBox<String> modeBox = new JComboBox<>(modes);
        modeBox.setMaximumSize(new Dimension(130, 28));
        modeBox.addActionListener(e ->
                graphPanel.setMode(modeBox.getSelectedIndex()));

        JButton clearBtn  = new JButton("Clear Graph");
        JButton sampleBtn = new JButton("Load Sample");
        clearBtn.addActionListener(e -> {
            graph = new Graph(false);
            graphPanel.setGraph(graph);
            statusBar.setText("  Graph cleared.");
        });
        sampleBtn.addActionListener(e -> loadSampleGraph());

        tb.add(new JLabel("Mode: "));
        tb.add(modeBox);
        tb.addSeparator(new Dimension(12, 0));
        tb.add(clearBtn);
        tb.add(Box.createHorizontalStrut(4));
        tb.add(sampleBtn);
        return tb;
    }

    // ─────────────────────────────────────────────────────
    //  Right sidebar: algorithm buttons + hints
    // ─────────────────────────────────────────────────────
    private JPanel buildControlPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(204, 0));
        p.setBorder(new EmptyBorder(12, 10, 12, 10));

        p.add(sectionLabel("Traversal"));
        p.add(Box.createVerticalStrut(5));
        p.add(wideButton("▶  BFS Traversal",
                e -> graphPanel.animateBFS()));
        p.add(Box.createVerticalStrut(4));
        p.add(wideButton("▶  DFS Traversal",
                e -> graphPanel.animateDFS()));
        p.add(Box.createVerticalStrut(14));

        p.add(sectionLabel("Shortest Path"));
        p.add(Box.createVerticalStrut(5));
        p.add(wideButton("Dijkstra  (pick 2 nodes)",
                e -> graphPanel.startDijkstraMode()));
        p.add(Box.createVerticalStrut(14));

        p.add(sectionLabel("Analysis"));
        p.add(Box.createVerticalStrut(5));
        p.add(wideButton("Influencer Radar",
                e -> graphPanel.showCentrality()));
        p.add(Box.createVerticalStrut(4));
        p.add(wideButton("Detect Communities",
                e -> graphPanel.showCommunities()));
        p.add(Box.createVerticalStrut(4));
        p.add(wideButton("↺  Reset Colors",
                e -> graphPanel.resetColors()));
        p.add(Box.createVerticalStrut(20));

        // Usage hints
        JTextArea hint = new JTextArea(
                "How to use:\n" +
                        "• Add Node → click canvas\n" +
                        "• Add Edge → click 2 nodes\n" +
                        "• Move Node → drag a node\n" +
                        "• Dijkstra → click button,\n" +
                        "  then pick source + dest\n\n" +
                        "Tip: Load Sample first,\nthen try each algorithm!");
        hint.setEditable(false);
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        hint.setBackground(p.getBackground());
        hint.setLineWrap(true);
        hint.setWrapStyleWord(true);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(hint);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        lbl.setForeground(new Color(83, 74, 183));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton wideButton(String label,
                               java.awt.event.ActionListener al) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(al);
        return btn;
    }

    // ─────────────────────────────────────────────────────
    //  Pre-built 10-node sample — same network from Phase 5
    // ─────────────────────────────────────────────────────
    private void loadSampleGraph() {
        graph = new Graph(false);

        Node alice = new Node("alice", "Alice", 22);
        Node bob   = new Node("bob",   "Bob",   24);
        Node carol = new Node("carol", "Carol", 21);
        Node dave  = new Node("dave",  "Dave",  25);
        Node eve   = new Node("eve",   "Eve",   23);
        Node frank = new Node("frank", "Frank", 26);
        Node grace = new Node("grace", "Grace", 20);
        Node henry = new Node("henry", "Henry", 29);
        Node iris  = new Node("iris",  "Iris",  27);
        Node jack  = new Node("jack",  "Jack",  28);

        graph.addEdge(alice, bob,   1.0);
        graph.addEdge(alice, carol, 3.0);
        graph.addEdge(alice, dave,  2.0);
        graph.addEdge(bob,   carol, 2.0);
        graph.addEdge(bob,   dave,  4.0);
        graph.addEdge(carol, eve,   1.0);
        graph.addEdge(dave,  eve,   2.0);
        graph.addEdge(dave,  frank, 3.0);
        graph.addEdge(eve,   grace, 1.0);
        graph.addEdge(frank, grace, 2.0);
        graph.addEdge(grace, henry, 3.0);
        graph.addEdge(henry, iris,  1.0);
        graph.addEdge(henry, jack,  2.0);
        graph.addEdge(iris,  jack,  1.0);

        graphPanel.setGraph(graph);
        graphPanel.layoutCircular();
        graphPanel.repaint();
        statusBar.setText("  Sample loaded — 10 nodes, 14 edges. Try the buttons!");
    }
}