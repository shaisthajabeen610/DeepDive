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
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.security.PreferencesManager;;

public class FieldInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    PreferencesManager ownerManager = PreferencesManager.getManager("instruction-editor-field-owner");
    PreferencesManager nameManager = PreferencesManager.getManager("instruction-editor-field-name");
    PreferencesManager typeManager = PreferencesManager.getManager("instruction-editor-field-type");
    
    OpcodeEditor opcodeEditor;
    EnabledButtonWrapper opcodeWrapper;
    
    MatchExpressionField ownerField;
    EnabledButtonWrapper ownerWrapper;

    MatchExpressionField nameField;
    EnabledButtonWrapper nameWrapper;
    
    MatchExpressionField typeField;
    EnabledButtonWrapper typeWrapper;
    
    public FieldInstructionDescriptorEditor() {
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
        opcodeEditor = new OpcodeEditor(OpcodeEditor.FieldInstructionOpcodes);
        opcodeWrapper = new EnabledButtonWrapper(opcodeEditor);
        opcodeEditor.setEnabled(false);
        add(opcodeWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        
        add(new JLabel("Owner"), labelGbc);
        ownerField = new MatchExpressionField();
        ownerWrapper = new EnabledButtonWrapper(ownerField);
        ownerField.setEnabled(ownerWrapper.isEnabled());
        add(ownerWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        add(new JLabel("Name"), labelGbc);
        nameField = new MatchExpressionField();
        nameWrapper = new EnabledButtonWrapper(nameField);
        nameField.setEnabled(nameWrapper.isEnabled());
        add(nameWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        add(new JLabel("Type"), labelGbc);
        typeField = new MatchExpressionField();
        typeWrapper = new EnabledButtonWrapper(typeField);
        typeField.setEnabled(typeWrapper.isEnabled());
        add(typeWrapper, fieldGbc);
    }
    
    public FieldInstructionDescriptor getDescriptor() {
        FieldInstructionDescriptor fd = new FieldInstructionDescriptor();
        
        if (opcodeWrapper.isEnabled())
            fd.setOpcode(opcodeEditor.getOpcode());
        
        if (ownerWrapper.isEnabled())
            fd.setOwnerPattern(ownerField.getMatchExpression());
        
        if (nameWrapper.isEnabled())
            fd.setNamePattern(nameField.getMatchExpression());
        
        if (typeWrapper.isEnabled())
            fd.setTypePattern(typeField.getMatchExpression());
        
        return fd;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (FieldInstructionDescriptor) d);
    }
    
    public void setDescriptor(FieldInstructionDescriptor fd) {
        Opcode opcode = fd.getOpcode();
        if (opcode != null) {
            opcodeEditor.setOpcode(opcode);
            opcodeEditor.setEnabled(true);
            opcodeWrapper.setEnabledButtonSelected(true);
        }
         
        MatchExpression me = fd.getOwnerPattern();
        if (me != null) {
            ownerField.setMatchExpression(me);
            ownerWrapper.setEnabledButtonSelected(true);
        }
        
        me = fd.getNamePattern();
        if (me != null) {
            nameField.setMatchExpression(me);
            nameWrapper.setEnabledButtonSelected(true);
        }
        
        me = fd.getTypePattern();
        if (me != null) {
            typeField.setMatchExpression(me);
            typeWrapper.setEnabledButtonSelected(true);
        }
    }

    public void loadPreferences() {
        String text = ownerField.expressionField.getText();
        String expressions[] = ownerManager.load();
        DefaultComboBoxModel model = ownerField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        ownerField.expressionField.setText(text);
        
        text = nameField.expressionField.getText();
        expressions = nameManager.load();
        model = nameField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        nameField.expressionField.setText(text);
        
        text = typeField.expressionField.getText();
        expressions = typeManager.load();
        model = typeField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        typeField.expressionField.setText(text);
    }
    
    public void savePreferences() {
        String text = ownerField.expressionField.getText().trim();
        if (text.length() > 0)
            ownerField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = ownerField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        ownerManager.save(expressions);
        
        text = nameField.expressionField.getText().trim();
        if (text.length() > 0)
            nameField.expressionField.itemChosen(text);
        model = nameField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        nameManager.save(expressions);
        
        text = typeField.expressionField.getText().trim();
        if (text.length() > 0)
            typeField.expressionField.itemChosen(text);
        model = typeField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        typeManager.save(expressions);
    }
}
