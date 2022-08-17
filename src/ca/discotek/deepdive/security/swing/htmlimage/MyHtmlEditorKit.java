package ca.discotek.deepdive.security.swing.htmlimage;

import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.gui.WarNode;


public class MyHtmlEditorKit extends HTMLEditorKit {

    class MyHTMLFactory extends HTMLFactory {
        
        WarNode warNode;
        HtmlFile htmlFile;
        
        MyHTMLFactory(WarNode warNode, HtmlFile htmlFile) {
            this.warNode = warNode;
            this.htmlFile=htmlFile;
        }
        
        public View create(Element elem) {
            Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
            if (o instanceof HTML.Tag) {
              HTML.Tag kind = (HTML.Tag) o;
              if (kind == HTML.Tag.IMG)
                return new MyImageView(elem, warNode, htmlFile);
            }
            return super.create( elem );
          }
    }
    
    MyHTMLFactory factory;

    public MyHtmlEditorKit(WarNode warNode, HtmlFile htmlFile) {
        factory = new MyHTMLFactory(warNode, htmlFile);
    }
    
    public ViewFactory getViewFactory() {
        return factory;
    }
}
