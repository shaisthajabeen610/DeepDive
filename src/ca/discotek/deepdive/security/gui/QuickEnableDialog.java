package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ca.discotek.common.swing.BrowserHelpBroker;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.common.swing.HistoryComboBox;
import ca.discotek.deepdive.security.ProfileSelectionConfiguration;
import ca.discotek.deepdive.security.SelectionConfiguration;
import ca.discotek.deepdive.security.dom.Apk;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.gui.config.exclusions.ExclusionEditor;
import ca.discotek.deepdive.security.gui.path.ExternalDependencyEditor;
import ca.discotek.common.swing.CheckBoxNode;
import ca.discotek.common.swing.CheckBoxTree;
import ca.discotek.common.swing.FileChooser;
import ca.discotek.deepdive.security.visitor.ArchiveVisitor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;

public class QuickEnableDialog extends JDialog {

    final Window parent;

    CheckBoxNode systemRootNode = null;
    CheckBoxNode userDefinedRootNode = null;
    CheckBoxNode thirdPartyRootNode = null;
        
    final AnalyzerConfiguration builtInParsers[];
    final AnalyzerConfiguration userDefinedParsers[];
    final AnalyzerConfiguration thirdPartyParsers[];
    
    DefaultTreeModel model; 
    CheckBoxTree tree;
    
    boolean wasCanceled = true;
   
    ProfileSelectionConfiguration psc;
    
    final ArchiveVisitor archives[];
    
    JCheckBox includePathReportField;

    JRadioButton filterDisableButton;
    JRadioButton filterIncludeOnlyButton;
    JRadioButton filterExcludeButton;
    JTextField filterField;

    
    JCheckBox enableDiskOutputField;
    FileChooser directoryChooserField;
    
    boolean editEnableFlagsOnly;
    
    boolean includePathReportAndNotCanceled = false;
    File externalDependencies[] = new File[0];
    boolean followAbstractMethods = false;
    
    public QuickEnableDialog
        (BrowserHelpBroker helpBroker, 
         JFrame parent, 
         AnalyzerConfiguration builtInParsers[], 
         AnalyzerConfiguration userDefinedParsers[], 
         AnalyzerConfiguration thirdPartyParsers[], 
         ProfileSelectionConfiguration psc,
         ArchiveVisitor archives[]) {
        
        super(parent, "Report Options", true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        this.parent = parent;
        this.psc = psc;
        
        this.builtInParsers = builtInParsers;
        this.userDefinedParsers = userDefinedParsers;
        this.thirdPartyParsers = thirdPartyParsers;
        
        this.archives = archives;
        
        editEnableFlagsOnly = false;
        buildGui(helpBroker);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public QuickEnableDialog
        (BrowserHelpBroker helpBroker, JDialog parent, AnalyzerConfiguration builtInParsers[], AnalyzerConfiguration userDefinedParsers[], AnalyzerConfiguration thirdPartyParsers[], ProfileSelectionConfiguration psc) {
        super(parent, "Report Options", true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        this.parent = parent;
        this.psc = psc;
        
        this.builtInParsers = builtInParsers;
        this.userDefinedParsers = userDefinedParsers;
        this.thirdPartyParsers = thirdPartyParsers;
        
        editEnableFlagsOnly = true;
        archives = null;
        buildGui(helpBroker);
    }
    
    void buildGui(BrowserHelpBroker helpBroker) {
        helpBroker.enableHelpKey(getRootPane(), editEnableFlagsOnly ? "interface" : "interface-reports", helpBroker.getHelpSet());
        
        setLayout(new BorderLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        gbc.insets = new Insets(2,2,2,2);
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());
        
        CheckBoxNode rootNode = new CheckBoxNode(null, "Root", true);
        systemRootNode = new CheckBoxNode(null, "System", true);
        rootNode.add(systemRootNode);
        buildAnalyzerConfigurationTree(systemRootNode, builtInParsers);
        
        if (psc != null)
            updateSelection(systemRootNode, psc.getSystemSelectionConfiguration());
        
        if (userDefinedParsers.length > 0) {
            userDefinedRootNode = new CheckBoxNode(null, "User Defined", true);
            rootNode.add(userDefinedRootNode);
            buildAnalyzerConfigurationTree(userDefinedRootNode, userDefinedParsers);
            
            if (psc != null)
                updateSelection(userDefinedRootNode, psc.getUserDefinedSelectionConfiguration());
        }
        
        if (thirdPartyParsers.length > 0) {
            thirdPartyRootNode = new CheckBoxNode(null, "Third Party", true);
            rootNode.add(thirdPartyRootNode);
            buildAnalyzerConfigurationTree(thirdPartyRootNode, thirdPartyParsers);
   
            if (psc != null)
                updateSelection(thirdPartyRootNode, psc.getThirdPartySelectionConfiguration());
        }
        
        model = new DefaultTreeModel(rootNode);
        tree = new CheckBoxTree(model);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        
        expand(rootNode);

        optionsPanel.add(new JScrollPane(tree), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new BorderLayout());
        final JCheckBox box = new JCheckBox("Select All", true);
        box.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean selected = box.isSelected();
                selectAll(selected);
            }
        });
        selectorPanel.add(box, BorderLayout.WEST);
        optionsPanel.add(selectorPanel, gbc);
        
        
        if (archives != null) {
            gbc.gridy++;
            optionsPanel.add(buildFilterPanel(), gbc);
        }
        
        if (!editEnableFlagsOnly) {
            
            gbc.gridy++;
            includePathReportField = new JCheckBox("Include Path Report", false);
            optionsPanel.add(includePathReportField, gbc);
            
            gbc.gridy++;
            enableDiskOutputField = new JCheckBox("Save Report to Disk", false);
            optionsPanel.add(enableDiskOutputField, gbc);
            
            gbc.gridy++;
            final javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
                public String getDescription() {
                    return "Directories only";
                }
                
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            };
            
            directoryChooserField = FileChooser.createJarChooser("report-directory", 50, filter);
            directoryChooserField.setEnabled(false);
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints panelGbc = new GridBagConstraints();
            panelGbc.gridx = panelGbc.gridy = 0;
            panelGbc.weightx = panelGbc.weighty = 1;
            panelGbc.fill = GridBagConstraints.NONE;
            panelGbc.insets = new Insets(2,2,2,2);
            panelGbc.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel("Report Directory"), panelGbc);
            panelGbc.gridx++;
            panelGbc.weightx = 1;
            panelGbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(directoryChooserField, panelGbc);
            optionsPanel.add(panel, gbc);
            

