package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class FieldEditorPanel extends JPanel {
    
    JComponent comp;
    JButton button;
    
    public FieldEditorPanel(JComponent comp, JButton button) {
        this.comp = comp;
        this.button = button;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = fieldGbc.gridy = 0;
        fieldGbc.weightx = 1; 
        fieldGbc.weighty = 0;
        fieldGbc.anchor = GridBagConstraints.EAST;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(0,0,0,2);
        
        GridBagConstraints buttonGbc = new GridBagConstraints();
        buttonGbc.gridx = 1; 
        buttonGbc.gridy = 0;
        buttonGbc.weightx = buttonGbc.weighty = 0;
        buttonGbc.anchor = GridBagConstraints.WEST;
        buttonGbc.fill = GridBagConstraints.NONE;
        buttonGbc.insets = new Insets(0,0,0,0);
        
        add(comp, fieldGbc);
        add(button, buttonGbc);
    }
    
    public void setEnabled(boolean enabled) {
        comp.setEnabled(enabled);
        button.setEnabled(enabled);
    }
}