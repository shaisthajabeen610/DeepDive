package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import ca.discotek.deepdive.grep.gui.GrepGui;
import ca.discotek.common.swing.FileChooserField;
import ca.discotek.common.swing.HistoryComboBox;

public class PreferencesDialog extends JDialog {
	
	DeepDiveGui gui;
	JSpinner historySizeField;
	
	FileChooserField decompilerField;
	
	private boolean wasCanceled = true;

	public PreferencesDialog(JFrame parent, DeepDiveGui gui) {
		super(parent, "Preferences", true);
		this.gui = gui;
		buildGui();
	}
	
	void buildGui() {
		setLayout(new BorderLayout());
		
		
		// putting this in a panel so it could later be easily added to a 
		// tabbed pane if necessary.
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		
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

		panel.add(new JLabel("Decompiler Path"), labelGbc);
		String decompilerPath = System.getProperty(GrepGui.DECOMPILER_PATH_SYSTEM_PROPERTY_NAME);
		decompilerField = decompilerPath == null ? new FileChooserField() : new FileChooserField(decompilerPath);
		panel.add(decompilerField, fieldGbc);
		
		add(panel, BorderLayout.CENTER);
		
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
		
		JButton button = new JButton("Okay");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wasCanceled = false;
				setVisible(false);
			}
		});
		
		buttonPanel.add(button, gbc);
		
		gbc.gridx++;
		
		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				wasCanceled = true;
				setVisible(false);
			}
		});
		
		buttonPanel.add(button, gbc);
		
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public boolean getWasCanceled() {
		return wasCanceled;
	}

    public String getDecompilerPath() {
        return decompilerField.getFile();
    }
	
}
