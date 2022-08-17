package ca.discotek.deepdive.security.gui.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class LinksEditor extends JPanel {

    Model model;
    JTable table;
    
    JButton addButton;
    JButton removeButton;
    JButton upButton;
    JButton downButton;
    
    public LinksEditor() {
        this(new String[0]);
    }
       
    public LinksEditor(String links[]) {
        model = new Model();
        for (int i=0; i<links.length; i++)
            model.addLink(links[i]);
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        add(buildButtonPanel(), BorderLayout.SOUTH);
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (! e.getValueIsAdjusting())
                    updateButtons();
            }
        });
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    }
    
    public String[] getLinks() {
        return model.getLinks();
    }
    
    void updateButtons() {
        int index = table.getSelectedRow();
        
        removeButton.setEnabled(index > -1);
        upButton.setEnabled(index > 0);
        downButton.setEnabled(index < model.getRowCount()-1);
    }
    
    JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        add(buttonPanel, BorderLayout.SOUTH);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,2,2,2);
        
        buttonPanel.add(new JLabel(""), gbc);
        
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        
        addButton = new JButton("Add");
        buttonPanel.add(addButton, gbc);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                model.addLink("");
            }
        });
        
        gbc.gridx++;
        
        removeButton = new JButton("Remove");
        buttonPanel.add(removeButton, gbc);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                model.removeLink(table.getSelectedRow());
            }
        });
        
        gbc.gridx++;
        
        upButton = new JButton("Up");
        buttonPanel.add(upButton, gbc);
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int row = table.getSelectedRow();
                String link = model.removeLink(row);
                model.insertLink(row-1, link);
                table.getSelectionModel().setSelectionInterval(row-1, row-1);
            }
        });
        
        
        gbc.gridx++;
        
        downButton = new JButton("Down");
        buttonPanel.add(downButton, gbc);
        downButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int row = table.getSelectedRow();
                String link = model.removeLink(row);
                model.insertLink(row+1, link);
                table.getSelectionModel().setSelectionInterval(row+1, row+1);
            }
        });
        
        return buttonPanel;
    }
    
    static final String COLUMN_NAMES[] = {"Link"};
    
    class Model extends DefaultTableModel {
        List<String> list = new ArrayList<String>();

        public Model() {
            super(COLUMN_NAMES, 0);
        }
        
        public int getRowCount() {
            return list == null ? 0 : list.size();
        }
        
        public Object getValueAt(int row, int column) {
            return list.get(row);
        }
        
        public void setValueAt(Object value,int row, int column) {
            String s = (String) value;
            list.set(row, s);
        }
        
        public void addLink(String link) {
            list.add(link);
            fireTableDataChanged();
        }
        
        public void insertLink(int index, String link) {
            list.add(index, link);
            fireTableDataChanged();
        }
        
        public String removeLink(int index) {
            String link = list.remove(index);
            fireTableDataChanged();
            return link;
        }
        
        public String[] getLinks() {
            return list.toArray(new String[list.size()]);
        }
        
        public String getLinkAt(int row) {
            return list.get(row);
        }
    }
}
