package ca.discotek.deepdive.security.gui.editor;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class EnabledButtonWrapper extends JPanel {

    final Component comp;
    
    JCheckBox enabledButton;
    
    public EnabledButtonWrapper(Component comp) {
        this.comp = comp;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        gbc.insets = new Insets(2,2,2,2);
        add(comp, gbc);
        
        gbc.gridx++;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        enabledButton = new JCheckBox("Enabled", false);
        add(enabledButton, gbc);
        
        enabledButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setComponentEnabled(enabledButton.isSelected());
            }
        });
    }
    
    public boolean isEnabled() {
        return enabledButton.isSelected();
    }
    
    public void setEnabledButtonSelected(boolean selected) {
        enabledButton.setSelected(selected);
    }
    
    public void setComponentEnabled(boolean enabled) {
        comp.setEnabled(enabled);
    }

}
