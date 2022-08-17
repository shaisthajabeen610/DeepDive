package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.discotek.deepdive.grep.classmatcher.AbstractInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.IincInstructionDescriptor;

public class IincInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    JTextField variableIndexField;
    EnabledButtonWrapper variableIndexWrapper;
    
    JTextField incrementByField;
    EnabledButtonWrapper incrementByWrapper;

    public IincInstructionDescriptorEditor() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = labelGbc.gridy = 0;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.anchor = GridBagConstraints.NORTHEAST;
        labelGbc.insets = new Insets(2,2,2,2);
        labelGbc.weightx = labelGbc.weighty = 0;
        
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1; 
        fieldGbc.gridy = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.anchor = GridBagConstraints.NORTHWEST;
        fieldGbc.insets = new Insets(2,2,2,2);
        fieldGbc.weightx = 1;
        fieldGbc.weighty = 0;
        
        add(new JLabel("Variable Index"), labelGbc);
        variableIndexField = new JTextField(3);
        variableIndexWrapper = new EnabledButtonWrapper(variableIndexField);
        variableIndexField.setEnabled(false);
        add(variableIndexWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        
        add(new JLabel("Increment By"), labelGbc);
        incrementByField = new JTextField(3);
        incrementByWrapper = new EnabledButtonWrapper(incrementByField);
        incrementByField.setEnabled(incrementByWrapper.isEnabled());
        add(incrementByWrapper, fieldGbc);
    }
    
    public IincInstructionDescriptor getDescriptor() {
        IincInstructionDescriptor d = new IincInstructionDescriptor();
        
        if (variableIndexWrapper.isEnabled())
            d.setVar(Integer.parseInt(variableIndexField.getText()));
        
        if (incrementByWrapper.isEnabled())
            d.setIncrement(Integer.parseInt(incrementByField.getText()));
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (IincInstructionDescriptor) d);
    }
    
    public void setDescriptor(IincInstructionDescriptor d) {
        int varIndex = d.getVar();
        if (varIndex > -1) {
            variableIndexField.setText(Integer.toString(varIndex));
            variableIndexWrapper.setEnabledButtonSelected(true);
        }
        
        Integer increment = d.getIncrement();
        if (increment != null) {
            incrementByField.setText(Integer.toString(increment));
            incrementByWrapper.setEnabledButtonSelected(true);
        }
    }

    @Override
    public void savePreferences() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadPreferences() {
        // TODO Auto-generated method stub
        
    }
}
