package ca.discotek.deepdive.security.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


import ca.discotek.common.swing.event.EventConstants;
import ca.discotek.projectmanager.ProjectManager;

public class NameEditor extends JPanel {
    JTextField nameField;
    
    public NameEditor() {
        buildGui(null);
    }
    
    public NameEditor(String name) {
        buildGui(name);
    }
    
    void buildGui(String name) {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10,10,10,10);
        
        add(new JLabel("New Project Name"), gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx++;
        nameField = new JTextField(20);
        nameField.setText(name == null ? "" : name);
        add(nameField, gbc);
    }
    
    public String getName() {
        return nameField.getText();
    }
}

