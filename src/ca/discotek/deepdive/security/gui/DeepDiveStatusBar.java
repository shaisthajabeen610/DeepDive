package ca.discotek.deepdive.security.gui;

import javax.swing.JFrame;
import javax.swing.JTextField;

import ca.discotek.common.swing.FadeTextUtil;
import ca.discotek.common.swing.statusbar.GreenRedLight;
import ca.discotek.common.swing.statusbar.StatusBar;
import ca.discotek.deepdive.security.EventManager;
import ca.discotek.deepdive.security.EventManager.Listener;

public class DeepDiveStatusBar extends StatusBar {

	protected GreenRedLight greenRedLight;
	protected JTextField statusField;
	
	public DeepDiveStatusBar() {
        greenRedLight = new GreenRedLight();
        greenRedLight.setGreen(false);
        setEastComponent(greenRedLight);
        
        statusField = new JTextField();
        statusField.setBorder(null);
        statusField.setEditable(false);
        setCenterComponent(statusField);
        
    	FadeTextUtil fadeTextUtil = new FadeTextUtil(statusField, 10000);
    	
    	EventManager.INSTANCE.addEventListener(new Listener() {
            public void publish(String message) {
                setStatus(message);
            }

            public void startTimer(String eventId) {}
            public void endTimer(String eventId) {}
        });

    }
	
	public void setGreen(boolean green) {
        greenRedLight.setGreen(green);
	}

	public void setStatus(String status) {
		statusField.setText(status);
	}
}
