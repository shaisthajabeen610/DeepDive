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
import ca.discotek.deepdive.grep.classmatcher.IntInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.grep.classmatcher.TypeInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.VarInstructionDescriptor;

public class VarInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    OpcodeEditor opcodeField;
    EnabledButtonWrapper opcodeWrapper;
    
    JTextField varIndexField;
    EnabledButtonWrapper varIndexWrapper;

    public VarInstructionDescriptorEditor() {
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
        opcodeField = new OpcodeEditor(OpcodeEditor.VarInstructionOpcodes);
        opcodeWrapper = new EnabledButtonWrapper(opcodeField);
        opcodeField.setEnabled(false);
        add(opcodeWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        
        add(new JLabel("Variable Index"), labelGbc);
        varIndexField = new JTextField(3);
        varIndexWrapper = new EnabledButtonWrapper(varIndexField);
        varIndexField.setEnabled(varIndexWrapper.isEnabled());
        add(varIndexWrapper, fieldGbc);
    }
    
    public VarInstructionDescriptor getDescriptor() {
        VarInstructionDescriptor d = new VarInstructionDescriptor();
        
        if (opcodeWrapper.isEnabled())
            d.setOpcode(opcodeField.getOpcode());
        
        if (varIndexWrapper.isEnabled())
            d.setVar(Integer.parseInt(varIndexField.getText()));
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (VarInstructionDescriptor) d);
    }
    
    public void setDescriptor(VarInstructionDescriptor d) {
        Opcode opcode = d.getOpcode();
        if (opcode != null) {
            opcodeField.setOpcode(opcode);
            opcodeWrapper.setEnabledButtonSelected(true);
        }
        
        int varIndex = d.getVar();
        if (varIndex > -1) {
            varIndexField.setText(Integer.toString(varIndex));
            varIndexWrapper.setEnabledButtonSelected(true);
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
