package ca.discotek.deepdive.security.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.discotek.deepdive.DeepDive;
import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.grep.ResourceUtil;

import ca.discotek.deepdive.grep.gui.GrepGui;
import ca.discotek.deepdive.security.TempFileUtil;
import ca.discotek.deepdive.security.gui.ReportViewer;
import ca.discotek.deepdive.security.gui.decompiler.SourceViewer;
import ca.discotek.deepdive.security.misc.DecompilerLauncher;

public class LinkLauncher {
    
    static SourceViewer sourceViewer = null;

    public static void launch(String link, JEditorPane editor) {
        int index = link.indexOf("://");
        if (index < 0) {
            JOptionPane.showMessageDialog(editor, "Invalid URL: " + link, "Resource Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String protocol = link.substring(0, index);
        if (protocol.startsWith("http")) {
            try {
                URI uri = new URI(link);
                Desktop.getDesktop().browse(uri);
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(editor, "Could not browse URL: " + link + ". See stderr for details.", "URL Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            String path = link.substring(index + "://".length());
            
//            String chunks[] = path.split("!/");
            String chunks[] = path.split("->");

            File file = new File(chunks[0]);
            Location location;
            if (file.isFile()) {
                Location directoryLocation = new Location.DirectoryLocation(file.getParentFile().getAbsolutePath());
                location = new Location.ArchiveLocation(file.getName());
                location.setParentLocation(directoryLocation);
            }
            else {
                location = new Location.DirectoryLocation(file.getAbsolutePath());
            }
            
//            location = new Location.ArchiveLocation(file.getName());
//            location.setParentLocation(parentLocation);
            Location parentLocation = location;
            
            for (int i=1; i<chunks.length-1; i++) {
//                if (chunks[i].equals("lib") && parentLocation instanceof Location.ArchiveLocation)
//                    location = new Location.EarLibLocation();
//                if (chunks[i].equals("WEB-INF/lib") && parentLocation instanceof Location.ArchiveLocation)
//                    location = new Location.WebInfLibLocation();
//                else if (chunks[i].equals("WEB-INF/classes") && parentLocation instanceof Location.ArchiveLocation)
//                    location = new Location.WebInfClassesLocation();
//                else
                    location = new Location.ArchiveLocation(chunks[i]);
                location.setParentLocation(parentLocation);
                parentLocation = location;
            }


            try {
//                InputStream is = ResourceUtil.getResource(location, chunks[chunks.length-1]);
                InputStream is = ResourceUtil.getResource(location, chunks[chunks.length-1]);
                if (is == null) {
                    String message = 
                        "Bug in analyzer. Couldn't find class resource in location " + path;
                    JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(editor), message, "Resource Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
     
                String s = chunks[chunks.length-1];
                int slashIndex = s.lastIndexOf('/');
                String name = s.substring(slashIndex+1, s.length());
                
                File tmpFile = TempFileUtil.createTemporaryFile(is, name);
                
//                if (protocol.equals("class")) {
//                    String decompilerPath = System.getProperty(GrepGui.DECOMPILER_PATH_SYSTEM_PROPERTY_NAME);
//                    if (decompilerPath == null)
//                        JOptionPane.showMessageDialog
//                            (editor, "Decompiler path not set. See Edit->Preferences.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
//                    else {
//                        DecompilerLauncher launcher = new DecompilerLauncher(decompilerPath);
//                        try {
//                            launcher.launch(tmpFile);
//                        } 
//                        catch (Exception e2) {
//                            e2.printStackTrace();
//                            JOptionPane.showMessageDialog
//                                (editor, "Error launching decompiler. See sterr.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
//                        }
//                    }
//                }
//                else if (protocol.equals("os")) {
                
                try {
                    if (name.endsWith(".class") && DeepDive.getDecompile()) {
                        if (sourceViewer == null) {
                            sourceViewer = new SourceViewer();
                            sourceViewer.setSize(800, 600);
                            Window w = SwingUtilities.windowForComponent(editor);
                            sourceViewer.setLocationRelativeTo(w);
                        }
                        
                        sourceViewer.addClass(tmpFile);
                        sourceViewer.setVisible(true);
                    }
                    else
                        Desktop.getDesktop().open(tmpFile);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Window w = SwingUtilities.windowForComponent(editor);
                    JOptionPane.showMessageDialog(w, 
                            "Failed to open resource. You probably need to associate a program with the given file extension. See stderr for more details.", 
                            "Launch Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
//                }
//                else
//                    JOptionPane.showMessageDialog(editor, "Unknown url format: " + link);
            }
            catch (IOException e) {
                e.printStackTrace();
                Window w = SwingUtilities.windowForComponent(editor);
                JOptionPane.showMessageDialog(w, "Error opening resource. See sterr.", "Resource Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
