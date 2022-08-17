package ca.discotek.deepdive.security.gui;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ca.discotek.deepdive.security.dom.CssFile;
import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.ImageFile;
import ca.discotek.deepdive.security.dom.JavascriptFile;
import ca.discotek.deepdive.security.dom.JspFile;
import ca.discotek.common.image.ImageRenderer;
import ca.discotek.deepdive.security.swing.htmlimage.MyHtmlEditorKit;

public class WarArtifactNode extends DefaultMutableTreeNode implements Viewable {

    final SimpleDateFormat FORMAT = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
    
    public final Object fileObject;
    public final String path;
    public final long date;
    public final byte bytes[];
    
    public WarArtifactNode(CssFile file) {
        super(file.path);
        this.fileObject = file;
        this.path = file.path;
        this.date = file.date;
        this.bytes = file.bytes;
    }
    
    public WarArtifactNode(JavascriptFile file) {
        super(file.path);
        this.fileObject = file;
        this.path = file.path;
        this.date = file.date;
        this.bytes = file.bytes;
    }
    
    public WarArtifactNode(JspFile file) {
        super(file.path);
        this.fileObject = file;
        this.path = file.path;
        this.date = file.date;
        this.bytes = file.bytes;
    }
    
    public WarArtifactNode(ImageFile file) {
        super(file.path);
        this.fileObject = file;
        this.path = file.path;
        this.date = file.date;
        this.bytes = file.bytes;
    }
    
    public WarArtifactNode(HtmlFile file) {
        super(file.path);
        this.fileObject = file;
        this.path = file.path;
        this.date = file.date;
        this.bytes = file.bytes;
    }

    @Override
    public String getText() throws IOException {
        
        if (fileObject instanceof ImageFile) {
            StringBuilder buffer = new StringBuilder();
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                buffer.append("<html>");
                buffer.append("<table>");
                
                buffer.append("<tr>");
                buffer.append("<th align=\"right\">");
                buffer.append("Name");
                buffer.append("</th>");
                buffer.append("<td>");
                buffer.append(path);
                buffer.append("</td>");
                buffer.append("</tr>");
                
                buffer.append("<tr>");
                buffer.append("<th align=\"right\">");
                buffer.append("Date");
                buffer.append("</th>");
                buffer.append("<td>");
                buffer.append(FORMAT.format(date));
                buffer.append("</td>");
                buffer.append("</tr>");
                
                buffer.append("<tr>");
                buffer.append("<th align=\"right\">");
                buffer.append("Size (bytes)");
                buffer.append("</th>");
                buffer.append("<td>");
                buffer.append(bytes.length);
                buffer.append("</td>");
                buffer.append("</tr>");
                
                buffer.append("<tr>");
                buffer.append("<th align=\"right\">");
                buffer.append("Width (pixels)");
                buffer.append("</th>");
                buffer.append("<td>");
                buffer.append(image.getWidth());
                buffer.append("</td>");
                buffer.append("</tr>");
                
                buffer.append("<tr>");
                buffer.append("<th align=\"right\">");
                buffer.append("Height (pixels)");
                buffer.append("</th>");
                buffer.append("<td>");
                buffer.append(image.getHeight());
                buffer.append("</td>");
                buffer.append("</tr>");
                
                buffer.append("</table>");
                buffer.append("</html>");

            }
            catch (Exception e) {
                buffer.append("Invalid image.");
            }
            return buffer.toString();
        }

        return null;
    }

    @Override
    public String getRaw() throws IOException {
        return new String(bytes);
    }
    
    public JComponent getRendered() {
        if (fileObject instanceof ImageFile)
            return new ImageRenderer( ((ImageFile) fileObject).bytes);
        else if (fileObject instanceof HtmlFile) {
            Document doc =  Jsoup.parse(new String(bytes));
            Elements elements = doc.getElementsByTag("html");
            
            if (elements == null || elements.size() == 0)
                elements = doc.getElementsByTag("HTML");
            if (elements == null || elements.size() == 0)
                return new JLabel("No <HTML> tag found.");
            else  {
                Attributes at = elements.get(0).attributes();
                Iterator<Attribute> it = at.iterator();
                String key;
                while (it.hasNext()){
                    key = it.next().getKey();
                    at.remove(key);
                }
                
                WarNode warNode = (WarNode) getParent().getParent();
                JEditorPane renderer = new JEditorPane();
                renderer.setEditable(false);
                MyHtmlEditorKit editorKit = new MyHtmlEditorKit(warNode, (HtmlFile) fileObject);
                renderer.setEditorKit(editorKit);
                renderer.setContentType("text/html");
                String text = elements.get(0).toString().trim();
                
                CssFile files[] = warNode.war.getCssFiles();
                elements = doc.getElementsByTag("link");
                Element element;
                Attributes attributes;
                String value;
                StyleSheet styleSheet = new StyleSheet();
                boolean foundStyle = false;
                for (int i=0; i<elements.size(); i++) {
                    element = elements.get(i);
                    attributes = element.attributes();
                    if (attributes != null) {
                        value = attributes.get("rel");
                        if (value.equalsIgnoreCase("stylesheet")) {
                            value = attributes.get("href");
                            byte bytes[] = getCss( (HtmlFile) fileObject, value, files);
                            if (bytes != null) {
                                StringReader r = new StringReader(new String(bytes));
                                try { 
                                    styleSheet.loadRules(r, null);
                                    foundStyle = true;
                                }
                                catch (Exception e) {
                                    System.out.println("Error loading style at " + value);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                
                if (foundStyle)
                    editorKit.setStyleSheet(styleSheet);
                
                renderer.setText(text);
                renderer.setCaretPosition(0);
                return new JScrollPane(renderer);
            }
        }
        
        return null;
    }
    
    byte[] getCss(HtmlFile htmlFile, String href, CssFile files[]) {
        String currentPathChunks[] = htmlFile.path.split("/");
        String requestChunks[] = href.split("/");
        String cssChunks[];
        
        for (int i=0; i<files.length; i++) {
            cssChunks = files[i].path.split("/");
            
            int count = 0;
            for (int j=0; j<requestChunks.length; j++) {
                if (requestChunks[j].equals(".."))
                    count++;
                else
                    break;
            }

            List<String> list = new ArrayList<String>();
            if (count > 0) {
                for (int j=0; j<currentPathChunks.length-count-1; j++)
                    list.add(currentPathChunks[j]);
                
                for (int j=count; j<requestChunks.length; j++)
                    list.add(requestChunks[j]);
            }
            else
                list.addAll(Arrays.asList(requestChunks));
            

            String chunks[] = list.toArray(new String[list.size()]);
            
            if (chunks.length == cssChunks.length) {
                boolean found = true;
                for (int j=0; j<chunks.length; j++) {
                    if (!chunks[j].equals(cssChunks[j])) {
                        found = false;
                        break;
                    }
                }
                
                if (found)
                    return files[i].bytes;
            }
            
        }
        
        return null;
    }
}
