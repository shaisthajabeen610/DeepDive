package ca.discotek.deepdive.security.gui.editor;

public class CustomAnalyzerDescriptor {

    public final String title;
    public final String summary;
    public final String description;
    public final String links[];
    public final String className;
    
    public CustomAnalyzerDescriptor(String title, String summary, String description, String links[], String className) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.links = links;
        this.className = className;
    }
}
