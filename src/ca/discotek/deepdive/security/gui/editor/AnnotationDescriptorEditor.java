package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.grep.classmatcher.AnnotationDescriptor;
import ca.discotek.deepdive.security.PreferencesManager;;

public class AnnotationDescriptorEditor extends JPanel {

    MatchExpressionField patternField;
    EnabledButtonWrapper patternFieldWrapper;
    
    PreferencesManager patternManager = PreferencesManager.getManager("annotation-editor-pattern");
    
    public AnnotationDescriptorEditor() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = labelGbc.gridy = 0;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.anchor = GridBagConstraints.NORTHEAST;
        labelGbc.insets = new Insets(6,2,2,2);
        labelGbc.weightx = labelGbc.weighty = 0;
        
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1; 
        fieldGbc.gridy = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.anchor = GridBagConstraints.NORTHWEST;
        fieldGbc.insets = new Insets(2,2,2,2);
        fieldGbc.weightx = 1;
        fieldGbc.weighty = 0;

        EnabledButtonWrapper wrapper;
        add(new JLabel("Pattern"), labelGbc);
        patternField = new MatchExpressionField();
        add(wrapper = patternFieldWrapper = new EnabledButtonWrapper(patternField), fieldGbc);
        wrapper.setEnabled(false);
    }
    
    public AnnotationDescriptor getDescriptor() {
        AnnotationDescriptor ad = new AnnotationDescriptor();
        if (patternFieldWrapper.isEnabled())
            ad.setPattern(patternField.getMatchExpression());
        return ad;
    }
    
    public void setDescriptor(AnnotationDescriptor ad) {
        MatchExpression me = ad.getPattern();
        if (me != null) {
            patternField.setMatchExpression(me);
            patternFieldWrapper.setEnabled(true);
        }
    }
    
    public void loadPreferences() {
        String expressions[] = patternManager.load();
        String text = patternField.expressionField.getText();
        DefaultComboBoxModel model = patternField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        patternField.expressionField.setText(text);
    }
    
    public void savePreferences() {
        String text = patternField.expressionField.getText().trim();
        if (text.length() > 0)
            patternField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = patternField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        patternManager.save(expressions);
    }
}
