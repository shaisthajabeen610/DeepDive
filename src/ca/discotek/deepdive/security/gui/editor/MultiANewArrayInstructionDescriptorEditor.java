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
import ca.discotek.deepdive.grep.classmatcher.MethodInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.MultiANewArrayInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Opcode;
import ca.discotek.deepdive.security.PreferencesManager;;

public class MultiANewArrayInstructionDescriptorEditor extends JPanel implements InstructionDescriptionEditor {

    PreferencesManager typeManager = PreferencesManager.getManager("multi-a-new-array-instruction-editor-type");
    
    JTextField dimensionsField;
    EnabledButtonWrapper dimensionsWrapper;
    
    MatchExpressionField typeField;
    EnabledButtonWrapper typeWrapper;
    
    public MultiANewArrayInstructionDescriptorEditor() {
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
        
        add(new JLabel("Dimensions"), labelGbc);
        dimensionsField = new JTextField(3);
        dimensionsWrapper = new EnabledButtonWrapper(dimensionsField);
        dimensionsField.setEnabled(dimensionsWrapper.isEnabled());
        add(dimensionsWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        add(new JLabel("Type"), labelGbc);
        typeField = new MatchExpressionField();
        typeWrapper = new EnabledButtonWrapper(typeField);
        typeField.setEnabled(typeWrapper.isEnabled());
        add(typeWrapper, fieldGbc);
    }
    
    public MultiANewArrayInstructionDescriptor getDescriptor() {
        MultiANewArrayInstructionDescriptor d = new MultiANewArrayInstructionDescriptor();
        
        if (dimensionsWrapper.isEnabled())
            d.setDims(Integer.parseInt(dimensionsField.getText()));
        
        if (typeWrapper.isEnabled())
            d.setDescPattern(typeField.getMatchExpression());
        
        return d;
    }
    
    public void setDescriptor(AbstractInstructionDescriptor d) {
        setDescriptor( (MultiANewArrayInstructionDescriptor) d);
    }
    
    public void setDescriptor(MultiANewArrayInstructionDescriptor d) {
        int dimensions = d.getDims();
        if (dimensions > -1) {
            dimensionsField.setText(Integer.toString(dimensions));
            dimensionsWrapper.setEnabledButtonSelected(true);
        }

        
        MatchExpression me = d.getDescPattern();
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
