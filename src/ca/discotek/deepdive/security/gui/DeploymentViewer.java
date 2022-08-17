package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ca.discotek.deepdive.grep.ResourceUtil;
import ca.discotek.deepdive.grep.gui.GrepGui;
import ca.discotek.deepdive.grep.gui.tree.LocationNode;
import ca.discotek.deepdive.security.TempFileUtil;
import ca.discotek.deepdive.security.dom.Apk;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.misc.DecompilerLauncher;
import ca.discotek.deepdive.security.visitor.ArchiveVisitor;

public class DeploymentViewer extends JPanel {

    final JFrame f;
    final DeepDiveGui gui;
    
    DefaultMutableTreeNode rootNode;
    DefaultTreeModel model;
    JTree tree;
    
    Map<String, MutableTreeNode> rootArchiveNodeMap = new HashMap<String, MutableTreeNode>();
    
    public DeploymentViewer(JFrame f, DeepDiveGui gui) {
        this.f = f;
        this.gui = gui;
        buildGui();
    }
    
    void buildGui() {
        rootNode = new DefaultMutableTreeNode("Root");
        model = new DefaultTreeModel(rootNode);
        tree = new JTree(model) {
            public void paint(Graphics g) {
                super.paint(g);
                
                if (rootNode.getChildCount() == 0) {
                    Dimension d = getSize();
                    
                    Color color = UIManager.getColor("Label.disabledForeground");
                    String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()); 
                    JLabel label = new JLabel("<html><center><font color=" + hex + ">Drag Ear, War, Jar, and APK files here.</font></center></html>");
                    label.setSize(d.width / 2, d.height / 2);

                    BufferedImage image = new BufferedImage(d.width / 2, d.height / 2, BufferedImage.TYPE_INT_RGB);
                    Graphics imageGraphics = image.getGraphics();
                    imageGraphics.setColor(getBackground());
                    imageGraphics.fillRect(0, 0, d.width, d.height);
                    label.paint(imageGraphics);
                    
                    g.drawImage(image, d.width / 4, d.height / 4, this);
                }
            }
        };
        tree.setToolTipText("Drag Ear, War, Jar, and APK files here.");
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        setLayout(new BorderLayout());

        add(new JScrollPane(tree), BorderLayout.CENTER);
        
