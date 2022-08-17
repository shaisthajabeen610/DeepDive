package ca.discotek.deepdive.security.gui.path;

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
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import ca.discotek.common.swing.FileChooser;
import ca.discotek.deepdive.security.visitor.assess.custom.ManifestClasspathUtil;
import ca.discotek.deepdive.security.visitor.assess.custom.ManifestClasspathUtil.Node;

public class ExternalDependencyEditor extends JPanel {

    FileChooser jarChooserField;
    DefaultMutableTreeNode rootNode;
    DefaultTreeModel model;
    JTree tree;
    
    JCheckBox includeManifestClassPathEntriesField;
    
    JCheckBox followAbstractMethodsField;
    
    JButton addButton;
    JButton deleteButton;
    
    List<File> fileList = new ArrayList<File>();
    
    JCheckBox enableDiskOutputField;
    FileChooser directoryChooserField;
    
    boolean includeSaveToDisk;
    
    public ExternalDependencyEditor(boolean includeSaveToDisk) {
        this.includeSaveToDisk = includeSaveToDisk;
        rootNode = new DefaultMutableTreeNode("root");
        model = new DefaultTreeModel(rootNode);
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        
        JPanel dependenciesPanel = new JPanel();
        dependenciesPanel.setBorder(BorderFactory.createTitledBorder("External Dependencies"));
        dependenciesPanel.setLayout(new BorderLayout());

        includeManifestClassPathEntriesField = new JCheckBox("Include Manifest Class-Path Entries", true);
        
        dependenciesPanel.add(includeManifestClassPathEntriesField, BorderLayout.NORTH);
        dependenciesPanel.add(buildTreePanel(), BorderLayout.CENTER);
        
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = getSelectedNode();
                deleteButton.setEnabled(rootNode.isNodeChild(node));
            }
        });
        
        includeManifestClassPathEntriesField.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean selected = includeManifestClassPathEntriesField.isSelected();
                int count = rootNode.getChildCount();
                TopLevelFileNode node;
                for (int i=0; i<count; i++) {
                    node = (TopLevelFileNode) rootNode.getChildAt(i);
                    if (selected)
                        node.addKids();
                    else {
                        int childCount = node.getChildCount();
                        for (int j=childCount-1; j>=0; j--)
                            model.removeNodeFromParent( (MutableTreeNode) node.getChildAt(j));
                    }
                }

            }
        });
        
        jarChooserField.load();
        
        add(dependenciesPanel, BorderLayout.CENTER);
        
        JPanel followAbstractMethodsPanel = new JPanel();
        followAbstractMethodsPanel.setBorder(BorderFactory.createTitledBorder("Path Traversal"));
        followAbstractMethodsField = new JCheckBox("Include Abstract and Interface Methods", true);
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");
        buffer.append("This option adds more overhead. Additionally, it includes paths for <i>all</i> potential concrete method matches.<br>");
        buffer.append("For example, if a method contains <i>List list = new ArrayList()</i> and a call to list.add(...),<br>");
        buffer.append("not only will <i>ArrayList.add(...)</i> be examined, but also the <i>add</i> methods for all other <i>List</i> implementations including<br>");
        buffer.append("<i>Stack</i>, <i>Vector</i>, <i>LinkedList</i>, etc.");
        buffer.append("</html>");
        followAbstractMethodsField.setToolTipText(buffer.toString());
        
        followAbstractMethodsPanel.setLayout(new BorderLayout());
        followAbstractMethodsPanel.add(followAbstractMethodsField, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(followAbstractMethodsPanel, BorderLayout.NORTH);
        if (includeSaveToDisk)
            panel.add(buildSaveToDiskPanel(), BorderLayout.SOUTH);
        
        add(panel, BorderLayout.SOUTH);
    }
    
    JPanel buildSaveToDiskPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Save to Disk"));
        optionsPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        gbc.insets = new Insets(2,2,2,2);
        
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
        
        directoryChooserField = directoryChooserField.createJarChooser("path-report-directory", 50, filter);
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
        
        return optionsPanel;
    }
    
    public void saveHistory() {
        jarChooserField.save();
    }
    
    void addFile() {
        String text = jarChooserField.getFile();
        if (text == null || text.trim().length() == 0) {
            Window w = SwingUtilities.windowForComponent(this);
            JOptionPane.showMessageDialog(w, "A file path must be entered into the Jar Chooser field before a dependency can be added.", "File Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            File file = new File(text);
            if (file.isFile()) {
                addFile(file);
                jarChooserField.save();
                jarChooserField.clear();
            }
            else if (fileList.contains(file)) {
                Window w = SwingUtilities.windowForComponent(this);
                JOptionPane.showMessageDialog(w, "File already added.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                Window w = SwingUtilities.windowForComponent(this);
                JOptionPane.showMessageDialog(w, text + " is not a valid file.", "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    void addFile(File file) {
        fileList.add(file);

        TopLevelFileNode node = new TopLevelFileNode(file);
        model.insertNodeInto(node, rootNode, rootNode.getChildCount());
        if (includeManifestClassPathEntriesField.isSelected()) 
            node.addKids();
        tree.expandPath(new TreePath(rootNode.getPath()));
        tree.expandPath(new TreePath(node.getPath()));

    }
    
    class TopLevelFileNode extends DefaultMutableTreeNode {
        
        File file;
        
        TopLevelFileNode(File file) {
            super(file.getAbsolutePath());
            this.file = file;
        }
        
        void addKids() {
            try {
                ManifestClasspathUtil util = new ManifestClasspathUtil(file.getAbsolutePath());
                Node nodes[] = util.getTopLevelClasspathNodes();
                Iterator<Node> it;
                for (int i=0; i<nodes.length; i++) {
                    model.insertNodeInto(new FileNode(nodes[i]), this, getChildCount());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    
    class FileNode extends DefaultMutableTreeNode {
        Node node;
        FileNode(Node node) {
            super(node.file.getAbsolutePath());
            this.node = node;
            
            Iterator<Node> it = node.childList.listIterator();
            while (it.hasNext())
                add(new FileNode(it.next()));
        }
    }
    
    
    DefaultMutableTreeNode getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            Object o = path.getLastPathComponent();
            if (o != null) 
                return (DefaultMutableTreeNode) o;
        }
        
        return null;
    }
    
    JPanel buildTreePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        
        panel.add(buildChooserPanel(), gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        
        tree = new JTree(model);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        
        panel.add(new JScrollPane(tree), gbc);


        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        
        panel.add(buildButtonPanel(), gbc);
        
        return panel;
    }
    
    JPanel buildChooserPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = gbc.weighty = 0;
        
        panel.add(new JLabel("Jar Selector"), gbc);
        
        final javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {
            public String getDescription() {
                return "Jars only";
            }
            
            public boolean accept(File f) {
                return f.isDirectory() || f.isFile() && f.getName().endsWith(".jar");
            }
        };
        jarChooserField = FileChooser.createJarChooser("external-dependencies", 10, filter);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(jarChooserField, gbc);
        
        return panel;
    }
    
    JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.EAST;
        
        panel.add(new JLabel(), gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFile();
            }
        });
        panel.add(addButton, gbc);
        
        gbc.gridx++;
        
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = getSelectedNode();
                model.removeNodeFromParent(node);
                fileList.remove( ((TopLevelFileNode) node).file );
            }
        });
        panel.add(deleteButton, gbc);
        
        return panel;
    }
    
    public boolean isSaveToDiskEnabled() {
        return enableDiskOutputField.isSelected();
    }
    
    public String getReportDirectory() {
        return directoryChooserField.getFile();
    }
    
    public File[] getExternalDependencies() {
        List<File> list = new ArrayList<File>();
        if (rootNode.getChildCount() > 0) {
            TopLevelFileNode node = (TopLevelFileNode) rootNode.getFirstChild();
            FileNode fileNode;
            do {
                list.add(node.file);
                if (node.getChildCount() > 0){
                    fileNode = (FileNode) node.getFirstChild();
                    do {
                        getExternalDependencies(fileNode, list);
                    }
                    while ( (fileNode = (FileNode) fileNode.getNextSibling()) != null);
                }
                
            }
            while ( (node = (TopLevelFileNode) node.getNextSibling()) != null);
        }
        
        return list.toArray(new File[list.size()]);
    }
    
    void getExternalDependencies(FileNode node, List<File> list) {
        list.add(node.node.file);
        if (node.getChildCount() > 0){
            FileNode fileNode = (FileNode) node.getFirstChild();
            do {
                getExternalDependencies(fileNode, list);
            }
            while ( (fileNode = (FileNode) fileNode.getNextSibling()) != null);
        }
    }
    
    public boolean getFollowAbstractMethods() {
        return followAbstractMethodsField.isSelected();
    }
}
