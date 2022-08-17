package ca.discotek.deepdive.grep.gui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ca.discotek.deepdive.grep.gui.tree.AbstractNode;
import ca.discotek.deepdive.grep.gui.tree.ClassNode;
import ca.discotek.deepdive.grep.gui.tree.DescriptionNode;
import ca.discotek.deepdive.grep.gui.tree.LocationNode;
import ca.discotek.deepdive.grep.gui.tree.TextFileDescriptionNode;
import ca.discotek.deepdive.grep.gui.tree.TextFileNode;

public class ResultsPanel extends JPanel {
	
    StatusBar statusBar;
    TextFileViewer textFileViewer;
    
    public ResultsPanel(JTree resultsComponent) {
        buildGui(resultsComponent);
        resultsComponent.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                TreePath path = evt.getPath();
                if (path == null) 
                    textFileViewer.clear();
                else {
                    Object o = path.getLastPathComponent();
                    if (o == null) 
                        textFileViewer.clear();
                    else {
                        if (o instanceof TextFileNode) {
                            textFileViewer.setLocation( (TextFileNode) o);
                        }
                        else if (o instanceof TextFileDescriptionNode) {
                            textFileViewer.setLocation( (TextFileDescriptionNode) o);
                        }
                        else if (o instanceof ClassNode) {
                            textFileViewer.setLocation( (ClassNode) o);
                        }
                        else if (o instanceof DescriptionNode) {
                            DescriptionNode descriptionNode = (DescriptionNode) o;
                            textFileViewer.setLocation( (ClassNode) descriptionNode.getParent());
                        }
                        else
                            textFileViewer.clear();
                    }
                }
            }
        });
    }
    
    public void buildGui(JComponent resultsComponent) {
        setLayout(new BorderLayout());

        if (resultsComponent instanceof JTree) {
            textFileViewer = new TextFileViewer();
            JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(resultsComponent), textFileViewer);
            splitter.setResizeWeight(0.45);
            add(splitter, BorderLayout.CENTER);
        }
        else 
            add(new JScrollPane(resultsComponent), BorderLayout.CENTER);
        
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }
    
    public void setStatusComplete() {
        statusBar.setComplete();
    }
    
    public void setError() {
        statusBar.setError();
    }
	
}
