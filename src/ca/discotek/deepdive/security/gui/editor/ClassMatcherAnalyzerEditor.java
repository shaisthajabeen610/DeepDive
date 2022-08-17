package ca.discotek.deepdive.security.gui.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.grep.classmatcher.ClassMatcher;
import ca.discotek.deepdive.grep.classmatcher.FieldDescriptor;
import ca.discotek.deepdive.grep.classmatcher.MethodDescriptor;
import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.common.swing.HistoryComboBox;
import ca.discotek.deepdive.security.PreferencesManager;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.ClassGrepAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.ClassMatcherAnalyzer;
import ca.discotek.rebundled.org.objectweb.asm.ClassWriter;

public class ClassMatcherAnalyzerEditor extends JPanel {

    static final String RESULT_TYPES[] = {
        "Class",
        "Field",
        "Method",
        "Instruction",
        "Annotation"
    };
    
    JTextField titleField;
    JTextField summaryField;
    JTextArea descriptionField;
    LinksField linksField;
    
    JComboBox resultTypeField;
    
    MatchExpressionField annotationField;
    EnabledButtonWrapper annotationWrapper;
    ModifierEditor modifierEditor;
    JTextField modifierTextField;
    List<Modifier> modifierList = new ArrayList<Modifier>();
    EnabledButtonWrapper modifierWrapper;
    
    MatchExpression interfacesPattern = null;
    MatchExpression superTypePattern = null;
    FieldDescriptor fieldDescriptor = null; 
    MethodDescriptor methodDescriptor = null;
    
    MatchExpressionField classNameField;
    EnabledButtonWrapper classNameWrapper;
    MatchExpressionField superTypeField;
    EnabledButtonWrapper superNameWrapper;    
    MatchExpressionField interfacesField;
    EnabledButtonWrapper interfacesWrapper;
    
    FieldDescriptorEditor fieldDescriptorEditor;
    MethodDescriptorEditor methodDescriptorEditor;
    
    PreferencesManager interfacesManager = PreferencesManager.getManager("class-matcher-analyzer-interfaces");
    PreferencesManager superTypeManager = PreferencesManager.getManager("class-matcher-analyzer-supertype");
    PreferencesManager classNameManager = PreferencesManager.getManager("class-matcher-analyzer-classname");
    
    public ClassMatcherAnalyzerEditor() {
        this(null, null, null, null);
    }
    
    public ClassMatcherAnalyzerEditor(String title, String summary, String description, String links[]) {
        buildGui(title, summary, description, links);
    }
    
    void buildGui(String title, String summary, String description, String links[]) {
        setLayout(new BorderLayout());
        JTabbedPane tabber = new JTabbedPane();
        add(tabber, BorderLayout.CENTER);
        
        tabber.addTab("Class", buildClassPanelGui(title, summary, description, links));
        
        fieldDescriptorEditor = new FieldDescriptorEditor();
        tabber.addTab("Field", fieldDescriptorEditor);
        
        methodDescriptorEditor = new MethodDescriptorEditor();
        tabber.addTab("Method", methodDescriptorEditor);
        
        updateModifiersField();
    }
    
