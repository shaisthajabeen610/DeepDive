package ca.discotek.deepdive.security.gui.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import ca.discotek.common.swing.BasicTable;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.grep.classmatcher.AbstractInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.FieldInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.IincInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.InstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.IntInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.InvokeDynamicInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.JumpInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.LoadConstantInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.LookupSwitchInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.MethodInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.MultiANewArrayInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.TableSwitchInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.TypeInstructionDescriptor;
import ca.discotek.deepdive.grep.classmatcher.VarInstructionDescriptor;

public class InstructionsEditor extends JPanel {
    static final Class INSTRUCTION_TYPES[] = {
        FieldInstructionDescriptor.class,
        MethodInstructionDescriptor.class,
        LoadConstantInstructionDescriptor.class,
        TypeInstructionDescriptor.class,
        InstructionDescriptor.class,
        VarInstructionDescriptor.class,
        IincInstructionDescriptor.class,
        IntInstructionDescriptor.class,
        InvokeDynamicInstructionDescriptor.class,
        JumpInstructionDescriptor.class,
        LookupSwitchInstructionDescriptor.class,
        MultiANewArrayInstructionDescriptor.class,
        TableSwitchInstructionDescriptor.class
    };
    
    static final String INSTRUCTION_DESCRIPTIONS[] = {
        "Field",
        "Method",
        "Load Constant",
        "Type",
        "Zero Operand",
        "Load/Store Variable",
        "Increment",
        "Single Int Argument",
        "Invoke Dynamic",
        "Jump",
        "Lookup Switch",
        "Create Multi-Dimensional Array",
        "Table Switch"
    };
    
    Model model;
    JTable table;
    
    JButton addButton;
    JButton editButton;
    JButton removeButton;
    
    JComboBox instructionTypeField;
    
