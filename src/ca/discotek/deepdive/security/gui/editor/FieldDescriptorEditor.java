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
import ca.discotek.deepdive.grep.classmatcher.AnnotationDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.security.PreferencesManager;;

public class FieldDescriptorEditor extends JPanel {

    MatchExpressionField annotationField;
    EnabledButtonWrapper annotationWrapper;
    ModifierEditor modifierEditor;
    JTextField modifierTextField;
    EnabledButtonWrapper modifierWrapper;
    List<Modifier> modifierList = new ArrayList<Modifier>();
    MatchExpressionField nameField;
    EnabledButtonWrapper nameWrapper;
    MatchExpressionField typeField;
    EnabledButtonWrapper typeWrapper;
    
    PreferencesManager nameManager = PreferencesManager.getManager("class-matcher-analyzer-field-name");
    PreferencesManager typeManager = PreferencesManager.getManager("class-matcher-analyzer-field-type");
    PreferencesManager annotationManager = PreferencesManager.getManager("class-matcher-analyzer-field-annotation");
    
    public FieldDescriptorEditor() {
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
        JButton button = new JButton("Edit...");
        annotationField.setBorder(BorderFactory.createTitledBorder("Annotation"));
        annotationWrapper = new EnabledButtonWrapper(annotationField);
        annotationWrapper.setEnabledButtonSelected(false);
        annotationField.setEnabled(annotationWrapper.isEnabled());
        add(annotationWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        modifierEditor = new ModifierEditor(ModifierEditor.FIELD_MODIFIERS);
        modifierTextField = new JTextField(50);
        modifierTextField.setEditable(false);
        button = new JButton("Edit...");
        FieldEditorPanel fieldEditor = new FieldEditorPanel(modifierTextField, button);
        fieldEditor.setBorder(BorderFactory.createTitledBorder("Modifiers"));
        modifierWrapper = new EnabledButtonWrapper(fieldEditor);
        fieldEditor.setEnabled(false);
        add(modifierWrapper, fieldGbc);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                modifierEditor.setModifiers(modifierList.toArray(new Modifier[modifierList.size()]));
                
                boolean wasCancelled = DialogWrapper.showDialogWrapper(FieldDescriptorEditor.this, "Modifier Editor", modifierEditor);
                if (!wasCancelled) {
                    if (!wasCancelled) {
                        modifierList.clear();
                        modifierList.addAll(Arrays.asList(modifierEditor.getModifiers()));
                        updateModifiersField();
                    }
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
        
        typeField = new MatchExpressionField();
        typeField.setBorder(BorderFactory.createTitledBorder("Type"));
        typeWrapper = new EnabledButtonWrapper(typeField);
        typeField.setEnabled(typeWrapper.isEnabled());
        add(typeWrapper, fieldGbc);
    }
    
    public FieldDescriptor getDescriptor() {
        
        if (!annotationWrapper.isEnabled() && !modifierWrapper.isEnabled() && !nameWrapper.isEnabled() && !typeWrapper.isEnabled())
            return null;
        
        FieldDescriptor fd = new FieldDescriptor();
        
        if (annotationWrapper.isEnabled())
            fd.setAnnotationPattern(annotationField.getMatchExpression());
        
        if (modifierWrapper.isEnabled())
            fd.setModifiers(modifierList.toArray(new Modifier[modifierList.size()]));

        
        if (nameWrapper.isEnabled())
            fd.setNamePattern(nameField.getMatchExpression());
        
        if (typeWrapper.isEnabled())
            fd.setTypePattern(typeField.getMatchExpression());
        
        return fd;
    }
    
    public void setDescriptor(FieldDescriptor fd) {
        MatchExpression annotationDescriptor = fd.getAnnotationPattern();
        if (annotationDescriptor != null) {
            annotationField.setMatchExpression(annotationDescriptor);
            annotationWrapper.setEnabledButtonSelected(true);
        }
         
        MatchExpression me = fd.getNamePattern();
        if (me != null) {
            nameField.setMatchExpression(me);
            nameWrapper.setEnabledButtonSelected(true);
        }
        
        me = fd.getTypePattern();
        if (me != null) {
            typeField.setMatchExpression(me);
            typeWrapper.setEnabledButtonSelected(true);
        }
        
        Modifier modifiers[] = fd.getModifiers();
        if (modifiers.length > 0) {
            modifierEditor.setModifiers(modifiers);
            modifierList.clear();
            modifierList.addAll(Arrays.asList(modifiers));
            modifierWrapper.setEnabledButtonSelected(true);
        }
        
        updateModifiersField();
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
        
        
        text = annotationField.expressionField.getText().trim();
        if (text.length() > 0)
            annotationField.expressionField.itemChosen(text);
        model = annotationField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        annotationManager.save(expressions);
    }
    
    public void loadPreferences() {
        String expressions[] = nameManager.load();
        String text = nameField.expressionField.getText();
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
        
        text = annotationField.expressionField.getText();
        expressions = annotationManager.load();
        model = annotationField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        annotationField.expressionField.setText(text);
    }
}