            enableDiskOutputField.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    directoryChooserField.setEnabled(enableDiskOutputField.isSelected());
                }
            });

        }
        
        add(optionsPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        add(buttonPanel, BorderLayout.SOUTH);
        gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,2,2,2);
        
        buttonPanel.add(new JLabel(""), gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        JButton okayButton = new JButton("Okay");
        buttonPanel.add(okayButton, gbc);
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                wasCanceled = false;
                if (!editEnableFlagsOnly && enableDiskOutputField.isSelected()) {
                    String value = directoryChooserField.getValue();
                    if (value != null && value.trim().length() > 0)
                        directoryChooserField.save();
                }
                if (!editEnableFlagsOnly) {
                    if (includePathReportField.isSelected()) {
                        final ExternalDependencyEditor editor = new ExternalDependencyEditor(false);
                        boolean wasCanceled = DialogWrapper.showDialogWrapper(QuickEnableDialog.this, "Options", editor);
                        if (!wasCanceled) {
                            includePathReportAndNotCanceled = true;
                            externalDependencies = editor.getExternalDependencies();
                            followAbstractMethods = editor.getFollowAbstractMethods();
                            setVisible(false);
                        }
                    }
                    else
                        setVisible(false);
                }
                else
                    setVisible(false);
            }
        });
        
        gbc.gridx++;
        
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton, gbc);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                wasCanceled = true;
                setVisible(false);
            }
        });
    }
    
    JPanel buildFilterPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = gbc.weighty = 0;
        gbc.insets = new Insets(2,2,2,2);
        
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        filterPanel.add(new JLabel("Filter Archives"), gbc);
        ButtonGroup group = new ButtonGroup();
        
        gbc.gridx++;
        filterDisableButton = new JRadioButton("Disabled", true);
        group.add(filterDisableButton);
        filterPanel.add(filterDisableButton, gbc);
        
        gbc.gridx++;
        filterIncludeOnlyButton = new JRadioButton("Include Only", false);
        group.add(filterIncludeOnlyButton);
        filterPanel.add(filterIncludeOnlyButton, gbc);
        
        gbc.gridx++;
        filterExcludeButton = new JRadioButton("Exclude", false);
        group.add(filterExcludeButton);
        filterPanel.add(filterExcludeButton, gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        filterField = new JTextField();
        filterField.setEditable(false);
        filterPanel.add(filterField, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        final JButton button = new JButton("Edit..");
        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ExclusionEditor editor = new ExclusionEditor();

                
                try {
                    for (int i=0; i<archives.length; i++) {
                        if (archives[i] instanceof Ear)
                            editor.addDeploymentUnit( ((Ear) archives[i]).path );
                        else if (archives[i] instanceof War)
                            editor.addDeploymentUnit( ((War) archives[i]).getPath().toString() );
                        else if (archives[i] instanceof Jar)
                            editor.addDeploymentUnit( ((Jar) archives[i]).getPath().toString() );
                        else if (archives[i] instanceof Apk)
                            editor.addDeploymentUnit( ((Apk) archives[i]).path );
                        else
                            throw new RuntimeException("Unknown archive type: " + archives[i].getClass().getName());
                    }
                } 
                catch (IOException e2) {
                    throw new RuntimeException("Error adding deployment units. Should never happen.");
                }
                
                String text = filterField.getText().trim();
                if (text.length() > 0) {
                    String paths[] = text.split(File.pathSeparator);
                    editor.setSelectedPaths(paths);
                }


                boolean canceled = DialogWrapper.showDialogWrapper(parent, "Deployment Selector", editor);
                if (!canceled) {
                    String paths[] = editor.getSelectedPaths();
                    StringBuilder buffer = new StringBuilder();
                    for (int i=0; i<paths.length; i++) {
                        buffer.append(paths[i]);
                        if (i<paths.length-1)
                            buffer.append(File.pathSeparator);
                    }
                    
                    filterField.setText(buffer.toString());
                }

                
            }
        });
        
        filterPanel.add(button, gbc);
        
        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                button.setEnabled(!filterDisableButton.isSelected());
            }
        };
        
        filterDisableButton.addChangeListener(cl);
        filterIncludeOnlyButton.addChangeListener(cl);
        filterExcludeButton.addChangeListener(cl);
        
        return filterPanel;
    }
    
    void updateSelection(CheckBoxNode node, SelectionConfiguration sc) {
        CheckBoxNode child = (CheckBoxNode) node.getFirstChild();
        CheckBoxNode dvChild;
        DeploymentVisitor dv;
        
        boolean allAnalyzerSelected = true;
        boolean allAnalyzerDeselected = true;
        
        boolean allCategoryAnalyzerSelected = true;
        boolean allCategoryAnalyzerDeselected = true;
        
        boolean selected;
        if (node.getChildCount() > 0) {
            do {
                if (child.getChildCount() > 0) {
                    dvChild = (CheckBoxNode) child.getFirstChild();
                    do {
                        dv = (DeploymentVisitor) dvChild.getValue();
                        selected = sc.containsAnalyzer(dv.getTitle());
                        dvChild.setSelected(selected, true, true);
                        if (selected) {
                            allCategoryAnalyzerDeselected = false;
                            allAnalyzerDeselected = false;
                        }
                        else {
                            allCategoryAnalyzerSelected = false;
                            allAnalyzerSelected = false;
                        }
                    }
                    while ( (dvChild = (CheckBoxNode) dvChild.getNextSibling()) != null);
                }
                
                if (allCategoryAnalyzerSelected) {
                    child.setSelected(true, false, true);
                }
                else if (allCategoryAnalyzerDeselected) {
                    child.setSelected(false, false, true);
                }
                
                allCategoryAnalyzerSelected = true;
                allCategoryAnalyzerDeselected = true;
            }
            while ( (child = (CheckBoxNode) child.getNextSibling()) != null);
            
            if (allAnalyzerSelected) {
                node.setSelected(true, false, true);
            }
            else if (allAnalyzerDeselected)
                node.setSelected(false, false, true);
        }
    }
    
    void expand(CheckBoxNode node) {
        tree.expandPath(new TreePath(node.getPath()));

        if (node.getChildCount() > 0) {
            CheckBoxNode child = (CheckBoxNode) node.getFirstChild();
            do {
                expand(child);
            }
            while ( (child = (CheckBoxNode) child.getNextSibling()) != null);
        }
    }
    
    void selectAll(boolean selected) {
        CheckBoxNode node = (CheckBoxNode) tree.getModel().getRoot();
        selectNodes(node, selected);
        tree.repaint();
    }
    
    void selectNodes(CheckBoxNode node, boolean selected) {
        node.setSelected(selected, true, true);
        if (node.getChildCount() > 0) {
            node = (CheckBoxNode) node.getFirstChild();
            do { selectNodes(node, selected); }
            while ( (node = (CheckBoxNode) node.getNextSibling()) != null);
        }
    }
    
    void buildAnalyzerConfigurationTree(CheckBoxNode node, AnalyzerConfiguration configs[]) {
        CheckBoxNode analyzerConfigurationNode;
        DeploymentVisitor dvs[];
        for (int i=0; i<configs.length; i++) {
            analyzerConfigurationNode = new CheckBoxNode(configs[i], configs[i].getCategory(), true);
            node.add(analyzerConfigurationNode);
            dvs = configs[i].getDeploymentVisitors();
            for (int j=0; j<dvs.length; j++)
                analyzerConfigurationNode.add(new CheckBoxNode(dvs[j], dvs[j].getTitle(), true));
        }
    }
    
    CheckBoxTree buildTree(AnalyzerConfiguration ac) {
        CheckBoxNode categoryNode = new CheckBoxNode(ac, ac.getCategory(), true);
        DeploymentVisitor dvs[] = ac.getDeploymentVisitors();
        CheckBoxNode analyzerNode;
        for (int i=0; i<dvs.length; i++) {
            analyzerNode = new CheckBoxNode(dvs[i], dvs[i].getTitle(), true);
            categoryNode.add(analyzerNode);
        }
        return new CheckBoxTree(categoryNode);
    }
    
    public ProfileSelectionConfiguration getProfileSelectionConfiguration() {
        ProfileSelectionConfiguration psc =  new ProfileSelectionConfiguration();
        setSelectionConfiguration(psc.getSystemSelectionConfiguration(), systemRootNode);
        if (userDefinedRootNode != null)
            setSelectionConfiguration(psc.getUserDefinedSelectionConfiguration(), userDefinedRootNode);
        if (thirdPartyRootNode != null)
            setSelectionConfiguration(psc.getThirdPartySelectionConfiguration(), thirdPartyRootNode);
        return psc;
    }
    
    public void setSelectionConfiguration(SelectionConfiguration sc, CheckBoxNode node) {
        
        CheckBoxNode child = (CheckBoxNode) node.getFirstChild();
        CheckBoxNode dvChild;
        do {
            if (child.getSelected()) {
                sc.addAnalyzerConfiguration( (AnalyzerConfiguration) child.getValue() );
                
                dvChild = (CheckBoxNode) child.getFirstChild();
                do {
                    if (dvChild.getSelected())
                        sc.addAnalyzer((DeploymentVisitor) dvChild.getValue());
                }
                while ( (dvChild = (CheckBoxNode) dvChild.getNextSibling()) != null);

            }
        }
        while ( (child = (CheckBoxNode) child.getNextSibling()) != null);
    }
    
    private AnalyzerConfiguration[] getSelectedAnalyzerConfigurations(CheckBoxNode acRootNode) {
        List<AnalyzerConfiguration> list = new ArrayList<AnalyzerConfiguration>();
        CheckBoxNode child = (CheckBoxNode) acRootNode.getFirstChild();
        
        do {
            if (child.getSelected())
                list.add( (AnalyzerConfiguration) child.getValue());
        }
        while ( (child = (CheckBoxNode) child.getNextSibling()) != null);
                
        return list.toArray(new AnalyzerConfiguration[list.size()]);
    }

    public boolean getWasCanceled() {
        return wasCanceled;
    }
    
    public boolean isSaveToDiskEnabled() {
        return enableDiskOutputField.isSelected();
    }
    
    public String getReportDirectory() {
        return directoryChooserField.getFile();
    }
    
    public boolean isPathReportIncluded() {
        return includePathReportAndNotCanceled;
    }
    
    public File[] getExternalDependencies() {
        return externalDependencies;
    }
    
    public boolean getFollowAbstractMethods() {
        return followAbstractMethods;
    }
    
    public boolean isFilterEnabled() {
        return !filterDisableButton.isSelected();
    }
    
    public boolean isInclusionFilter() {
        return filterIncludeOnlyButton.isSelected();
    }
    
    public boolean isExclusionFilter() {
        return filterExcludeButton.isSelected();
    }
    
    public String[] getPaths() {
        return filterField.getText().replace('\\', '/').trim().split(File.pathSeparator);
    }
}
