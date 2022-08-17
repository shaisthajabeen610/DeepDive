package ca.discotek.deepdive.security.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ca.discotek.common.swing.DialogWrapper;

public class LinksField extends JPanel {

    JTextField linksField;
    
    List<String> linkList = new ArrayList<String>();
    
    public LinksField() {
        this(new String[0]);
    }
    
    public LinksField(String links[]) {
        if (links != null)
            linkList.addAll(Arrays.asList(links));
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,2,2,2);
        
        linksField = new JTextField();
        linksField.setEditable(false);
        add(linksField, gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JButton button = new JButton("Edit...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LinksEditor editor = new LinksEditor(linkList.toArray(new String[linkList.size()]));
                Window w = SwingUtilities.windowForComponent(LinksField.this);
                boolean wasCanceled = DialogWrapper.showDialogWrapper(w, "Link Editor", editor, new Dimension(400, 400));
                if (!wasCanceled) {
                    String links[] = editor.getLinks();
                    linkList.clear();
                    linkList.addAll(Arrays.asList(links));
                    updateLinkField();
                }
            }
        });
        
        add(button, gbc);
        
        updateLinkField();
    }
    
    void updateLinkField() {
        if (linkList.size() == 0)
            linksField.setText("<No links defined>");
        else {
            StringBuilder buffer = new StringBuilder();
            
            Iterator<String> it = linkList.listIterator();
            while (it.hasNext()) {
                buffer.append(it.next());
                if (it.hasNext())
                    buffer.append(", ");
            }
            
            linksField.setText(buffer.toString());
        }
    }
    
    public String[] getLinks() {
        return linkList.toArray(new String[linkList.size()]);
    }
    
    public void setLinks(String links[]) {
        linkList.clear();
        if (links != null)
            linkList.addAll(Arrays.asList(links));
        updateLinkField();
    }
}