    JPanel buildClassPanelGui(String title, String summary, String description, String links[]) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
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
        panel.add(label, labelGbc);
        titleField = new JTextField(100);
        if (title != null) titleField.setText(title);
        panel.add(titleField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        label = new JLabel("Summary");
        panel.add(label, labelGbc);
        summaryField = new JTextField(100);
        if (summary != null) summaryField.setText(title);
        panel.add(summaryField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        fieldGbc.weighty = 1;
        fieldGbc.fill = GridBagConstraints.BOTH;
        
        label = new JLabel("Description");
        panel.add(label, labelGbc);
        descriptionField = new JTextArea(4, 10);
        descriptionField.setLineWrap(true);
        if (description!= null) {
            descriptionField.setText(description);
            descriptionField.setCaretPosition(0);
        }
        panel.add(new JScrollPane(descriptionField), fieldGbc);

        fieldGbc.weighty = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Links"), labelGbc);
        linksField = new LinksField(links);
        panel.add(linksField, fieldGbc);
        
        fieldGbc.weighty = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;

        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Output Type"), labelGbc);
        resultTypeField = new JComboBox(RESULT_TYPES);
        panel.add(resultTypeField, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Class Annotation"), labelGbc);
        annotationField = new MatchExpressionField();
        annotationWrapper = new EnabledButtonWrapper(annotationField);
        annotationWrapper.setEnabledButtonSelected(false);
        annotationField.setEnabled(annotationWrapper.isEnabled());
        panel.add(annotationWrapper, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;

        panel.add(new JLabel("Class Modifiers"), labelGbc);
        modifierEditor = new ModifierEditor(ModifierEditor.CLASS_MODIFIERS);
        modifierTextField = new JTextField(50);
        modifierTextField.setEditable(false);
        JButton button = new JButton("Edit...");
        FieldEditorPanel fieldEditor = new FieldEditorPanel(modifierTextField, button);
        modifierWrapper = new EnabledButtonWrapper(fieldEditor);
        fieldEditor.setEnabled(false);
        panel.add(modifierWrapper, fieldGbc);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                modifierEditor.setModifiers(modifierList.toArray(new Modifier[modifierList.size()]));
                
                boolean wasCancelled = DialogWrapper.showDialogWrapper(ClassMatcherAnalyzerEditor.this, "Modifier Editor", modifierEditor);
                if (!wasCancelled) {
                    modifierList.clear();
                    modifierList.addAll(Arrays.asList(modifierEditor.getModifiers()));
                    updateModifiersField();
                }
            }
        });
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Class Name"), labelGbc);
        classNameField = new MatchExpressionField();
        classNameField.setEnabled(false);
        classNameWrapper = new EnabledButtonWrapper(classNameField);
        panel.add(classNameWrapper, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Super Class Name"), labelGbc);
        superTypeField = new MatchExpressionField();
        superTypeField.setEnabled(false);
        superNameWrapper = new EnabledButtonWrapper(superTypeField);
        panel.add(superNameWrapper, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        
        panel.add(new JLabel("Interface Names"), labelGbc);
        interfacesField = new MatchExpressionField();
        interfacesField.setEnabled(false);
        interfacesWrapper = new EnabledButtonWrapper(interfacesField);
        panel.add(interfacesWrapper, fieldGbc);
        
        return panel;
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
    
    public void setAnalyzer(ClassMatcherAnalyzer analyzer) {
        titleField.setText(analyzer.getTitle());
        summaryField.setText(analyzer.getSummary());
        descriptionField.setText(analyzer.getDescription());
        descriptionField.setCaretPosition(0);
        linksField.setLinks(analyzer.getLinks());
        
        resultTypeField.setSelectedIndex(analyzer.outputType);
        
        Modifier modifiers[] = analyzer.matcher.getModifiers();
        if (modifiers != null && modifiers.length > 0) {
            modifierList.addAll(Arrays.asList(modifiers));
            modifierWrapper.setEnabledButtonSelected(true);
        }
        
        MatchExpression me = analyzer.matcher.getAnnotationPattern();
        if (me != null) {
            annotationField.setMatchExpression(me);
            annotationWrapper.setEnabledButtonSelected(true);
        }
        
        me = analyzer.matcher.getNamePattern();
        if (me != null) {
            classNameField.setMatchExpression(me);
            classNameWrapper.setEnabledButtonSelected(true);
        }
        me = analyzer.matcher.getSuperTypeNamePattern();
        if (me != null) {
            superTypeField.setMatchExpression(me);
            superNameWrapper.setEnabledButtonSelected(true);
        }
        me = analyzer.matcher.getInterfaceNamePattern();
        if (me != null) {
            interfacesField.setMatchExpression(me);
            interfacesWrapper.setEnabledButtonSelected(true);
        }
        FieldDescriptor fd = analyzer.matcher.getFieldDescriptor();
        if (fd != null)
            fieldDescriptorEditor.setDescriptor(fd);
        
        MethodDescriptor md = analyzer.matcher.getMethodDescriptor();
        if (md != null)
            methodDescriptorEditor.setDescriptor(md);
        
        updateModifiersField();
    }
    
    public DeploymentVisitor getAnalyzer() {
        ClassMatcher matcher = new ClassMatcher();
        matcher.setModifiers(modifierList.toArray(new Modifier[modifierList.size()]));
        if (annotationWrapper.isEnabled())
            matcher.setAnnotationPattern(annotationField.getMatchExpression());
        if (classNameWrapper.isEnabled())
            matcher.setNamePattern(classNameField.getMatchExpression());
        if (superNameWrapper.isEnabled())
            matcher.setSuperTypeNamePattern(superTypeField.getMatchExpression());
        if (interfacesWrapper.isEnabled())
            matcher.setInterfaceNamePattern(interfacesField.getMatchExpression());
        matcher.setFieldDescriptor(fieldDescriptorEditor.getDescriptor());
        matcher.setMethodDescriptor(methodDescriptorEditor.getDescriptor());
        return new ClassMatcherAnalyzer(titleField.getText(), summaryField.getText(), descriptionField.getText(), linksField.getLinks(), matcher, resultTypeField.getSelectedIndex());
    }
    
    public void loadPreferences() {
        String expressions[] = classNameManager.load();

        String text = classNameField.expressionField.getText();
        DefaultComboBoxModel model = classNameField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        classNameField.expressionField.setText(text);
        
        text = interfacesField.expressionField.getText();
        expressions = interfacesManager.load();
        model = interfacesField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        interfacesField.expressionField.setText(text);
        
        text = superTypeField.expressionField.getText();
        expressions = superTypeManager.load();
        model = superTypeField.expressionField.getDefaultModel();
        for (int i=0; i<expressions.length; i++)
            model.addElement(expressions[i]);
        superTypeField.expressionField.setText(text);
        
        fieldDescriptorEditor.loadPreferences();
        methodDescriptorEditor.loadPreferences();
    }
    
    public void savePreferences() {
        String text = classNameField.expressionField.getText().trim();
        if (text.length() > 0)
            classNameField.expressionField.itemChosen(text);
        DefaultComboBoxModel model = classNameField.expressionField.getDefaultModel();
        int size = model.getSize();
        String expressions[] = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        classNameManager.save(expressions);
        
        text = interfacesField.expressionField.getText().trim();
        if (text.length() > 0)
            interfacesField.expressionField.itemChosen(text);
        model = interfacesField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        interfacesManager.save(expressions);
        
        text = superTypeField.expressionField.getText().trim();
        if (text.length() > 0)
            superTypeField.expressionField.itemChosen(text);
        model = superTypeField.expressionField.getDefaultModel();
        size = model.getSize();
        expressions = new String[size];
        for (int i=0; i<expressions.length; i++)
            expressions[i] = (String) model.getElementAt(i);
        superTypeManager.save(expressions);
        
        fieldDescriptorEditor.savePreferences();
        methodDescriptorEditor.savePreferences();
    }
}
