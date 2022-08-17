package ca.discotek.deepdive.security.gui.path;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

public class DeploymentNode extends DefaultMutableTreeNode {

    public DeploymentNode(File file) {
        super(file.getName());
    }
}
