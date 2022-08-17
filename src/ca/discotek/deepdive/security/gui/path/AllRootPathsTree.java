package ca.discotek.deepdive.security.gui.path;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;





import java.util.Stack;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import ca.discotek.deepdive.security.misc.HtmlList;
import ca.discotek.deepdive.security.visitor.assess.custom.path.Node;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathProcessor;
import ca.discotek.deepdive.security.visitor.assess.custom.path.ProjectData;

public class AllRootPathsTree extends JTree {

    public AllRootPathsTree(PathProcessor pathProcessor) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
        DefaultTreeModel model = new DefaultTreeModel(rootNode);

        MethodNode child;
        Map.Entry<Integer, Map<Integer, Set<Integer>>> entries[] = pathProcessor.rootMap.entrySet().toArray(new Map.Entry[pathProcessor.rootMap.size()]);
        Comparator c = new Comparator<Map.Entry<Integer, Map<Integer, Integer>>>() {
            public int compare(Entry<Integer, Map<Integer, Integer>> o1, Entry<Integer, Map<Integer, Integer>> o2) {
                return ProjectData.classNameMap.get(o1.getKey()).compareTo(ProjectData.classNameMap.get(o2.getKey()));
            }
        };
        
        Arrays.sort(entries, c);
        
        int classId, methodId, descId;
        Map<Integer, Set<Integer>> methodMap;
        Map.Entry<Integer, Set<Integer>>[] methodEntries;
        Iterator<Integer> descIt;
        Node childNode;
        HtmlList list;
        for (int i=0; i<entries.length; i++) {
            methodMap = entries[i].getValue();
            methodEntries = methodMap.entrySet().toArray(new Map.Entry[methodMap.size()]);
            classId = entries[i].getKey();
            
            for (int j=0; j<methodEntries.length; j++) {
                methodId = methodEntries[j].getKey();
                descIt = methodEntries[j].getValue().iterator();
                while (descIt.hasNext()) {
                    descId = descIt.next();
                    childNode = pathProcessor.getNode(classId, methodId, descId, false);
                    child = new MethodNode(childNode, false, false, model, pathProcessor.followAbstractMethods);
                    rootNode.insert(child, rootNode.getChildCount());
                }
            }
        }
        
        setModel(model);
        
        addTreeWillExpandListener(new TreeWillExpandListener() {
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                if (path != null) {
                    Object o = path.getLastPathComponent();
                    if (o instanceof MethodNode) {
                        MethodNode node = (MethodNode) o;
                        if (!node.childrenAdded)
                            node.addChildren();
                    }
                }
            }
            
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
        });
        
        setShowsRootHandles(true);
        setRootVisible(false);
    }
}
