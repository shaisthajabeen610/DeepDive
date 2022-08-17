package ca.discotek.deepdive.security.gui.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class CategoryNameEditor extends JPanel {
    JTextField nameField;
    
    public CategoryNameEditor() {
        buildGui(null);
    }
    
    public CategoryNameEditor(String name) {
        buildGui(name);
    }
    
    void buildGui(String name) {
        setLayout(new BorderLayout());
        
        JLabel label = new JLabel("<html>Warning: This action cannot be reversed with the <i>Configuration Editors</i>'s <i>Cancel</i> button.</html>");
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(label, BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10,10,10,10);
        
        panel.add(new JLabel("Category Name"), gbc);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx++;
        nameField = new JTextField(20);
        nameField.setText(name == null ? "" : name);
        panel.add(nameField, gbc);
        
        
        add(panel, BorderLayout.CENTER);
    }
    
    public String getCategoryName() {
        return nameField.getText();
    }
}

