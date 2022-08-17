package ca.discotek.deepdive.security.gui;

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ca.discotek.common.image.ImageRenderer;
import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.UnknownFile;
import ca.discotek.deepdive.security.dom.ImageFile;

public class UnknownFileNode extends AbstractNode implements Viewable {
    
    UnknownFile file;
    String path;

    
    static String getName(String path) {
        return path.startsWith("WEB-INF") ?
               path.substring("WEB-INF/".length(), path.length()) : 
               path;
    }
    
    public UnknownFileNode(UnknownFile file) {
        super(file.getPath());
        this.file = file;
        setUserObject(getName(file.getPath()));
        path = file.getPath().toLowerCase();
    }

    public String getText() throws IOException {
        return null;
    }

    public String getRaw() throws IOException {
        String path[] = DeploymentViewer.getPath(this);
        return new String(ResourceUtil.getResource(path));
    }
    
    public static String trimHtmlForSwingRender(byte bytes[]) {
        Document doc =  Jsoup.parse(new String(bytes));
        Elements elements = doc.getElementsByTag("html");
        
        if (elements == null || elements.size() == 0)
            elements = doc.getElementsByTag("HTML");
        if (elements == null || elements.size() == 0)
            return "No <HTML> tag found.";
        else
            return elements.get(0).toString().trim();
    }

    
    public JComponent getRendered() {
        if (path.endsWith(".html")) {
            JEditorPane renderer = new JEditorPane();
            renderer.setContentType("text/html");
            renderer.setText(trimHtmlForSwingRender(file.getBytes()));
            return renderer;
        }
        else if (path.endsWith(".gif") || path.endsWith(".jpg") || path.endsWith(".bmp") || path.endsWith(".tiff") || path.endsWith(".png")) 
            return new ImageRenderer( file.getBytes() );
        
        return null;
    }
}