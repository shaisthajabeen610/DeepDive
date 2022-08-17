package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.common.swing.HistoryComboBox;
import ca.discotek.deepdive.security.PreferencesManager;
import ca.discotek.deepdive.security.visitor.assess.ClassGrepAnalyzer;

public class ClassGrepAnalyzerEditor extends JPanel {

    JTextField titleField;
    JTextField summaryField;
    JTextArea descriptionField;
    LinksField linksField;

    MatchExpressionField expressionField;
    EnabledButtonWrapper expressionWrapper;
    
    PreferencesManager manager = PreferencesManager.getManager("grep-analyzer-editor");
    
    public ClassGrepAnalyzerEditor() {
        this(null, null, null, null, null);
    }
    
    public ClassGrepAnalyzerEditor(String title, String summary, String description, String links[], MatchExpression me) {
        buildGui(title, summary, description, links, me);
    }
    
    void buildGui(String title, String summary, String description, String links[], MatchExpression me) {
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
        
        JLabel label = new JLabel("Title");
        add(label, labelGbc);
        titleField = new JTextField(100);
        if (title != null) titleField.setText(title);
        add(titleField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        label = new JLabel("Summary");
        add(label, labelGbc);
        summaryField = new JTextField(100);
        if (summary != null) summaryField.setText(summary);
        add(summaryField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        fieldGbc.weighty = 1;
        fieldGbc.fill = GridBagConstraints.BOTH;
        
        label = new JLabel("Description");
        add(label, labelGbc);
        descriptionField = new JTextArea(4, 10);
        descriptionField.setLineWrap(true);
        if (description!= null) {
            descriptionField.setText(description);
            descriptionField.setCaretPosition(0);
        }
        add(new JScrollPane(descriptionField), fieldGbc);

        fieldGbc.weighty = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        add(new JLabel("Links"), labelGbc);
        linksField = new LinksField(links);
        add(linksField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        add(new JLabel("Pattern"), labelGbc);
        
        expressionField = new MatchExpressionField();
        expressionField.setEnabled(false);
        expressionWrapper = new EnabledButtonWrapper(expressionField);
        add(expressionWrapper, fieldGbc);
        
    }
    
    public void setAnalyzer(ClassGrepAnalyzer analyzer) {
        titleField.setText(analyzer.getTitle());
        summaryField.setText(analyzer.getSummary());
        descriptionField.setText(analyzer.getDescription());
        descriptionField.setCaretPosition(0);
        linksField.setLinks(analyzer.getLinks());
        expressionField.setMatchExpression(analyzer.me);
        expressionWrapper.setEnabledButtonSelected(analyzer.me != null);
    }
    
    public ClassGrepAnalyzer getAnalyzer() {
        return new ClassGrepAnalyzer(titleField.getText(), summaryField.getText(), descriptionField.getText(), linksField.getLinks(), expressionWrapper.isEnabled() ? expressionField.getMatchExpression() : null);
    }

    public void loadPreferences() {
        String expressions[] = manager.load();

        DefaultComboBoxModel model = expressionField.expressionField.getDefaultModel();
        String text = expressionField.expressionField.getText();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        expressionField.expressionField.setText(text);
    }
    
    public void savePreferences() {
        String text = expressionField.expressionField.getText().trim();
        if (text.length() > 0)
            expressionField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = expressionField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        manager.save(expressions);
    }
}
