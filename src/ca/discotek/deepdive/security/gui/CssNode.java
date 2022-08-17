package ca.discotek.deepdive.security.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import ca.discotek.deepdive.security.dom.CssFile;

public class CssNode extends DefaultMutableTreeNode {
    public CssNode() {
        super("Css");
    }
    
    public void add(CssFile file) {
        add(new WarArtifactNode(file));
    }
        
}