package ca.discotek.deepdive.security.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractNode extends DefaultMutableTreeNode {
    
    public final String path;
    
    public AbstractNode(String path) {
        super(path);
        
        
        if (path.startsWith("WEB-INF/classes/") && path.endsWith(".class")) {
            String className = path.substring("WEB-INF/classes".length() + 1).replace('/', '.');
            setUserObject("WEB-INF/classes/" + className);
        }
        else if (path.endsWith(".class")) {
            setUserObject(path.replace('/', '.'));
        } 

        
        this.path = path;
    }
}