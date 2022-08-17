package ca.discotek.deepdive.security.gui.config;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.discotek.common.swing.BasicTable;
import ca.discotek.common.swing.BrowserHelpBroker;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.common.swing.DirtyListener;
import ca.discotek.deepdive.security.ConfigurationUtil;
import ca.discotek.deepdive.security.ProfileSelectionConfiguration;
import ca.discotek.deepdive.security.ProjectConfiguration;
import ca.discotek.deepdive.security.SelectionConfiguration;
import ca.discotek.deepdive.security.ThirdPartyUtil;
import ca.discotek.deepdive.security.gui.Actions;
import ca.discotek.deepdive.security.gui.DeepDiveIconUtil;
import ca.discotek.deepdive.security.gui.QuickEnableDialog;
import ca.discotek.deepdive.security.gui.editor.AnalyzerEditor;
import ca.discotek.deepdive.security.gui.editor.CustomAnalyzerDescriptor;
import ca.discotek.common.swing.ButtonPanel;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.deepdive.security.visitor.assess.ClassGrepAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.ClassMatcherAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.FileFinderAnalyzer;
import ca.discotek.deepdive.security.visitor.assess.custom.ParameterizedCustomDeploymentVisitor;
import ca.discotek.projectmanager.Project;
import ca.discotek.projectmanager.ProjectManager;

public class ConfigurationView extends JDialog implements PropertyChangeListener, VetoableChangeListener {

    static final Pattern ILLEGAL_CATEGORY_NAME_PATTERN = Pattern.compile("[^a-z,A-Z,0-9, ,-,_,.]");
    
    Project project;
    final ProjectManager projectManager;
    DirtyListener dirtyListener;
    
    JPanel systemPanel;
    JPanel userDefinedPanel;
    JPanel thirdPartyPanel;

    CategoryButtonPanel categoryButtonPanel;
    
    JTabbedPane tabber;
    JTabbedPane systemTabber;
    JTabbedPane userDefinedTabber;
    JTabbedPane thirdPartyTabber;
    
    File currentDirectory = null;
    final AbstractAction QUICK_SELECT_EDITOR_ACTION = new Actions.QuickSelectEditorAction(this);
    final AbstractAction IMPORT_ACTION = new Actions.ImportAction(this);
    final AbstractAction EXPORT_ACTION = new Actions.ExportAction(this);
    
    JFrame parent;
    
    final BrowserHelpBroker helpBroker;
    
    public ConfigurationView(BrowserHelpBroker helpBroker, JFrame parent, Project project, ProjectManager projectManager) {
        super(parent, "Discotek.ca - DeepDive - Configuration Editor", true);
        this.helpBroker = helpBroker;
        this.parent = parent;
        setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
        this.project = project;
        this.projectManager = projectManager;
        
        buildGui();
        
        openProject(project);
    }
    
    public void selectUserDefinedTab() {
        tabber.setSelectedComponent(userDefinedPanel);
    }
    
