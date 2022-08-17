package ca.discotek.deepdive.security.gui;

import java.util.jar.Manifest;

import javax.swing.tree.MutableTreeNode;

import ca.discotek.deepdive.security.UnknownFile;
import ca.discotek.deepdive.security.dom.ClassFile;
import ca.discotek.deepdive.security.dom.CssFile;
import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.ImageFile;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.JavascriptFile;
import ca.discotek.deepdive.security.dom.JspFile;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.visitor.ArchiveReader;

public class WarNode extends AbstractNode {
        public final War war;
        
        public final MutableTreeNode webInfNode;
        public final MutableTreeNode webInfClassesNode;
        public final MutableTreeNode libsNode;
        public final MutableTreeNode jspNode;
        public final MutableTreeNode cssNode;
        public final MutableTreeNode imageNode;
        public final MutableTreeNode htmlNode;
        public final MutableTreeNode javascriptNode;
        
        public WarNode(War war) {
            super(war.getPath().getLastPath());
            this.war = war;
            
            this.webInfNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.WEB_INF_NODE);
            add(webInfNode);
            
            this.webInfClassesNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.WEB_INF_CLASSES_NODE);
            webInfNode.insert(webInfClassesNode, 0);
            
            this.libsNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.LIBS_NODE);
            add(libsNode);
            
            this.jspNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.JSP_NODE);
            add(jspNode);
            
            this.cssNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.CSS_NODE);
            add(cssNode);
            
            this.imageNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.IMAGE_NODE);
            add(imageNode);
            
            this.htmlNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.HTML_NODE);
            add(htmlNode);
            
            this.javascriptNode = (MutableTreeNode) NodeRegistry.getNodeObject(NodeRegistry.JAVASCRIPT_NODE);
            add(javascriptNode);
            
            Manifest manifest = war.getManifest();
            if (manifest != null) {
                add(new ManifestNode(manifest));
            }
            
            webInfNode.insert(new WebXmlNode(war.getWebXml()), 0);
            

            ClassFile classFiles[] = war.getClassFiles();
            for (int i=0; i<classFiles.length; i++)
                webInfClassesNode.insert(new ClassFileNode(classFiles[i]), webInfClassesNode.getChildCount());
            
            Jar jars[] = war.getLibJars();
            for (int i=0; i<jars.length; i++)
                libsNode.insert(new JarNode(jars[i]), libsNode.getChildCount());
            
            HtmlFile htmlFiles[] = war.getHtmlFiles();
            for (int i=0; i<htmlFiles.length; i++)
                htmlNode.insert(new WarArtifactNode(htmlFiles[i]), htmlNode.getChildCount());
            
            CssFile cssFiles[] = war.getCssFiles();
            for (int i=0; i<cssFiles.length; i++)
                cssNode.insert(new WarArtifactNode(cssFiles[i]), cssNode.getChildCount());
            
            ImageFile imageFiles[] = war.getImageFiles();
            for (int i=0; i<imageFiles.length; i++)
                imageNode.insert(new WarArtifactNode(imageFiles[i]), imageNode.getChildCount());
            
            JavascriptFile javascriptFiles[] = war.getJavascriptFiles();
            for (int i=0; i<javascriptFiles.length; i++)
                javascriptNode.insert(new WarArtifactNode(javascriptFiles[i]), javascriptNode.getChildCount());
            
            JspFile jspFiles[] = war.getJspFiles();
            for (int i=0; i<jspFiles.length; i++)
                jspNode.insert(new WarArtifactNode(jspFiles[i]), jspNode.getChildCount());
            
            UnknownFile unknownFiles[] = war.getUnknownFiles();
            UnknownFileNode node;
            for (int i=0; i<unknownFiles.length; i++) {
                node = new UnknownFileNode(unknownFiles[i]);
                if (unknownFiles[i].getPath().startsWith("WEB-INF"))
                    webInfNode.insert(node, webInfNode.getChildCount());
                else
                    add(node);
            }
        }
    }