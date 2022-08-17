package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ca.discotek.common.io.ProductInfo;
import ca.discotek.common.logo.sphere.AboutDialog;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.common.swing.SwingUtilitiesX;
import ca.discotek.common.swing.splash.DiscotekSplashScreen;
import ca.discotek.deepdive.ExpiryTracker;
import ca.discotek.deepdive.grep.Location;
import ca.discotek.deepdive.grep.gui.GrepGui;
import ca.discotek.deepdive.security.ConfigurationUtil;
import ca.discotek.deepdive.security.DeepDive;
import ca.discotek.deepdive.security.EventManager;
import ca.discotek.deepdive.security.IOUtil;
import ca.discotek.deepdive.security.ProfileSelectionConfiguration;
import ca.discotek.deepdive.security.ProjectConfiguration;
import ca.discotek.deepdive.security.dom.Apk;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.gui.config.ConfigurationView;
import ca.discotek.deepdive.security.gui.path.PathToolView;
import ca.discotek.deepdive.security.misc.FilterUtil;
import ca.discotek.deepdive.security.report.ReportGenerator;
import ca.discotek.deepdive.security.report.ZipUtil;
import ca.discotek.deepdive.security.visitor.ApkReader;
import ca.discotek.deepdive.security.visitor.ArchiveReader;
import ca.discotek.deepdive.security.visitor.ArchiveVisitor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.EarProcessor;
import ca.discotek.deepdive.security.visitor.EarReader;
import ca.discotek.deepdive.security.visitor.JarProcessor;
import ca.discotek.deepdive.security.visitor.WarProcessor;
import ca.discotek.deepdive.security.visitor.WarReader;
import ca.discotek.deepdive.security.visitor.assess.AnalyzeExceptionTracker;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerException;
import ca.discotek.deepdive.security.visitor.assess.custom.path.PathTool;
import ca.discotek.projectmanager.Project;
import ca.discotek.projectmanager.ProjectManager;
import ca.discotek.projectmanager.gui.ProjectManagerView;

import javax.help.HelpSet;
import javax.help.HelpSetException;

import ca.discotek.common.swing.BrowserHelpBroker;

public class DeepDiveGui extends JPanel {

    public static final String HELPSET_NAME = "help.hs";
    
    public static final String APP_NAME = "DeepDive";
    
    public static final String PROJECT_MANAGER_NAME = "deepdive";
    
    JFrame f;
    DeploymentViewer tree;
    TextViewer distilledTextViewer;
    TextViewer rawTextViewer;
    JPanel renderedViewer;
    
    JTabbedPane tabber;
    
    ProjectManager projectManager = null;
    ProjectManagerView projectManagerView;
    
    public final Actions.NewProjectAction NEW_PROJECT_ACTION = new Actions.NewProjectAction(this);
    public final Actions.DeleteProjectAction DELETE_PROJECT_ACTION = new Actions.DeleteProjectAction(this);
    public final Actions.OpenDeploymentAction OPEN_DEPLOYMENT_ACTION = new Actions.OpenDeploymentAction(this);
    public final Actions.RemoveDeploymentAction REMOVE_DEPLOYMENT_ACTION = new Actions.RemoveDeploymentAction(this);
    public final Actions.ReportAction REPORT_ACTION = new Actions.ReportAction(this);
    public final Actions.ExitAction EXIT_ACTION = new Actions.ExitAction(this);
    public final Actions.PreferencesAction PREFERENCES_ACTION = new Actions.PreferencesAction(this);
    public final Actions.PathAnalyzerToolAction PATH_ANALYZER_TOOL_ACTION = new Actions.PathAnalyzerToolAction(this);
    public final Actions.BytecodeGrepperAction BYTECODE_GREPPER_ACTION = new Actions.BytecodeGrepperAction(this);
    public final Actions.PerformanceAction PERFORMANCE_ACTION = new Actions.PerformanceAction(this);
    
    public final Actions.EditConfigurationAction EDIT_CONFIGURATION_ACTION = new Actions.EditConfigurationAction(this);
    public static ProductInfo productInfo = null;
    