    void buildGui() {
        helpBroker.enableHelpKey(getRootPane(), "interface-analyzers", helpBroker.getHelpSet());
        setLayout(new BorderLayout());
        
        tabber = new JTabbedPane();
        add(tabber, BorderLayout.CENTER);

        systemPanel = new JPanel();
        systemPanel.setLayout(new BorderLayout());
        tabber.addTab("System", systemPanel);
        userDefinedPanel = new JPanel();
        userDefinedPanel.setLayout(new BorderLayout());
        tabber.addTab("User Defined", userDefinedPanel);
        thirdPartyPanel = new JPanel();
        thirdPartyPanel.setLayout(new BorderLayout());
        tabber.addTab("Third Party", thirdPartyPanel);
        
        categoryButtonPanel = new CategoryButtonPanel();
        categoryButtonPanel.setEnabled(false);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(categoryButtonPanel, BorderLayout.NORTH);
        
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton button = new JButton("Okay");
        buttonPanel.addButton(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConfigurationView.this.setVisible(false);
                ConfigurationView.this.dispose();
                saveProject(project);
            }
        });
        
        button = new JButton("Cancel");
        buttonPanel.addButton(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConfigurationView.this.setVisible(false);
                ConfigurationView.this.dispose();
            }
        });
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(panel, BorderLayout.SOUTH);
        
        projectManager.addPropertyChangeListener(this);
        projectManager.addVetoableChangeListener(this);
        
        dirtyListener = new DirtyListener();
        
        tabber.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCategoryButtons();
            }
        });
        
        dirtyListener = new DirtyListener();
        
        updateCategoryButtons();
        
        buildMenu();
    }
    
    void buildMenu() {
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu menu = new JMenu("File");
        bar.add(menu);
        
        JMenuItem item = new JMenuItem(IMPORT_ACTION);
        menu.add(item);
        item = new JMenuItem(EXPORT_ACTION);
        menu.add(item);
        
        item = new JMenuItem("Close");
        menu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        
        menu = new JMenu("Edit");
        bar.add(menu);
        item = new JMenuItem(QUICK_SELECT_EDITOR_ACTION);
        menu.add(item);
    }
    
    void updateCategoryButtons() {
        categoryButtonPanel.setEnabled(false);
        
        if (tabber.getSelectedIndex() == 1) {
            categoryButtonPanel.setNewButtonEnabled(true);
            int tabCount = userDefinedTabber.getTabCount();
            boolean oneOrMore = tabCount > 0;
            categoryButtonPanel.setEditButtonEnabled(oneOrMore);
            categoryButtonPanel.setDeleteButtonEnabled(oneOrMore);
            
            if (oneOrMore) {
                int index = userDefinedTabber.getSelectedIndex();
                boolean isFarLeft = index == 0;
                categoryButtonPanel.setMoveLeftButtonEnabled(!isFarLeft);
                boolean isFarRight = index == tabCount-1;
                categoryButtonPanel.setMoveRightButtonEnabled(!isFarRight);
            }
        }
    }
    
    class CategoryButtonPanel extends JPanel {
        JButton newButton;
        JButton editButton;
        JButton deleteButton;
        JButton moveLeftButton;
        JButton moveRightButton;
        
        CategoryButtonPanel() {
            buildGui();
        }
        
        void buildGui() {
            setLayout(new GridBagLayout());
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2,2,2,2);
            
            add(new JLabel(), gbc);
            
            gbc.gridx++;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            
            newButton = new JButton("New Category...");
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    CategoryNameEditor editor = new CategoryNameEditor();
                    
                    boolean stillEditing = true;
                    
                    while (stillEditing) {
                        boolean wasCancelled = DialogWrapper.showDialogWrapper(ConfigurationView.this, "Category Name Editor", editor);
                        if (wasCancelled)
                            stillEditing = false;
                        else {
                            String name = editor.getCategoryName();
                            Matcher matcher = ILLEGAL_CATEGORY_NAME_PATTERN.matcher(name);
                            if (matcher.find())
                                showValidCategoryCharsDialog(ConfigurationView.this);
                            else if (name.trim().length() == 0)
                                JOptionPane.showMessageDialog(ConfigurationView.this, "Category name may not contain only white-space characters.", "Category Name Error", JOptionPane.ERROR_MESSAGE);
                            else {
                                int count = userDefinedTabber.getTabCount();
                                boolean foundExistingName = false;
                                for (int i=0; i<count; i++) {
                                    if (name.equals(userDefinedTabber.getTitleAt(i))) {
                                        foundExistingName = true;
                                        break;
                                    }
                                }

                                if (foundExistingName)
                                    JOptionPane.showMessageDialog(ConfigurationView.this, "An User Defined category with name " + name + " already exists.");
                                else {
                                    AnalyzerConfiguration ac = new AnalyzerConfiguration();
                                    userDefinedTabber.addTab(name, new DetailsTable(ac, null, true));
                                    userDefinedTabber.setSelectedIndex(userDefinedTabber.getTabCount()-1);
                                    updateCategoryButtons();
                                    stillEditing = false;
                                    dirtyListener.setDirty(true);
                                }
                            }
                        }
                    }
                }
            });
            add(newButton, gbc);

            gbc.gridx++;
            
            editButton = new JButton("Edit");
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = userDefinedTabber.getSelectedIndex();
                    String oldName = userDefinedTabber.getTitleAt(index);
                    
                    CategoryNameEditor editor = new CategoryNameEditor(oldName);
                    
                    boolean stillEditing = true;
                    while (stillEditing) {
                        boolean wasCancelled = DialogWrapper.showDialogWrapper(ConfigurationView.this, "Category Name Editor", editor);
                        if (wasCancelled)
                            stillEditing = false;
                        else {
                            String name = editor.getCategoryName();
                            
                            Matcher matcher = ILLEGAL_CATEGORY_NAME_PATTERN.matcher(name);
                            if (matcher.find())
                                showValidCategoryCharsDialog(ConfigurationView.this);
                            else if (name.trim().length() == 0)
                                JOptionPane.showMessageDialog(ConfigurationView.this, "Category name may not contain only white-space characters.", "Category Name Error", JOptionPane.ERROR_MESSAGE);
                            else {
                                int count = userDefinedTabber.getTabCount();
                                boolean foundExistingName = false;
                                for (int i=0; i<count; i++) {
                                    if (name.equals(userDefinedTabber.getTitleAt(i))) {
                                        foundExistingName = true;
                                        break;
                                    }
                                }

                                if (foundExistingName)
                                    JOptionPane.showMessageDialog(ConfigurationView.this, "A category with name " + name + " already exists.");
                                else {
                                    File directory = project.getDirectory();
                                    File oldNameFile = new File(directory, oldName + ".xml");
                                    File newNameFile = new File(directory, editor.getCategoryName() + ".xml");
                                    oldNameFile.renameTo(newNameFile);
                                    userDefinedTabber.setTitleAt(index, editor.getCategoryName());
                                    updateCategoryButtons();
                                    stillEditing = false;
                                    dirtyListener.setDirty(true);
                                }
                            }
                        }
                    }
                }
            });
            add(editButton, gbc);
            
            gbc.gridx++;
            
            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String message = "<html>This action cannot be reversed using the <i>Cancel</i> button. Do you really want to delete this category?</html>";
                    int option = 
                        JOptionPane.showConfirmDialog(ConfigurationView.this, message, "Delete Confirmation", JOptionPane.WARNING_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        int index = userDefinedTabber.getSelectedIndex();
                        File directory = project.getDirectory();
                        File file = new File(directory, userDefinedTabber.getTitleAt(index) + ".xml");
                        file.delete();
                        userDefinedTabber.removeTabAt(index);
                        updateCategoryButtons();
                        dirtyListener.setDirty(true);
                    }

                }
            });
            add(deleteButton, gbc);

            gbc.gridx++;
            
            moveLeftButton = new JButton("Move Left");
            moveLeftButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = userDefinedTabber.getSelectedIndex();
                    Component c = userDefinedTabber.getComponentAt(index);
                    String title = userDefinedTabber.getTitleAt(index);
                    userDefinedTabber.removeTabAt(index);
                    userDefinedTabber.insertTab(title, null, c, null, index-1);
                    userDefinedTabber.setSelectedIndex(index-1);
                    updateCategoryButtons();
                    dirtyListener.setDirty(true);
                }
            });
            add(moveLeftButton, gbc);

            gbc.gridx++;
            
            moveRightButton = new JButton("Move Right");
            moveRightButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int index = userDefinedTabber.getSelectedIndex();
                    Component c = userDefinedTabber.getComponentAt(index);
                    String title = userDefinedTabber.getTitleAt(index);
                    userDefinedTabber.removeTabAt(index);
                    userDefinedTabber.insertTab(title, null, c, null, index+1);
                    userDefinedTabber.setSelectedIndex(index+1);
                    updateCategoryButtons();
                    dirtyListener.setDirty(true);
                }
            });
            add(moveRightButton, gbc);
        }
        
        public void setEnabled(boolean enabled) {
            newButton.setEnabled(enabled);
            editButton.setEnabled(enabled);
            deleteButton.setEnabled(enabled);
            moveLeftButton.setEnabled(enabled);
            moveRightButton.setEnabled(enabled);
        }
        
        public void setNewButtonEnabled(boolean enabled) {
            newButton.setEnabled(enabled);
        }
        
        public void setEditButtonEnabled(boolean enabled) {
            editButton.setEnabled(enabled);
        }
        
        public void setDeleteButtonEnabled(boolean enabled) {
            deleteButton.setEnabled(enabled);
        }
        
        public void setMoveLeftButtonEnabled(boolean enabled) {
            moveLeftButton.setEnabled(enabled);
        }
        
        public void setMoveRightButtonEnabled(boolean enabled) {
            moveRightButton.setEnabled(enabled);
        }
    }
    
    public static void showValidCategoryCharsDialog(Component parent) {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("<html>");
        buffer.append("Category names must use only the following valid character sets:");
        buffer.append("<ul>");
        buffer.append("<li>");
        buffer.append("a-z, A-Z");
        buffer.append("</li>");
        buffer.append("<li>");
        buffer.append("0-9");
        buffer.append("</li>");
        buffer.append("<li>");
        buffer.append("'-' (hyphen), '_' (underscore), ' ' (space), and '.' (dot).");            
        buffer.append("</li>");
        buffer.append("</ul>");
        
        buffer.append("</html>");
        
        JOptionPane.showMessageDialog(parent, buffer.toString(), "Category Name Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void setSelectedProject(Project project) {
        openProject(project);
    }
    
    public ProjectManager getProjectManager() {
        return projectManager;
    }
    
    public ProjectConfiguration getProjectConfiguration() {
        ProjectConfiguration configuration = new ProjectConfiguration();
        
        int count = systemTabber.getTabCount();
        String name;
        DetailsTable detailsTable;
        int analyzerCount;
        DeploymentVisitor dv;
        boolean enabled;
        for (int i=0; i<count; i++) {
            name = systemTabber.getTitleAt(i);
            detailsTable = (DetailsTable) systemTabber.getComponentAt(i);
            analyzerCount = detailsTable.model.list.size();
            
            for (int j=0; j<analyzerCount; j++) {
                dv = detailsTable.model.list.get(j);
                enabled = detailsTable.model.enabledList.get(j);
                if (!enabled)
                    configuration.setSystemDeploymentVisitorEnabled(name + "->" + dv.getTitle(), false);
            }
        }
        
        count = userDefinedTabber.getTabCount();
        for (int i=0; i<count; i++) {
            name = userDefinedTabber.getTitleAt(i);
            detailsTable = (DetailsTable) userDefinedTabber.getComponentAt(i);

            AnalyzerConfiguration ac = new AnalyzerConfiguration();
            configuration.addConfiguration(name, ac);
            
            analyzerCount = detailsTable.model.list.size();
            
            for (int j=0; j<analyzerCount; j++) {
                dv = detailsTable.model.list.get(j);
                ac.addDeploymentVisitor(dv);
                enabled = detailsTable.model.enabledList.get(j);
                if (!enabled)
                    configuration.setUserDefinedDeploymentVisitorEnabled(name + "->" + dv.getTitle(), false);
            }
        }
        
        
        count = thirdPartyTabber.getTabCount();
        for (int i=0; i<count; i++) {
            name = thirdPartyTabber.getTitleAt(i);
            detailsTable = (DetailsTable) thirdPartyTabber.getComponentAt(i);

            analyzerCount = detailsTable.model.list.size();

            for (int j=0; j<analyzerCount; j++) {
                dv = detailsTable.model.list.get(j);
                enabled = detailsTable.model.enabledList.get(j);
                if (!enabled)
                    configuration.setThirdPartyDeploymentVisitorEnabled(name + "->" + dv.getTitle(), false);
            }
        }
        
        
        return configuration;
    }
    
    public void setProjectConfiguration(ProjectConfiguration config) {
        String systemConfigurationNames[] = config.getSystemConfigurationNames();
        AnalyzerConfiguration systemConfigurations[] = config.getSystemConfigurations();

        systemTabber = new JTabbedPane();
        DetailsTable detailsView;
        int count;
        boolean enabled[];
        DeploymentVisitor dvs[];
        for (int i=0; i<systemConfigurationNames.length; i++) {
            dvs = systemConfigurations[i].getDeploymentVisitors();
            enabled = new boolean[dvs.length];
            for (int j=0; j<dvs.length; j++)
                enabled[j] = config.getSystemEnabled(dvs[j].getTitle());
            detailsView = new DetailsTable(systemConfigurations[i], enabled, false);
            count = detailsView.model.list.size();
            for (int j=0; j<count; j++)
                detailsView.model.setEnabled(j, config.getSystemEnabled(detailsView.model.list.get(j).getTitle()));
            systemTabber.addTab(systemConfigurationNames[i], detailsView);
        }
        systemTabber.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCategoryButtons();
            }
        });
        
        systemPanel.removeAll();
        systemPanel.add(systemTabber, BorderLayout.CENTER);
        
        String userDefinedConfigurationNames[] = config.getUserDefinedConfigurationNames();
        AnalyzerConfiguration userDefinedConfigurations[] = config.getConfigurations();
        
        final DetailsTable userDefinedTables[] = new DetailsTable[userDefinedConfigurations.length];
        userDefinedTabber = new JTabbedPane();
        for (int i=0; i<userDefinedConfigurationNames.length; i++) {
            dvs = userDefinedConfigurations[i].getDeploymentVisitors();
            enabled = new boolean[dvs.length];
            for (int j=0; j<dvs.length; j++)
                enabled[j] = config.getUserDefinedEnabled(dvs[j].getTitle());
            userDefinedTables[i] = new DetailsTable(userDefinedConfigurations[i], enabled, true);
            count = userDefinedTables[i].model.list.size();
            for (int j=0; j<count; j++)
                userDefinedTables[i].model.setEnabled(j, config.getUserDefinedEnabled(userDefinedTables[i].model.list.get(j).getTitle()));

            userDefinedTabber.addTab(userDefinedConfigurationNames[i], userDefinedTables[i]);
        }
        userDefinedTabber.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCategoryButtons();
                for (int i=0; i<userDefinedTables.length; i++) {
                    userDefinedTables[i].updateButtons();
                }
            }
        });
        
        userDefinedPanel.removeAll();
        userDefinedPanel.add(userDefinedTabber, BorderLayout.CENTER);
        
        

        AnalyzerConfiguration thirdPartyConfigurations[] = ThirdPartyUtil.getThirdPartyFiles();
        String thirdPartyConfigurationNames[] = new String[thirdPartyConfigurations.length];
        for (int i=0; i<thirdPartyConfigurations.length; i++)
            thirdPartyConfigurationNames[i] = thirdPartyConfigurations[i].getCategory();
        
        final DetailsTable thirdPartyTables[] = new DetailsTable[thirdPartyConfigurations.length];
        thirdPartyTabber = new JTabbedPane();
        for (int i=0; i<thirdPartyConfigurationNames.length; i++) {
            dvs = thirdPartyConfigurations[i].getDeploymentVisitors();
            enabled = new boolean[dvs.length];
            for (int j=0; j<dvs.length; j++)
                enabled[j] = config.getThirdPartyEnabled(dvs[j].getTitle());
            thirdPartyTables[i] = new DetailsTable(thirdPartyConfigurations[i], enabled, true);
            count = thirdPartyTables[i].model.list.size();
            for (int j=0; j<count; j++)
                thirdPartyTables[i].model.setEnabled(j, config.getThirdPartyEnabled(thirdPartyTables[i].model.list.get(j).getTitle()));

            thirdPartyTabber.addTab(thirdPartyConfigurationNames[i], thirdPartyTables[i]);
        }
        thirdPartyTabber.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateCategoryButtons();
                for (int i=0; i<thirdPartyTables.length; i++) {
                    thirdPartyTables[i].updateButtons();
                }
            }
        });
        
        thirdPartyPanel.removeAll();
        thirdPartyPanel.add(thirdPartyTabber, BorderLayout.CENTER);
        
        
        systemPanel.invalidate();
        systemPanel.validate();
        systemPanel.repaint();
        
        userDefinedPanel.invalidate();
        userDefinedPanel.validate();
        userDefinedPanel.repaint();
        
        thirdPartyPanel.invalidate();
        thirdPartyPanel.validate();
        thirdPartyPanel.repaint();
        
        updateCategoryButtons();
    }
    
    class DetailsTable extends JPanel {
        AnalyzerConfiguration analyzerConfiguration;
        boolean enabled[];
        final boolean editable;
        
        ConfigurationModel model;
        JTable table;
        
        AnalyzerButtonPanel buttonPanel;

        public DetailsTable(AnalyzerConfiguration analyzerConfiguration, boolean enabled[], boolean editable) {
            this.analyzerConfiguration = analyzerConfiguration;
            this.enabled = enabled;
            this.editable = editable;
            buildGui();
        }
        
        void buildGui() {
            setLayout(new BorderLayout());
            model = new ConfigurationModel(analyzerConfiguration.getDeploymentVisitors(), enabled);

            
            table = new BasicTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            buttonPanel = new AnalyzerButtonPanel(table, model, editable);
            add(buttonPanel, BorderLayout.SOUTH);
            buttonPanel.setEnabled(false);
            buttonPanel.setNewButtonEnabled(true);
            
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        updateButtons();
                    }
                }
            });
        }

        public void updateButtons() {
            buttonPanel.updateButtons();
        }
        
        class AnalyzerButtonPanel extends JPanel {
            JButton newAnalyzerButton;
            JButton editButton;
            JButton deleteButton;
            JButton toggleEnabledButton;
            JButton moveUpButton;
            JButton moveDownButton;
            
            ConfigurationModel model;
            JTable table;
            boolean editable;
            
            AnalyzerButtonPanel(JTable table, ConfigurationModel model, boolean editable) {
                this.table = table;
                this.model = model;
                this.editable = editable;
                buildGui();
            }
            
            void buildGui() {
                setLayout(new GridBagLayout());
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = gbc.gridy = 0;
                gbc.weightx = 0;
                gbc.weighty = 0;
                gbc.insets = new Insets(2,2,2,2);
                gbc.fill = GridBagConstraints.NONE;
                
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                
                add(new JLabel(), gbc);
                
                gbc.gridx++;
                gbc.weightx = 0;
                gbc.fill = GridBagConstraints.NONE;
                
                newAnalyzerButton = new JButton("New Analyzer...");
                newAnalyzerButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        AnalyzerEditor editor = new AnalyzerEditor();
                        editor.loadPreferences();
                        
                        boolean stillEditing = true;
                        
                        while (stillEditing) {
                            boolean wasCancelled = DialogWrapper.showDialogWrapper(ConfigurationView.this, "Analyzer Editor", editor);
                            if (wasCancelled)
                                stillEditing = false;
                            else {
                                try {
                                    DeploymentVisitor dv = null;
                                    if (editor.isCustomAnalyzer()) {
                                        CustomAnalyzerDescriptor descriptor = editor.getCustomAnalyzerDescriptor();
                                        try {
                                            Constructor c = Class.forName(descriptor.className).getConstructor(new Class[]{String.class, String.class, String.class, String[].class});
                                            dv = (DeploymentVisitor) c.newInstance(new Object[]{descriptor.title, descriptor.summary, descriptor.description, descriptor.links});
                                        }
                                        catch (Exception e2) {
                                            e2.printStackTrace();
                                            JOptionPane.showMessageDialog(ConfigurationView.this, "An error occured while instantiating a custom analyzer. See stderr for details.");
                                            continue;
                                        }
                                    }
                                    else
                                        dv = editor.getDeploymentVisitor();

                                    editor.savePreferences();
                                    
                                    if (dv.getTitle().trim().equals("")) {
                                        JOptionPane.showMessageDialog(ConfigurationView.this, "Analyzer title cannot be white space only.");
                                        continue;
                                    }
                                    
                                    int count = model.getRowCount();
                                    boolean foundExistingName = false;
                                    for (int i=0; i<count; i++) {
                                        if (model.list.get(i).getTitle().equals(dv.getTitle())) {
                                            foundExistingName = true;
                                            break;
                                        }
                                    }
                                    
                                    if (foundExistingName)
                                        JOptionPane.showMessageDialog(ConfigurationView.this, "An analyzer with title " + dv.getTitle() + " already exists.");
                                    else {
                                        int index = table.getSelectedRow();
                                        model.addDeploymentVisitor(dv);
                                        table.getSelectionModel().setSelectionInterval(index, index);
                                        updateButtons();
                                        stillEditing = false;
                                        dirtyListener.setDirty(true);
                                    }
                                }
                                catch (Throwable e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    }
                });
                add(newAnalyzerButton, gbc);

                gbc.gridx++;
                
                editButton = new JButton("Edit");
                editButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        DeploymentVisitor dv = model.list.get(table.getSelectedRow());
                        AnalyzerEditor editor = null;

                        if (dv instanceof ClassGrepAnalyzer)
                            editor = new AnalyzerEditor( (ClassGrepAnalyzer) dv);
                        else if (dv instanceof FileFinderAnalyzer)
                            editor = new AnalyzerEditor( (FileFinderAnalyzer) dv);
                        else if (dv instanceof ClassMatcherAnalyzer)
                            editor = new AnalyzerEditor( (ClassMatcherAnalyzer) dv);
                        else 
                            editor = new AnalyzerEditor(dv);

                        editor.loadPreferences();
                        
                        int tabIndex = tabber.getSelectedIndex();
                        
//                        Project project = projectManagerView.getCurrentProject();
                        ProjectConfiguration pc = getProjectConfiguration(); 

                        boolean isEditable = tabIndex == 1;


                        if (isEditable) {
                            boolean stillEditing = true;
                            while (stillEditing) {
                                AnalyzerEditViewCopyDialog dialog = new AnalyzerEditViewCopyDialog(editor, pc, isEditable);
                                dialog.pack();
                                dialog.setLocationRelativeTo(ConfigurationView.this);
                                dialog.setVisible(true);

                                boolean wasCancelled = dialog.wasCanceled;
                                if (wasCancelled)
                                    stillEditing = false;
                                else {
                                    if (editor.isCustomAnalyzer()) {
                                        CustomAnalyzerDescriptor descriptor = editor.getCustomAnalyzerDescriptor();
                                        try {
                                            Constructor c = Class.forName(descriptor.className).getConstructor(new Class[]{String.class, String.class, String.class, String[].class});
                                            dv = (DeploymentVisitor) c.newInstance(new Object[]{descriptor.title, descriptor.summary, descriptor.description, descriptor.links});
                                        }
                                        catch (Exception e2) {
                                            e2.printStackTrace();
                                            JOptionPane.showMessageDialog(ConfigurationView.this, "An error occured while instantiating a custom analyzer. See stderr for details.");
                                            continue;
                                        }
                                    }
                                    else
                                        try {
                                            dv = editor.getDeploymentVisitor();
                                            
                                            editor.savePreferences();
                                            
                                            if (dv.getTitle().trim().equals("")) {
                                                JOptionPane.showMessageDialog(ConfigurationView.this, "Analyzer title cannot white space only.");
                                                continue;
                                            }
                                            
                                            int index = table.getSelectedRow();
                                            model.setDeploymentVisitor(index, dv);
                                            stillEditing = false;
                                            dirtyListener.setDirty(true);
                                            table.getSelectionModel().setSelectionInterval(index, index);
                                        } 
                                        catch (Exception e2) {
                                            stillEditing = true;
                                            System.err.println("DeepDive:: Caught exception (most likely due to configuration errors with regular expressions: " + e2.getMessage());
                                            JOptionPane.showMessageDialog
                                                (ConfigurationView.this, "An error occured while instantiating the analyzer. See stderr for details.", "Analyzer Configuration Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                }
                            }
                        }
                        else {
                            AnalyzerEditViewCopyDialog dialog = new AnalyzerEditViewCopyDialog(editor, pc, isEditable);
                            dialog.pack();
                            dialog.setLocationRelativeTo(ConfigurationView.this);
                            dialog.setVisible(true);
                        }
                    }
                });
                add(editButton, gbc);

                gbc.gridx++;
                
                deleteButton = new JButton("Delete");
                deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int option = 
                            JOptionPane.showConfirmDialog(ConfigurationView.this, "Do you really want to delete this analyzer?", "Delete Confirmation", JOptionPane.WARNING_MESSAGE);
                        if (option == JOptionPane.YES_OPTION)
                            model.removeDeploymentVisitorAt(table.getSelectedRow());
                    }
                });
                add(deleteButton, gbc);
                
                gbc.gridx++;
                
                toggleEnabledButton = new JButton("Toggle Enabled");
                toggleEnabledButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = table.getSelectedRow();
                        model.setEnabled(index, !model.getEnabled(index));
                        table.getSelectionModel().setSelectionInterval(index, index);
                        dirtyListener.setDirty(true);
                    }
                });
                add(toggleEnabledButton, gbc);

                gbc.gridx++;
                
                moveUpButton = new JButton("Move Up");
                moveUpButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = table.getSelectedRow();
                        model.moveUp(index);
                        table.getSelectionModel().setSelectionInterval(index-1, index-1);
                        dirtyListener.setDirty(true);
                    }
                });
                add(moveUpButton, gbc);

                gbc.gridx++;
                
                moveDownButton = new JButton("Move Down");
                moveDownButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int index = table.getSelectedRow();
                        model.moveDown(index);
                        table.getSelectionModel().setSelectionInterval(index+1, index+1);
                        dirtyListener.setDirty(true);
                    }
                });
                add(moveDownButton, gbc);
                
                updateButtons();
            }
            
            void updateButtons() {
                int index = table.getSelectedRow();
                if (index < 0) {
                    setDeleteButtonEnabled(false);
                    setEditButtonEnabled(false);
                    setMoveUpButtonEnabled(false);
                    setMoveDownButtonEnabled(false);
                }
                else {
                    setEditButtonEnabled(true);
                    setDeleteButtonEnabled(true && editable);
                    setToggleEnabledButtonEnabled(true);
                    setMoveUpButtonEnabled(index != 0 && editable);
                    setMoveDownButtonEnabled(index != model.getRowCount() - 1 && editable);
                }
            }
            
            public void setEnabled(boolean enabled) {
                newAnalyzerButton.setEnabled(enabled && editable);
                editButton.setEnabled(enabled && editable);
                deleteButton.setEnabled(enabled && editable);
                toggleEnabledButton.setEnabled(enabled && editable);
                moveUpButton.setEnabled(enabled && editable);
                moveDownButton.setEnabled(enabled && editable);
            }
            
            public void setNewButtonEnabled(boolean enabled) {
                newAnalyzerButton.setEnabled(enabled && editable);
            }
            
            public void setEditButtonEnabled(boolean enabled) {
                editButton.setEnabled(enabled);
            }
            
            public void setDeleteButtonEnabled(boolean enabled) {
                deleteButton.setEnabled(enabled && editable);
            }
            
            public void setToggleEnabledButtonEnabled(boolean enabled) {
                toggleEnabledButton.setEnabled(enabled);
            }
            
            public void setMoveUpButtonEnabled(boolean enabled) {
                moveUpButton.setEnabled(enabled && editable);
            }
            
            public void setMoveDownButtonEnabled(boolean enabled) {
                moveDownButton.setEnabled(enabled && editable);
            }

        }

    }
    
    static final String COLUMN_NAMES[] = {"Title", "Summary", "Enabled"};
    class ConfigurationModel extends DefaultTableModel {
        List<DeploymentVisitor> list = new ArrayList<DeploymentVisitor>();
        List<Boolean> enabledList = new ArrayList<Boolean>();
        
        public ConfigurationModel(DeploymentVisitor dvs[], boolean enabled[]) {
            super(COLUMN_NAMES, 0);
            for (int i=0; i<dvs.length; i++) {
                list.add(dvs[i]);
                enabledList.add(enabled == null ? true : enabled[i]);
            }
        }
        
        public int getRowCount() {
            return list == null ? 0 : list.size();
        }
        
        public Object getValueAt(int row, int column) {
            DeploymentVisitor dv = (DeploymentVisitor) list.get(row);
            
            switch (column) {
                case 0: return dv.getTitle();
                case 1: return dv.getSummary();
                case 2: return enabledList.get(row);
                default:
                    throw new RuntimeException("Bug. Unknown column " + column);
            }
        }
        
        public void addDeploymentVisitor(DeploymentVisitor dv) {
            list.add(dv);
            enabledList.add(true);
            fireTableDataChanged();
        }
        
        public void setDeploymentVisitor(int index, DeploymentVisitor dv) {
            list.set(index, dv);
            fireTableDataChanged();
        }
        
        public void removeDeploymentVisitor(DeploymentVisitor dv) {
            removeDeploymentVisitorAt(list.indexOf(dv));
        }
        
        public void removeDeploymentVisitorAt(int index) {
            list.remove(index);
            enabledList.remove(index);
            fireTableDataChanged();
        }
        
        public void setEnabled(DeploymentVisitor dv, boolean enabled) {
            setEnabled(list.indexOf(dv), enabled);
        }
        
        public void setEnabled(int index, boolean enabled) {
            enabledList.set(index, enabled);
            fireTableDataChanged();
        }
        

        public boolean getEnabled(DeploymentVisitor dv) {
            return getEnabled(list.indexOf(dv));
        }
        
        public boolean getEnabled(int index) {
            return enabledList.get(index);
        }
        
        public void moveUp(int index) {
            DeploymentVisitor dv = list.remove(index);
            list.add(index-1, dv);
            
            Boolean b = enabledList.remove(index);
            enabledList.add(index-1, b);
            fireTableDataChanged();
        }
        
        public void moveDown(int index) {
            DeploymentVisitor dv = list.remove(index);
            list.add(index+1, dv);
            
            Boolean b = enabledList.remove(index);
            enabledList.add(index+1, b);
            fireTableDataChanged();
        }
    }
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (evt.getNewValue() != null && evt.getNewValue().equals(ProjectManager.PROPERTY_OLD_VALUE)) // vetoed - trying to return to previous state
            return;
        String property = evt.getPropertyName();
        if (property.equals(ProjectManager.PROPERTY_SWITCH_NEW_PROJECT)) {
            if (dirtyListener.isDirty()) {
                String message = 
                    "You have unsaved changes. Save before creating new project?";
                int result = 
                    JOptionPane.showConfirmDialog(this, message, "Confirm Save", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    projectManager.saveProject(project);
                    dirtyListener.reset();
                }
                else if (result == JOptionPane.CANCEL_OPTION) 
                    throw new PropertyVetoException("New project vetoed.", evt);
                else { 
                    dirtyListener.reset();
                    /* let er rip */ 
                }
            }
        }
        else if (property.equals(ProjectManager.PROPERTY_SWITCH_OPEN_PROJECT)) {
            if (dirtyListener.isDirty()) {
                String message = 
                    "You have unsaved changes. Save before opening new project?";
                int result = JOptionPane.showConfirmDialog(this, message, "Confirm Save", JOptionPane.YES_NO_CANCEL_OPTION);
                if (result == JOptionPane.YES_OPTION) projectManager.saveProject(project);
                else if (result == JOptionPane.CANCEL_OPTION) throw new PropertyVetoException("Open project vetoed.", evt);
                else {
                    dirtyListener.reset();
                    /* let er rip */ 
                }
            }
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (property.equals(ProjectManager.PROPERTY_NEW_PROJECT)) {
            newProject( (String) evt.getNewValue());
        }
        else if (property.equals(ProjectManager.PROPERTY_OPEN_PROJECT)) {
            
            // we have to invokeLater, otherwise the open event
            // will be fired before the delete event, when an open
            // event occurs as a result of a delete event, which will
            // cause the gui to be disabled
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Project project = (Project) evt.getNewValue();
                    openProject(project);
                }
            });
        }
        else if (property.equals(ProjectManager.PROPERTY_SAVE_PROJECT)) {
            Project project = (Project) evt.getNewValue();
            saveProject(project);
        }
        else if (property.equals(ProjectManager.PROPERTY_DELETE_PROJECT)) {
            setEnabled(false);
        }
    }
    
    public void newProject(String name) {
        Project project = projectManager.getProject(name);
        ProjectConfiguration config = new ProjectConfiguration();
        setProjectConfiguration(config);
        updateCategoryButtons();
    }
    
    public void openProject(Project project) {
        File directory = project.getDirectory();
        try {
            dirtyListener.setIgnoreChangesEnabled(true);
            ProjectConfiguration config = ProjectConfiguration.readConfiguration(directory);
            setProjectConfiguration(config);
            dirtyListener.setIgnoreChangesEnabled(false);

            QUICK_SELECT_EDITOR_ACTION.setEnabled(true);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured reading project configuration for " + project.getName() + ". See stderr for stack trace.");
        }
        updateCategoryButtons();
    }
    
    public void saveProject(Project project) {
        try {
            ProjectConfiguration configuration = getProjectConfiguration();
            ProjectConfiguration.writeConfiguration(configuration, project.getDirectory());
            dirtyListener.reset();
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occured saving this project. See stderr for details.");
        }
    }
    
    public void setCurrentDirectory(File directory) {
        this.currentDirectory = directory;
    }
    
    public File getCurrentDirectory() {
        return currentDirectory;
    }
    
    public Project getSelectedProject() {
        return project;
    }
    
    public Project[] getAllProjects() {
        String names[] = projectManager.getProjectNames();
        Project projects[] = new Project[names.length];
        for (int i=0; i<projects.length; i++)
            projects[i] = projectManager.getProject(names[i]);
        return projects;
    }
    
    public void quickSelectEditor() {
        
        try {
            ProjectConfiguration projectConfiguration = null;
            if (project != null)
                projectConfiguration = ProjectConfiguration.readConfiguration(project.getDirectory());
            AnalyzerConfiguration configs[][] = ConfigurationUtil.getAnalyzerConfigurations(projectConfiguration);
            ProfileSelectionConfiguration psc = getProfileSelectionConfiguration();
            QuickEnableDialog dialog = new QuickEnableDialog(helpBroker, this, configs[0], configs[1], configs[2], psc);

            dialog.pack();
            Dimension size = dialog.getSize();
            
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension d = toolkit.getScreenSize();
            if (size.height > d.height) {
                dialog.setSize(new Dimension(size.width / 4, d.height/3*2));
            }
            
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
            if (!dialog.getWasCanceled()){
                ProfileSelectionConfiguration updatedPsc =  dialog.getProfileSelectionConfiguration();
                updateGui(systemTabber, updatedPsc.getSystemSelectionConfiguration());
                updateGui(userDefinedTabber, updatedPsc.getUserDefinedSelectionConfiguration());
                updateGui(thirdPartyTabber, updatedPsc.getThirdPartySelectionConfiguration());
                
                dirtyListener.setDirty(true);
            }
            dialog.dispose();
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occured. See stderr for details." , "Quick Enable Editor Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    ProfileSelectionConfiguration getProfileSelectionConfiguration() throws SAXException, IOException, ParserConfigurationException {
        if (project == null)
            return null;

        ProfileSelectionConfiguration psc = new ProfileSelectionConfiguration();
        psc.setSystemSelectionConfiguration(getSelectionConfiguration(systemTabber));
        psc.setUserDefinedSelectionConfiguration(getSelectionConfiguration(userDefinedTabber));
        psc.setThirdPartySelectionConfiguration(getSelectionConfiguration(thirdPartyTabber));
        return psc;
    }
    
    SelectionConfiguration getSelectionConfiguration(JTabbedPane tabber) {
        SelectionConfiguration sc = new SelectionConfiguration();

        int count = tabber.getTabCount();
        DetailsTable table;
        DeploymentVisitor dvs[];
        for (int i=0; i<count; i++) {
            table = (DetailsTable) tabber.getComponentAt(i);
            sc.addAnalyzerConfiguration(table.analyzerConfiguration);
            
            dvs = table.model.list.toArray(new DeploymentVisitor[table.model.list.size()]);
            for (int j=0; j<dvs.length; j++) {
                if (table.model.enabledList.get(j))
                    sc.addAnalyzer(dvs[j]);
            }
        }
        
        return sc;
    }
    
    void updateGui(JTabbedPane tabber, SelectionConfiguration config) {
        int count = tabber.getTabCount();
        DetailsTable table;
        DeploymentVisitor dvs[];
        for (int i=0; i<count; i++) {
            table = (DetailsTable) tabber.getComponentAt(i);
            dvs = table.model.list.toArray(new DeploymentVisitor[table.model.list.size()]);
            for (int j=0; j<dvs.length; j++)
                table.model.enabledList.set(j, config.containsAnalyzer(dvs[j].getTitle()));
            table.model.fireTableDataChanged();
        }
    }
    
    public void reloadCurrentProject() {
        if (dirtyListener.isDirty()) {
            String message = 
                "You have unsaved changes. Save before refreshing?";
            int result = 
                JOptionPane.showConfirmDialog(this, message, "Confirm Save", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                projectManager.saveProject(project);
                dirtyListener.reset();

                dirtyListener.setIgnoreChangesEnabled(true);

                try {
                    ProjectConfiguration config = ProjectConfiguration.readConfiguration(project.getDirectory());
                    setProjectConfiguration(config);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "An error occured while reloading project \"" + project.getName() + "\". See stderr for details." , "Reload Error", JOptionPane.ERROR_MESSAGE);
                }
                dirtyListener.setIgnoreChangesEnabled(false);

            }
        }
    }
    
    class AnalyzerEditViewCopyDialog extends JDialog {
        
        ProjectConfiguration projectConfiguration;
        
        boolean wasCanceled = true;
        boolean isEditable;
        AnalyzerEditor analyzerEditor;
        
        public AnalyzerEditViewCopyDialog(AnalyzerEditor editor, ProjectConfiguration projectConfiguration, boolean isEditable) {
            super(ConfigurationView.this, "Analyzer Editor", true);
            this.projectConfiguration = projectConfiguration;
            this.isEditable = isEditable;
            buildGui(editor);
        }
        
        void buildGui(final AnalyzerEditor editor) {
            setLayout(new BorderLayout());
            
            add(editor, BorderLayout.CENTER);
            
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.insets = new Insets(2,2,2,2);
            
            panel.add(new JLabel(), gbc);
            
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;

            
            JButton button = new JButton("Copy to...");
            panel.add(button, gbc);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    
                    if (projectConfiguration == null) {
                        JOptionPane.showMessageDialog(AnalyzerEditViewCopyDialog.this, "Please create/select a project before copying.", "Invalid Project", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    String configNames[] = new String[userDefinedTabber.getTabCount()];
                    for (int i=0; i<configNames.length; i++)
                        configNames[i] = userDefinedTabber.getTitleAt(i);
                    
                    DetailsTable table = null;
                    String message = "Where would you like to copy this analzyer?";
                    String title = "Copy Destination";
                    String options[] = {"A new category", "An existing category"};
                    Object result = JOptionPane.showInputDialog(editor, message, title, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (result == options[0]) {
                        CategoryNameEditor categoryNameEditorditor = new CategoryNameEditor();
                        
                        boolean stillEditing = true;
                        
                        while (stillEditing) {
                            boolean wasCancelled = DialogWrapper.showDialogWrapper(editor, "Category Name Editor", categoryNameEditorditor);
                            if (wasCancelled)
                                stillEditing = false;
                            else {
                                String name = categoryNameEditorditor.getCategoryName();
                                Matcher matcher = ILLEGAL_CATEGORY_NAME_PATTERN.matcher(name);
                                if (matcher.find())
                                    showValidCategoryCharsDialog(ConfigurationView.this);
                                else {
                                    int count = userDefinedTabber.getTabCount();
                                    boolean foundExistingName = false;
                                    for (int i=0; i<count; i++) {
                                        if (name.equals(userDefinedTabber.getTitleAt(i))) {
                                            foundExistingName = true;
                                            break;
                                        }
                                    }

                                    if (foundExistingName)
                                        JOptionPane.showMessageDialog(categoryNameEditorditor, "An User Defined category with name " + name + " already exists.");
                                    else {
                                        AnalyzerConfiguration ac = new AnalyzerConfiguration();
                                        userDefinedTabber.addTab(name, table = new DetailsTable(ac, null, true));
                                        userDefinedTabber.setSelectedIndex(userDefinedTabber.getTabCount()-1);
                                        updateCategoryButtons();
                                        stillEditing = false;
                                        dirtyListener.setDirty(true);
                                    }
                                }
                            }
                        }
                        
                    }
                    else if (result == options[1]) {
                        if (configNames.length > 0) {
                            String names[] = new String[configNames.length];
                            for (int i=0; i<names.length; i++)
                                names[i] = configNames[i];
                            
                            result = JOptionPane.showInputDialog(editor, "Choose an existing category", "Select Category", JOptionPane.INFORMATION_MESSAGE, null, names, names[0]);
                            int index = -1;
                            for (int i=0; i<names.length; i++) {
                                if (result == names[i]) {
                                    index = i;
                                    table = (DetailsTable) userDefinedTabber.getComponentAt(i);
                                    break;
                                }
                            }
                            if (index == -1)
                                throw new Error("Bug. Should be impossible.");
                        }
                        else
                            JOptionPane.showMessageDialog
                                (AnalyzerEditViewCopyDialog.this, 
                                 "No category names exist in the \"User Defined\" tab. Please create one before copying analyzers to it.", 
                                 "No User Defined Categories Exist", 
                                 JOptionPane.ERROR_MESSAGE);
                    }
                    
                    if (table != null) {
                        DeploymentVisitor dv = editor.getDeploymentVisitor();
                        table.model.addDeploymentVisitor(dv);
                    }
                }
            });
            
            if (isEditable) {
                gbc.gridx++;
                button = new JButton("Okay");
                panel.add(button, gbc);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        wasCanceled = false;
                        AnalyzerEditViewCopyDialog.this.setVisible(false);
                        AnalyzerEditViewCopyDialog.this.dispose();
                    }
                });
                
                gbc.gridx++;
                button = new JButton("Cancel");
                panel.add(button, gbc);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        wasCanceled = true;
                        AnalyzerEditViewCopyDialog.this.setVisible(false);
                        AnalyzerEditViewCopyDialog.this.dispose();
                    }
                });
            }
            else {
                gbc.gridx++;
                button = new JButton("Close");
                panel.add(button, gbc);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        AnalyzerEditViewCopyDialog.this.setVisible(false);
                        AnalyzerEditViewCopyDialog.this.dispose();
                    }
                });
            }
            
            add(panel, BorderLayout.SOUTH);
        }
        
        public boolean wasCanceled() {
            return wasCanceled;
        }
    }
    
    public BrowserHelpBroker getHelpBroker() {
        return helpBroker;
    }
}
