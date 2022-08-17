package ca.discotek.deepdive.security.gui;

import java.io.IOException;
import java.util.jar.Manifest;

import javax.swing.JComponent;

import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.dom.Jar;

public class ManifestNode extends AbstractNode implements Viewable {
    
    public final Manifest manifest;
    
    public ManifestNode(Manifest manifest) {
        super(Jar.MANIFEST_PATH);
        this.manifest = manifest;
    }

    public String getText() throws IOException {
        return DistillUtil.getDistilledManifest(manifest);
    }

    public String getRaw() throws IOException {
        String path[] = DeploymentViewer.getPath(this);
        return new String(ResourceUtil.getResource(path));
    }
    
    public JComponent getRendered() {
        return null;
    }
}