package ca.discotek.deepdive.security.gui;

import java.util.jar.Manifest;

import ca.discotek.deepdive.security.UnknownFile;
import ca.discotek.deepdive.security.dom.ClassFile;
import ca.discotek.deepdive.security.dom.EjbJarXml;
import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.Jar;

public class JarNode extends AbstractNode {
    
    public final Jar jar;
    
    public JarNode(Jar jar) {
        super(jar.getPath().getLastPath());
        this.jar = jar;
        
        EjbJarXml xml = jar.getEjbJarXml();
        if (xml != null)
            add(new EjbJarXmlNode(xml));
        
        Manifest manifest = jar.getManifest();
        if (manifest != null) 
            add(new ManifestNode(manifest));

        Jar unknownJars[] = jar.getUnknownJars();
        if (unknownJars.length > 0) {
            for (int i=0; i<unknownJars.length; i++)
                add(new JarNode(unknownJars[i]));
        }
        
        ClassFile classFiles[] = jar.getClassFiles();
        for (int i=0; i<classFiles.length; i++)
            add(new ClassFileNode(classFiles[i]));
        
        UnknownFile unknownFiles[] = jar.getUnknownFiles();
        for (int i=0; i<unknownFiles.length; i++) {
            if (xml != null && unknownFiles[i].getPath().equals(Jar.EJB_JAR_XML_PATH))
                continue;
            else if (manifest != null && unknownFiles[i].getPath().equals(Jar.MANIFEST_PATH))
                continue;
            else
                add(new UnknownFileNode(unknownFiles[i]));
        }
    }
}