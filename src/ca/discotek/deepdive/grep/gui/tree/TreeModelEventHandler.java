package ca.discotek.deepdive.grep.gui.tree;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import ca.discotek.deepdive.grep.AbstractEventHandler;
import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.grep.MatchExpression;

public class TreeModelEventHandler extends AbstractEventHandler {

    DefaultTreeModel model;
    RootNode rootNode;
    
    public TreeModelEventHandler(MatchExpression matchExpression) {
        super(matchExpression);
        rootNode = new RootNode();
        model = new DefaultTreeModel(rootNode);
    }
    
    public DefaultTreeModel getModel() {
        return model;
    }

    @Override
    public void handleByteCodeReference(Location location, String className, String source, int lineNumber, String description) {
        LocationNode node = getLocationNode(location);
        ClassNode classNode = (ClassNode) getChildByName(className, node);
        if (classNode == null) {
            classNode = new ClassNode(className, source);
            try {
                insertNodeInto(model, classNode, node, node.getChildCount());
            } catch (RuntimeException e) {
                throw e;
            }
        }
        
        DescriptionNode descriptionNode = new DescriptionNode(description, lineNumber);
        insertNodeInto(model, descriptionNode, classNode, classNode.getChildCount());
    }
    
    @Override
    public void handleTextReference(Location location, String fileName, String description, int lineNumber, int start, int end) {
        LocationNode node = getLocationNode(location);
        TextFileNode textFileNode = (TextFileNode) getChildByName(fileName, node);
        if (textFileNode == null) {
            textFileNode = new TextFileNode(fileName);
            try {
                insertNodeInto(model, textFileNode, node, node.getChildCount());
            } 
            catch (RuntimeException e) {
                throw e;
            }
        }
        
        TextFileDescriptionNode descriptionNode = new TextFileDescriptionNode(description, lineNumber, start, end);
        insertNodeInto(model, descriptionNode, textFileNode, textFileNode.getChildCount());
    }
    
    void insertNodeInto(final DefaultTreeModel model, final MutableTreeNode child, final MutableTreeNode parent, final int index) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    model.insertNodeInto(child, parent, index);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public LocationNode getLocationNode(Location location) {
        Location parent = location.getParentLocation();
        
        if (parent == null) {
            String name = location.getName();
            LocationNode node = (LocationNode) getChildByName(name, rootNode);
            if (node == null) {
                node = new LocationNode(location);
                try {
                    insertNodeInto(model, node, rootNode, rootNode.getChildCount());
                } 
                catch (RuntimeException e) {
                    throw e;
                }
            }
            
            return node;
        }
        else {
            LocationNode parentNode = getLocationNode(parent);
            LocationNode childNode = (LocationNode) getChildByName(location.getName(), parentNode);
            if (childNode == null) {
                childNode = new LocationNode(location);
                try {
                    insertNodeInto(model, childNode, parentNode, parentNode.getChildCount());
         
                } 
                catch (RuntimeException e) {
                    throw e;
                }
            }
            return childNode;
        }
    }
    
    public static AbstractNode getChildByName(String name, DefaultMutableTreeNode parentNode) {
        int count = parentNode.getChildCount();
        AbstractNode child;
        for (int i=0; i<count; i++) {
            child = (AbstractNode) parentNode.getChildAt(i);
            if (child.getName().equals(name)) return child;
        }
        return null;
    }

}
