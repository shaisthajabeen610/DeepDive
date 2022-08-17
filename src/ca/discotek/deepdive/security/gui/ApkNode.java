package ca.discotek.deepdive.security.gui;

import java.io.File;

import javax.swing.tree.MutableTreeNode;

import ca.discotek.deepdive.security.dom.Apk;
import ca.discotek.deepdive.security.dom.ClassFile;
import ca.discotek.deepdive.security.visitor.assess.custom.path.util.ExplodedApk;


public class ApkNode extends AbstractNode {
    
    public final Apk apk;
    
    AbstractNode classesDexNode;
    
    public ApkNode(Apk apk) {
        super(apk.path);
        this.apk = apk;
        
        
        String fileNames[] = apk.getOtherFileNames();
        for (int i=0; i<fileNames.length; i++) {
            add(new UnknownFileNode( apk.getOtherFile(fileNames[i])));
        }

        this.classesDexNode = (AbstractNode) NodeRegistry.getNodeObject(NodeRegistry.CLASSES_DEX_NODE, byte[].class, apk.getDexClasses());
        add(classesDexNode);
        
        ClassFile classes[] = apk.getClasses();
        ClassFileNode node;
        for (int i=0; i<classes.length; i++) {
            node = (ClassFileNode) NodeRegistry.getNodeObject(NodeRegistry.CLASSFILE_NODE, ClassFile.class, classes[i]);
            classesDexNode.add(node);
        }
    }
}