package ca.discotek.deepdive.grep.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ca.discotek.deepdive.grep.FileNotifier;
import ca.discotek.deepdive.grep.Grepper;
import ca.discotek.deepdive.grep.MatchExpression;
import ca.discotek.deepdive.grep.ResourceUtil;
import ca.discotek.deepdive.grep.gui.tree.AbstractNode;
import ca.discotek.deepdive.grep.gui.tree.ClassNode;
import ca.discotek.deepdive.grep.gui.tree.DescriptionNode;
import ca.discotek.deepdive.grep.gui.tree.LocationNode;
import ca.discotek.deepdive.grep.gui.tree.RootNode;
import ca.discotek.deepdive.grep.gui.tree.TreeModelEventHandler;
import ca.discotek.common.swing.CloseableTab;
import ca.discotek.common.swing.HistoryComboBox;
import ca.discotek.deepdive.security.TempFileUtil;
import ca.discotek.deepdive.security.gui.DeepDiveIconUtil;
import ca.discotek.deepdive.security.misc.DecompilerLauncher;

public class GrepGui extends JFrame {
	
	static final String HISTORY_SIZE_PREF_KEY = "history-size";
    public static final String DECOMPILER_PATH_PREF_KEY = "decompiler-path";
    
    public static final String DECOMPILER_PATH_SYSTEM_PROPERTY_NAME = "decompiler-path";
	
	int historySize = HistoryComboBox.DEFAULT_HISTORY_SIZE;

	public static final Preferences rootPreferences = Preferences.userNodeForPackage(GrepGui.class);
	public static final Preferences locationPreferences = rootPreferences.node("location");
	public static final Preferences expressionPreferences = rootPreferences.node("expression");
	public static final Preferences containsPreferences = rootPreferences.node("contains");
	public static final Preferences caseSensitivePreferences = rootPreferences.node("case-sensitive");
	
	Map<String, Boolean> expressionContainsMap = new HashMap<String, Boolean>();
	Map<String, Boolean> expressionCaseSensitiveMap = new HashMap<String, Boolean>();
	
	File currentFile = null;
	
	HistoryComboBox locationField;
	HistoryComboBox expressionField;
    JRadioButton containsButton;
    JRadioButton regularExpressionButton;
    JCheckBox caseSensitiveButton;
	
	JTabbedPane tabber;
	
	ItemListener expressionFieldItemListener;
	
	final boolean isStandalone;
	
    public GrepGui() {
        this(true);
    }
	
	public GrepGui(boolean isStandalone) {
	    super(isStandalone ? "Application Grepper" : "Deep Dive - Application Grepper");
	    this.isStandalone = isStandalone;
		buildGui();
	}
	
