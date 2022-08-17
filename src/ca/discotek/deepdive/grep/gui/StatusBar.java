package ca.discotek.deepdive.grep.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class StatusBar extends JPanel {
	
	JProgressBar bar;

	public StatusBar() {
		buildGui();
	}
	
	void buildGui() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2,2,2,2);
		
		add(new JLabel("Status"), gbc);
		
		bar = new JProgressBar(JProgressBar.HORIZONTAL);
		bar.setIndeterminate(true);
		
		gbc.gridx++;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		add(bar, gbc);
		
		bar.setString("Processing...");
		bar.setStringPainted(true);
	}
	
	public void setDescription(String description) {
	    bar.setString("Processing " + description);
	}
	
	public void setComplete() {
		bar.setIndeterminate(false);
		bar.setString("Complete!");
	}
	
	public void setError() {
		bar.setIndeterminate(false);
		bar.setString("Error!");
	}
}
