package ca.discotek.deepdive.security.gui.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

//import ca.discotek.deepdive.security.visitor.assess.custom.path.dijkstra.Node;

public class MethodNode extends DefaultMutableTreeNode {
    
    final DefaultTreeModel model;
    public final ca.discotek.deepdive.security.visitor.assess.custom.path.Node node;
    public final List<ca.discotek.deepdive.security.visitor.assess.custom.path.Node> sortedChildNodeList;
    boolean childrenAdded = false;
    
    final boolean followAbstractMethods;
    
    public MethodNode(ca.discotek.deepdive.security.visitor.assess.custom.path.Node node, boolean isInterfaceMethod, boolean isAbstractMethod, DefaultTreeModel model, boolean followAbstractMethods) {
        setUserObject(toString(node, isInterfaceMethod, isAbstractMethod));
        this.node = node;
        this.model = model;
        this.followAbstractMethods = followAbstractMethods;
        sortedChildNodeList = new ArrayList<ca.discotek.deepdive.security.visitor.assess.custom.path.Node>();
        sortedChildNodeList.addAll(node.childSet);
    }
    
    public static String toString(ca.discotek.deepdive.security.visitor.assess.custom.path.Node node, boolean isInterfaceMethod, boolean isAbstractMethod) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(node.toString());
        if (isInterfaceMethod)
            buffer.append(" (interface impl)");
        else if (isAbstractMethod)
            buffer.append(" (abstract impl)");
        return buffer.toString();
    }
    
    void addChildren() {
        Iterator<ca.discotek.deepdive.security.visitor.assess.custom.path.Node> it = sortedChildNodeList.listIterator();
        ca.discotek.deepdive.security.visitor.assess.custom.path.Node child;
        while (it.hasNext()) {
            child = it.next();
            boolean isInterfaceMethod = node.isChildInterface(child);
            boolean isAbstractMethod = node.isChildAbstract(child);
            if (!followAbstractMethods && (isAbstractMethod || isInterfaceMethod))
                continue;
            else
                model.insertNodeInto(new MethodNode(child, isInterfaceMethod, isAbstractMethod, model, followAbstractMethods), this, getRealizedChildCount());
        }
        
        childrenAdded = true;
    }
    
    public int getRealizedChildCount() {
        return super.getChildCount();
    }
    
    public int getChildCount() {
        if (!followAbstractMethods) {
            int count = 0;
            Iterator<ca.discotek.deepdive.security.visitor.assess.custom.path.Node> it = node.childSet.iterator();
            ca.discotek.deepdive.security.visitor.assess.custom.path.Node childNode;
            while (it.hasNext()) {
                childNode = it.next();
                if (!(node.childAbstractMethodNodeSet.contains(childNode) || node.childInterfaceMethodNodeSet.contains(childNode)))
                        count++;
            }
            return count;
        }
        else 
            return node.childSet.size();
    }
}
