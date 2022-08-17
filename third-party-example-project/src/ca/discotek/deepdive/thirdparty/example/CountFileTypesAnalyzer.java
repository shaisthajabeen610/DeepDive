package ca.discotek.deepdive.thirdparty.example;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.security.UnknownFile;
import ca.discotek.deepdive.security.dom.ClassFile;
import ca.discotek.deepdive.security.dom.CssFile;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.HtmlFile;
import ca.discotek.deepdive.security.dom.ImageFile;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.JavascriptFile;
import ca.discotek.deepdive.security.dom.JspFile;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.visitor.assess.AbstractDeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerException;
import ca.discotek.deepdive.security.visitor.assess.OutputException;

public class CountFileTypesAnalyzer extends AbstractDeploymentVisitor {
    
    static final String UNKNOWN_TYPE = "Unknown";
    
    Map<String, Integer> typeCountMap = new HashMap<>();

    public CountFileTypesAnalyzer(String title, String summary, String description, String[] links) {
        super(title, summary, description, links);
    }
    
    void addUnknownType(String file) {
        int index = file.lastIndexOf('.');
        String type = index > -1 ? file.substring(index+1, file.length()) : UNKNOWN_TYPE;
        addType(type);
    }
    
    void addType(String type) {
        Integer count = typeCountMap.get(type);
        typeCountMap.put(type, count == null ? 1 : ++count);
    }
    
    
    public void visitStart() throws AnalyzerException {
        typeCountMap.clear();
    }

    public void visitClass(ClassFile classFile, Location location) throws AnalyzerException {
        addType("class");
    }
    
    public void visitEar(Ear ear, Location location) throws AnalyzerException {
        addType("ear");
    }
    
    public void visitEarLib(Jar jar, Location location) throws AnalyzerException {
        addType("jar");
    }
    
    public void visitUnknownJar(Jar jar, Location location) throws AnalyzerException {
        addType("jar");
    }
    
    public void visitWar(War war, Location location) throws AnalyzerException {
        addType("war");
    }
    
    public void visitEjbJar(Jar jar, Location location) throws AnalyzerException {
        addType("jar");    
    }
    
    public void visitWebInfClass(ClassFile classFile, Location location) throws AnalyzerException {
        addType("class");    
    }
    
    public void visitWebInfLibJar(Jar jar, Location location) throws AnalyzerException {
        addType("jar");    
    }
    
    public void visitJsp(JspFile jsp, Location location) throws AnalyzerException {
        addType("jsp");    
    }
    
    public void visitImage(ImageFile image, Location location) throws AnalyzerException {
        addUnknownType(image.path);    
    }
    
    public void visitJavascript(JavascriptFile javascript, Location location) throws AnalyzerException {
        addType("js");    
    }
    
    public void visitCss(CssFile css, Location location) throws AnalyzerException {
        addType("css");    
    }
    
    public void visitHtml(HtmlFile html, Location location) throws AnalyzerException {
        addType("html");    
    }
    
    public void visitUnknownFile(UnknownFile unknownFile, Location location) throws AnalyzerException {
        addUnknownType(unknownFile.getPath());    
    }
    
    public void visitManifest(Manifest manifest, Location location) throws AnalyzerException {
        addType("mf");
    }
    
    

    @Override
    public void outputToHtml(StringBuilder buffer, boolean hyperlinkPolicy, Pattern fileFilterPattern) throws OutputException {
        
        Map.Entry<String, Integer> entries[] = typeCountMap.entrySet().toArray(new Map.Entry[typeCountMap.size()]);
        Arrays.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
                return entry2.getValue() - entry1.getValue();
            }
        });
        
        buffer.append("<table>");
        buffer.append("<tr><th>Type</th><th>Count</th></tr>");
        for (int i=0; i<entries.length; i++) {
            buffer.append("<tr>");
            buffer.append("<td>");
            buffer.append(entries[i].getKey());
            buffer.append("</td>");
            buffer.append("<td>");
            buffer.append(entries[i].getValue());
            buffer.append("</td>");
            buffer.append("</tr>");
        }
        buffer.append("</table>");
    }

}
