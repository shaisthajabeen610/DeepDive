package ca.discotek.deepdive.security.gui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog {

    JFrame owner;
    
    JProgressBar bar;
    
    public ProgressDialog(JFrame owner, String title) {
        super(owner, title, false);
        this.owner = owner;
        
        setLayout(new BorderLayout());
        bar = new JProgressBar(JProgressBar.HORIZONTAL);
        bar.setIndeterminate(true);
        add(bar);
    }

    public void start() {
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }
    
    public void stop() {
        setVisible(false);
    }
}
