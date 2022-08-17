package ca.discotek.deepdive.security.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class DeploymentSelector extends JPanel {

    File files[];
    JCheckBox fileBoxes[];
    
    public DeploymentSelector(File files[]) {
        this.files = files;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,2,2,2);
        
        fileBoxes = new JCheckBox[files.length];
        for (int i=0; i<files.length; i++) {
            fileBoxes[i] = new JCheckBox(files[i].getAbsolutePath(), true);
            add(fileBoxes[i], gbc);
            gbc.gridy++;
        }
        
        final JCheckBox selectAllCheckBox = new JCheckBox("Select All", true);
        add(selectAllCheckBox, gbc);
        selectAllCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean select = selectAllCheckBox.isSelected();
                for (int i=0; i<fileBoxes.length; i++)
                    fileBoxes[i].setSelected(select);
            }
        });
    }
    
    public File[] getSelectedFiles() {
        List<File> list = new ArrayList<File>();
        for (int i=0; i<fileBoxes.length; i++) {
            if (fileBoxes[i].isSelected())
                list.add(files[i]);
        }
        
        return list.toArray(new File[list.size()]);
    }
}
