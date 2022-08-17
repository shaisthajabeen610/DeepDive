package ca.discotek.deepdive.security.gui;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.dom.EjbJarXml;

class EjbJarXmlNode extends AbstractNode implements Viewable {
    public final EjbJarXml ejbJarXml;
    
    public EjbJarXmlNode(EjbJarXml ejbJarXml) {
        super("META-INF/ejb-jar.xml");
        this.ejbJarXml = ejbJarXml;
    }
    
    public String getText() {
        return ejbJarXml.toString();
    }
    
    public String getRaw() throws IOException {
        return new String(ejbJarXml.bytes);
    }
    
    public JComponent getRendered() {
        return null;
    }
}