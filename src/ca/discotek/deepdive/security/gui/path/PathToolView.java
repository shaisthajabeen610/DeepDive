package ca.discotek.deepdive.security.gui.path;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;




//import ca.discotek.deepdive.security.visitor.assess.custom.path.DeploymentData;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.MethodData;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.MethodDescriptor;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.PathAnalyzer;
//import ca.discotek.deepdive.security.visitor.assess.custom.path.dijkstra.Node;
import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.security.EventManager;
import ca.discotek.deepdive.security.dom.Apk;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.gui.DeepDiveGui;
import ca.discotek.deepdive.security.visitor.ArchiveReader;
import ca.discotek.deepdive.security.visitor.ArchiveVisitor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.EarProcessor;
import ca.discotek.deepdive.security.visitor.JarProcessor;
import ca.discotek.deepdive.security.visitor.WarProcessor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzeExceptionTracker;
import ca.discotek.deepdive.security.visitor.assess.custom.path.LongestPathsHelper;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathProcessor;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathTool;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PotentialBottleneckPathsHelper;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PotentialDeadlockPathsHelper;
import ca.discotek.deepdive.security.visitor.assess.custom.path.util.ManifestClasspathUtil;

public class PathToolView extends JPanel {
    final JFrame parent;

    final PathTool pathTool;
    
    public PathToolView(JFrame parent, PathTool pathTool) {
        this.parent = parent;
        this.pathTool = pathTool;
        buildGui();
    }
    
    void buildGui() {
        JTabbedPane tabber = new JTabbedPane();
        AllRootPathsTree allRootsPathTree = new AllRootPathsTree(pathTool.getPathProcessor());
        tabber.addTab("All Root Paths", new JScrollPane(allRootsPathTree));
        
        String outputDirectory = pathTool.getOutputDirectory();
        
        LongestPathsView longestPathsView = new LongestPathsView(new JTree(pathTool.getLongestPathsHelper().getTreeModel()));
        tabber.addTab("Longest Paths", longestPathsView);
        
        PotentialDeadlockPathsView potentialDeadlockPathsView = new PotentialDeadlockPathsView(new JTree(pathTool.getPotentialDeadlockPathsHelper().getTreeModel()));
        tabber.addTab("Most Synchronized Paths", potentialDeadlockPathsView);
        
        PotentialBottleneckPathsView potentialBottleneckPathsView = new PotentialBottleneckPathsView(new JTree(pathTool.getPotentialBottleneckPathsHelper().getTreeModel()));
        tabber.addTab("Most Nested Loops Paths", potentialBottleneckPathsView);
        
        setLayout(new BorderLayout());
        add(tabber, BorderLayout.CENTER);
    }
    
}
