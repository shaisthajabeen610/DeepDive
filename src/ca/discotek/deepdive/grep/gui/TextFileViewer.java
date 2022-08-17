package ca.discotek.deepdive.grep.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.tree.TreeNode;

import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.grep.ResourceUtil;
import ca.discotek.deepdive.grep.Location.ArchiveLocation;
import ca.discotek.deepdive.grep.Location.DirectoryLocation;
import ca.discotek.deepdive.grep.gui.tree.ClassNode;
import ca.discotek.deepdive.grep.gui.tree.LocationNode;
import ca.discotek.deepdive.grep.gui.tree.TextFileDescriptionNode;
import ca.discotek.deepdive.grep.gui.tree.TextFileNode;
import ca.discotek.deepdive.io.IOUtil;
import ca.discotek.deepdive.security.gui.decompiler.DecompileUtil;

public class TextFileViewer extends JPanel {

    static final String NEW_LINE = System.getProperty("line.separator");
    
    JTextPane textPane;
    Highlighter highlighter;
    HighlightPainter selectedHighlightPainter;
    HighlightPainter highlightPainter;
    
    TextFileDescriptionNode selectedTextFileDescriptionNode = null;
    TextFileDescriptionNode allTextFileDescriptionNode[] = null;
    int highlightOffsets[][];
    
    public TextFileViewer() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Courier New", Font.PLAIN, 12));
        add(new JScrollPane(textPane), BorderLayout.CENTER);
        
        highlighter = textPane.getHighlighter();
        selectedHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);

    }
    
    public void clear() {
        textPane.setText("");
        selectedTextFileDescriptionNode = null;
        allTextFileDescriptionNode = null;
    }


    
    public void setLocation(TextFileNode node) {
        TextFileDescriptionNode descriptionNode = (TextFileDescriptionNode) node.getChildAt(0);
        setLocation(descriptionNode);
    }
    
    public void setLocation(TextFileDescriptionNode node) {
        try {
            if (node == selectedTextFileDescriptionNode) return;
            else if (selectedTextFileDescriptionNode != null && selectedTextFileDescriptionNode.getParent() == node.getParent()) {
                selectedTextFileDescriptionNode = node;
                updateHighlights();
                return;
            }
            else {
                selectedTextFileDescriptionNode = node;
                TreeNode textFileNode = node.getParent();
                int count = textFileNode.getChildCount();
                allTextFileDescriptionNode = new TextFileDescriptionNode[count];
                for (int i=0; i<count; i++) 
                    allTextFileDescriptionNode[i] = (TextFileDescriptionNode) textFileNode.getChildAt(i);
                highlightOffsets = new int[count][2];

                update();
                updateHighlights();
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured. See stderr.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void setLocation(ClassNode node) {
        LocationNode locationNode = (LocationNode) node.getParent();
        try {
            InputStream is = ResourceUtil.getResource(locationNode.location, node.className);
            
            int index = node.className.lastIndexOf('.');
            if (index < 0)
                index = 0;
            else
                index++;
            
            String s = node.className.substring(index, node.className.length());
            
            File tmpFile = ca.discotek.deepdive.security.TempFileUtil.createTemporaryFile(is, s);
            String source = DecompileUtil.decomile(tmpFile);
            textPane.setText(source);
            textPane.setCaretPosition(0);
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    void updateHighlights() throws Exception {
        highlighter.removeAllHighlights();
        
        HighlightPainter painter;
        for (int i=0; i<highlightOffsets.length; i++) {
            if (selectedTextFileDescriptionNode == allTextFileDescriptionNode[i]) {
                painter = selectedHighlightPainter;
                textPane.setCaretPosition(highlightOffsets[i][0]);
            }
            else
                painter = highlightPainter;
            highlighter.addHighlight(highlightOffsets[i][0], highlightOffsets[i][1], painter);
        }
    }
    
    void update() throws IOException {
        TextFileNode textFileNode = (TextFileNode) selectedTextFileDescriptionNode.getParent();
        LocationNode locationNode = (LocationNode) textFileNode.getParent();
        Location location = locationNode.location;
        String name = textFileNode.getName();
        
        InputStream is = ResourceUtil.getResource(location, name);
        if (is == null)
            throw new IllegalStateException("Unable to find resource for " + location + ", " + name);
        
        StringBuilder buffer = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        int lineCount = 1;
        int newLineCount = 0;
        while ( (line = br.readLine()) != null) {
            if (buffer.length() > 0) {
                buffer.append(NEW_LINE);
                newLineCount++;
            }
            
            for (int i=0; i<allTextFileDescriptionNode.length; i++) {
                if (lineCount == allTextFileDescriptionNode[i].lineNumber) {
                    highlightOffsets[i][0] = buffer.length() - newLineCount + allTextFileDescriptionNode[i].start;
                    highlightOffsets[i][1] = buffer.length() - newLineCount + allTextFileDescriptionNode[i].end;
                }
            }
            
            buffer.append(line);
            lineCount++;
        }
        
        textPane.setText(buffer.toString());
    }
}
