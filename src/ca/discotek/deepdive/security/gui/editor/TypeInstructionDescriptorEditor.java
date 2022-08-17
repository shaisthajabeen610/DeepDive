package ca.discotek.deepdive.security.gui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.MultiANewArrayInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.grep.classmatcher.TypeInstructionDescriptor;
import ca.discotek.deepdive.security.PreferencesManager;;

public class TypeInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    PreferencesManager typeManager = PreferencesManager.getManager("type-instruction-editor-type");
    
    OpcodeEditor opcodeEditor;
    EnabledButtonWrapper opcodeWrapper;
    
    MatchExpressionField typeField;
    EnabledButtonWrapper typeWrapper;
    
    public TypeInstructionDescriptorEditor() {
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
        opcodeEditor = new OpcodeEditor(OpcodeEditor.TypeInstructionOpcodes);
        opcodeWrapper = new EnabledButtonWrapper(opcodeEditor);
        opcodeEditor.setEnabled(false);
        add(opcodeWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;


        add(new JLabel("Type"), labelGbc);
        typeField = new MatchExpressionField();
        typeWrapper = new EnabledButtonWrapper(typeField);
        typeField.setEnabled(typeWrapper.isEnabled());
        add(typeWrapper, fieldGbc);
    }
    
    public TypeInstructionDescriptor getDescriptor() {
        TypeInstructionDescriptor d = new TypeInstructionDescriptor();
        
        if (opcodeWrapper.isEnabled())
            d.setOpcode(opcodeEditor.getOpcode());
        
        if (typeWrapper.isEnabled())
            d.setTypePattern(typeField.getMatchExpression());
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (TypeInstructionDescriptor) d);
    }
    
    
    public void setDescriptor(TypeInstructionDescriptor d) {
        Opcode opcode = d.getOpcode();
        if (opcode != null) {
            opcodeEditor.setOpcode(opcode);
            opcodeEditor.setEnabled(true);
            opcodeWrapper.setEnabledButtonSelected(true);
        }
         
        MatchExpression me = d.getTypePattern();
        if (me != null) {
            typeField.setMatchExpression(me);
            typeWrapper.setEnabledButtonSelected(true);
        }
    }
    
    public void loadPreferences() {
        String text = typeField.expressionField.getText();
        String expressions[] = typeManager.load();
        DefaultComboBoxModel model = typeField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        typeField.expressionField.setText(text);
    }
    
    public void savePreferences() {
        String text = typeField.expressionField.getText().trim();
        if (text.length() > 0)
            typeField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = typeField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        typeManager.save(expressions);
    }
}
