import gui.MainWindow;
import javax.swing.*;

public class GraphVisualizer {
    public static void main(String[] args) {
        // Use native OS look and feel (Windows/Mac/Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // All Swing work must happen on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}