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
import ca.discotek.deepdive.grep.classmatcher.InstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.LoadConstantInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.security.PreferencesManager;;

public class LoadConstantInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    PreferencesManager manager = PreferencesManager.getManager("load-constant-editor-constant");
    
    MatchExpressionField constantField;
    EnabledButtonWrapper constantWrapper;
    
    public LoadConstantInstructionDescriptorEditor() {
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
        
        add(new JLabel("Constant"), labelGbc);
        constantField = new MatchExpressionField();
        constantWrapper = new EnabledButtonWrapper(constantField);
        constantField.setEnabled(constantWrapper.isEnabled());
        add(constantWrapper, fieldGbc);
    }
    
    public LoadConstantInstructionDescriptor getDescriptor() {
        LoadConstantInstructionDescriptor d = new LoadConstantInstructionDescriptor();
        
        if (constantWrapper.isEnabled())
            d.setConstantPattern(constantField.getMatchExpression());
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (LoadConstantInstructionDescriptor) d);
    }
    
    public void setDescriptor(LoadConstantInstructionDescriptor d) {
        MatchExpression me = d.getConstantPattern();
        if (me != null) {
            constantField.setMatchExpression(me);
            constantWrapper.setEnabledButtonSelected(true);
        }
    }

    public void loadPreferences() {
        String text = constantField.expressionField.getText();
        String expressions[] = manager.load();
        DefaultComboBoxModel model = constantField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        constantField.expressionField.setText(text);
    }
    
    public void savePreferences() {
        String text = constantField.expressionField.getText().trim();
        if (text.length() > 0)
            constantField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = constantField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        manager.save(expressions);
    }
}
