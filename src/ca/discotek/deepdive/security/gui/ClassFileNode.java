package ca.discotek.deepdive.security.gui;

import java.io.IOException;

import javax.swing.JComponent;

import ca.discotek.deepdive.grep.ClassGrepper;
import ca.discotek.deepdive.security.ResourceUtil;
import ca.discotek.deepdive.security.asm.ClassTemplate;
import ca.discotek.deepdive.security.dom.ClassFile;

public class ClassFileNode extends AbstractNode implements Viewable {
    
    public final ClassFile classFile;
    
    static String getName(String path) {
        return path.startsWith("WEB-INF/classes/") ?
               path.substring("WEB-INF/classes/".length(), path.length()).replace('/', '.') : 
               path;
    }
    
    public ClassFileNode(ClassFile classFile) {
        super(classFile.path);
        setUserObject(getName(classFile.path));
        this.classFile = classFile;
    }

    public String getRaw() throws IOException {
        return null;
    }

    public String getText() throws IOException {
        byte bytes[] = classFile.bytes;
        return ClassTemplate.getClassTemplate(bytes).toString();
    }
    
    public JComponent getRendered() {
        return null;
    }
}