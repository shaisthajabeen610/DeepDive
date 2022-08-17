package ca.discotek.deepdive.security.gui;

import java.io.IOException;

import javax.swing.JComponent;

public interface Viewable {

    public String getText() throws IOException;
    public String getRaw() throws IOException;
    public JComponent getRendered() throws IOException;
}
