package ca.discotek.deepdive.security.gui;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.dom.ApplicationXml;
import ca.discotek.deepdive.security.dom.Ear;

public class ApplicationXmlNode extends AbstractNode implements Viewable {
    public final ApplicationXml applicationXml;
    
    public ApplicationXmlNode(ApplicationXml applicationXml) {
        super(Ear.APPLICATION_XML_PATH);
        this.applicationXml = applicationXml;
    }
    
    public String getText() {
        return applicationXml.toString();
    }
    
    public String getRaw() throws IOException {
        return new String(applicationXml.bytes);
    }
    
    public JComponent getRendered() {
        return null;
    }
}