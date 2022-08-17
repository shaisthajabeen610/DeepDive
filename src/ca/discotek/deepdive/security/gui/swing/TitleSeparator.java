package ca.discotek.deepdive.security.gui.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class TitleSeparator extends JPanel {

    public TitleSeparator(String title) {
        buildGui(title);
    }
    
    void buildGui(String title) {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,2,2,2);
        gbc.weightx = 1;
        gbc.weighty = 0;
        
        add(new JSeparator(), gbc);
 
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        add(new JLabel(title), gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        add(new JSeparator(), gbc);
        
 
    }
}
