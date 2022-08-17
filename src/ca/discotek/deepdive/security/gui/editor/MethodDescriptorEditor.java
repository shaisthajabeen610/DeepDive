package ca.discotek.deepdive.security.gui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import ca.discotek.deepdive.grep.classmatcher.MethodDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.security.PreferencesManager;;

public class MethodDescriptorEditor extends JPanel {

    MatchExpressionField annotationField;
    EnabledButtonWrapper annotationWrapper;
    ModifierEditor modifierEditor;
    JTextField modifierTextField;
    List<Modifier> modifierList = new ArrayList<Modifier>();
    EnabledButtonWrapper modifierWrapper;
    MatchExpressionField nameField;
    EnabledButtonWrapper nameWrapper;
    MatchExpressionField returnTypeField;
    EnabledButtonWrapper returnTypeWrapper;
    MatchExpressionField parameterTypesField;
    EnabledButtonWrapper parameterTypesWrapper;
    MatchExpressionField exceptionsField;
    EnabledButtonWrapper exceptionsWrapper;
    
    List<AbstractInstructionDescriptor> instructionList = new ArrayList<AbstractInstructionDescriptor>();
    JTextField instructionsField;
    EnabledButtonWrapper instructionsWrapper;
    InstructionsEditor instructionsEditor;
    
    PreferencesManager nameManager = PreferencesManager.getManager("class-matcher-analyzer-method-name");
    PreferencesManager returnTypeManager = PreferencesManager.getManager("class-matcher-analyzer-method-return-type");
    PreferencesManager parameterTypesManager = PreferencesManager.getManager("class-matcher-analyzer-method-parameter-types");
    PreferencesManager annotationManager = PreferencesManager.getManager("class-matcher-analyzer-method-annotation");
    