    public InstructionsEditor() {
        model = new Model();
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        
        add(buildInstructionSelectorPanel(), BorderLayout.NORTH);
        
        table = new BasicTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateButtons();
                }
            }
        });
    }
    
    JPanel buildInstructionSelectorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2,2,2,2);
        
        panel.add(new JLabel("Instruction Type"), gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        instructionTypeField = new JComboBox(INSTRUCTION_DESCRIPTIONS);
        panel.add(instructionTypeField, gbc);
        
        return panel;
    }
    
    JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
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
                int index = instructionTypeField.getSelectedIndex();

                JComponent editor = null;
                if (INSTRUCTION_TYPES[index] == FieldInstructionDescriptor.class) 
                    editor = new FieldInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == MethodInstructionDescriptor.class)
                    editor = new MethodInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == LoadConstantInstructionDescriptor.class) 
                    editor = new LoadConstantInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == TypeInstructionDescriptor.class)
                    editor = new TypeInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == InstructionDescriptor.class)
                    editor = new InstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == VarInstructionDescriptor.class)
                    editor = new VarInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == IincInstructionDescriptor.class)
                    editor = new IincInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == IntInstructionDescriptor.class) 
                    editor = new IntInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == InvokeDynamicInstructionDescriptor.class) 
                    editor = new InvokeDynamicInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == JumpInstructionDescriptor.class) 
                    editor = new JumpInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == LookupSwitchInstructionDescriptor.class) 
                    model.addInstruction(new LookupSwitchInstructionDescriptor());
                else if (INSTRUCTION_TYPES[index] == MultiANewArrayInstructionDescriptor.class) 
                    editor = new MultiANewArrayInstructionDescriptorEditor();
                else if (INSTRUCTION_TYPES[index] == TableSwitchInstructionDescriptor.class) 
                    model.addInstruction(new TableSwitchInstructionDescriptor());
                
                if (editor != null) {
                    if (editor instanceof InstructionDescriptionEditor)
                        ( (InstructionDescriptionEditor) editor).loadPreferences();
                    Window w = SwingUtilities.windowForComponent(InstructionsEditor.this);
                    boolean wasCancelled = DialogWrapper.showDialogWrapper(w, INSTRUCTION_DESCRIPTIONS[index] +  "Instruction Editor", editor);
                    if (!wasCancelled) {
                        model.addInstruction( ((InstructionDescriptionEditor) editor).getDescriptor() );
                        if (editor instanceof InstructionDescriptionEditor)
                            ( (InstructionDescriptionEditor) editor).savePreferences();
                    }
                    int row = model.list.size() - 1;
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        });
        
        gbc.gridx++;
        
        editButton = new JButton("Edit");
        buttonPanel.add(editButton, gbc);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int row = table.getSelectedRow();
                AbstractInstructionDescriptor d = model.getInstructionAt(row);
                
                JComponent editor = null;
                if (d instanceof FieldInstructionDescriptor) 
                    editor = new FieldInstructionDescriptorEditor();
                else if (d instanceof MethodInstructionDescriptor)
                    editor = new MethodInstructionDescriptorEditor();
                else if (d instanceof LoadConstantInstructionDescriptor) 
                    editor = new LoadConstantInstructionDescriptorEditor();
                else if (d instanceof TypeInstructionDescriptor)
                    editor = new TypeInstructionDescriptorEditor();
                else if (d instanceof InstructionDescriptor)
                    editor = new InstructionDescriptorEditor();
                else if (d instanceof VarInstructionDescriptor)
                    editor = new VarInstructionDescriptorEditor();
                else if (d instanceof IincInstructionDescriptor)
                    editor = new IincInstructionDescriptorEditor();
                else if (d instanceof IntInstructionDescriptor) 
                    editor = new IntInstructionDescriptorEditor();
                else if (d instanceof InvokeDynamicInstructionDescriptor) 
                    editor = new InvokeDynamicInstructionDescriptorEditor();
                else if (d instanceof JumpInstructionDescriptor) 
                    editor = new JumpInstructionDescriptorEditor();
                else if (d instanceof LookupSwitchInstructionDescriptor) 
                    model.addInstruction(new LookupSwitchInstructionDescriptor());
                else if (d instanceof MultiANewArrayInstructionDescriptor) 
                    editor = new MultiANewArrayInstructionDescriptorEditor();
                else if (d instanceof TableSwitchInstructionDescriptor) 
                    model.addInstruction(new TableSwitchInstructionDescriptor());
                
                if (editor == null) 
                    JOptionPane.showMessageDialog(InstructionsEditor.this, "Selected instruction type as not editable properties.", "Edit Instruction Error", JOptionPane.ERROR_MESSAGE);
                else {
                    if (editor instanceof InstructionDescriptionEditor)
                        ( (InstructionDescriptionEditor) editor).loadPreferences();
                    ( (InstructionDescriptionEditor) editor).setDescriptor(d);
                    Window w = SwingUtilities.windowForComponent(InstructionsEditor.this);
                    boolean wasCancelled = DialogWrapper.showDialogWrapper(w, "Instruction Editor", editor);
                    if (!wasCancelled) {
                        model.setInstructionAt(row, ((InstructionDescriptionEditor) editor).getDescriptor() );
                        if (editor instanceof InstructionDescriptionEditor)
                            ( (InstructionDescriptionEditor) editor).savePreferences();
                    }
                    table.getSelectionModel().setSelectionInterval(row,  row);
                }
            }
        });
        
        gbc.gridx++;
        
        removeButton = new JButton("Remove");
        buttonPanel.add(removeButton, gbc);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                model.removeInstruction(table.getSelectedRow());
            }
        });
        
        gbc.gridx++;
        
        updateButtons();
        
        return buttonPanel;
    }
    
    void updateButtons() {
        int row = table.getSelectedRow();
        editButton.setEnabled(row > -1);
        removeButton.setEnabled(row > -1);
    }
    
    public void setInstructions(AbstractInstructionDescriptor instructions[]) {
        model.setInstructions(instructions);
    }
    
    public AbstractInstructionDescriptor[] getInstructions() {
        return model.getInstructions();
    }
    
    static final String COLUMN_NAMES[] = {"Type", "Details"};
    class Model extends DefaultTableModel {

        List<AbstractInstructionDescriptor> list = new ArrayList<AbstractInstructionDescriptor>();

        public Model() {
            super(COLUMN_NAMES, 0);
        }
        
        public int getRowCount() {
            return list == null ? 0 : list.size();
        }
        
        public Object getValueAt(int row, int column) {
            AbstractInstructionDescriptor d = list.get(row);
            switch (column) {
                case 0:
                    return d.getName();
                case 1:
                    return d.getDetails();
                default:
                    throw new RuntimeException("Unknown column: " + column);
            }
        }
        
        public void setInstructions(AbstractInstructionDescriptor instructions[]) {
            list.clear();
            list.addAll(Arrays.asList(instructions));
            fireTableDataChanged();
        }
        
        public AbstractInstructionDescriptor[] getInstructions() {
            return list.toArray(new AbstractInstructionDescriptor[list.size()]);
        }
 
        public AbstractInstructionDescriptor getInstructionAt(int index) {
            return list.get(index);
        }
        
        public void addInstruction(AbstractInstructionDescriptor d) {
            list.add(d);
            fireTableDataChanged();
        }
        
        public void setInstructionAt(int index,AbstractInstructionDescriptor d) {
            list.set(index, d);
            fireTableDataChanged();
        }
        
        public void removeInstruction(int index) {
            list.remove(index);
            fireTableDataChanged();
        }
    }
}
