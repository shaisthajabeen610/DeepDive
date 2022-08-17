package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import ca.discotek.common.swing.BasicTable;
import ca.discotek.common.swing.BrowserHelpBroker;
import ca.discotek.deepdive.security.EventManager;

public class PerformanceView extends JPanel {

    static final SimpleDateFormat FORMAT = new SimpleDateFormat("hh:mm:ss SSS");
    
    Model model;
    JTable table;
    BrowserHelpBroker helpBroker;
    
    public PerformanceView(BrowserHelpBroker helpBroker) {
        this.helpBroker = helpBroker;
        buildGui();
    }
    
    void buildGui() {
        helpBroker.enableHelpKey(this, "performance-data", helpBroker.getHelpSet());
        setLayout(new BorderLayout());
        model = new Model();
        table = new BasicTable(model);
        
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

        int columnIndexToSort = 3;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    public void refresh() {
        model.fireTableDataChanged();
    }
    
    public void reset() {
        EventManager.INSTANCE.reset();
        model.fireTableDataChanged();
    }
    
    static String COLUMN_NAMES[] = {"Event", "Start", "Finish", "Ellapsed (ms)"};
    class Model extends DefaultTableModel {
        
        public Model() {
            super(COLUMN_NAMES, 0);
        }
        
        public int getRowCount() {
            String eventIds[] = EventManager.INSTANCE.getTimerEventIds();
            return eventIds.length;
        }
        
        public Object getValueAt(int row, int column) {
            String eventIds[] = EventManager.INSTANCE.getTimerEventIds();
            switch (column) {
                case 0: return eventIds[row];
                case 1: return FORMAT.format(EventManager.INSTANCE.getStartTime(eventIds[row]));
                case 2: return FORMAT.format(EventManager.INSTANCE.getStartTime(eventIds[row]) + EventManager.INSTANCE.getResultTime(eventIds[row]));
                case 3: return EventManager.INSTANCE.getResultTime(eventIds[row]);
                default: throw new RuntimeException("Unknown column index: " + column);
            }
        }
        
        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                    return Long.class;
                case 2:
                    return Long.class;
                default:
                    return Long.class;
            }
        }
    }
}
