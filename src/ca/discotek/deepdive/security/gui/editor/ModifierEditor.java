package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.discotek.deepdive.grep.classmatcher.Modifier;

public class ModifierEditor extends JPanel {

    public static Modifier CLASS_MODIFIERS[] = 
        {Modifier.Public, Modifier.Private, Modifier.Protected, Modifier.PackageProtected, Modifier.Static, Modifier.Abstract, Modifier.Deprecated, Modifier.Final};
    public static Modifier FIELD_MODIFIERS[] = 
        {Modifier.Public, Modifier.Private, Modifier.Protected, Modifier.PackageProtected, Modifier.Static, Modifier.Transient, Modifier.Volatile, Modifier.Deprecated};
    public static Modifier METHOD_MODIFIERS[] = 
        {Modifier.Public, Modifier.Private, Modifier.Protected, Modifier.PackageProtected, Modifier.Static, Modifier.Abstract, Modifier.Bridge, Modifier.Synthentic, Modifier.Deprecated, Modifier.Final, Modifier.Native, Modifier.Synchronized};
    
    final Modifier modifiers[];
    JCheckBox fields[];
    EnabledButtonWrapper wrappers[];
    
    public ModifierEditor(Modifier modifiers[]) {
        this.modifiers = modifiers;
        fields = new JCheckBox[modifiers.length];
        wrappers = new EnabledButtonWrapper[modifiers.length];
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(2, 2, 2, 2);

        for (int i=0; i<modifiers.length; i++) {
            fields[i] = new JCheckBox(modifiers[i].name, true);
            wrappers[i] = new EnabledButtonWrapper(fields[i]);
            add(wrappers[i], gbc);
            wrappers[i].setEnabledButtonSelected(false);
            fields[i].setEnabled(wrappers[i].isEnabled());
            gbc.gridy++;
        }
    }
    
    public Modifier[] getModifiers() {
        List<Modifier> list = new ArrayList<Modifier>();

        for (int i=0; i<modifiers.length; i++) {
            if (wrappers[i].isEnabled() && fields[i].isSelected())
                list.add(modifiers[i]);
        }
        
        return list.toArray(new Modifier[list.size()]);
    }
    
    public void setModifiers(Modifier modifiers[]) {
        for (int i=0; i<modifiers.length; i++) {
            for (int j=0; j<this.modifiers.length; j++) {
                if (modifiers[i] == this.modifiers[j]) {
                    fields[j].setSelected(true);
                    wrappers[j].setEnabledButtonSelected(true);
                }
            }
        }
    }

}
