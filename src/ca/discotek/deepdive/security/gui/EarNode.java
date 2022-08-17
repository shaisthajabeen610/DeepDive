package ca.discotek.deepdive.security.gui;

import java.util.jar.Manifest;

import javax.swing.tree.MutableTreeNode;

import ca.discotek.deepdive.security.dom.ApplicationXml;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;

public class EarNode extends AbstractNode {
    
    public final Ear ear;
    public final MutableTreeNode modulesNode;
    public final MutableTreeNode libsNode;
    
    public EarNode(Ear ear) {
        super(ear.path);
        this.ear = ear;
        
        this.modulesNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.MODULES_NODE);
        this.libsNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.LIBS_NODE);
        
        ApplicationXml applicationXml = ear.getApplicationXml();
        if (applicationXml != null) {
            MutableTreeNode applicationXmlNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.APPLICATION_XML_NODE, ApplicationXml.class, applicationXml);
            add(applicationXmlNode);
        }

        Manifest manifest = ear.getManifest();
        if (manifest != null) {
            MutableTreeNode manifestNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.MANIFEST_NODE, Manifest.class, manifest);
            add(manifestNode);
        }
        add(modulesNode);
        add(libsNode);
        
        String modules[] = ear.getModules();
        Object module;
        MutableTreeNode node;
        for (int i=0; i<modules.length; i++) {
            module = ear.getModule(modules[i]);
            if (module instanceof Jar) {
                Jar jar = (Jar) module;
                node = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.JAR_NODE, Jar.class, jar);
                modulesNode.insert(node, modulesNode.getChildCount());
            }
            else if (module instanceof War) {
                War war = (War) module;
                node = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.WAR_NODE, War.class, war);
                modulesNode.insert(node, modulesNode.getChildCount());
            }
        }
        
        String libraryNames[] = ear.getLibraryNames();
        for (int i=0; i<libraryNames.length; i++) {
            node = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.JAR_NODE, Jar.class, ear.getLibrary(libraryNames[i]));
            libsNode.insert(node, libsNode.getChildCount());
        }

        String fileNames[] = ear.getOtherFileNames();
        for (int i=0; i<fileNames.length; i++) {
            add(new UnknownFileNode( ear.getOtherFile(fileNames[i])));
        }
    }
}