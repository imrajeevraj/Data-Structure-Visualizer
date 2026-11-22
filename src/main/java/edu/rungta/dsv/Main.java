package edu.rungta.dsv;
import edu.rungta.dsv.view.*;
import javax.swing.SwingUtilities;
public final class Main {
    private Main() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {Theme.install();new VisualizerFrame().setVisible(true);});
    }
}
