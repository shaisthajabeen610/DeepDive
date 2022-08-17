package ca.discotek.deepdive.grep.gui.tree;

public class TextFileNode extends AbstractNode {

    public final String fileName;
    
    public TextFileNode(String fileName) {
        this.fileName = fileName;
        setUserObject(fileName);
    }
    
    @Override
    public String getName() {
        return fileName;
    }

}
