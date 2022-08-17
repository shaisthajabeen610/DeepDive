package ca.discotek.deepdive.security.gui;

import java.io.IOException;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.UnknownFile;

public class ClassesDexNode extends AbstractNode implements Viewable {
    
    byte bytes[];
    
    public ClassesDexNode(byte bytes[]) {
        super("classes.dex");
    }

    public String getText() throws IOException {
        return null;
    }

    public String getRaw() throws IOException {
        return null;
    }
    
    public JComponent getRendered() {
        return null;
    }
}