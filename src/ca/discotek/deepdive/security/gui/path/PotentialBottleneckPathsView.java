package ca.discotek.deepdive.security.gui.path;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class PotentialBottleneckPathsView extends JPanel {

    public PotentialBottleneckPathsView(JTree tree) {
        buildGui(tree);
    }
    
    void buildGui(JTree tree) {
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }
}
