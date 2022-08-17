package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.discotek.common.swing.BrowserHelpBroker;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.security.ProjectConfiguration;
import ca.discotek.deepdive.security.gui.config.ConfigurationView;
import ca.discotek.deepdive.security.misc.ImportExportUtil;
import ca.discotek.deepdive.security.misc.ImportExportUtil.ProjectConfigurationImportDescriptor;
import ca.discotek.deepdive.security.misc.ImportExportUtil.ProjectExportDescriptor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.projectmanager.InvalidProjectNameException;
import ca.discotek.projectmanager.Project;
import ca.discotek.projectmanager.ProjectManager;
import ca.discotek.projectmanager.gui.ProjectNameEditor;

public class ImportDialog extends JDialog {

    ConfigurationView gui;
    
    ProjectConfigurationImportDescriptor pcid;
    
    AnalyzerConfiguration configs[];
    
    JCheckBox projectCheckBoxes[];
    JCheckBox categoryCheckBoxes[][];
    JCheckBox analyzerCheckBoxes[][][];
    
    final BrowserHelpBroker helpBroker;
    
    public ImportDialog(BrowserHelpBroker helpBroker, ConfigurationView gui, ProjectConfigurationImportDescriptor projectConfigurationImportDescriptor) {
        super(gui, "Import Configuration", true);
        this.helpBroker = helpBroker;
        this.gui = gui;
        setModalityType(ModalityType.DOCUMENT_MODAL);
        this.pcid = projectConfigurationImportDescriptor;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        add(buildCheckBoxPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
        
        helpBroker.enableHelpKey(getRootPane(), "import-server", helpBroker.getHelpSet());
    }
    
    JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(2,2,2,2);
        
        panel.add(new JLabel(), gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        JButton button = new JButton("Import...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ProjectConfiguration pc = gui.getProjectConfiguration();
                    doImport(pc);
                    gui.setProjectConfiguration(pc);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(ImportDialog.this, "Error occured while importing. See stderr for details.", "Import Error", JOptionPane.ERROR_MESSAGE);
                    setVisible(false);
                }
                finally {
                    setVisible(false);
                }
            }
        });
        panel.add(button, gbc);
        
        gbc.gridx++;
        
        button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        panel.add(button, gbc);
        
        return panel;
    }
    
    void doImport(ProjectConfiguration pc) throws IOException, SAXException, ParserConfigurationException {
       boolean isCanceled = false;
        String message;
        Object selectionValues[];
        Object object;
        String configNames[] = pc.getUserDefinedConfigurationNames();
        List<String> configNameList = Arrays.asList(configNames);

        for (int i=0; i<pcid.names.length; i++) {
            if (!projectCheckBoxes[i].isSelected())
                continue;
                    
                String importConfigNames[] = pcid.configs[i].getUserDefinedConfigurationNames();
                for (int j=0; j<importConfigNames.length; j++) {
                    if (!categoryCheckBoxes[i][j].isSelected() || !categoryCheckBoxes[i][j].isEnabled())
                        continue;
                    if (configNameList.contains(importConfigNames[j])) {
                        message = "A category with name\"" + configNames[j] + "\" already exists in project \"" + pcid.names[i] + "\".";
                        selectionValues = new String[] {
                                "Import analyzers into existing category",
                                "Import categories into new category"
                        };
                        object = JOptionPane.showInputDialog(
                                gui, 
                                message , 
                                "Category Exists Warning", 
                                JOptionPane.WARNING_MESSAGE, 
                                null, 
                                selectionValues, selectionValues[0]);

                        if (object == selectionValues[0]) {
                            AnalyzerConfiguration analyzerConfig = pc.getUserDefinedConfiguration(importConfigNames[j]);
                            AnalyzerConfiguration importConfig = pcid.configs[i].getUserDefinedConfiguration(importConfigNames[j]);
                            
                            isCanceled = importAnalyzer(importConfigNames[j], importConfig, analyzerConfig, analyzerCheckBoxes[i][j]);
                        }
                        else if (object == selectionValues[1]) {
                            NameEditor editor = new NameEditor(importConfigNames[j]);
                            String categoryName;
                            boolean foundValidName = false;
                            while (!foundValidName) {
                                boolean wasCanceled = DialogWrapper.showDialogWrapper(this, "New Category Name Editor", editor);
                                if (wasCanceled)
                                    break;
                                else if ( configNameList.contains( (categoryName = editor.getName())) ) {
                                    message = "The new category name already exists project \"" + pcid.names[i] + "\". Try a new name?";
                                    int result = JOptionPane.showConfirmDialog(this, message, "New Name Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                                    if (result == JOptionPane.CANCEL_OPTION) {
                                        isCanceled = true;
                                        break;
                                    }
                                }
                                else {
                                    AnalyzerConfiguration ac = new AnalyzerConfiguration(categoryName);
                                    AnalyzerConfiguration importConfig = pcid.configs[i].getUserDefinedConfiguration(pcid.names[i]);
                                    ac.clone(importConfig);
                                    pc.addConfiguration(categoryName, ac);
                                    foundValidName = true;
                                }
                            }
                        }

                        if (isCanceled)
                            break;
                    }
                    else {
                        AnalyzerConfiguration analyzerConfig = pc.getUserDefinedConfiguration(importConfigNames[j]);
                        if (analyzerConfig == null) {
                            analyzerConfig = new AnalyzerConfiguration(importConfigNames[j]);
                            pc.addConfiguration(importConfigNames[j], analyzerConfig);
                        }
                        AnalyzerConfiguration importConfig = pcid.configs[i].getUserDefinedConfiguration(importConfigNames[j]);
                        isCanceled = importAnalyzer(importConfigNames[j], importConfig, analyzerConfig, analyzerCheckBoxes[i][j]);
                    }
                }

            if (isCanceled)
                break;
        }
        
        gui.reloadCurrentProject();
    }
    
    boolean importAnalyzer(String importConfigName, AnalyzerConfiguration importConfig, AnalyzerConfiguration analyzerConfig, JCheckBox analyzerCheckBoxes[]) {
        String analyzerNames[] = analyzerConfig.getDeploymentVisitorNames();
        List<String> analyzerNameList = new ArrayList<String>();
        analyzerNameList.addAll(Arrays.asList(analyzerNames));

        String importAnalyzerNames[] = importConfig.getDeploymentVisitorNames();
        String message;
        Object object;
        Object selectionValues[];
        for (int k=0; k<importAnalyzerNames.length; k++) {
            if (!analyzerCheckBoxes[k].isSelected() || !analyzerCheckBoxes[k].isEnabled())
                continue;
            if (analyzerNameList.contains(importAnalyzerNames[k])) {
                message = "A analyzer with name\"" + importAnalyzerNames[k] + "\" already exists category \"" + importConfigName + "\".";
                selectionValues = new String[] {
                        "Rename imported analyzer",
                        "Replace existing analyzer"
                };
                object = JOptionPane.showInputDialog(
                        gui, 
                        message , 
                        "Analyzer Exists Warning", 
                        JOptionPane.WARNING_MESSAGE, 
                        null, 
                        selectionValues, selectionValues[0]);
                
                if (object == selectionValues[0]) {
                    NameEditor editor = new NameEditor(importAnalyzerNames[k]);
                    String analyzerName;
                    boolean foundValidName = false;
                    while (!foundValidName) {
                        boolean wasCanceled = DialogWrapper.showDialogWrapper(this, "New Category Name Editor", editor);
                        if (wasCanceled)
                            break;
                        else if ( analyzerNameList.contains( (analyzerName = editor.getName())) ) {
                            message = "The new analyzer name already exists in category \"" + importConfigName + "\". Try a new name?";
                            int result = JOptionPane.showConfirmDialog(this, message, "New Name Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                            if (result == JOptionPane.CANCEL_OPTION)
                                return true;
                        }
                        else {
                            DeploymentVisitor dv = importConfig.getDeploymentVisitor(importAnalyzerNames[k]);
                            dv.setTitle(analyzerName);
                            analyzerConfig.addDeploymentVisitor(dv);
                            foundValidName = true;
                        }
                    }
                }
                else if (object == selectionValues[1]) {
                    DeploymentVisitor dv = importConfig.getDeploymentVisitor(importAnalyzerNames[k]);
                    analyzerConfig.removeDeploymentVisitor(dv);
                    analyzerConfig.addDeploymentVisitor(dv);
                }
            }
            else
                analyzerConfig.addDeploymentVisitor(importConfig.getDeploymentVisitor(importAnalyzerNames[k]));
         }
        return false;
    }
    
    JPanel buildCheckBoxPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = gbc.weighty = 0;
        gbc.insets = new Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.WEST;
        
        projectCheckBoxes = new JCheckBox[pcid.configs.length];
        
        categoryCheckBoxes = new JCheckBox[pcid.configs.length][];
        analyzerCheckBoxes = new JCheckBox[pcid.configs.length][][];

        int gridWidth = getMaxAnalyzers() + 2;
        
        
        JLabel label = new JLabel("Project");
        panel.add(label, gbc);
        
        gbc.gridx++;
        
        label = new JLabel("Category");
        panel.add(label, gbc);
        
        gbc.gridx++;
        
        label = new JLabel("Analyzers");
        panel.add(label, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        
        addSeparator(panel, gridWidth, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy++;

        String category;
        String analyzerNames[];
        CheckBoxEnableListener projectCheckBoxEnableListener;
        CheckBoxEnableListener categoryCheckBoxEnableListener;
        for (int i=0; i<pcid.configs.length; i++) {
            projectCheckBoxes[i] = new JCheckBox(pcid.names[i], true);
            projectCheckBoxEnableListener = new CheckBoxEnableListener(projectCheckBoxes[i]);
            panel.add(projectCheckBoxes[i], gbc);
            gbc.gridx++;
            
            configs = pcid.configs[i].getConfigurations();
            categoryCheckBoxes[i] = new JCheckBox[configs.length];
            analyzerCheckBoxes[i] = new JCheckBox[configs.length][];
            for (int j=0; j<configs.length; j++) {
                category = configs[j].getCategory();
                categoryCheckBoxes[i][j] = new JCheckBox(category, true);
                projectCheckBoxEnableListener.add(categoryCheckBoxes[i][j]);
                categoryCheckBoxEnableListener = new CheckBoxEnableListener(categoryCheckBoxes[i][j]);
                panel.add(categoryCheckBoxes[i][j], gbc);
                gbc.gridx++;
                
                analyzerNames = configs[j].getDeploymentVisitorNames();
                analyzerCheckBoxes[i][j] = new JCheckBox[analyzerNames.length];
                for (int k=0; k<analyzerNames.length; k++) {
                    analyzerCheckBoxes[i][j][k] = new JCheckBox(analyzerNames[k], true);
                    projectCheckBoxEnableListener.add(analyzerCheckBoxes[i][j][k]);
                    categoryCheckBoxEnableListener.add(analyzerCheckBoxes[i][j][k]);
                    panel.add(analyzerCheckBoxes[i][j][k], gbc);
                    gbc.gridy++;
                }
                gbc.gridx = 1;
                gbc.gridy++;
            }
            
            gbc.gridx = 0;
            gbc.gridy++;
            addSeparator(panel, gridWidth, gbc);
            gbc.gridy++;
        }
        
        return panel;
    }
    
    void addSeparator(JPanel panel, int gridWidth, GridBagConstraints gbc) {
        gbc.gridwidth = gridWidth;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
    }
    
    int getMaxAnalyzers() {
        
        int max = -1;
        
        AnalyzerConfiguration configs[];
        for (int i=0; i<pcid.configs.length; i++) {
            configs = pcid.configs[i].getConfigurations();
            for (int j=0; j<configs.length; j++) {
                max = Math.max(max, configs[j].getDeploymentVisitorNames().length);
            }
        }
        
        return max;
    }
    
    class CheckBoxEnableListener implements ItemListener {
        final JCheckBox box;
        final List<JCheckBox> list = new ArrayList<JCheckBox>();
        
        CheckBoxEnableListener(JCheckBox box) {
            this.box = box;
            box.addItemListener(this);
        }
        
        void add(JCheckBox box) {
            list.add(box);
        }

        public void itemStateChanged(ItemEvent e) {
            boolean enabled = box.isSelected();
            Iterator<JCheckBox> it = list.listIterator();
            while (it.hasNext())
                it.next().setEnabled(enabled);
        }
    }
    
    static final ImportExportFileFilter IMPORT_EXPORT_FILE_FILTER = new ImportExportFileFilter();
    
    static class ImportExportFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.getName().endsWith(".jar");
        }

        public String getDescription() {
            return "*.jar";
        }
    }
}
