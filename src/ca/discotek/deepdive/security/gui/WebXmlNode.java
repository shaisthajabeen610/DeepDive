package ca.discotek.deepdive.security.gui;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.dom.WebXml;

public class WebXmlNode extends AbstractNode implements Viewable {
    public final WebXml webXml;
    
    public WebXmlNode(WebXml webXml) {
        super("web.xml");
        this.webXml = webXml;
    }
    
    public String getText() {
        return webXml.toString();
    }
    
    public String getRaw() throws IOException {
        return new String(webXml.bytes);
    }
    
    public JComponent getRendered() {
        return null;
    }
}