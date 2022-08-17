package ca.discotek.deepdive.security.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.discotek.deepdive.security.PreferencesManager;

public class NetworkImportPreferencesView extends JPanel {

    String addressOptions[];
    InetAddress addresses[];

    JCheckBox enabledField;
    JCheckBox specifyAddressField;
    JComboBox addressField;
    JTextField portField;
    
    public NetworkImportPreferencesView() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = labelGbc.gridy = 0;
        labelGbc.weightx = labelGbc.weighty = 0;
        labelGbc.anchor = GridBagConstraints.EAST;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.insets = new Insets(2,2,2,2);
        
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = 0;
        fieldGbc.weightx = 1;
        fieldGbc.weighty = 0;
        fieldGbc.anchor = GridBagConstraints.WEST;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(2,2,2,2);

        add(new JLabel("Enabled"), labelGbc);
        enabledField = new JCheckBox();
        enabledField.setSelected(true);
        add(enabledField, fieldGbc);

        labelGbc.gridy = ++fieldGbc.gridy;
        
        add(new JLabel("Specify Address"), labelGbc);
        specifyAddressField = new JCheckBox();
        specifyAddressField.setSelected(false);
        add(specifyAddressField, fieldGbc);
        
        labelGbc.gridy = ++fieldGbc.gridy;
        
        add(new JLabel("Listen Address"), labelGbc);
    
        List<InetAddress> addressList = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> interfaceEnum = NetworkInterface.getNetworkInterfaces();
            NetworkInterface ni;
            Enumeration<InetAddress> addressEnum;
            while (interfaceEnum.hasMoreElements()) {
                ni = interfaceEnum.nextElement();
                addressEnum = ni.getInetAddresses();
                while (addressEnum.hasMoreElements())
                    addressList.add(addressEnum.nextElement());
            }
            addresses = addressList.toArray(new InetAddress[addressList.size()]);

        }
        catch (SocketException e) {
            addresses = new InetAddress[0];
        }
        
        addressField = new JComboBox(addresses);
        add(addressField, fieldGbc);
        addressField.setEnabled(false);
        if (addresses.length == 0)
            specifyAddressField.setEnabled(false);

        
        labelGbc.gridy = ++fieldGbc.gridy;
        
        add(new JLabel("Port"), labelGbc);
        portField = new JTextField("5678");
        add(portField, fieldGbc);
        
        specifyAddressField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean selected = specifyAddressField.isSelected();
                addressField.setEnabled(selected && addressField.getItemCount() > 0);
            }
        });
        
        PreferencesManager manager = PreferencesManager.getManager("default");
        enabledField.setSelected(manager.getImportServerEnabledPreferences());
        specifyAddressField.setSelected(manager.getImportServerSpecifyAddressPreferences());
        ( (JTextField) addressField.getEditor().getEditorComponent() ).setText(manager.getImportServerAddressPreferences());
        String port = manager.getImportServerPortPreferences();
        if (port != null && port.trim().length() > 0)
            portField.setText(port.trim());
    }
    
    public boolean getEnabled() {
        return enabledField.isSelected();
    }
    
    public boolean isAddressSpecified() {
        return specifyAddressField.isSelected();
    }
    
    public InetAddress getAddress() {
        return addresses[addressField.getSelectedIndex()];
    }
    
    public String getPort() {
        return portField.getText();
    }
}
