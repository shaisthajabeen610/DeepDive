package ca.discotek.deepdive.security.gui.path;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

//import ca.discotek.deepdive.security.gui.path.PathTool.PathData;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.DeploymentData;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.MethodData;



import ca.discotek.deepdive.security.misc.HtmlList;
import ca.discotek.deepdive.security.visitor.assess.custom.path.Node;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathProcessor;

public class PotentialDeadlockPathsView extends JPanel {

    public PotentialDeadlockPathsView(JTree tree) {
        buildGui(tree);
    }
    
    void buildGui(JTree tree) {
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }
}
