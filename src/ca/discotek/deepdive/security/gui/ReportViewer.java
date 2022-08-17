package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import ca.discotek.common.swing.BasicTable;
import ca.discotek.deepdive.grep.classmatcher.iterator.OutputIterator;
import ca.discotek.deepdive.security.gui.LinkLauncher;
import ca.discotek.deepdive.security.ProfileSelectionConfiguration;
import ca.discotek.deepdive.security.dom.Ear;
import ca.discotek.deepdive.security.dom.Jar;
import ca.discotek.deepdive.security.dom.War;
import ca.discotek.deepdive.security.report.ReportGenerator;
import ca.discotek.deepdive.security.visitor.ArchiveVisitor;
import ca.discotek.deepdive.security.visitor.DeploymentVisitor;
import ca.discotek.deepdive.security.visitor.assess.AnalyzerConfiguration;
import ca.discotek.deepdive.security.visitor.assess.OutputException;
import ca.discotek.deepdive.security.visitor.assess.custom.MultiAnalyzer;

public class ReportViewer extends JFrame {
    
    JTabbedPane tabber;
    
    ProfileSelectionConfiguration psc;
    
    public ReportViewer(ProfileSelectionConfiguration psc) {
        super("Discotek.ca - DeepDive - ReportViewer");
        this.psc = psc;
        buildGui();
    }
    
    void buildGui() {
        setIconImages(DeepDiveIconUtil.DEEPDIVE_ICON_LIST);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        tabber = new JTabbedPane();
        add(tabber, BorderLayout.CENTER);
        
        
        List<AnalyzerConfiguration> allAnalyzerConfigurationList = new ArrayList<AnalyzerConfiguration>();
        allAnalyzerConfigurationList.addAll(Arrays.asList(psc.getAnalyzerConfigurations()));
        List<DeploymentVisitor> allAnalyzerList = new ArrayList<DeploymentVisitor>();
        allAnalyzerList.addAll(Arrays.asList(psc.getDeploymentVisitors()));
        
        JTabbedPane systemTabber = new JTabbedPane();
        AnalyzerConfiguration systemParsers[] = psc.getSystemSelectionConfiguration().getAnalyzerConfigurations();
        for (int i=0; i<systemParsers.length; i++) {
            if (allAnalyzerConfigurationList.contains(systemParsers[i])) {
                systemTabber.addTab(systemParsers[i].getCategory(), new ReportTabView(systemParsers[i], allAnalyzerList));
            }
        }
        if (systemTabber.getTabCount() > 0)
            tabber.addTab("System", systemTabber);


        JTabbedPane userDefinedTabber = new JTabbedPane();
        AnalyzerConfiguration userDefinedParsers[] = psc.getUserDefinedSelectionConfiguration().getAnalyzerConfigurations();
        for (int i=0; i<userDefinedParsers.length; i++) {
            if (allAnalyzerConfigurationList.contains(userDefinedParsers[i])) {
                userDefinedTabber.addTab(userDefinedParsers[i].getCategory(), new ReportTabView(userDefinedParsers[i], allAnalyzerList));
            }
        }
        if (userDefinedTabber.getTabCount() > 0)
            tabber.addTab("User Defined", userDefinedTabber);
        
        JTabbedPane thirdPartyTabber = new JTabbedPane();
        AnalyzerConfiguration thirdPartyParsers[] = psc.getThirdPartySelectionConfiguration().getAnalyzerConfigurations();
        for (int i=0; i<thirdPartyParsers.length; i++) {
            if (allAnalyzerConfigurationList.contains(thirdPartyParsers[i])) {
                thirdPartyTabber.addTab(thirdPartyParsers[i].getCategory(), new ReportTabView(thirdPartyParsers[i], allAnalyzerList));
            }
        }
        if (thirdPartyTabber.getTabCount() > 0)
            tabber.addTab("Third Party", thirdPartyTabber);
    }
    
    static String COLUMN_NAMES[] = { "Title", "Description" };
    
    class AnalyzerModel extends DefaultTableModel {

