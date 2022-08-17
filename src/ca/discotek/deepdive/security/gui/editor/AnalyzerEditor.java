package ca.discotek.deepdive.security.gui.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.ClassGrepAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.ClassMatcherAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.FileFinderAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.custom.ParameterizedCustomDeploymentVisitor;

public class AnalyzerEditor extends JPanel {

    JTabbedPane tabber;
    
    ClassGrepAnalyzerEditor classGrepAnalyzerEditor;
    FileFinderAnalyzerEditor fileFinderAnalyzerEditor;
    ClassMatcherAnalyzerEditor classMatcherAnalyzerEditor;
    CustomAnalyzerEditor customAnalyzerEditor;

    List<String[]> parameterList = new ArrayList<String[]>();
    JTextField parameterField;
    
    public AnalyzerEditor(ClassGrepAnalyzer analyzer) {
        this();
        
        classGrepAnalyzerEditor.setAnalyzer(analyzer);
        tabber.setSelectedIndex(0);
    }
    
    public AnalyzerEditor(FileFinderAnalyzer analyzer) {
        this();
        fileFinderAnalyzerEditor.setAnalyzer(analyzer);
        tabber.setSelectedIndex(1);
    }
    
    public AnalyzerEditor(ClassMatcherAnalyzer analyzer) {
        this();
        classMatcherAnalyzerEditor.setAnalyzer(analyzer);
        tabber.setSelectedIndex(2);
    }
    
    public AnalyzerEditor(DeploymentVisitor analyzer) {
        this();
        
        customAnalyzerEditor.setAnalyzer(analyzer);
        tabber.setSelectedIndex(3);
    }
    
    public AnalyzerEditor() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        tabber = new JTabbedPane();
        add(tabber, BorderLayout.CENTER);
        
        classGrepAnalyzerEditor = new ClassGrepAnalyzerEditor();
        tabber.addTab("Class Grep", classGrepAnalyzerEditor);
        
        fileFinderAnalyzerEditor = new FileFinderAnalyzerEditor();
        tabber.addTab("File Finder", fileFinderAnalyzerEditor);
        
        classMatcherAnalyzerEditor = new ClassMatcherAnalyzerEditor();
        tabber.addTab("Class Matcher", classMatcherAnalyzerEditor);
        
        customAnalyzerEditor = new CustomAnalyzerEditor();
        tabber.addTab("Custom", customAnalyzerEditor);
        
        JPanel parameterPanel = new JPanel();
        parameterPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2,2,2,2);
        
        parameterPanel.add(new JLabel("Parameters"), gbc);

        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        
        parameterField = new JTextField();
        parameterField.setEditable(false);
        parameterPanel.add(parameterField, gbc);
        
        
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx++;
        JButton button = new JButton("Edit...");
        parameterPanel.add(button, gbc);
        
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ParameterEditor editor = new ParameterEditor(parameterList);
                
                Window w = SwingUtilities.windowForComponent(AnalyzerEditor.this);
                boolean wasCanceled = DialogWrapper.showDialogWrapper(w, "Parameter Editor", editor);
                if (!wasCanceled) {
                    parameterList.removeAll(parameterList);
                    String parameters[][] = editor.getParameters();
                    for (int i=0; i<parameters.length; i++) 
                        parameterList.add(parameters[i]);
                    
                    updateParameterField();
                }
            }
        });
        
        add(parameterPanel, BorderLayout.SOUTH);
    }
    
    void updateParameterField() {
        parameterField.setText(toParameterText(parameterList));
    }
    
    public static String toParameterText(List<String[]> parameterList) {
        Iterator<String[]> it = parameterList.listIterator();
        String param[];
        StringBuilder buffer = new StringBuilder();
        while (it.hasNext()) {
            param = it.next();
            buffer.append(param[0]);
            buffer.append('=');
            buffer.append(param[1]);
            if (it.hasNext())
                buffer.append(", ");
        }
        
        return buffer.toString();
    }
    
    public boolean isCustomAnalyzer() {
        return tabber.getSelectedIndex() == tabber.getTabCount() - 1;
    }
    
    public DeploymentVisitor getDeploymentVisitor() {
        int index = tabber.getSelectedIndex();
        switch (index) {
            case 0:
                return getClassGrepAnalyzer();
            case 1:
                return getFileFinderAnalyzer();
            case 2:
                return getClassMatcherAnalyzer();
            case 3:
                throw new RuntimeException("For custom analyzers, call getCustomAnalyzer()");
            default: throw new RuntimeException("Unknown index type: " + index);
        }
    }
    
    private DeploymentVisitor getClassGrepAnalyzer() {
        return classGrepAnalyzerEditor.getAnalyzer();
    }
    
    
    private DeploymentVisitor getFileFinderAnalyzer() {
        return fileFinderAnalyzerEditor.getAnalyzer();
    }
    
    private DeploymentVisitor getClassMatcherAnalyzer() {
        return classMatcherAnalyzerEditor.getAnalyzer();
    }
    
    public CustomAnalyzerDescriptor getCustomAnalyzerDescriptor() {
        return new CustomAnalyzerDescriptor(customAnalyzerEditor.titleField.getText(), customAnalyzerEditor.summaryField.getText(), customAnalyzerEditor.descriptionField.getText(), customAnalyzerEditor.linksField.getLinks(), customAnalyzerEditor.classField.getText());
    }
    
    public void savePreferences() {
        classGrepAnalyzerEditor.savePreferences();
        fileFinderAnalyzerEditor.savePreferences();
        classMatcherAnalyzerEditor.savePreferences();
        customAnalyzerEditor.savePreferences();
    }
    
    public void loadPreferences() {
        classGrepAnalyzerEditor.loadPreferences();
        fileFinderAnalyzerEditor.loadPreferences();
        classMatcherAnalyzerEditor.loadPreferences();
        customAnalyzerEditor.loadPreferences();
    }
}