    DeepDiveStatusBar statusBar;
    
    JFrame performanceViewFrame = null;
    
    ProgressDialog progressDialog;
    
    JButton editButton;
    
    ImportServer server = null;
    BrowserHelpBroker helpBroker;
    
    public DeepDiveGui(final JFrame f) {
        f.setTitle(productInfo.getVendorName() + " - " + productInfo.getProductName() + " " + productInfo.getVersion());
        this.f = f;
        
        progressDialog = new ProgressDialog(f, "Processing...");
        
        f.setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
        
        try {
            projectManager = ProjectManager.getProjectManager(PROJECT_MANAGER_NAME);
        }
        catch (Exception e) {
            throw new Error("Bug. Should never happen!");
        }
        
        buildGui();
        
        UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(DeepDiveGui.this, "An unexpected error occured. See stderr for the stack trace.", "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(handler);
        
        try {
            startImportServer();
        }
        catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(f, "Failed to start import server on port " + ImportServer.DEFAULT_PORT + ". See stderr.", "Import Server Failure", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
    
    public JFrame getFrame() {
        return f;
    }
    
    InetAddress importServerAddress = null;
    int importServerPort = ImportServer.DEFAULT_PORT;
    
    public void startImportServer() throws IOException {
        if (importServerAddress == null)
            System.out.println("Starting import server on port " + importServerPort);
        else
            System.out.println("Starting import server on " + importServerAddress + ":" + importServerPort);

        server = importServerAddress == null ? new ImportServer(importServerPort, this) : new ImportServer(importServerAddress, importServerPort, this);
        // do not change this line or remove. Attack Surface depends on it.
        System.out.println("Import Server started.");
    }
    
    public void stopImportServer() throws IOException {
        if (server != null) {
            System.out.println("Stopping import server.");
            server.stop();
        }
    }
    
    public void setImportServerAddress(InetAddress address) {
        this.importServerAddress = address;
    }
    
    public void setImportServerPort(int port) {
        this.importServerPort = port;
    }
    
    public InetAddress getImportServerAddress() {
        return importServerAddress;
    }
    
    public int getImportServerPort() {
        return importServerPort;
    }
    
    void buildGui() {
        try { buildHelp(); }
        catch (HelpSetException e3) {
            e3.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occured while building help. See stderr.", "Help Error", JOptionPane.ERROR_MESSAGE);
        }
        setLayout(new BorderLayout());
        
        editButton = new JButton("Edit...");
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editConfiguration();
            }
        });
        editButton.setEnabled(false);
        projectManagerView = new ProjectManagerView(projectManager, new JButton[]{editButton});
        add(projectManagerView, BorderLayout.NORTH);
        
        tree = new DeploymentViewer(f, this);
        tree.setBorder(BorderFactory.createTitledBorder("Deployments"));

        rawTextViewer = new TextViewer();
        distilledTextViewer = new TextViewer();
        renderedViewer = new JPanel();
        renderedViewer.setLayout(new BorderLayout());
        tabber = new JTabbedPane();

        tabber.addTab("Raw", rawTextViewer);
        tabber.addTab("Distilled", distilledTextViewer);
        tabber.addTab("Rendered", renderedViewer);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, tabber);
        splitter.setResizeWeight(0.2);
        
        add(splitter, BorderLayout.CENTER);
        
        statusBar = new DeepDiveStatusBar();
        add(statusBar, BorderLayout.SOUTH);
        
        buildMenuBar();
        
