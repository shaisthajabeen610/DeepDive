package ca.discotek.deepdive.grep.gui.tree;

public class TextFileDescriptionNode extends AbstractNode {

    public final String description;
    public final int lineNumber;
    public final int start;
    public final int end;
    
    public TextFileDescriptionNode(String description, int lineNumber, int start, int end) {
        this.description = description;
        this.lineNumber = lineNumber;
        this.start = start;
        this.end = end;
        setUserObject(description);
    }
    
    @Override
    public String getName() {
        return description;
    }

}