    public MethodDescriptorEditor() {
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
        
        annotationField = new MatchExpressionField();
        annotationField.setBorder(BorderFactory.createTitledBorder("Annotation"));
        annotationWrapper = new EnabledButtonWrapper(annotationField);
        annotationWrapper.setEnabledButtonSelected(false);
        annotationField.setEnabled(annotationWrapper.isEnabled());
        add(annotationWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        modifierEditor = new ModifierEditor(ModifierEditor.METHOD_MODIFIERS);
        modifierTextField = new JTextField(50);
        modifierTextField.setEditable(false);
        JButton button = new JButton("Edit...");
        FieldEditorPanel fieldEditor = new FieldEditorPanel(modifierTextField, button);
        fieldEditor.setBorder(BorderFactory.createTitledBorder("Modifiers"));
        modifierWrapper = new EnabledButtonWrapper(fieldEditor);
        fieldEditor.setEnabled(false);
        add(modifierWrapper, fieldGbc);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                modifierEditor.setModifiers(modifierList.toArray(new Modifier[modifierList.size()]));
                
                boolean wasCancelled = DialogWrapper.showDialogWrapper(MethodDescriptorEditor.this, "Modifier Editor", modifierEditor);
                if (!wasCancelled) {
                    modifierList.clear();
                    modifierList.addAll(Arrays.asList(modifierEditor.getModifiers()));
                    updateModifiersField();
                }
            }
        });
        
        labelGbc.gridy = ++fieldGbc.gridy;

        nameField = new MatchExpressionField();
        nameField.setBorder(BorderFactory.createTitledBorder("Name"));
        nameWrapper = new EnabledButtonWrapper(nameField);
        nameField.setEnabled(nameWrapper.isEnabled());
        add(nameWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;
        
        returnTypeField = new MatchExpressionField();
        returnTypeField.setBorder(BorderFactory.createTitledBorder("Return Type"));
        returnTypeWrapper = new EnabledButtonWrapper(returnTypeField);
        returnTypeField.setEnabled(returnTypeWrapper.isEnabled());
        add(returnTypeWrapper, fieldGbc);

        labelGbc.gridy = ++fieldGbc.gridy;

        parameterTypesField = new MatchExpressionField();
        parameterTypesField.setBorder(BorderFactory.createTitledBorder("Parameter Types"));
        parameterTypesWrapper = new EnabledButtonWrapper(parameterTypesField);
        parameterTypesField.setEnabled(parameterTypesWrapper.isEnabled());
        add(parameterTypesWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        exceptionsField = new MatchExpressionField();
        exceptionsField.setBorder(BorderFactory.createTitledBorder("Exceptions"));
        exceptionsWrapper = new EnabledButtonWrapper(exceptionsField);
        exceptionsField.setEnabled(exceptionsWrapper.isEnabled());
        add(exceptionsWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        instructionsField = new JTextField();
        instructionsField.setEditable(false);
        button = new JButton("Edit...");
        FieldEditorPanel editorPanel = new FieldEditorPanel(instructionsField, button);
        editorPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
        instructionsWrapper = new EnabledButtonWrapper(editorPanel);
        editorPanel.setEnabled(exceptionsWrapper.isEnabled());
        add(instructionsWrapper, fieldGbc);
        
        instructionsEditor = new InstructionsEditor();
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean wasCancelled = DialogWrapper.showDialogWrapper(MethodDescriptorEditor.this, "Instructions Editor", instructionsEditor);
                if (!wasCancelled) {
                    AbstractInstructionDescriptor instructions[] = instructionsEditor.getInstructions();
                    instructionList.clear();
                    instructionList.addAll(Arrays.asList(instructions));
                    updateInstructionsField();
                }
            }
        });
    }
    
    void updateInstructionsField() {
        StringBuilder buffer = new StringBuilder();
        AbstractInstructionDescriptor instructions[] = instructionList.toArray(new AbstractInstructionDescriptor[instructionList.size()]);
        for (int i=0; i<instructions.length; i++) {
            buffer.append(instructions[i].getName() + " " + instructions[i].getDetails());
            if (i<instructions.length-1)
                buffer.append("; ");
            instructionsField.setText(buffer.toString());
            instructionList.removeAll(instructionList);
            instructionList.addAll(Arrays.asList(instructions));
        }
    }
    
    public MethodDescriptor getDescriptor() {
        if (!annotationWrapper.isEnabled() && 
            !modifierWrapper.isEnabled() && 
            !nameWrapper.isEnabled() && 
            !returnTypeWrapper.isEnabled() && 
            !parameterTypesWrapper.isEnabled() && 
            !instructionsWrapper.isEnabled() && 
            !exceptionsWrapper.isEnabled()) 
            
            return null;
        
        MethodDescriptor md = new MethodDescriptor();
        
        MatchExpression me = annotationField.getMatchExpression();
        if (annotationWrapper.isEnabled() && me != null)
            md.setAnnotationPattern(me);
        
        if (modifierWrapper.isEnabled()) {
            Modifier modifiers[] = modifierEditor.getModifiers();
            for (int i=0; i<modifiers.length; i++)
                md.addModifier(modifiers[i]);
        }
        
        if (nameWrapper.isEnabled())
            md.setNamePattern(nameField.getMatchExpression());
        
        if (returnTypeWrapper.isEnabled())
            md.setReturnTypePattern(returnTypeField.getMatchExpression());
        
        if (parameterTypesWrapper.isEnabled())
            md.setParameterPattern(parameterTypesField.getMatchExpression());

        if (exceptionsWrapper.isEnabled())
            md.setExceptionsPattern(exceptionsField.getMatchExpression());
        
        if (instructionsWrapper.isEnabled())
            md.setInstructions(instructionList.toArray(new AbstractInstructionDescriptor[instructionList.size()]));
        
        return md;
    }
    
    void updateModifiersField() {
        StringBuilder buffer = new StringBuilder();
        Iterator<Modifier> it = modifierList.listIterator();
        while (it.hasNext()) {
            buffer.append(it.next());
            if (it.hasNext())
                buffer.append(", ");
        }
        modifierTextField.setText(buffer.toString());
    }
    
    public void setDescriptor(MethodDescriptor md) {
        MatchExpression me = md.getAnnotationPattern();
        if (me != null) {
            annotationField.setMatchExpression(me);
            annotationWrapper.setEnabledButtonSelected(true);
        }
         
        me = md.getNamePattern();
        if (me != null) {
            nameField.setMatchExpression(me);
            nameWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getReturnTypePattern();
        if (me != null) {
            returnTypeField.setMatchExpression(me);
            returnTypeWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getParameterTypesPattern();
        if (me != null) {
            parameterTypesField.setMatchExpression(me);
            parameterTypesWrapper.setEnabledButtonSelected(true);
        }
        
        me = md.getExceptionsPattern();
        if (me != null) {
            exceptionsField.setMatchExpression(me);
            exceptionsWrapper.setEnabledButtonSelected(true);
        }
        
        AbstractInstructionDescriptor instructions[] = md.getInstructions();
        if (instructions.length > 0) {
            instructionList.clear();
            instructionList.addAll(Arrays.asList(instructions));
            instructionsEditor.setInstructions(instructions);
            instructionsWrapper.setEnabledButtonSelected(true);
        }
        
        Modifier modifiers[] = md.getModifiers();
        if (modifiers.length > 0) {
            modifierList.clear();
            modifierList.addAll(Arrays.asList(modifiers));
            modifierEditor.setModifiers(modifiers);
            modifierWrapper.setEnabledButtonSelected(true);
        }

        updateInstructionsField();
        updateModifiersField();
    }
    
    public void loadPreferences() {
        String expressions[] = nameManager.load();
        
        String text = nameField.expressionField.getText();
        DefaultComboBoxModel model = nameField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        nameField.expressionField.setText(text);
        
        text = returnTypeField.expressionField.getText();
        expressions = returnTypeManager.load();
        model = returnTypeField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        returnTypeField.expressionField.setText(text);
        
        text = parameterTypesField.expressionField.getText();
        expressions = parameterTypesManager.load();
        model = parameterTypesField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        parameterTypesField.expressionField.setText(text);
        
        text = annotationField.expressionField.getText();
        expressions = annotationManager.load();
        model = annotationField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        annotationField.expressionField.setText(text);
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
        
        text = returnTypeField.expressionField.getText().trim();
        if (text.length() > 0)
            returnTypeField.expressionField.itemChosen(text);
        model = returnTypeField.expressionField.getDefaultModel();
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
        
        text = annotationField.expressionField.getText().trim();
        if (text.length() > 0)
            annotationField.expressionField.itemChosen(text);
        model = annotationField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        annotationManager.save(expressions);;
    }
}
