package ca.discotek.deepdive.security.gui;

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

public class TextViewer extends JPanel {

    static final String NEW_LINE = System.getProperty("line.separator");
    
    JTextPane textPane;
    
    public TextViewer() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        textPane = new JTextPane();
        textPane.setFont(new Font("Courier New", Font.PLAIN, 12));
        textPane.setEditable(false);
        add(new JScrollPane(textPane), BorderLayout.CENTER);
    }
    
    public void clear() {
        textPane.setText("");
    }

    public void setText(String text, boolean forceNoHtml) {
        if (text == null)
            clear();
        else {
            if (!forceNoHtml && text.toLowerCase().startsWith("<html>"))
                textPane.setContentType("text/html");
            textPane.setText(text);
            textPane.setCaretPosition(0);
        }
    }
}
