package ca.discotek.deepdive.security.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.discotek.deepdive.security.PreferencesManager;

public class DecompilePreferencesView extends JPanel {

    JRadioButton decompileButton;
    JRadioButton classButton;
    
    public DecompilePreferencesView() {
        this(false);
    }
    
    public DecompilePreferencesView(boolean decompileEnabled) {
        buildGui();
        setDecompileEnabled(decompileEnabled);
        PreferencesManager manager = PreferencesManager.getManager("default");
        boolean decompile = manager.getDecompilerPreferences();
        decompileButton.setSelected(decompile);
        classButton.setSelected(!decompile);
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = labelGbc.gridy = 0;
        labelGbc.weightx = labelGbc.weighty = 0;
        labelGbc.anchor = GridBagConstraints.EAST;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.insets = new Insets(2,2,2,2);
        
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = 0;
        fieldGbc.weightx = 1;
        fieldGbc.weighty = 0;
        fieldGbc.anchor = GridBagConstraints.WEST;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(2,2,2,2);
        
        add(new JLabel("Class Output"), labelGbc);
        ButtonGroup group = new ButtonGroup();
        decompileButton = new JRadioButton("Decompile to Source", false);
        decompileButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                e.getSource();
            }
        });
        group.add(decompileButton);
        classButton = new JRadioButton("Use .class", true);
        group.add(classButton);
        
        JPanel panel = new JPanel();
        panel.add(decompileButton);
        panel.add(classButton);
        
        add(panel, fieldGbc);
    }
    
    public boolean getDecompileEnabled() {
        return decompileButton.isSelected();
    }
    
    public void setDecompileEnabled(boolean enabled) {
        decompileButton.setSelected(enabled);
        classButton.setSelected(!enabled);
    }
}