        projectManager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals(ProjectManager.PROPERTY_OPEN_PROJECT) ||
                    name.equals(ProjectManager.PROPERTY_NEW_PROJECT) ||
                    name.equals(ProjectManager.PROPERTY_SWITCH_NEW_PROJECT) || 
                    name.equals(ProjectManager.PROPERTY_SWITCH_NEW_PROJECT) ||
                    name.equals(ProjectManager.PROPERTY_DELETE_PROJECT)) {
                    Project p = projectManagerView.getSelectedProject();
                    editButton.setEnabled(p != null);
                    DELETE_PROJECT_ACTION.setEnabled(p != null);
                    EDIT_CONFIGURATION_ACTION.setEnabled(p != null);
                }
            }
        });
        
        tree.addTreeListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                TreePath path = evt.getPath();
                if (path == null)
                    clearTextViewers();
                else {
                    Object o = path.getLastPathComponent();
                    if (o != null) {
                        int index = 0;
                        if (o instanceof Viewable) {
                            try {
                                String raw = ((Viewable) o).getRaw();
                                rawTextViewer.setText(raw == null ? "" : raw.replace("\t", "    "), true);
                            } 
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                            try {
                                String text = ((Viewable) o).getText();
                                distilledTextViewer.setText( text , false);
                                if (text != null) index = 1;
                            } 
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            
                            try {
                                JComponent comp = ((Viewable) o).getRendered();
                                renderedViewer.removeAll();
                                if (comp != null) {
                                    renderedViewer.add(comp, BorderLayout.CENTER);
                                    index = 2;
                                }
                                renderedViewer.invalidate();
                                renderedViewer.validate();
                                renderedViewer.repaint();
                            } 
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        
                        tabber.setSelectedIndex(index);
                    }
                    else 
                        clearTextViewers();
                }
            }
        });
        
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                exit();
            }
        });
        
        load();
    }
    
    void buildHelp() throws HelpSetException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = HelpSet.findHelpSet(loader, HELPSET_NAME);
        HelpSet helpSet = new HelpSet(loader, url);
        helpBroker = new BrowserHelpBroker(helpSet);
        helpBroker.enableHelpKey(f.getRootPane(), "interface", helpSet);
    }
    
    void clearTextViewers() {
        rawTextViewer.clear();
        distilledTextViewer.clear();
    }
    
    void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        f.setJMenuBar(menuBar);
        
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        
        JMenuItem item = new JMenuItem(NEW_PROJECT_ACTION);
        menu.add(item);
        
        item = new JMenuItem(DELETE_PROJECT_ACTION);
        menu.add(item);
        
        menu.add(new JSeparator());
        
        item = new JMenuItem(OPEN_DEPLOYMENT_ACTION);
        menu.add(item);
        
        item = new JMenuItem(REMOVE_DEPLOYMENT_ACTION);
        menu.add(item);
        
        menu.add(new JSeparator());
        
        item = new JMenuItem(REPORT_ACTION);
        menu.add(item);
        
        menu.add(new JSeparator());
        
        item = new JMenuItem(EXIT_ACTION);
        menu.add(item);
        
        menu = new JMenu("Edit");
        menuBar.add(menu);
        
        item = new JMenuItem(PREFERENCES_ACTION);
        menu.add(item);

        item = new JMenuItem(EDIT_CONFIGURATION_ACTION);
        menu.add(item);  
        
        menu = new JMenu("Tools");
        menuBar.add(menu);
        
        item = new JMenuItem(PATH_ANALYZER_TOOL_ACTION);
        menu.add(item);
        
        item = new JMenuItem(BYTECODE_GREPPER_ACTION);
        menu.add(item);
        
        menu = new JMenu("Help");
        menuBar.add(menu);
        
        item = new JMenuItem("Contents");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpBroker.setCurrentID("table-of-contents");
                helpBroker.setDisplayed(true);
            }
        });
        menu.add(item);
        
        item = new JMenuItem(PERFORMANCE_ACTION);
        menu.add(item);
        
        item = new JMenuItem("Contact Vendor");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discotek.ca/contact.xhtml"));
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(DeepDiveGui.this, "Could not launch browser. See stderr for details.", "Contact Vendor Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(item);
        
        item = new JMenuItem("About");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = 
                        new AboutDialog(
                            DeepDiveGui.this.f, 
                            productInfo.getVendorName(), 
                            productInfo.getProductName(), 
                            productInfo.getVersion());
                    dialog.setVisible(true);
            }
        });
        menu.add(item);
    }
    
    void load() {
        String decompilerPath = GrepGui.rootPreferences.get(GrepGui.DECOMPILER_PATH_PREF_KEY, null);
        if (decompilerPath != null) GrepGui.setDecompilerPath(decompilerPath);
    }
    
    public void exit() {
        int result = 
            JOptionPane.showConfirmDialog(this, "Do you really want to exit?", "Exit Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (result == JOptionPane.OK_OPTION)
            System.exit(0);
    }
    
    public ArchiveVisitor[] getDeployments() {
        return tree.getDeploymentUnits();
    }
    
    public void removeDeployments() {
        ArchiveVisitor avs[] = tree.getSelectedDeploymentUnits();
        if (avs.length == 0)
            JOptionPane.showMessageDialog(this, "No deployments selected.", "Deployment Selection Error", JOptionPane.ERROR_MESSAGE);
        else{
            int result = 
                    JOptionPane.showConfirmDialog(this, "Do you really want to remove the selected deployments?", "Remove Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                tree.removeDeployments(avs);
            }
        }
    }
    
    public boolean newProject() {
        return projectManagerView.addNewProject();
    }
    
    public boolean newProject(String name) {
        return projectManagerView.addNewProject(name);
    }
    
    public void deleteProject() {
        Project project = projectManagerView.getSelectedProject();
        try {
            projectManager.deleteProject(project, false);
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An I/O error occured. See stderr for details.", "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (PropertyVetoException e) {}
    }
    
    public void open() {
        JFileChooser chooser = new JFileChooser();
        
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file.isDirectory())
                openDirectory(file.getAbsolutePath());
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".ear"))
                openEar(file.getAbsolutePath());
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".war"))
                openWar(file.getAbsolutePath());
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".jar"))
                openJar(file.getAbsolutePath());
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".apk"))
                openApk(file.getAbsolutePath());
            else
                JOptionPane.showMessageDialog(this, "Unsupported file type: " + file.getAbsolutePath());
        }
    }
    
    public void editConfiguration() {
        editConfiguration(false);
    }
    
    ConfigurationView editingConfigurationView = null;
    
    public void editConfiguration(boolean selectUserDefinedTab) {
        editingConfigurationView = new ConfigurationView(helpBroker, f, projectManagerView.getSelectedProject(), projectManager);
        editingConfigurationView.setSize(800, 600);
        Point p = SwingUtilitiesX.getCenterPlacement(editingConfigurationView);
        editingConfigurationView.setLocation(p);
        if (selectUserDefinedTab) 
            editingConfigurationView.selectUserDefinedTab();
        editingConfigurationView.setVisible(true);
        editingConfigurationView = null;
    }
    
    public void openApk(final String path) {
        progressDialog.start();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    EventManager.INSTANCE.publish("Opening APK " + path);
                    Apk apk = new Apk(path);
                    
                    ApkReader apkReader = new ApkReader(path, ApkReader.getZipBytes(new File(path)));
                    apkReader.accept(apk);
                    
                    tree.addApk(apk);
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DeepDiveGui.this, "Error occured for apk " + path + ". See stderr.");
                }
                finally {
                    progressDialog.stop();
                }
            }
        });
        thread.start();
    }
    
    public void openEar(final String path) {
        progressDialog.start();
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    EventManager.INSTANCE.publish("Opening ear " + path);
                    Ear ear = new Ear(path);
                    
                    EarReader er = new EarReader(new File(path));
                    er.accept(ear);
                    
                    tree.addEar(ear);
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DeepDiveGui.this, "Error occured for ear " + path + ". See stderr.");
                }
                finally {
                    progressDialog.stop();
                }
            }
        });
        thread.start();
    }
    
    public void openWar(final String path) {
        progressDialog.start();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    EventManager.INSTANCE.publish("Opening war " + path);
                    War war = new War(path);
                    
                    WarReader wr = new WarReader(new File(path));
                    wr.accept(war);
                    
                    tree.addWar(war);
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DeepDiveGui.this, "Error occured for war " + path + ". See stderr.");
                }
                finally {
                    progressDialog.stop();
                }
            }
        });
        thread.start();
    }
    
    public void openJar(final String path) {
        progressDialog.start();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    EventManager.INSTANCE.publish("Opening jar " + path);
                    Jar jar = new Jar(path);
                    
                    ArchiveReader jarReader = new ArchiveReader(new File(path));
                    jarReader.accept(jar);
                    
                    tree.addJar(jar);
                } 
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DeepDiveGui.this, "Error occured for jar " + path + ". See stderr.");
                }
                finally {
                    progressDialog.stop();
                }
            }
        });
        thread.start();
    }
    
    public void openDirectory(String path) {
        openDirectory(path, false, false);
    }
    
    public void openDirectory(String path, boolean showSelectionDialog, boolean recurse) {
        File directory = new File(path);
        File files[] = recurse ? IOUtil.listFilesRecursively(path, false) : directory.listFiles();
        
        List<File> list = new ArrayList<File>();

        if (showSelectionDialog) {
            String name;
            for (int i=0; i<files.length; i++) {
                name = files[i].getName().toLowerCase();
                if (name.endsWith(".ear") || name.endsWith(".jar") || name.endsWith(".war")) 
                    list.add(files[i]);
            }
            
            if (list.size() > 0) {
                DeploymentSelector editor = new DeploymentSelector(list.toArray(new File[list.size()]));
                boolean wasCanceled = DialogWrapper.showDialogWrapper(this, "Deployment Selector", new JScrollPane(editor));
                if (!wasCanceled) {
                    File selectedFiles[] = editor.getSelectedFiles();
                    for (int i=0; i<selectedFiles.length; i++) {
                        name = selectedFiles[i].getName().toLowerCase();
                        if (selectedFiles[i].isFile()) {
                            if (name.endsWith(".ear"))
                                openEar(selectedFiles[i].getAbsolutePath());
                            else if (name.endsWith(".war"))
                                openWar(selectedFiles[i].getAbsolutePath());
                            else if (name.endsWith(".jar"))
                                openJar(selectedFiles[i].getAbsolutePath());
                        }
                    }       
                }
            }
                
            
        }
        else {
            String name;
            for (int i=0; i<files.length; i++) {
                name = files[i].getName().toLowerCase();
                if (files[i].isFile()) {
                    if (name.endsWith(".ear"))
                        openEar(files[i].getAbsolutePath());
                    else if (name.endsWith(".war"))
                        openWar(files[i].getAbsolutePath());
                    else if (name.endsWith(".jar"))
                        openJar(files[i].getAbsolutePath());
                }
                else if (files[i].isDirectory()) 
                    openDirectory(files[i].getAbsolutePath());
            }
        }
        
    }
    
    public void generateReports(boolean processJeeOnly) throws IOException, SAXException, ParserConfigurationException {

        if (tree.getDeploymentCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add one or more deployments before generating a report.", "Report Generation Error", JOptionPane.ERROR_MESSAGE);
            return;            
        }

        Project project = projectManagerView.getSelectedProject(); 
        
        if (project == null) {
            String message = "No project selected. No user-defined analyzers will be applied. Continue?";
            int result = 
                JOptionPane.showConfirmDialog(this, message, "Confirm No Selected Project", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
        }
        
        ProjectConfiguration projectConfiguration = null;
        if (project != null) {
            projectConfiguration = ProjectConfiguration.readConfiguration(project.getDirectory());
        }
        
        AnalyzerConfiguration configs[][] = ConfigurationUtil.getAnalyzerConfigurations(projectConfiguration);
        
        QuickEnableDialog dialog = 
            new QuickEnableDialog(
                    helpBroker, 
                    f, 
                    configs[0], 
                    configs[1], 
                    configs[2], 
                    projectConfiguration == null ? null : projectConfiguration.getProfileSelectionConfiguration(),
                    tree.getDeploymentUnits());
        dialog.pack();
        Dimension size = dialog.getSize();
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension d = toolkit.getScreenSize();
        if (size.height > d.height) {
            dialog.setSize(new Dimension(size.width / 4, d.height/3*2));
        }
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        dialog.dispose();
        
        if (!dialog.getWasCanceled()) {
            statusBar.setGreen(true);
            progressDialog.start();

            try {
                ProfileSelectionConfiguration psc = dialog.getProfileSelectionConfiguration();
                AnalyzeExceptionTracker tracker = new AnalyzeExceptionTracker();
                
                DeploymentVisitor deploymentVisitors[] = psc.getDeploymentVisitors();
                ArchiveVisitor archives[] = tree.getDeploymentUnits();

                visitStart(deploymentVisitors);

                Set<String> filterPathSet = null;
                Boolean isIncludeOnly;
                if (dialog.isFilterEnabled()) {
                    isIncludeOnly = dialog.isInclusionFilter();
                    filterPathSet = new HashSet<String>();
                    filterPathSet.addAll(Arrays.asList(dialog.getPaths()));
                }
                else
                    isIncludeOnly = null;
                
                File file;
                boolean filtered;
                for (int i=0; i<archives.length; i++) {
                    if (archives[i] instanceof Ear) {
                        Ear ear = (Ear) archives[i];
                        
                        filtered = FilterUtil.isFiltered(isIncludeOnly, filterPathSet, ear.path);
                        
                        EventManager.INSTANCE.publish("Processing " + ear.path);
                        EventManager.INSTANCE.startTimer("Processing " + ear.path);

                        try {
                            if (!filtered)
                                visitDeployment(new File(ear.path), deploymentVisitors);
                            EarProcessor earProcessor = new EarProcessor(processJeeOnly, isIncludeOnly, filterPathSet);
                            file = new File(ear.path);
                            earProcessor.process(new Location.DirectoryLocation(file.getParentFile().getAbsolutePath()), ear, deploymentVisitors, tracker);
                        }
                        finally {
                            EventManager.INSTANCE.endTimer("Processing " + ear.path);
                        }
                        
                    }
                    else if (archives[i] instanceof War) {
                        War war = (War) archives[i];
                        
                        filtered = FilterUtil.isFiltered(isIncludeOnly, filterPathSet, war.getPath().toString());
                        
                        EventManager.INSTANCE.publish("Processing " + war.getPath());
                        EventManager.INSTANCE.startTimer("Processing " + war.getPath());
                        try {
                            if (!filtered)
                                visitDeployment(new File(war.getPath().toString()), deploymentVisitors);
                            
                            WarProcessor warProcessor = new WarProcessor(processJeeOnly, isIncludeOnly, filterPathSet);
                            file = new File(war.getPath().toString());
                            warProcessor.process(new Location.DirectoryLocation(file.getParentFile().getAbsolutePath()), war, deploymentVisitors, tracker);
                        }
                        finally {
                            EventManager.INSTANCE.endTimer("Processing " + war.getPath());
                        }
                    }
                    else if (archives[i] instanceof Jar) {
                        Jar jar = (Jar) archives[i];
                        
                        if (FilterUtil.isFiltered(isIncludeOnly, filterPathSet, jar.getPath().toString()))
                            continue;
                        
                        EventManager.INSTANCE.publish("Processing " + jar.getPath());
                        EventManager.INSTANCE.startTimer("Processing " + jar.getPath());
                        try {
                            visitDeployment(new File(jar.getPath().toString()), deploymentVisitors);
                            
                            JarProcessor jarProcessor = new JarProcessor(processJeeOnly);
                            file = new File(jar.getPath().toString());
                            for (int j=0; j<deploymentVisitors.length; j++)
                                jarProcessor.process(new Location.DirectoryLocation(file.getParentFile().getAbsolutePath()), jar, deploymentVisitors[j], tracker);
                        }
                        finally {
                            EventManager.INSTANCE.endTimer("Processing " + jar.getPath());
                        }
                    }
                    else if (archives[i] instanceof Apk) {
                        Apk apk = (Apk) archives[i];
                        
                        if (FilterUtil.isFiltered(isIncludeOnly, filterPathSet, apk.path))
                            continue;
                        
                        EventManager.INSTANCE.publish("Processing " + apk.path);
                        EventManager.INSTANCE.startTimer("Processing " + apk.path);
                        try {
                            visitDeployment(new File(apk.path), deploymentVisitors);
                            
                            JarProcessor jarProcessor = new JarProcessor(processJeeOnly);
                            file = new File(apk.path);
                            jarProcessor.process(new Location.DirectoryLocation(file.getParentFile().getAbsolutePath()), apk, deploymentVisitors, tracker);
                        }
                        finally {
                            EventManager.INSTANCE.endTimer("Processing " + apk.path);
                        }
                    }
                    
                }
                
                visitEnd(deploymentVisitors);
                
                if (dialog.isSaveToDiskEnabled()) {
                    String reportDirectory = dialog.getReportDirectory();
                    EventManager.INSTANCE.publish("Saving report to at " + reportDirectory);
                    ReportGenerator reportGenerator = new ReportGenerator(reportDirectory);
                    try {
                        EventManager.INSTANCE.publish("Exploding deployment(s).");
                        EventManager.INSTANCE.startTimer("Exploding Deployments");
                        File unzipFolder = new File(dialog.getReportDirectory(), "archives");
                        ReportGenerator.FileTreeCallback callback = new ReportGenerator.FileTreeCallback(unzipFolder);
                        ZipUtil.unzip(archives, unzipFolder, callback);
                        File allLinksFile = callback.generate();
                        EventManager.INSTANCE.endTimer("Exploding Deployments");

                        EventManager.INSTANCE.publish("Starting HTML report output.");
                        EventManager.INSTANCE.startTimer("Generating HTML Report");
                        reportGenerator.generate(archives, psc, dialog.isPathReportIncluded());
                        EventManager.INSTANCE.startTimer("Generating HTML Report");
                        
                        EventManager.INSTANCE.publish("Completed HTML report output.");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                final JFrame f = new JFrame("Deep Dive " + DeepDiveGui.productInfo.getVersion() + " - Path Analyzer");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                PathTool tool = null;
                PathToolView view = null;
                if (dialog.isPathReportIncluded()) {
                    tool = new PathTool(getDeployments(), dialog.getExternalDependencies(), dialog.getFollowAbstractMethods(), dialog.isSaveToDiskEnabled() ? dialog.getReportDirectory() : null);
                    view = new PathToolView(f, tool);
                }
                
                ReportViewer viewer = new ReportViewer(psc);
                
                viewer.setSize(800, 600);
                viewer.setExtendedState( f.getExtendedState()|JFrame.MAXIMIZED_BOTH );
                viewer.setLocationRelativeTo(DeepDiveGui.this);
                viewer.setVisible(true);
                 
                if (dialog.isPathReportIncluded()) {
                    Actions.buildPathReportFrame(f, view);
                    f.setLocationRelativeTo(this);
                    f.setVisible(true);
                }

            }
            finally {
                statusBar.setGreen(false);
                progressDialog.stop();
            }
            

        }
    }
    
    void visitStart(DeploymentVisitor visitors[]) {
        for (int i=0; i<visitors.length; i++) {
            try { visitors[i].visitStart(); }
            catch (AnalyzerException e) {
                // todo: output to error log (to display)
                e.printStackTrace();
            }
        }
    }
    
    void visitDeployment(File file, DeploymentVisitor visitors[]) {
        for (int i=0; i<visitors.length; i++) {
            try { visitors[i].visitDeployment(file); }
            catch (AnalyzerException e) {
                // todo: output to error log (to display)
                e.printStackTrace();
            }
        }
    }
    
    void visitEnd(DeploymentVisitor visitors[]) {
        for (int i=0; i<visitors.length; i++) {
            try { visitors[i].visitEnd(); }
            catch (AnalyzerException e) {
                // todo: output to error log (to display)
                e.printStackTrace();
            }
        }
    }
    

    public void showPerformanceData() {
        
        if (performanceViewFrame == null) {
            performanceViewFrame = new JFrame("DeepDive Performance");
            performanceViewFrame.setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
            performanceViewFrame.setLayout(new BorderLayout());
            final PerformanceView performanceView;
            performanceViewFrame.add(performanceView = new PerformanceView(helpBroker), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(2,2,2,2);
            gbc.weightx = 1;
            gbc.weighty = 0;
            
            buttonPanel.add(new JLabel(), gbc);
            
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.gridx++;
            
            JButton button = new JButton("Refresh");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performanceView.refresh();
                }
            });
            buttonPanel.add(button, gbc);
            

            button = new JButton("Reset");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performanceView.reset();
                }
            });
            gbc.gridx++;
            buttonPanel.add(button, gbc);
            
            button = new JButton("Close");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    performanceViewFrame.setVisible(false);
                }
            });
            gbc.gridx++;
            buttonPanel.add(button, gbc);
            
            performanceViewFrame.add(buttonPanel, BorderLayout.SOUTH);
            
            performanceViewFrame.pack();
            performanceViewFrame.setLocationRelativeTo(f);
        }

        performanceViewFrame.setVisible(true);
    }
    
    public void importNetworkProject(String name, String xml[]) throws IOException {
        
        if (editingConfigurationView != null) {
            JOptionPane.showMessageDialog
                (this, "Import server received project, but cannot create it while another project is being edited. Close the current configuration editor first.", "Import Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean created = newProject(name);

        if (created) {
//            System.out.println(projectManagerView.getSelectedProject().getName());
            Project project = projectManagerView.getSelectedProject();
            
            AnalyzerConfiguration ac;
            File file;
            FileOutputStream fos;
            
            try {
                for (int i=0; i<xml.length; i++) {
                    ac = new AnalyzerConfiguration();
                    ac.parse(new ByteArrayInputStream(xml[i].getBytes()));
                    file = new File(project.getDirectory(), ac.getCategory() + ".xml");
                    fos = new FileOutputStream(file);
                    fos.write(xml[i].getBytes());
                    fos.close();
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editConfiguration(true);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occured importing project over network.", "Network Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public BrowserHelpBroker getHelpBroker() {
        return helpBroker;
    }
    
    public static void main(final String[] args) {
        try {
            productInfo = ProductInfo.getProductInfo("discotek-deepdive.properties");                
        }
        catch (Exception e) {
            System.out.println("ProductInfo couldn't find discotek-deepdive.properties. Using dummy values.");
            productInfo = new ProductInfo("Discotek.ca", "DeepDive", "1.x");
        }
        
        
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
                    catch (Exception e) {}
                    
                    JFrame f = new JFrame();
                    DeepDiveGui gui = new DeepDiveGui(f);
                    f.add(gui);
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    Dimension d = toolkit.getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                    f.setExtendedState( f.getExtendedState()|JFrame.MAXIMIZED_BOTH );
                    Point p = SwingUtilitiesX.getCenterPlacement(f);
                    f.setLocation(p);
                    f.setVisible(true);
                    
                    if (args != null) {
                        File file;
                        for (int i=0; i<args.length; i++) {
                            file = new File(args[i]);
                            if (file.isDirectory())
                                gui.openDirectory(args[i], true, Boolean.getBoolean("-Drecurse-dirs"));
                            else if (args[i].endsWith(".ear"))
                                gui.openEar(args[i]);
                            else if (args[i].endsWith(".war"))
                                gui.openWar(args[i]);
                            else if (args[i].endsWith(".jar"))
                                gui.openJar(args[i]);
                            else if (args[i].endsWith(".apk"))
                                gui.openApk(args[i]);
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Unable to create project. See error below. Exiting");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };
        
        DiscotekSplashScreen splashScreen = new DiscotekSplashScreen
                (productInfo.getProductName(), "Version " + productInfo.getVersion(), r, 75);
       splashScreen.pack();
       splashScreen.setLocation(SwingUtilitiesX.getCenterPlacement(splashScreen));
       splashScreen.setVisible(true);
       splashScreen.start();
    }
}
