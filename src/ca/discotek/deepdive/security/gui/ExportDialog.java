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

import ca.discotek.deepdive.security.ProjectConfiguration;
import ca.discotek.deepdive.security.misc.ImportExportUtil;
import ca.discotek.deepdive.security.misc.ImportExportUtil.ProjectConfigurationImportDescriptor;
import ca.discotek.deepdive.security.misc.ImportExportUtil.ProjectExportDescriptor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.projectmanager.InvalidProjectNameException;
import ca.discotek.projectmanager.Project;

public class ExportDialog extends JDialog {

    Project selectedProject;
    Project projects[];
    ProjectConfiguration projectConfigurations[];
    
    AnalyzerConfiguration configs[];
    
    JCheckBox projectCheckBoxes[];
    JCheckBox categoryCheckBoxes[][];
    JCheckBox analyzerCheckBoxes[][][];
    
    public ExportDialog(JDialog parent, Project selectedProject, Project projects[], ProjectConfiguration projectConfigurations[]) {
        super(parent, "Export Configuration", true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        this.selectedProject = selectedProject;
        this.projects = projects;
        this.projectConfigurations = projectConfigurations;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        add(buildCheckBoxPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
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

        JButton button = new JButton("Export...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    export();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(ExportDialog.this, "Error occured while exporting. See stderr for details.", "Export Error", JOptionPane.ERROR_MESSAGE);
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
    
    void export() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        File currentDirectory = ImportExportUtil.getCurrentDirectory();
        if (currentDirectory != null)
            chooser.setCurrentDirectory(currentDirectory);
        chooser.setDialogTitle("Export to Jar File");
        chooser.setFileFilter(IMPORT_EXPORT_FILE_FILTER);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            ImportExportUtil.setCurrentDirectory(chooser.getSelectedFile().getParentFile());
            List<ProjectExportDescriptor> list = new ArrayList<ProjectExportDescriptor>();
            
            ProjectExportDescriptor ped;
            for (int i=0; i<projectCheckBoxes.length; i++) {
                if (projectCheckBoxes[i] != null && projectCheckBoxes[i].isSelected()) {
                    ped = new ProjectExportDescriptor();
                    list.add(ped);
                    for (int j=0; j<categoryCheckBoxes[i].length; j++) {
                        if (categoryCheckBoxes[i][j].isSelected()) {
                            for (int k=0; k<analyzerCheckBoxes[i][j].length; k++) {
                                if (analyzerCheckBoxes[i][j][k].isSelected()) {
                                    ped.add(projectConfigurations[i], projects[i], categoryCheckBoxes[i][j].getText(), analyzerCheckBoxes[i][j][k].getText());
                                }
                            }
                        }
                    }
                }
            }
            
            ImportExportUtil.exportToFile(list.toArray(new ProjectExportDescriptor[list.size()]), chooser.getSelectedFile());
        }
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
        
        projectCheckBoxes = new JCheckBox[projectConfigurations.length];
        
        categoryCheckBoxes = new JCheckBox[projectConfigurations.length][];
        analyzerCheckBoxes = new JCheckBox[projectConfigurations.length][][];

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
        
        /*
         * Originally you could export all the projects, which is why there is a loop for projects.
         * I only want to export the project currently being edited so this is why there is
         * an if statement for that project in the looping. I am leaving the project loop
         * intact just because I am not 100% I want to keep it this way.
         */
        
        for (int i=0; i<projectConfigurations.length; i++) {
            
            if (selectedProject != projects[i]) 
                continue;
            
            projectCheckBoxes[i] = new JCheckBox(projects[i].getName(), true);
            projectCheckBoxEnableListener = new CheckBoxEnableListener(projectCheckBoxes[i]);
            panel.add(projectCheckBoxes[i], gbc);
            gbc.gridx++;
            
            configs = projectConfigurations[i].getConfigurations();
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
        for (int i=0; i<projectConfigurations.length; i++) {
            configs = projectConfigurations[i].getConfigurations();
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
            return f.isDirectory() || f.getName().endsWith(".jar");
        }

        public String getDescription() {
            return "*.jar";
        }
    }
}