        tree.setDragEnabled(true);
        tree.setTransferHandler(new TreeTransferHandler());
        
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(evt.getX(),  evt.getY());
                    if (path != null) {
                        Object o = path.getLastPathComponent();
                        if (o != null) {
                            ClassFileNode classNode = null;
                            
                            if (o instanceof ClassFileNode) {
                                classNode = (ClassFileNode) o;
                                try {
                                    String decompilerPath = System.getProperty(GrepGui.DECOMPILER_PATH_SYSTEM_PROPERTY_NAME);
                                    if (decompilerPath == null)
                                        JOptionPane.showMessageDialog
                                            (DeploymentViewer.this, "Decompiler path not set. See Edit->Preferences.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
                                    else {
                                        DecompilerLauncher launcher = new DecompilerLauncher(decompilerPath);
                                        try {
                                            File file = TempFileUtil.createTemporaryFile(classNode.classFile.bytes, ".class");
                                            launcher.launch(file);
                                        } 
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            JOptionPane.showMessageDialog
                                                (DeploymentViewer.this, "Error launching decompiler. See sterr.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } 
                                catch (Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(DeploymentViewer.this, "Error opening resource. See sterr.", "Resource Error", JOptionPane.ERROR_MESSAGE);
                                }

                            }
                        }
                    }
                }
            }
        });
    }
    
    class TreeTransferHandler extends TransferHandler {
        public boolean canImport(TransferHandler.TransferSupport info) {
            return true;
        }
        
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }
            
            if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                JOptionPane.showMessageDialog(tree, "Only files can be drag and dropped here.");
                return false;
            }

            Transferable t = info.getTransferable();
            try {
                List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator<File> it = fileList.listIterator();
                File file;
                while (it.hasNext()) {
                    file = it.next();
                    if (file.isDirectory())
                        gui.openDirectory(file.getAbsolutePath());
                    else if (file.isFile()) {
                        if (file.getName().endsWith(".apk") || file.getName().endsWith(".xapk"))
                            gui.openApk(file.getAbsolutePath());
                        else if (file.getName().endsWith(".ear"))
                            gui.openEar(file.getAbsolutePath());
                        else if (file.getName().endsWith(".war"))
                            gui.openWar(file.getAbsolutePath());
                        else if (file.getName().endsWith(".jar"))
                            gui.openJar(file.getAbsolutePath());
                        else
                            JOptionPane.showMessageDialog(tree, file.getAbsolutePath() + " is not a known deployment unit. Aborting.");
                    }
                    else
                        JOptionPane.showMessageDialog(tree, "Unknown file type for file " + file.getAbsolutePath() + ". Aborting.");

                }
            } 
            catch (Exception e) { return false; }

            
            return true;
        }
    }
    
    public int getDeploymentCount() {
        return rootNode.getChildCount();
    }
    
    public ArchiveVisitor[] getSelectedDeploymentUnits() {
        List<ArchiveVisitor> list = new ArrayList<ArchiveVisitor>();
        TreePath paths[] = tree.getSelectionPaths();
        if (paths != null) {
            Object o;
            for (int i=0; i<paths.length; i++) {
                o = paths[i].getLastPathComponent();
                if (o != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                    if (node.getParent() == rootNode) {
                        if (o instanceof EarNode)
                            list.add( ((EarNode) o).ear );
                        else if (o instanceof WarNode)
                            list.add( ((WarNode) o).war );
                        else if (o instanceof JarNode)
                            list.add( ((JarNode) o).jar );
                        else if (o instanceof ApkNode)
                            list.add( ((ApkNode) o).apk );
                    }
                }
            }
        }
        
        return list.toArray(new ArchiveVisitor[list.size()]);
    }
    
    public ArchiveVisitor[] getDeploymentUnits() {
        List<ArchiveVisitor> list = new ArrayList<ArchiveVisitor>();

        if (rootNode.getChildCount() > 0) {
            AbstractNode child = (AbstractNode) rootNode.getFirstChild();
            do {
                if (child instanceof ApkNode) 
                    list.add( ((ApkNode) child).apk );
                else if (child instanceof EarNode) 
                    list.add( ((EarNode) child).ear );
                else if (child instanceof WarNode) 
                    list.add( ((WarNode) child).war );
                else if (child instanceof JarNode) 
                    list.add( ((JarNode) child).jar );
                else
                    throw new RuntimeException("Unexpected node type: " + child.getClass().getName());
            }
            while ((child = (AbstractNode) child.getNextSibling()) != null);
        }
        
        return list.toArray(new ArchiveVisitor[list.size()]);
    }
    
    public void removeDeployments(ArchiveVisitor avs[]) {
        List<AbstractNode> list = new ArrayList<AbstractNode>();
        
        AbstractNode child = (AbstractNode) rootNode.getFirstChild();
        do {
            Object o;
            if (child instanceof EarNode) {
                o = ((EarNode) child).ear;
            }
            else if (child instanceof WarNode) {
                o = ((WarNode) child).war;
            }
            else if (child instanceof JarNode) {
                o = ((JarNode) child).jar;
            }
            else if (child instanceof ApkNode) {
                o = ((ApkNode) child).apk;
            }
            else
                throw new RuntimeException("Unexpected node type: " + child.getClass().getName());
            
            for (int i=0; i<avs.length; i++) {
                if (avs[i] == o) {
                    list.add(child);
                    break;
                }
            }
       }
        while ((child = (AbstractNode) child.getNextSibling()) != null);
        
        Iterator<AbstractNode> it = list.listIterator();
        AbstractNode node;
        while (it.hasNext()) {
            node = it.next();
            model.removeNodeFromParent(node);
            rootArchiveNodeMap.remove(node.path);
        }

    }
    
    public void addTreeListener(TreeSelectionListener l) {
        tree.addTreeSelectionListener(l);
    }
    
    public void addApk(Apk apk) {
        MutableTreeNode node = rootArchiveNodeMap.get(apk.path);
        if (node != null) {
            int result = 
                JOptionPane.showConfirmDialog(this, "Apk at " + apk.path + " already exists. Replace it?", "Apk Exists", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
            else {
                rootArchiveNodeMap.remove(node);
                model.removeNodeFromParent(node);
            }
        }
        
        ApkNode apkNode = (ApkNode) NodeRegistry.getNodeObject(NodeRegistry.APK_NODE, Apk.class, apk);
        rootArchiveNodeMap.put(apk.path, apkNode);
        model.insertNodeInto(apkNode, rootNode, rootNode.getChildCount());
        
        tree.expandPath(new TreePath(rootNode.getPath()));
    }

    public void addEar(Ear ear) {
        MutableTreeNode node = rootArchiveNodeMap.get(ear.path);
        if (node != null) {
            int result = 
                JOptionPane.showConfirmDialog(this, "Ear at " + ear.path + " already exists. Replace it?", "Ear Exists", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
            else {
                rootArchiveNodeMap.remove(node);
                model.removeNodeFromParent(node);
            }
        }
        
        EarNode earNode = (EarNode) NodeRegistry.getNodeObject(NodeRegistry.EAR_NODE, Ear.class, ear);
        rootArchiveNodeMap.put(ear.path, earNode);
        model.insertNodeInto(earNode, rootNode, rootNode.getChildCount());
        
        tree.expandPath(new TreePath(rootNode.getPath()));
    }
    
    public void addWar(War war) {
        String path = war.getPath().getLastPath();
        MutableTreeNode node = rootArchiveNodeMap.get(path);
        if (node != null) {
            int result = 
                JOptionPane.showConfirmDialog(this, "War at " + path + " already exists. Replace it?", "War Exists", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
            else {
                rootArchiveNodeMap.remove(node);
                model.removeNodeFromParent(node);
            }
        }
        
        WarNode warNode = (WarNode) NodeRegistry.getNodeObject(NodeRegistry.WAR_NODE, War.class, war);
        rootArchiveNodeMap.put(path, warNode);
        model.insertNodeInto(warNode, rootNode, rootNode.getChildCount());
        
        tree.expandPath(new TreePath(rootNode.getPath()));
    }
    
    public synchronized void addJar(Jar jar) {
        String path = jar.getPath().getLastPath();
        MutableTreeNode node = rootArchiveNodeMap.get(path);
        if (node != null) {
            int result = 
                JOptionPane.showConfirmDialog(this, "Jar at " + path + " already exists. Replace it?", "Jar Exists", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
                return;
            else {
                rootArchiveNodeMap.remove(node);
                model.removeNodeFromParent(node);
            }
        }
        
        JarNode jarNode = (JarNode) NodeRegistry.getNodeObject(NodeRegistry.JAR_NODE, Jar.class, jar);
        rootArchiveNodeMap.put(path, jarNode);
        model.insertNodeInto(jarNode, rootNode, rootNode.getChildCount());
        
        tree.expandPath(new TreePath(rootNode.getPath()));
    }
    
    public static String[] getPath(AbstractNode node) {
        DefaultMutableTreeNode cursor = node;

        List<String> pathList = new ArrayList<String>();

        while (cursor.getParent() != null) {
            if (cursor instanceof AbstractNode)
                pathList.add( ((AbstractNode) cursor).path );
            cursor = (DefaultMutableTreeNode) cursor.getParent();
        }

        Collections.reverse(pathList);
        
        return (String[]) pathList.toArray(new String[pathList.size()]);
    }

}
