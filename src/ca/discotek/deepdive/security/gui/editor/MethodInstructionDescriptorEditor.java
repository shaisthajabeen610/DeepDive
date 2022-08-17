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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.grep.classmatcher.AbstractInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.AnnotationDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.LoadConstantInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.MethodInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.security.PreferencesManager;;

public class MethodInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    PreferencesManager ownerManager = PreferencesManager.getManager("method-instruction-editor-owner");
    PreferencesManager nameManager = PreferencesManager.getManager("method-instruction-editor-name");
    PreferencesManager returnTypeManager = PreferencesManager.getManager("method-instruction-editor-return-type");
    PreferencesManager parameterTypesManager = PreferencesManager.getManager("method-instruction-editor-parameter-types");
    
    OpcodeEditor opcodeEditor;
    EnabledButtonWrapper opcodeWrapper;
    
    MatchExpressionField ownerField;
    EnabledButtonWrapper ownerWrapper;

    MatchExpressionField nameField;
    EnabledButtonWrapper nameWrapper;
    
    MatchExpressionField returnField;
    EnabledButtonWrapper returnWrapper;
    
    MatchExpressionField parameterTypesField;
    EnabledButtonWrapper parameterTypesWrapper;

    JCheckBox isInterfaceField;
    EnabledButtonWrapper isInterfaceWrapper;
    
    public MethodInstructionDescriptorEditor() {
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
        opcodeEditor = new OpcodeEditor(OpcodeEditor.MethodInstructionOpcodes);
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

        add(new JLabel("Return Type"), labelGbc);
        returnField = new MatchExpressionField();
        returnWrapper = new EnabledButtonWrapper(returnField);
        returnField.setEnabled(returnWrapper.isEnabled());
        add(returnWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        add(new JLabel("Parameter Types"), labelGbc);
        parameterTypesField = new MatchExpressionField();
        parameterTypesWrapper = new EnabledButtonWrapper(parameterTypesField);
        parameterTypesField.setEnabled(parameterTypesWrapper.isEnabled());
        add(parameterTypesWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        add(new JLabel("Is Interface"), labelGbc);
        isInterfaceField = new JCheckBox();
        isInterfaceField.setSelected(true);
        isInterfaceWrapper = new EnabledButtonWrapper(isInterfaceField);
        isInterfaceField.setEnabled(isInterfaceWrapper.isEnabled());
        add(isInterfaceWrapper, fieldGbc);
    }
    
    public MethodInstructionDescriptor getDescriptor() {
        MethodInstructionDescriptor md = new MethodInstructionDescriptor();
        
        if (opcodeWrapper.isEnabled())
            md.setOpcode(opcodeEditor.getOpcode());
        
        if (ownerWrapper.isEnabled())
            md.setOwnerPattern(ownerField.getMatchExpression());
        
        if (nameWrapper.isEnabled())
            md.setNamePattern(nameField.getMatchExpression());
        
        if (returnWrapper.isEnabled())
            md.setReturnTypePattern(returnField.getMatchExpression());

        if (parameterTypesWrapper.isEnabled())
            md.setParameterTypesPattern(parameterTypesField.getMatchExpression());
        
        if (isInterfaceWrapper.isEnabled())
            md.setIsInterface(isInterfaceField.isSelected());

        return md;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (MethodInstructionDescriptor) d);
    }
    
    public void setDescriptor(MethodInstructionDescriptor md) {
        Opcode opcode = md.getOpcode();
        if (opcode != null) {
            opcodeEditor.setOpcode(opcode);
            opcodeEditor.setEnabled(true);
            opcodeWrapper.setEnabledButtonSelected(true);
        }
         
        MatchExpression me = md.getOwnerPattern();
        if (me != null) {
            ownerField.setMatchExpression(me);
            ownerWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getNamePattern();
        if (me != null) {
            nameField.setMatchExpression(me);
            nameWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getReturnTypePattern();
        if (me != null) {
            returnField.setMatchExpression(me);
            returnWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getParameterTypesPattern();
        if (me != null) {
            parameterTypesField.setMatchExpression(me);
            parameterTypesWrapper.setEnabledButtonSelected(true);
        }
        
        Boolean b = md.getIsInterface();
        if (b != null) {
            isInterfaceField.setSelected(b);
            isInterfaceWrapper.setEnabledButtonSelected(true);
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
        
        text = returnField.expressionField.getText();
        expressions = returnTypeManager.load();
        model = returnField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        returnField.expressionField.setText(text);
        
        text = parameterTypesField.expressionField.getText();
        expressions = parameterTypesManager.load();
        model = parameterTypesField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        parameterTypesField.expressionField.setText(text);
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
        
        text = returnField.expressionField.getText().trim();
        if (text.length() > 0)
            returnField.expressionField.itemChosen(text);
        model = returnField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        returnTypeManager.save(expressions);
        
        
        text = parameterTypesField.expressionField.getText().trim();
        if (text.length() > 0)
            parameterTypesField.expressionField.itemChosen(text);
        model = parameterTypesField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        parameterTypesManager.save(expressions);
    }
}
