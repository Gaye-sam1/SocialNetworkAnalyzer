package gui;

import graph.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class GraphPanel extends JPanel {

    // ── Visual Constants ───────────────────────────────────────
    private static final int   NODE_R       = 26;
    private static final Color C_BG         = new Color(247, 247, 251);
    private static final Color C_GRID       = new Color(200, 200, 220, 38);
    private static final Color C_EDGE       = new Color(160, 160, 190);
    private static final Color C_EDGE_PATH  = new Color(24,  95, 165);
    private static final Color C_NODE_F     = new Color(200, 200, 230);
    private static final Color C_NODE_B     = new Color(83,  74, 183);
    private static final Color C_VISIT_F    = new Color(83,  74, 183);
    private static final Color C_VISIT_B    = new Color(60,  50, 140);
    private static final Color C_FRONT_F    = new Color(239, 159,  39);
    private static final Color C_FRONT_B    = new Color(186, 117,  23);
    private static final Color C_PATH_F     = new Color(24,  95, 165);
    private static final Color C_PATH_B     = new Color(12,  68, 124);
    private static final Color C_SEL_F      = new Color(255, 200,  50);
    private static final Color C_SEL_B      = new Color(180, 140,   0);
    private static final Color C_TEXT       = new Color(30,  30,  80);

    // Community palette (fill + border pairs)
    private static final Color[] COMM_F = {
            new Color(238,237,254), new Color(225,245,238),
            new Color(250,236,231), new Color(234,243,222),
            new Color(250,238,218), new Color(230,241,251)
    };
    private static final Color[] COMM_B = {
            new Color(83,74,183), new Color(29,158,117),
            new Color(153,60,29), new Color(59,109,17),
            new Color(186,117,23), new Color(24,95,165)
    };

    // ── State ──────────────────────────────────────────────────
    private Graph             graph;
    private Map<Node, Point>  positions  = new LinkedHashMap<>();
    private Map<Node, Color>  fillCol    = new HashMap<>();
    private Map<Node, Color>  bordCol    = new HashMap<>();
    private Map<Node, Double> nodeSz     = new HashMap<>();
    private Set<Node>         visited    = new LinkedHashSet<>();
    private Set<Node>         frontier   = new LinkedHashSet<>();
    private List<Node>        shortPath  = new ArrayList<>();
    private int               mode       = 0;
    private Node              edgeStart  = null;
    private Node              dijkSrc    = null;
    private boolean           dijkMode   = false;
    private Node              dragNode   = null;
    private Point             dragOffset = null;
    private int               nodeCount  = 0;
    private Consumer<String>  statusCB;

    // ── Constructor ────────────────────────────────────────────
    public GraphPanel(Graph graph) {
        this.graph = graph;
        setBackground(C_BG);
        setPreferredSize(new Dimension(820, 640));

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e)  { onClicked(e);  }
            @Override public void mousePressed(MouseEvent e)  { onPressed(e);  }
            @Override public void mouseDragged(MouseEvent e)  { onDragged(e);  }
            @Override public void mouseReleased(MouseEvent e) { dragNode = null; }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // ── Public API ─────────────────────────────────────────────
    public void setStatusCallback(Consumer<String> cb) { statusCB = cb; }

    public void setGraph(Graph g) {
        graph = g;
        positions.clear(); fillCol.clear(); bordCol.clear(); nodeSz.clear();
        visited.clear();   frontier.clear(); shortPath.clear();
        edgeStart = null;  dijkSrc = null;  dijkMode = false;
        nodeCount = g.getAllNodes().size();
        repaint();
    }

    public void setMode(int m) {
        mode = m; edgeStart = null; dijkMode = false; dijkSrc = null;
        status(modeName(m) + " mode active.");
        repaint();
    }

    // Arrange all nodes as a regular polygon (circle)
    public void layoutCircular() {
        List<Node> list = new ArrayList<>(graph.getAllNodes());
        int n = list.size();
        if (n == 0) return;
        int cx = getPreferredSize().width  / 2;
        int cy = getPreferredSize().height / 2;
        int r  = Math.min(cx, cy) - NODE_R - 24;
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            positions.put(list.get(i), new Point(
                    (int)(cx + r * Math.cos(angle)),
                    (int)(cy + r * Math.sin(angle))));
        }
    }

    // ── Painting ───────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawGrid(g);
        drawEdges(g);
        drawAllNodes(g);
        drawSelectionRing(g);
        drawLegend(g);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(C_GRID);
        g.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < getWidth();  x += 40) g.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40) g.drawLine(0, y, getWidth(), y);
    }

    private void drawEdges(Graphics2D g) {
        Set<String> drawn = new HashSet<>();
        for (Node src : graph.getAllNodes()) {
            Point p1 = positions.get(src);
            if (p1 == null) continue;
            for (Edge edge : graph.getNeighbors(src)) {
                Node  dst = edge.getDestination();
                Point p2  = positions.get(dst);
                if (p2 == null) continue;
                String key = canonKey(src, dst);
                if (drawn.contains(key)) continue;
                drawn.add(key);

                boolean onPath = isOnPath(src, dst);
                g.setColor(onPath ? C_EDGE_PATH : C_EDGE);
                g.setStroke(new BasicStroke(onPath ? 3.5f : 1.8f,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Draw from border to border, not center to center
                double dx = p2.x-p1.x, dy = p2.y-p1.y;
                double d  = Math.sqrt(dx*dx + dy*dy);
                if (d < 1) continue;
                double ux = dx/d, uy = dy/d;
                int r1 = radius(src), r2 = radius(dst);
                g.drawLine((int)(p1.x+ux*r1), (int)(p1.y+uy*r1),
                        (int)(p2.x-ux*r2), (int)(p2.y-uy*r2));

                // Weight label
                g.setColor(new Color(130, 130, 170));
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                g.drawString(String.valueOf((int)edge.getWeight()),
                        (p1.x+p2.x)/2 + 3, (p1.y+p2.y)/2 - 2);
            }
        }
    }

    private void drawAllNodes(Graphics2D g) {
        for (Node node : graph.getAllNodes()) {
            Point p = positions.get(node);
            if (p != null) drawNode(g, node, p);
        }
    }

    private void drawNode(Graphics2D g, Node node, Point p) {
        int r = radius(node);

        // Priority order: frontier > shortPath > visited > selected > custom > default
        Color fill, bord;
        if      (frontier.contains(node))  { fill=C_FRONT_F; bord=C_FRONT_B; }
        else if (shortPath.contains(node)) { fill=C_PATH_F;  bord=C_PATH_B;  }
        else if (visited.contains(node))   { fill=C_VISIT_F; bord=C_VISIT_B; }
        else if (node.equals(edgeStart) ||
                node.equals(dijkSrc))     { fill=C_SEL_F;   bord=C_SEL_B;   }
        else {
            fill = fillCol.getOrDefault(node, C_NODE_F);
            bord = bordCol.getOrDefault(node, C_NODE_B);
        }

        // Drop shadow
        g.setColor(new Color(0, 0, 0, 22));
        g.fillOval(p.x-r+3, p.y-r+3, r*2, r*2);

        // Fill circle
        g.setColor(fill);
        g.fillOval(p.x-r, p.y-r, r*2, r*2);

        // Border
        g.setColor(bord);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(p.x-r, p.y-r, r*2, r*2);

        // Name — font scales with node size
        g.setColor(C_TEXT);
        int fs = (int) Math.max(9, 12 * nodeSz.getOrDefault(node, 1.0));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.min(fs, 14)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(node.getName(),
                p.x - fm.stringWidth(node.getName())/2,
                p.y + fm.getDescent());
    }

    private void drawSelectionRing(Graphics2D g) {
        // Animated dashed ring around the currently selected node
        Node sel = (edgeStart != null) ? edgeStart
                : (dijkSrc  != null) ? dijkSrc : null;
        if (sel == null) return;
        Point p = positions.get(sel);
        if (p == null) return;
        int r = radius(sel) + 7;
        g.setColor(new Color(255, 180, 0, 160));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 0, new float[]{6, 4}, 0));
        g.drawOval(p.x-r, p.y-r, r*2, r*2);
    }

    private void drawLegend(Graphics2D g) {
        int x = 10, y = getHeight() - 118;
        g.setColor(new Color(255, 255, 255, 210));
        g.fillRoundRect(x-4, y-14, 168, 112, 8, 8);
        g.setColor(new Color(180, 180, 210, 160));
        g.setStroke(new BasicStroke(0.5f));
        g.drawRoundRect(x-4, y-14, 168, 112, 8, 8);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        g.setColor(new Color(80, 80, 140));
        g.drawString("LEGEND", x, y); y += 14;

        Object[][] rows = {
                {C_NODE_F, "Default node"},
                {C_FRONT_F,"Current (frontier)"},
                {C_VISIT_F,"Visited"},
                {C_PATH_F, "Shortest path"},
                {C_SEL_F,  "Selected"},
        };
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        for (Object[] row : rows) {
            g.setColor((Color) row[0]);
            g.fillOval(x, y-9, 11, 11);
            g.setColor(new Color(60, 60, 100));
            g.drawString((String) row[1], x+16, y);
            y += 14;
        }
    }

    // ── Mouse Handlers ─────────────────────────────────────────
    private void onClicked(MouseEvent e) {
        Node hit = nodeAt(e.getPoint());

        if (mode == 0) {                        // ── Add Node
            if (hit == null) addNodeAt(e.getPoint());

        } else if (mode == 1) {                 // ── Add Edge
            if (hit == null) return;
            if (edgeStart == null) {
                edgeStart = hit;
                status("Source selected: " + hit.getName() + ". Click the target node.");
            } else if (!edgeStart.equals(hit)) {
                String wInput = JOptionPane.showInputDialog(
                        this, "Edge weight:", "1");
                double w = 1.0;
                try { if (wInput != null) w = Double.parseDouble(wInput.trim()); }
                catch (NumberFormatException ignored) {}
                graph.addEdge(edgeStart, hit, w);
                status("Edge added: " + edgeStart.getName()
                        + " ↔ " + hit.getName() + "  (w=" + w + ")");
                edgeStart = null;
            } else {
                edgeStart = null;
                status("Deselected. Click source node again.");
            }

        } else if (mode == 3 && dijkMode) {     // ── Dijkstra Select
            if (hit == null) return;
            if (dijkSrc == null) {
                dijkSrc = hit;
                status("Source: " + hit.getName() + ". Now click destination.");
            } else if (!dijkSrc.equals(hit)) {
                runDijkstra(dijkSrc, hit);
                dijkSrc = null; dijkMode = false;
            } else {
                dijkSrc = null;
                status("Deselected. Click source node again.");
            }
        }
        repaint();
    }

    private void onPressed(MouseEvent e) {
        if (mode == 2) {
            Node hit = nodeAt(e.getPoint());
            if (hit != null) {
                dragNode   = hit;
                Point p    = positions.get(hit);
                dragOffset = new Point(e.getX()-p.x, e.getY()-p.y);
            }
        }
    }

    private void onDragged(MouseEvent e) {
        if (mode == 2 && dragNode != null) {
            int nx = Math.max(NODE_R, Math.min(getWidth() -NODE_R, e.getX()-dragOffset.x));
            int ny = Math.max(NODE_R, Math.min(getHeight()-NODE_R, e.getY()-dragOffset.y));
            positions.put(dragNode, new Point(nx, ny));
            repaint();
        }
    }

    private Node nodeAt(Point p) {
        for (Map.Entry<Node,Point> e : positions.entrySet())
            if (e.getValue().distance(p) <= NODE_R + 6) return e.getKey();
        return null;
    }

    private void addNodeAt(Point p) {
        String name = JOptionPane.showInputDialog(
                this, "Node name:", "Add Node", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();
        String id = name.toLowerCase().replaceAll("\\s+","_") + (++nodeCount);
        Node node = new Node(id, name, 0);
        graph.addNode(node);
        positions.put(node, new Point(p.x, p.y));
        status("Node added: " + name);
        repaint();
    }

    // ── Phase 2: BFS & DFS Animations ─────────────────────────
    public void animateBFS() {
        if (graph.getAllNodes().isEmpty()) { status("Graph is empty."); return; }
        clearAnimState();
        Node start = graph.getAllNodes().iterator().next();
        List<Node> order = BFSTraversal.bfs(graph, start);
        status("BFS from: " + start.getName());
        playAnimation(order, "BFS complete! Visited " + order.size() + " nodes (level by level).");
    }

    public void animateDFS() {
        if (graph.getAllNodes().isEmpty()) { status("Graph is empty."); return; }
        clearAnimState();
        Node start = graph.getAllNodes().iterator().next();
        List<Node> order = DFSTraversal.dfs(graph, start);
        status("DFS from: " + start.getName());
        playAnimation(order, "DFS complete! Visited " + order.size() + " nodes (depth first).");
    }

    // Timed animation — lights up each node in sequence
    private void playAnimation(List<Node> order, String doneMsg) {
        final int[] idx = {0};
        Timer timer = new Timer(550, null);
        timer.addActionListener(e -> {
            if (idx[0] > 0) {
                // Move previous frontier node to visited (purple)
                Node prev = order.get(idx[0]-1);
                frontier.remove(prev);
                visited.add(prev);
            }
            if (idx[0] < order.size()) {
                // Highlight current node as frontier (amber)
                frontier.add(order.get(idx[0]));
                status("Visiting: " + order.get(idx[0]).getName()
                        + "  [" + (idx[0]+1) + "/" + order.size() + "]");
            }
            idx[0]++;
            repaint();
            if (idx[0] > order.size()) {
                ((Timer) e.getSource()).stop();
                frontier.clear();
                status(doneMsg);
                repaint();
            }
        });
        timer.start();
    }

    // ── Phase 3: Dijkstra ──────────────────────────────────────
    public void startDijkstraMode() {
        clearAnimState();
        dijkMode = true; dijkSrc = null; mode = 3;
        status("Dijkstra: switch to Select mode, then click SOURCE node.");
        repaint();
    }

    private void runDijkstra(Node src, Node dst) {
        shortPath.clear();
        DijkstraAlgorithm.Result result = DijkstraAlgorithm.dijkstra(graph, src);
        List<Node> path = DijkstraAlgorithm.getPath(result, src, dst);
        if (path.isEmpty()) {
            status("No path found: " + src.getName() + " → " + dst.getName());
        } else {
            shortPath = path;
            double dist = result.distances.get(dst);
            status("Shortest path: " + pathStr(path) + "  |  Distance: " + dist);
        }
        repaint();
    }

    // ── Phase 4: Centrality / Influencer ──────────────────────
    public void showCentrality() {
        if (graph.getAllNodes().isEmpty()) return;
        clearAnimState();
        Map<Node,Double> pr  = CentralityAnalyzer.pageRank(graph, 0.85, 100);
        Map<Node,Double> deg = CentralityAnalyzer.degreeCentrality(graph);
        double maxPR  = pr.values().stream().mapToDouble(d->d).max().orElse(1);
        double maxDeg = deg.values().stream().mapToDouble(d->d).max().orElse(1);

        Node topNode = null; double topScore = -1;
        for (Node node : graph.getAllNodes()) {
            double score = ((pr.getOrDefault(node,0.0)/maxPR)
                    + (deg.getOrDefault(node,0.0)/maxDeg)) / 2.0;
            // Size: 0.75 (low) to 1.5 (top)
            nodeSz.put(node, 0.75 + score * 0.75);
            // Purple shade: light = low, deep = high
            int v = (int)(score * 148);
            fillCol.put(node, new Color(
                    Math.max(0,238-v), Math.max(0,237-v), Math.max(80,254-v/2)));
            bordCol.put(node, new Color(83, 74, 183));
            if (score > topScore) { topScore = score; topNode = node; }
        }
        String top = (topNode != null) ? topNode.getName() : "?";
        status("Influencer Radar: node size = influence. 👑 Top: " + top);
        repaint();
    }

    // ── Phase 5: Community Detection ──────────────────────────
    public void showCommunities() {
        if (graph.getAllNodes().isEmpty()) return;
        clearAnimState();
        int target = Math.max(2, graph.getAllNodes().size() / 4);
        List<List<Node>> comms =
                CommunityDetector.detectByEdgeBetweenness(graph, target);
        for (int i = 0; i < comms.size(); i++) {
            Color f = COMM_F[i % COMM_F.length];
            Color b = COMM_B[i % COMM_B.length];
            for (Node n : comms.get(i)) { fillCol.put(n,f); bordCol.put(n,b); }
        }
        status("Detected " + comms.size() + " communities (edge betweenness method).");
        repaint();
    }

    public void resetColors() {
        clearAnimState();
        fillCol.clear(); bordCol.clear(); nodeSz.clear();
        status("Colors reset. Graph ready.");
        repaint();
    }

    // ── Helpers ────────────────────────────────────────────────
    private void clearAnimState() {
        visited.clear(); frontier.clear(); shortPath.clear();
        dijkSrc = null;  dijkMode = false; edgeStart = null;
    }

    private int radius(Node n) {
        return (int)(NODE_R * nodeSz.getOrDefault(n, 1.0));
    }

    private boolean isOnPath(Node a, Node b) {
        for (int i = 0; i < shortPath.size()-1; i++) {
            if ((shortPath.get(i).equals(a) && shortPath.get(i+1).equals(b)) ||
                    (shortPath.get(i).equals(b) && shortPath.get(i+1).equals(a)))
                return true;
        }
        return false;
    }

    private String canonKey(Node a, Node b) {
        return a.getId().compareTo(b.getId()) < 0
                ? a.getId()+"-"+b.getId() : b.getId()+"-"+a.getId();
    }

    private String pathStr(List<Node> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size()-1) sb.append(" → ");
        }
        return sb.toString();
    }

    private String modeName(int m) {
        if (m == 0) return "Add Node";
        if (m == 1) return "Add Edge";
        if (m == 2) return "Move Node";
        if (m == 3) return "Select";
        return "Unknown";
    }

    private void status(String msg) {
        if (statusCB != null)
            SwingUtilities.invokeLater(() -> statusCB.accept(msg));
    }
}