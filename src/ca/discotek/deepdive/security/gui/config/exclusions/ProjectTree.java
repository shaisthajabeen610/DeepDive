package ca.discotek.deepdive.security.gui.config.exclusions;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ca.discotek.deepdive.security.Path;

public class ProjectTree extends JPanel {

    DefaultMutableTreeNode rootNode;
    DefaultTreeModel model;
    JTree tree;
    
    ProjectTree() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        
        rootNode = new DefaultMutableTreeNode("root");
        model = new DefaultTreeModel(rootNode);
        tree = new JTree(model);
        
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setExpandsSelectedPaths(true);

        add(new JScrollPane(tree), BorderLayout.CENTER);
    }
    
    public void addDeployment(File file) throws IOException {
        processArchive(file);
    }
    
    public String[] getSelectedPaths() {
        TreePath treePaths[] = tree.getSelectionModel().getSelectionPaths();
        String paths[] = new String[treePaths == null ? 0 : treePaths.length];
        AbstractNode node;

        for (int i=0; i<paths.length; i++) {
            node = (AbstractNode) treePaths[i].getLastPathComponent();
            paths[i] = node.path.toString();
        }
        
        return paths;
    }
    
    public void setSelectedPaths(String paths[]) {

        List<TreePath> list = new ArrayList<TreePath>();
        
        DefaultMutableTreeNode parentNode = rootNode;
        AbstractNode childNode = null;
        int count;
        String path[];
        boolean found;
        for (int i=0; i<paths.length; i++) {
            path = paths[i].split("->");
            found = false;
            for (int j=0; j<path.length; j++) {
                count = parentNode.getChildCount();
                for (int k=0; k<count; k++) {
                    childNode = (AbstractNode) parentNode.getChildAt(k);
                    if (childNode.path.getLastPath().equals(path[j])) {
                        if (j==path.length-1) {
                            list.add(new TreePath(childNode.getPath()));
                            found = true;
                            break;
                        }
                        else 
                            parentNode = childNode;
                    }
                    
                    if (found)
                        break;
                }
                
                if (found)
                    break;
            }
        }
        
        tree.setSelectionPaths(list.toArray(new TreePath[list.size()]));
    }
    
    abstract class AbstractNode extends DefaultMutableTreeNode {
        final Path path;
        
        public AbstractNode(Path path) {
            super(path.getLastPath());
            this.path = path;
        }
    }
    
    class TopLevelNode extends AbstractNode {
        public TopLevelNode(Path path) {
            super(path);
        }
    }
    
    class SubLevelNode extends AbstractNode {
        
        public SubLevelNode(Path path) {
            super(path);
        }
    }

    void processArchive(File file) throws IOException {
        Path path = new Path(file.getAbsolutePath());

        TopLevelNode node = new TopLevelNode(path);
        model.insertNodeInto(node, rootNode, rootNode.getChildCount());
        tree.expandPath(new TreePath(rootNode.getPath()));
        tree.expandPath(new TreePath(node.getPath()));
        
        FileInputStream fis = new FileInputStream(file);
        JarInputStream jis = new JarInputStream(fis);

        processArchive(path, jis, node);
        jis.close();
    }
    
    void processArchive(Path path, JarInputStream jis, AbstractNode parentNode) throws IOException {
        JarEntry entry;
        
        String name;
        Path newPath;
        while ( (entry = jis.getNextJarEntry()) != null) {
            name = entry.getName();
            if (name.endsWith(".war") || name.endsWith(".jar")) {
                newPath = path.newPath(name);
                
                SubLevelNode node = new SubLevelNode(path.newPath(name));
                model.insertNodeInto(node, parentNode, parentNode.getChildCount());
                
                tree.expandPath(new TreePath(parentNode.getPath()));
                tree.expandPath(new TreePath(node.getPath()));
                    
                processArchive(newPath, new JarInputStream(jis), node);
            }
        }        
    }
}
