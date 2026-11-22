package edu.rungta.dsv.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public final class VisualizerFrame extends JFrame {
    public VisualizerFrame() {
        super("Structura — Data Structure Visualizer");
        WorkspacePanel workspace=new WorkspacePanel();setContentPane(workspace);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);setMinimumSize(new Dimension(1120,680));
        Rectangle screen=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1440,screen.width),Math.min(900,screen.height));setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){workspace.shutdown();}});
    }
}
