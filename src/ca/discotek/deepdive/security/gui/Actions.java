package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.IconUIResource;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.DeepDive;
import ca.discotek.deepdive.grep.gui.GrepGui;
import ca.discotek.deepdive.security.PreferencesManager;
import ca.discotek.deepdive.security.ProjectConfiguration;
import ca.discotek.deepdive.security.gui.config.ConfigurationView;
import ca.discotek.deepdive.security.gui.path.ExternalDependencyEditor;
import ca.discotek.deepdive.security.gui.path.PathToolView;
import ca.discotek.deepdive.security.misc.ImportExportUtil;
import ca.discotek.deepdive.security.misc.ImportExportUtil.ProjectConfigurationImportDescriptor;
import ca.discotek.common.swing.ButtonPanel;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathTool;
import ca.discotek.projectmanager.InvalidProjectNameException;
import ca.discotek.projectmanager.Project;
import ca.discotek.projectmanager.ProjectManager;

public class Actions {
    
    public static abstract class GuiAction extends AbstractAction {
        
        final DeepDiveGui gui;
        
        public GuiAction(DeepDiveGui gui) {
            this.gui = gui;
        }
    }
    
    public static class NewProjectAction extends GuiAction {
        
        public NewProjectAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "New Project...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.newProject();
        }
    }
    
    public static class DeleteProjectAction extends GuiAction {
        
        public DeleteProjectAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Delete Project...");
            setEnabled(false);
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.deleteProject();
        }
    }
    
    public static class OpenDeploymentAction extends GuiAction {
        
        public OpenDeploymentAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Open Deployment Unit...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.open();
        }
    }
    
    public static class RemoveDeploymentAction extends GuiAction {
        
        public RemoveDeploymentAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Remove Deployment Unit...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.removeDeployments();
        }
    }
    
    public static class EditConfigurationAction extends GuiAction {
        
        public EditConfigurationAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Project...");
            setEnabled(false);
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.editConfiguration();
        }
    }
    
    public static class ReportAction extends GuiAction {
        
        public ReportAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Generate Report...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        gui.generateReports(DeepDive.PROCESS_JEE_ONLY);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(gui, "Couldn't generate report. See stderr for stack trace.");
                    }
                }
                
            };
            

            Thread t = new Thread(r);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
    }
    
    public static class ImportAction extends AbstractAction {
        
        ConfigurationView gui;
        
        public ImportAction(ConfigurationView gui) {
            this.gui = gui;
            putValue(AbstractAction.NAME, "Import...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            File currentDirectory = ImportExportUtil.getCurrentDirectory();
            if (currentDirectory != null)
                chooser.setCurrentDirectory(currentDirectory);
            chooser.setDialogTitle("Import from Jar File");
            chooser.setFileFilter(ExportDialog.IMPORT_EXPORT_FILE_FILTER);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = chooser.showOpenDialog(gui);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    ImportExportUtil.setCurrentDirectory(chooser.getSelectedFile().getParentFile());
                    ProjectConfigurationImportDescriptor pcid = ImportExportUtil.importFromFile(chooser.getSelectedFile());
                    if (pcid.names.length == 0)
                        JOptionPane.showMessageDialog(gui, "There is nothing to import. Aborting.", "Empty Configuration Import Error", JOptionPane.ERROR_MESSAGE);
                    else {
                        ImportDialog dialog = new ImportDialog(gui.getHelpBroker(), gui, pcid);
                        dialog.pack();
                        dialog.setLocationRelativeTo(gui);
                        dialog.setVisible(true);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(gui, "An error occured. See stderr for details.", "Import Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    public static class ExportAction extends AbstractAction {
        
        ConfigurationView gui;
        
        Project selectedProject = null;
        
        public ExportAction(ConfigurationView gui) {
            this.gui = gui;
            putValue(AbstractAction.NAME, "Export...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                ProjectManager manager = ProjectManager.getProjectManager(DeepDiveGui.PROJECT_MANAGER_NAME);
                String names[] = manager.getProjectNames();
                Project projects[] = new Project[names.length];
                ProjectConfiguration configs[] = new ProjectConfiguration[projects.length];
                for (int i=0; i<names.length; i++) {
                    projects[i] = manager.getProject(names[i]);
                    configs[i] = ProjectConfiguration.readConfiguration(projects[i].getDirectory());
                }
                
                ExportDialog d = new ExportDialog(gui, gui.getSelectedProject(), projects, configs);
                d.pack();
                d.setLocationRelativeTo(gui);
                d.setVisible(true);
            }
            catch (InvalidProjectNameException e) {
                e.printStackTrace();
                throw new Error("Bug.", e);
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(gui, "Unable to load configuration. See stderr for details.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }
    
    public static class RefreshConfigurationAction extends AbstractAction {
        
        ConfigurationView gui;
        
        public RefreshConfigurationAction(ConfigurationView gui) {
            this.gui = gui;
            putValue(AbstractAction.NAME, "Refresh");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.reloadCurrentProject();
        }
    }
    
    public static class QuickSelectEditorAction extends AbstractAction {
        
        ConfigurationView gui;
        
        public QuickSelectEditorAction(ConfigurationView gui) {
            this.gui = gui;
            putValue(AbstractAction.NAME, "Analyzer Quick Pick...");
            setEnabled(false);
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.quickSelectEditor();
        }
    }

    
    public static class ExitAction extends GuiAction {
        
        public ExitAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Exit");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.exit();
        }
    }
    
    public static class PreferencesAction extends GuiAction {
        
        public PreferencesAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Preferences...");
        }
        
        public void actionPerformed(ActionEvent evt) {
            final NetworkImportPreferencesView networkImportView = new NetworkImportPreferencesView();
            final DecompilePreferencesView decompileView = new DecompilePreferencesView(DeepDive.getDecompile());
            JTabbedPane tabber = new JTabbedPane();
            tabber.add("Network Import", networkImportView);
            tabber.add("Decompile", decompileView);
            final JDialog d = new JDialog(gui.getFrame(), "Preferences", true);
            d.setLayout(new BorderLayout());
            d.add(tabber, BorderLayout.CENTER);
            
            ButtonPanel panel = new ButtonPanel();
            JButton button = new JButton("Okay");
            panel.addButton(button);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PreferencesManager manager = PreferencesManager.getManager("default");
                    manager.saveDecompilerPreferences(decompileView.getDecompileEnabled());
                    
                    manager.saveImportServerPreferences(networkImportView.getEnabled(), networkImportView.isAddressSpecified(), networkImportView.getAddress().toString(), networkImportView.getPort());
                    
                    
                    DeepDive.setDecompile(decompileView.getDecompileEnabled());
                    InetAddress address = networkImportView.isAddressSpecified() ? networkImportView.getAddress() : null;
                    String portText = networkImportView.getPort();
                    
                    int port;
                    try {
                        port = Integer.parseInt(portText);
                        if (port < 0)
                            port = -1;
                    }
                    catch (Exception e2) {
                        port = -1;
                    }

                    if (port < 0)
                        JOptionPane.showMessageDialog(gui,  "Invalid Port.", "Port Error", JOptionPane.ERROR_MESSAGE);
                    else {

                        try {
                            gui.stopImportServer();
                            gui.setImportServerAddress(address);
                            gui.setImportServerPort(port);
                            if (networkImportView.getEnabled())
                                gui.startImportServer();
                        }
                        catch (Exception e2) {
                            e2.printStackTrace();
                            JOptionPane.showMessageDialog(gui, "An error occured starting Network Import Server. See stderr for stack trace.", "Network Import Server Error", JOptionPane.ERROR_MESSAGE);
                        }
                        
                        d.setVisible(false);
                    }
                }
            });
            button = new JButton("Cancel");
            panel.addButton(button);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    d.setVisible(false);
                }
            });
            
            d.add(panel, BorderLayout.SOUTH);
            
            d.pack();
            d.setLocationRelativeTo(gui);
            d.setVisible(true);
        }
    }
    
    public static class BytecodeGrepperAction extends GuiAction {
        
        public BytecodeGrepperAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Application Grepper");
        }
        
        public void actionPerformed(ActionEvent evt) {
            GrepGui grepGui = new GrepGui(false);
            grepGui.setSize(800, 600);
            grepGui.setLocationRelativeTo(gui);
            grepGui.setVisible(true);
        }
    }
    
    public static class PathAnalyzerToolAction extends GuiAction {
        
        ProgressDialog progressDialog;
        
        public PathAnalyzerToolAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Path Analyzer");
        }
        
        public void actionPerformed(ActionEvent evt) {
            if (gui.getDeployments().length == 0)
                JOptionPane.showMessageDialog(gui, "First add one or more deployments to Deep Dive.", "No Deployment Error", JOptionPane.ERROR_MESSAGE);
            else {
                progressDialog = new ProgressDialog(gui.getFrame(), "Processing...");
                
                final ExternalDependencyEditor editor = new ExternalDependencyEditor(true);
                boolean wasCanceled = DialogWrapper.showDialogWrapper(gui, "Options", editor);
                if (!wasCanceled) {
                    
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            progressDialog.start();

                            File files[] = editor.getExternalDependencies();
                            
                            try {
                                final JFrame f = new JFrame("Deep Dive " + DeepDiveGui.productInfo.getVersion() + " - Path Analyzer");
                                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                PathTool tool = new PathTool(gui.getDeployments(), files, editor.getFollowAbstractMethods(), editor.isSaveToDiskEnabled() ? editor.getReportDirectory() : null);
                                PathToolView view = new PathToolView(f, tool);
                                buildPathReportFrame(f, view);
                                f.setLocationRelativeTo(gui);
                                f.setVisible(true);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                JOptionPane.showMessageDialog(gui, "Unable to process paths. See stderr for details.", "Path Analyzer Error", JOptionPane.ERROR_MESSAGE);
                            }
                            finally {
                                progressDialog.stop();
                            }
                        }
                    });
                    thread.start();
                }
            }
        }
    }
    
    public static void buildPathReportFrame(final JFrame frame, PathToolView tool) {
        frame.setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
        frame.setLayout(new BorderLayout());
        frame.add(tool, BorderLayout.CENTER);
        
        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
        ButtonPanel panel = new ButtonPanel();
        panel.add(button);
        
        frame.add(panel, BorderLayout.SOUTH);
        frame.setSize(800, 600);
    }
    
    public static class PerformanceAction extends GuiAction {
        
        public PerformanceAction(DeepDiveGui gui) {
            super(gui);
            putValue(AbstractAction.NAME, "Performance Data");
        }
        
        public void actionPerformed(ActionEvent evt) {
            gui.showPerformanceData();
        }
    }
}
