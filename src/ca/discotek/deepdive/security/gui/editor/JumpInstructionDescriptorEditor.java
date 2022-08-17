package ca.discotek.deepdive.security.gui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.grep.classmatcher.AbstractInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.AnnotationDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.IincInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.InstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.IntInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.InvokeDynamicInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;

public class JumpInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    OpcodeEditor opcodeField;
    EnabledButtonWrapper opcodeWrapper;
    
    public JumpInstructionDescriptorEditor() {
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
        
        add(new JLabel("Opcode"), labelGbc);
        opcodeField = new OpcodeEditor(OpcodeEditor.JumpInstructionOpcodes);
        opcodeWrapper = new EnabledButtonWrapper(opcodeField);
        opcodeField.setEnabled(false);
        add(opcodeWrapper, fieldGbc);
    }
    
    public InstructionDescriptor getDescriptor() {
        InstructionDescriptor d = new InstructionDescriptor();
        
        if (opcodeWrapper.isEnabled())
            d.setOpcode(opcodeField.getOpcode());
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (InstructionDescriptor) d);
    }
    
    public void setDescriptor(InstructionDescriptor d) {
        Opcode opcode = d.getOpcode();
        if (opcode != null) {
            opcodeField.setOpcode(opcode);
            opcodeWrapper.setEnabledButtonSelected(true);
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