	void buildGui() {
	    setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
		buildMenuBar();
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.gridx = labelGbc.gridy = 0;
		labelGbc.fill = GridBagConstraints.NONE;
		labelGbc.anchor = GridBagConstraints.EAST;
		labelGbc.insets = new Insets(2,2,2,2);
		labelGbc.weightx = labelGbc.weighty = 0;
		
		GridBagConstraints fieldGbc = new GridBagConstraints();
		fieldGbc.gridx = 1; 
		fieldGbc.gridy = 0;
		fieldGbc.fill = GridBagConstraints.HORIZONTAL;
		fieldGbc.anchor = GridBagConstraints.WEST;
		fieldGbc.insets = new Insets(2,2,2,2);
		fieldGbc.weightx = 1;
		fieldGbc.weighty = 0;
		
		GridBagConstraints buttonGbc = new GridBagConstraints();
		buttonGbc.gridx = 2;
		buttonGbc.gridy = 0;
		buttonGbc.fill = GridBagConstraints.NONE;
		buttonGbc.anchor = GridBagConstraints.WEST;
		buttonGbc.insets = new Insets(2,2,2,2);
		buttonGbc.weightx = buttonGbc.weighty = 0;
		
		add(new JLabel("Scan Location"), labelGbc);
		locationField = new HistoryComboBox(50);
		add(locationField, fieldGbc);
		JButton button = new JButton("Browse...");
		add(button, buttonGbc);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = selectFile();
				if (file != null) locationField.setSelectedItem(file.getAbsolutePath());
			}
		});
		
		labelGbc.gridy++;
		fieldGbc.gridy++;
		buttonGbc.gridy++;
		
		add(new JLabel("Pattern"), labelGbc);
		expressionField = new HistoryComboBox(50);
		expressionFieldItemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    updateContainsAndCaseSensitiveButtons();
            }
        };
		expressionField.addItemListener(expressionFieldItemListener);
		add(expressionField, fieldGbc);
		
        labelGbc.gridy++;
        fieldGbc.gridy++;
        buttonGbc.gridy++;
		
		add(new JLabel("Type"), labelGbc);
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new GridBagLayout());
		GridBagConstraints typeGbc = new GridBagConstraints();
		typeGbc.gridx = typeGbc.gridy = 0;
		typeGbc.fill = GridBagConstraints.NONE;
		typeGbc.anchor = GridBagConstraints.WEST;
		typeGbc.weightx = typeGbc.weighty = 0;
		ButtonGroup group = new ButtonGroup();
        containsButton = new JRadioButton("String.contains(...)", false);
        group.add(containsButton);
        typePanel.add(containsButton, typeGbc);

        typeGbc.gridx++;
        
        regularExpressionButton = new JRadioButton("Java Regular Expression", true);
        group.add(regularExpressionButton);
        typePanel.add(regularExpressionButton, typeGbc);
        
        typeGbc.gridx++;
        typeGbc.fill = GridBagConstraints.HORIZONTAL;
        typeGbc.weightx = 1;
        
        typePanel.add(new JLabel(), typeGbc);
        
        add(typePanel, fieldGbc);
        
        labelGbc.gridy++;
        fieldGbc.gridy++;
        buttonGbc.gridy++;
        
        add(new JLabel("Case Sensitive"), labelGbc);
        caseSensitiveButton = new JCheckBox();
        caseSensitiveButton.setSelected(false);
        add(caseSensitiveButton, fieldGbc);
        
        containsButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                caseSensitiveButton.setEnabled(containsButton.isSelected());
            }
        });
        
		labelGbc.gridy++;
		fieldGbc.gridy++;
		buttonGbc.gridy++;
		
		buttonGbc.gridx = 0;
		buttonGbc.gridwidth = 2;
		buttonGbc.anchor = GridBagConstraints.EAST;
		
		button = new JButton("Grep");
		add(button, buttonGbc);
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				grep();
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = labelGbc.gridy + 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(2,2,2,2);
		
		tabber = new JTabbedPane();
		tabber.setBorder(BorderFactory.createTitledBorder("Results"));
		tabber.setPreferredSize(new Dimension(300, 300));
		add(tabber, gbc);

		locationField.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				currentFile = new File((String) e.getItem());
			}
		});
		
		load();
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		updateContainsAndCaseSensitiveButtons();
	}
	
	void updateContainsAndCaseSensitiveButtons() {
        String expression = expressionField.getText();
        Boolean value = expressionContainsMap.get(expression);
        if (value != null)
            containsButton.setSelected(value);
        value = expressionCaseSensitiveMap.get(expression);
        if (value != null)
            caseSensitiveButton.setSelected(value);
	}
	
	void buildMenuBar() {
		JMenuBar bar = new JMenuBar();
		
		JMenu menu = new JMenu("File");
		bar.add(menu);
		
		JMenuItem item = new JMenuItem("Exit");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Clear History");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearHistory();
			}
		});
		menu.add(item);
		
		menu = new JMenu("Edit");
		bar.add(menu);
		
		item = new JMenuItem("Preferences...");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PreferencesDialog d = new PreferencesDialog(GrepGui.this);
				d.setLocationRelativeTo(GrepGui.this);
				d.pack();
				d.setSize(400, d.getHeight());
				d.setVisible(true);
				
				if (!d.getWasCanceled()) {
					setHistorySize(d.getHistorySize());
					setDecompilerPath(d.getDecompilerPath());
				}
				
			}
		});
		menu.add(item);
		
		setJMenuBar(bar);
	}
	
	void grep() {
		String locationText = locationField.getText();
		File file = new File(locationText);
		if (!file.exists()) 
			JOptionPane.showMessageDialog(this, "File or directory does not exist.");
		else {
			String patternText = expressionField.getText();
			if (patternText.equals(""))
				JOptionPane.showMessageDialog(this, "No regular expression specified.");
			else {
				try {
				    MatchExpression matchExpression = containsButton.isSelected() ?
				        new MatchExpression(expressionField.getText(), caseSensitiveButton.isSelected()) :
				        new MatchExpression(expressionField.getText());
					
					locationField.itemChosen(locationText);
					expressionField.removeItemListener(expressionFieldItemListener);
					expressionField.itemChosen(patternText);
					expressionField.addItemListener(expressionFieldItemListener);
					
                    expressionContainsMap.put(expressionField.getText(), containsButton.isSelected());
                    expressionCaseSensitiveMap.put(expressionField.getText(), caseSensitiveButton.isSelected());
					
					save();
					
					grep(file, matchExpression);
				}
				catch (PatternSyntaxException e) {
					JOptionPane.showMessageDialog(this, "Invalid pattern: " + e.getMessage());
				}
			}
		}
	}
	
	private void grep(final File file, MatchExpression matchExpression) {
	    expressionContainsMap.put(matchExpression.expression, matchExpression.pattern == null);
	    expressionCaseSensitiveMap.put(matchExpression.expression, matchExpression.isCaseSensitive);
	    
        final TreeModelEventHandler handler = new TreeModelEventHandler(matchExpression);
        final DefaultTreeModel model = handler.getModel();
        final RootNode rootNode = (RootNode) model.getRoot();
        final JTree tree = new JTree(model);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.getModel().addTreeModelListener(new TreeModelListener() {
            public void treeNodesInserted(TreeModelEvent e) {

                Object children[] = e.getChildren();
                AbstractNode childNode;
                for (int i=0; i<children.length; i++) {
                    childNode = (AbstractNode) children[i];
                    if (childNode.getParent() == rootNode) {
                        tree.expandPath(new TreePath(rootNode.getPath()));
                    }
                }
            }

            public void treeStructureChanged(TreeModelEvent e) {}
            public void treeNodesRemoved(TreeModelEvent e) {}
            public void treeNodesChanged(TreeModelEvent e) {}
        });
        
        tree.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent evt) {
                String tooltip = null;
                TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
                if (path != null) {
                    Object o = path.getLastPathComponent();
                    if (o != null) {
                        if (o instanceof AbstractNode) {
                            tooltip = ( (AbstractNode) o ).getUserObject().toString();
                        }
                    }
                }
                tree.setToolTipText(tooltip);
            }
        });
        
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(evt.getX(),  evt.getY());
                    if (path != null) {
                        Object o = path.getLastPathComponent();
                        if (o != null) {
                            ClassNode classNode = null;
                            if (o instanceof ClassNode) {
                                classNode = (ClassNode) o;
                            }
                            else if (o instanceof DescriptionNode) {
                                DescriptionNode descriptionNode = (DescriptionNode) o;
                                TreeNode node = descriptionNode.getParent();
                                if (node instanceof ClassNode)
                                    classNode = (ClassNode) node;
                            }
                            
                            if (classNode != null) {
                                LocationNode locationNode = (LocationNode) classNode.getParent();
                                try {
                                    InputStream is = ResourceUtil.getResource(locationNode.location, classNode.getName());
                                    if (is == null) {
                                        String message = 
                                            "Bug in analyzer. Couldn't find class resource in location " + 
                                            locationNode.location + " and class name " + classNode.getName();
                                        JOptionPane.showMessageDialog(GrepGui.this, message, "Resource Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    
                                    String decompilerPath = System.getProperty(GrepGui.DECOMPILER_PATH_SYSTEM_PROPERTY_NAME);
                                    if (decompilerPath == null)
                                        JOptionPane.showMessageDialog
                                            (GrepGui.this, "Decompiler path not set. See Edit->Preferences.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
                                    else {
                                        DecompilerLauncher launcher = new DecompilerLauncher(decompilerPath);
                                        try {
                                            File file = TempFileUtil.createTemporaryFile(is, ".class");
                                            launcher.launch(file);
                                        } 
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            JOptionPane.showMessageDialog
                                                (GrepGui.this, "Error launching decompiler. See sterr.", "Decompiler Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } 
                                catch (Exception e) {
                                    e.printStackTrace();
                                    JOptionPane.showMessageDialog(GrepGui.this, "Error opening resource. See sterr.", "Resource Error", JOptionPane.ERROR_MESSAGE);
                                }

                            }
                        }
                    }
                }
            }
        });

        final ResultsPanel resultsPanel = new ResultsPanel(tree);
        
        String tabText = file.getName() + ": " + matchExpression.toString();
        tabber.addTab(tabText, resultsPanel);
        int index = tabber.getTabCount()-1;
        tabber.setTabComponentAt(index, new CloseableTab(tabText, tabber, resultsPanel));
        tabber.setSelectedIndex(index);
        
        Thread thread = new Thread() {
            public void run() {
                try {
                    FileNotifier notifier = new FileNotifier() {
                        public void notify(String file) {
                            resultsPanel.statusBar.setDescription(file);
                        }
                    };
                    Grepper grepper = new Grepper();
                    grepper.grep(file.getAbsolutePath(), handler, notifier);
                    resultsPanel.setStatusComplete();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GrepGui.this, "An error occured during the grep. See stderr for details.");
                    resultsPanel.setError();
                }
            }
        };
        thread.start();

		
	}
	
	
	File selectFile() {
		JFileChooser chooser = currentFile == null ? new JFileChooser() : new JFileChooser(currentFile);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			currentFile = chooser.getSelectedFile();
			return currentFile;
		}
		else return null;
	}
	
	
	void exit() {
	    String message = isStandalone ?
            "Do you really want to exit?" :
            "Do you really want to close this window?";
	    
        String title = isStandalone ?
            "Confirm Exit" :
            "Confirm Close";
	    
		int result = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
		    if (isStandalone)
			    System.exit(0);
		    else {
		        setVisible(false);
		        dispose();
		    }
		}
	}
	
	void load() {
	    if (locationPreferences == null || expressionPreferences == null || containsPreferences == null || caseSensitivePreferences == null || rootPreferences == null)
	        return;
		try {
			List<Integer> list = new ArrayList<Integer>();
			String keys[] = locationPreferences.keys();
			for (int i=0; i<keys.length; i++) {
				try { list.add(Integer.parseInt(keys[i])); }
				catch (Exception e) { /* skip */ }
			}
			
			Collections.sort(list);
			
			String value;
			DefaultComboBoxModel model = locationField.getDefaultModel();
			for (Integer i : list) {
				value = locationPreferences.get(Integer.toString(i), null);
				if (value != null) model.addElement(value);
			}
		} 
		catch (Exception e) {}
		
		try {
			List<Integer> list = new ArrayList<Integer>();
			String keys[] = expressionPreferences.keys();
			for (int i=0; i<keys.length; i++) {
				try { list.add(Integer.parseInt(keys[i])); }
				catch (Exception e) { /* skip */ }
			}
			
			Collections.sort(list);
			
			String value;
			DefaultComboBoxModel model = expressionField.getDefaultModel();
			for (Integer i : list) {
				value = expressionPreferences.get(Integer.toString(i), null);
				if (value != null) {
				    model.addElement(value);
				    expressionContainsMap.put(value, containsPreferences.getBoolean(Integer.toString(i), false));
                    expressionCaseSensitiveMap.put(value, caseSensitivePreferences.getBoolean(Integer.toString(i), false));
				}
			}
		} 
		catch (Exception e) {}
		
		historySize = rootPreferences.getInt(HISTORY_SIZE_PREF_KEY, HistoryComboBox.DEFAULT_HISTORY_SIZE);
		setHistorySize(historySize);
	
        String decompilerPath = rootPreferences.get(DECOMPILER_PATH_PREF_KEY, null);
        if (decompilerPath != null) setDecompilerPath(decompilerPath);
	}
	
	void save() {
        if (locationPreferences == null || expressionPreferences == null || containsPreferences == null || caseSensitivePreferences == null)
            return;
		int size = locationField.getModel().getSize();
		for (int i=0; i<size; i++) 
			locationPreferences.put(Integer.toString(i), (String) locationField.getModel().getElementAt(i));
		
		size = expressionField.getModel().getSize();
		String expression;
		for (int i=0; i<size; i++) {
		    expression = (String) expressionField.getModel().getElementAt(i);
			expressionPreferences.put(Integer.toString(i), expression);
			containsPreferences.putBoolean(Integer.toString(i), expressionContainsMap.get(expression));
			caseSensitivePreferences.putBoolean(Integer.toString(i), expressionCaseSensitiveMap.get(expression));
		}
	}
	
	void clearHistory() {
		locationField.getDefaultModel().removeAllElements();
		expressionField.getDefaultModel().removeAllElements();

        if (locationPreferences == null || expressionPreferences == null || containsPreferences == null || caseSensitivePreferences == null)
            return;
		try {
			locationPreferences.clear();
			expressionPreferences.clear();
			containsPreferences.clear();
			caseSensitivePreferences.clear();
		} 
		catch (BackingStoreException e) {}
	}
	
	public int getHistorySize() {
		return historySize;
	}
	
	public void setHistorySize(int historySize) {
		this.historySize = historySize;
		locationField.setHistorySize(historySize);
		expressionField.setHistorySize(historySize);
        if (rootPreferences == null)
            return;
		rootPreferences.putInt(HISTORY_SIZE_PREF_KEY, historySize);
	}
	
	public static void setDecompilerPath(String decompilerPath) {
	    System.setProperty(DECOMPILER_PATH_SYSTEM_PROPERTY_NAME, decompilerPath);
	    
        if (rootPreferences == null)
            return;
	    rootPreferences.put(DECOMPILER_PATH_PREF_KEY, decompilerPath);
	}

	public static void main(String[] args) {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
		catch (Exception e) {}

		
		GrepGui gui = new GrepGui();
		gui.pack();
		gui.setVisible(true);
	}
}
