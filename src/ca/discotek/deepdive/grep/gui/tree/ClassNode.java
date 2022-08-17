package ca.discotek.deepdive.grep.gui.tree;

import ca.discotek.deepdive.grep.ClassGrepper;

public class ClassNode extends AbstractNode {

    public final String className;
    public final String source;
    
    public ClassNode(String className, String source) {
        this.className = className;
        this.source = source;
        setUserObject(ClassGrepper.slashToDotName(className));
    }
    
    @Override
    public String getName() {
        return className;
    }

}
