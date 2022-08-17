package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.common.swing.HistoryComboBox;

public class MatchExpressionField extends JPanel {

    HistoryComboBox expressionField;
    JRadioButton containsButton;
    JRadioButton regularExpressionButton;
    JCheckBox caseSensitiveButton;
    
    public MatchExpressionField() {
        this(null, true, true);
    }
    
    public MatchExpressionField(String expression, boolean isRegEx, boolean caseSensitive) {
        buildGui(expression, isRegEx, caseSensitive);
    }
    
    void buildGui(String expression, boolean isRegEx, boolean caseSensitive) {
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2,2,2,2);
        gbc.weightx = gbc.weighty = 0;
        
        expressionField = new HistoryComboBox(50);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridwidth = 4;
        add(expressionField, gbc);

        gbc.gridwidth = 1;
        
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy++;
        
        add(new JLabel("Expression Type:"), gbc);
        
        gbc.gridx++;
        ButtonGroup group = new ButtonGroup();
        containsButton = new JRadioButton("String.contains(...)", true);
        containsButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateButtons();
            }
        });
        group.add(containsButton);
        add(containsButton, gbc);

        gbc.gridx++;
        
        regularExpressionButton = new JRadioButton("Java Regular Expression", false);
        group.add(regularExpressionButton);
        add(regularExpressionButton, gbc);
        
        gbc.gridx++;
        
        caseSensitiveButton = new JCheckBox("Case Sensitive", false);
        add(caseSensitiveButton, gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        
        add(new JLabel(), gbc);
        
        updateButtons();
    }
    
    void updateButtons() {
        caseSensitiveButton.setEnabled(containsButton.isSelected());
    }
    
    public MatchExpression getMatchExpression() {
        return regularExpressionButton.isSelected() ?
            new MatchExpression(expressionField.getText()) :
            new MatchExpression(expressionField.getText(), caseSensitiveButton.isSelected());
    }
    
    public void setMatchExpression(MatchExpression me) {
        containsButton.setSelected(me != null && me.pattern == null);
        regularExpressionButton.setSelected(me != null && me.pattern != null);
        caseSensitiveButton.setSelected(me != null && me.isCaseSensitive);
        expressionField.setText( (me == null || me.expression == null) ? "" : me.expression);
    }
    
    public void setEnabled(boolean enabled) {
        expressionField.setEnabled(enabled);
        containsButton.setEnabled(enabled);
        regularExpressionButton.setEnabled(enabled);
        caseSensitiveButton.setEnabled(enabled);
        
        if (enabled)
            updateButtons();
    }
}
