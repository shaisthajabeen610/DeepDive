package ca.discotek.deepdive.grep.gui.tree;

public class DescriptionNode extends AbstractNode {

    public final String description;
    public final int lineNumber;
    
    public DescriptionNode(String description, int lineNumber) {
        this.description = description;
        this.lineNumber = lineNumber;
        setUserObject(description);
    }
    
    @Override
    public String getName() {
        return description;
    }

}