        DeploymentVisitor visitors[];
        
        public AnalyzerModel(AnalyzerConfiguration parser, List<DeploymentVisitor> allAnalyzerList) {
            super(COLUMN_NAMES, 0);
            List<DeploymentVisitor> list = new ArrayList<DeploymentVisitor>();
            visitors = parser.getDeploymentVisitors();
            for (int i=0; i<visitors.length; i++) {
                if (allAnalyzerList.contains(visitors[i])) {
                    if (visitors[i] instanceof MultiAnalyzer) {
                        MultiAnalyzer ma = (MultiAnalyzer) visitors[i];
                        DeploymentVisitor multiVisitors[] = ma.getAnalyzers();
                        for (int j=0; j<multiVisitors.length; j++)
                            list.add(multiVisitors[j]);
                    }
                    else
                        list.add(visitors[i]);
                }
            }
            
            visitors = list.toArray(new DeploymentVisitor[list.size()]);
        }
        
        public int getRowCount() { 
            return visitors == null ? 0 : visitors.length;
        }
        
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return visitors[row].getTitle();
                case 1:
                    return visitors[row].getDescription();
                default:
                    throw new RuntimeException("Unknown column: " + column);
            }
        }
    }
    
    class ReportTabView extends JPanel implements HyperlinkListener {
        AnalyzerConfiguration parser;
        AnalyzerModel model;
        JEditorPane renderer = new JEditorPane();
        String html = null;
        
        Stack<Point> backStack = new Stack<Point>();
        Stack<Point> forwardStack = new Stack<Point>();
        JScrollPane scroller;
        
        JTextField filterField;
        JButton filterButton;
        JButton backButton;
        JButton forwardButton;
        
        JTextField searchField;
        JCheckBox regexButton;
        JCheckBox caseSensitiveButton;
        JButton searchForwardButton;
        JButton searchBackButton;
        JCheckBox highlightButton;
        
        Set<Object> highlightSet = new HashSet<Object>();
        Object searchHighlight = null;
        int searchHighlightStart = -1, searchHighlightEnd = -1;
        
        JTable table;

        Highlighter highlighter;
        HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        HighlightPainter searchPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
        
        public ReportTabView(AnalyzerConfiguration parser, List<DeploymentVisitor> allAnalyzerList) {
            this.parser = parser;
            this.model = new AnalyzerModel(parser, allAnalyzerList);
            buildGui();
        }
        
        void buildGui() {
            JPanel rendererPanel = new JPanel();
            rendererPanel.setLayout(new BorderLayout());

            rendererPanel.add(buildFilterNavigatePanel(), BorderLayout.NORTH);
            
            Caret caret = new DefaultCaret() {
                public void focusLost(FocusEvent e) {}
            };
            
            renderer.setEditable(false);
            renderer.setCaret(caret);
            renderer.setContentType("text/html");
            scroller = new JScrollPane(renderer);
            renderer.addHyperlinkListener(this);
            highlighter = renderer.getHighlighter();
            rendererPanel.add(scroller, BorderLayout.CENTER);
            
            rendererPanel.setPreferredSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
            
            setLayout(new BorderLayout());

            table = new BasicTable(model);

            JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), rendererPanel);
            splitter.setResizeWeight(0.5);
            add(splitter, BorderLayout.CENTER);
            
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) 
                        renderer.setText("<html><center>Rendering...</center></html>");
                    else {
                        int row = table.getSelectedRow();
                        
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("<html>");

                        try {
                            ReportGenerator.generateReportHeader(buffer, model.visitors[row]);
                            model.visitors[row].outputToHtml(buffer, OutputIterator.HYPERLINK_TO_MEMORY, null);
                        }
                        catch (OutputException e1) {
                            e1.printStackTrace();
                        }

                        buffer.append("</html>");
                        renderer.setText(buffer.toString());
                        renderer.setCaretPosition(0);
                    }
                }
            });
            
            add(buildSearchPanel(), BorderLayout.SOUTH);
        }
        
        JPanel buildFilterNavigatePanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(2,2,2,2);
            gbc.weightx = gbc.weighty = 0;
            
            panel.add(new JLabel("Output File Filter"), gbc);

            gbc.gridx++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            
            filterField = new JTextField(25);
            panel.add(filterField, gbc);

            gbc.gridx++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;

            
            filterButton = new JButton("Filter");
            filterButton.setEnabled(false);
            filterField.getDocument().addDocumentListener(new DocumentListener() {
                public void removeUpdate(DocumentEvent e) {
                    updateButton();
                }
                
                public void insertUpdate(DocumentEvent e) {
                    updateButton();
                }
                
                public void changedUpdate(DocumentEvent e) {
                    updateButton();
                }
                
                void updateButton() {
                    filterButton.setEnabled(filterField.getText().length() > 0);
                }
            });
            panel.add(filterButton, gbc);
            filterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Pattern pattern = Pattern.compile(filterField.getText());
                        renderer.setText("");
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            StringBuilder buffer = new StringBuilder();
                            buffer.append("<html>");

                            try {
                                model.visitors[row].outputToHtml(buffer, OutputIterator.HYPERLINK_TO_MEMORY, pattern);
                            }
                            catch (OutputException e1) {
                                e1.printStackTrace();
                            }

                            buffer.append("</html>");
                            renderer.setText(buffer.toString());
                            renderer.setCaretPosition(0);
                        }
                    }
                    catch (PatternSyntaxException  e2) {
                        JOptionPane.showMessageDialog(ReportViewer.this, "Invalid Regular Expression for Filter", "Invalid Filter", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            gbc.gridx++;
            
            JButton button = new JButton("Reset");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    renderer.setText("");
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        StringBuilder buffer = new StringBuilder();
                        buffer.append("<html>");

                        try {
                            model.visitors[row].outputToHtml(buffer, OutputIterator.HYPERLINK_TO_MEMORY, null);
                        }
                        catch (OutputException e1) {
                            e1.printStackTrace();
                        }

                        buffer.append("</html>");
                        renderer.setText(buffer.toString());
                        renderer.setCaretPosition(0);
                    }
                }
            });
            panel.add(button, gbc);
            
            
            gbc.gridx++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            panel.add(new JLabel(""), gbc);
            
            
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx++;
            backButton = new JButton("<<");
            panel.add(backButton, gbc);
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Point point = new Point(scroller.getHorizontalScrollBar().getValue(), scroller.getVerticalScrollBar().getValue());
                    forwardStack.push(point);
                    forwardButton.setEnabled(true);
                    
                    point = backStack.pop();
                    scroller.getHorizontalScrollBar().setValue(point.x);
                    scroller.getVerticalScrollBar().setValue(point.y);
                    backButton.setEnabled(backStack.size() > 0);

                }
            });
            backButton.setEnabled(false);
            
            gbc.gridx++;

            forwardButton = new JButton(">>");
            panel.add(forwardButton, gbc);
            forwardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Point point = new Point(scroller.getHorizontalScrollBar().getValue(), scroller.getVerticalScrollBar().getValue());
                    backStack.push(point);
                    backButton.setEnabled(true);
                    
                    point = forwardStack.pop();
                    scroller.getHorizontalScrollBar().setValue(point.x);
                    scroller.getVerticalScrollBar().setValue(point.y);
                    forwardButton.setEnabled(forwardStack.size() > 0);
                }
            });
            forwardButton.setEnabled(false);
            
            return panel;        
        }
        
        JPanel buildSearchPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.insets = new Insets(2,2,2,2);
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            panel.add(new JLabel(), gbc);

            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx++;
            
            panel.add(new JLabel("Search"), gbc);
            
            gbc.gridx++;
            
            searchField = new JTextField(25);
            panel.add(searchField, gbc);

            gbc.gridx++;

            regexButton = new JCheckBox("Regular Expression", false);
            panel.add(regexButton, gbc);
            gbc.gridx++;
            
            caseSensitiveButton = new JCheckBox("Case Sensitive", false);
            panel.add(caseSensitiveButton, gbc);
            gbc.gridx++;
            
            searchBackButton = new JButton("<");
            searchBackButton.setEnabled(false);
            searchBackButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { search(false); }
                    catch (BadLocationException e1) {}
                }
            });
            searchForwardButton = new JButton(">");
            searchForwardButton.setEnabled(false);
            searchForwardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try { search(true); }
                    catch (BadLocationException e1) {}
                }
            });
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                public void removeUpdate(DocumentEvent e) {
                    updateButtons();
                }
                
                public void insertUpdate(DocumentEvent e) {
                    updateButtons();
                }
                
                public void changedUpdate(DocumentEvent e) {
                    updateButtons();
                }
                
                void updateButtons() {
                    boolean enabled = searchField.getText().length() > 0;
                    searchBackButton.setEnabled(enabled);
                    searchForwardButton.setEnabled(enabled);
                    highlightButton.setEnabled(enabled);
                    if (highlightButton.isSelected())
                        highlight(highlightButton.isEnabled());
                }
            });
            panel.add(searchBackButton, gbc);
            gbc.gridx++;
            panel.add(searchForwardButton, gbc);
            gbc.gridx++;
            highlightButton = new JCheckBox("Highlight", true);
            panel.add(highlightButton, gbc);
            highlightButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    highlight(highlightButton.isSelected());
                }
            });
            
            return panel;        
        }
        
        public void search(boolean forward) throws BadLocationException {
            boolean highlightEnabled = highlightButton.isSelected();
            if (highlightEnabled)
                highlight(false);
            
            String text = searchField.getText();
            if (regexButton.isSelected()) {
                try {
                    Pattern pattern = caseSensitiveButton.isSelected() ? Pattern.compile(text) : Pattern.compile(text, Pattern.CASE_INSENSITIVE);
                    search(pattern, forward);
                }
                catch (PatternSyntaxException  e) {
                    JOptionPane.showMessageDialog(this, "Search term is not a valid regular expression.", "Invalid Regular Expression", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
                try { search(text, forward); }
                catch (BadLocationException e) {}
            }
            
            if (highlightEnabled)
                highlight(true);
        }
        
        public void search(Pattern pattern, boolean forward) throws BadLocationException {
            Document document = renderer.getDocument();
            String renderedText = document.getText(0, document.getLength());
            
            int index = -1;
            int end = -1;
            int startPosition;
            Matcher matcher = pattern.matcher(renderedText);
            if (forward) {
                startPosition = searchHighlight == null ? 0 : searchHighlightStart + 1;
                if (matcher.find(startPosition)) {
                    index = matcher.start();
                    end = matcher.end();
                }
            }
            else {
                List<int[]> list = new ArrayList<int[]>();
                
                while ( (matcher.find()) && end < searchHighlightEnd) {
                    index = matcher.start();
                    end = matcher.end();
                    list.add(new int[]{matcher.start(), matcher.end()});
                }

                int size = list.size();
                int values[];
                if (size > 0) {
                    values = list.get(size-1);
                    index = values[0];
                    end = values[1];
                    
                    if (searchHighlight != null) {
                        if (index == searchHighlightStart && end == searchHighlightEnd) {
                            if (size > 1) {
                                values = list.get(size-2);
                                index = values[0];
                                end = values[1];
                            }
                        }
                    }
                }
            }
            
            if (index > -1) {
                searchHighlightStart = index;
                renderer.setCaretPosition(index);
                searchHighlightEnd = end;
                if (searchHighlight != null) 
                    highlighter.removeHighlight(searchHighlight);
                searchHighlight = 
                    highlighter.addHighlight(searchHighlightStart, searchHighlightEnd, searchPainter);
            }
        }
        
        public void search(String text, boolean forward) throws BadLocationException {
            boolean caseSensitive = caseSensitiveButton.isSelected();
            Document document = renderer.getDocument();
            String renderedText = document.getText(0, document.getLength());
            if (!caseSensitive)
                renderedText = renderedText.toLowerCase();
            
            String searchText = caseSensitive ? text : text.toLowerCase();
            int length = searchText.length();

            int index;
            int startPosition;
            if (forward) {
                startPosition = searchHighlight == null ? 0 : searchHighlightStart + 1;
                index = renderedText.indexOf(searchText, startPosition);
            }
            else {
                int truncatePosition;
                if (searchHighlight == null) 
                    truncatePosition = searchHighlightEnd;
                else {
                    if (searchText.equals(renderedText.substring(searchHighlightStart, searchHighlightEnd)))
                        truncatePosition = searchHighlightEnd-1;
                    else
                        truncatePosition = searchHighlightEnd;
                }
                
                String searchString = truncatePosition < 1 ? null : renderedText.substring(0, truncatePosition);
                index = searchString == null ? -1 : searchString.lastIndexOf(searchText);
            }
            if (index > -1) {
                renderer.setCaretPosition(index);
                searchHighlightStart = index;
                searchHighlightEnd = searchHighlightStart + length;
                if (searchHighlight != null) 
                    highlighter.removeHighlight(searchHighlight);
                searchHighlight = 
                    highlighter.addHighlight(searchHighlightStart, searchHighlightEnd, searchPainter);
            }
        }
        
        public void highlight(boolean enabled) {
            Iterator it = highlightSet.iterator();
            while (it.hasNext())
                highlighter.removeHighlight(it.next());


            if (enabled) {
                String text = searchField.getText();
                try {
                    if (regexButton.isSelected()) {
                        Pattern pattern = caseSensitiveButton.isSelected() ? Pattern.compile(text) : Pattern.compile(text, Pattern.CASE_INSENSITIVE);
                        highlight(pattern);
                    }
                    else
                        highlight(text);
                }
                catch (Exception e) {}
            }
        }
        
        public void highlight(String text) throws BadLocationException {
            boolean caseSensitive = caseSensitiveButton.isSelected();
            Document document = renderer.getDocument();
            String renderedText = document.getText(0, document.getLength());
            if (!caseSensitive)
                renderedText = renderedText.toLowerCase();

            int start = 0;
            int end = 0;
            
            int modifiedStart = 0;
            int modifiedEnd = 0;
            
            int length = text.length();
            
            String searchText = caseSensitive ? text : text.toLowerCase();
            
            while ( (start = renderedText.indexOf(searchText, end)) > -1) {
                end = start + length;
                modifiedStart = start;
                modifiedEnd = end;
                if (modifiedStart >= searchHighlightStart && modifiedStart <= searchHighlightEnd)
                    modifiedStart = searchHighlightEnd;
                if (modifiedEnd >= searchHighlightStart && modifiedEnd <= searchHighlightEnd)
                    modifiedEnd = searchHighlightStart;
                if (modifiedEnd > modifiedStart) {
                    try { highlightSet.add( highlighter.addHighlight(start,  end, highlightPainter) ); }
                    catch (BadLocationException e) {}
                }
            }
        }
        
        public void highlight(Pattern pattern) throws BadLocationException {
            Document document = renderer.getDocument();
            String renderedText = document.getText(0, document.getLength());
            Matcher matcher = pattern.matcher(renderedText);
            
            int start = 0;
            int end = 0;
            
            while ( matcher.find(end)) {
                start = matcher.start();
                end = matcher.end();
                try { highlightSet.add( highlighter.addHighlight(start,  end, highlightPainter) ); }
                catch (BadLocationException e) {}
            }
        }
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            EventType type = e.getEventType();
            if (type == EventType.ACTIVATED) {
                String description = e.getDescription();
                if (description.startsWith("#")) {
                    backStack.push(new Point(scroller.getHorizontalScrollBar().getValue(), scroller.getVerticalScrollBar().getValue()));
                    backButton.setEnabled(true);
                    renderer.scrollToReference(description.substring(1, description.length()));
                    forwardStack.clear();
                    forwardButton.setEnabled(false);
                }
                else
                    LinkLauncher.launch(description, renderer);
            }
        }
    }
}
