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
import ca.discotek.deepdive.grep.classmatcher.IntInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.InvokeDynamicInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.security.PreferencesManager;;

public class InvokeDynamicInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    MatchExpressionField nameField;
    EnabledButtonWrapper nameWrapper;
    
    MatchExpressionField typeField;
    EnabledButtonWrapper typeWrapper;
    
    PreferencesManager nameManager = PreferencesManager.getManager("invoke-dynamic-editor-field-name");
    PreferencesManager typeManager = PreferencesManager.getManager("invoke-dynamic-editor-field-type");
    
    public InvokeDynamicInstructionDescriptorEditor() {
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
    
    public InvokeDynamicInstructionDescriptor getDescriptor() {
        InvokeDynamicInstructionDescriptor d = new InvokeDynamicInstructionDescriptor();

        if (nameWrapper.isEnabled())
            d.setNamePattern(nameField.getMatchExpression());
        
        if (typeWrapper.isEnabled())
            d.setTypePattern(typeField.getMatchExpression());
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (InvokeDynamicInstructionDescriptor) d);
    }
    
    public void setDescriptor(InvokeDynamicInstructionDescriptor d) {
        MatchExpression me = d.getNamePattern();
        if (me != null) {
            nameField.setMatchExpression(me);
            nameWrapper.setEnabledButtonSelected(true);
        }
        
        me = d.getTypePattern();
        if (me != null) {
            typeField.setMatchExpression(me);
            typeWrapper.setEnabledButtonSelected(true);
        }
    }

    public void loadPreferences() {
        String text = nameField.expressionField.getText();
        String expressions[] = nameManager.load();
        DefaultComboBoxModel model = nameField.expressionField.getDefaultModel();
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
        String text = nameField.expressionField.getText().trim();
        if (text.length() > 0)
            nameField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = nameField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
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
