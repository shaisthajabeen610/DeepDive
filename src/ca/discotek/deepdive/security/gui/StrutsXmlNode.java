package ca.discotek.deepdive.security.gui;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.dom.WebXml;

public class StrutsXmlNode extends AbstractNode implements Viewable {
    
    public final String path;
    public final byte bytes[];
    
    public StrutsXmlNode(String path, byte bytes[]) {
        super("struts.xml");
        this.path = path;
        this.bytes = bytes;
    }
    
    public String getText() {
        return null;
    }
    
    public String getRaw() throws IOException {
        return new String(bytes);
    }
    
    public JComponent getRendered() {
        return null;
    }
}