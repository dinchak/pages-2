package org.monome.pages.gui;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.monome.pages.Main;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;
import org.monome.pages.configuration.SerialOSCArc;
import org.monome.pages.configuration.SerialOSCDevice;
import org.monome.pages.configuration.SerialOSCMonome;

import javax.swing.JComboBox;

public class SerialOSCSetupFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton discoverBtn = null;
	private JButton closeButton = null;
	private int nextDeviceHeight = 40;
    private JComboBox libSelect = null;
    private JLabel portLbl = null;
    private JTextField portTF = null;
    private ArrayList<String> addedDevices;
	
	public SerialOSCSetupFrame() {
		super();
		addedDevices = new ArrayList<String>();
		initialize();
		this.pack();
	}
	
	private void initialize() {
		this.setSize(505, 247);
		this.setTitle("SerialOSC Setup");
		this.setContentPane(getJContentPane());
		this.setResizable(true);
	}
	
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getDiscoverBtn(), null);
			jContentPane.add(getCloseButton(), null);
			jContentPane.add(getLibSelect(), null);
			jContentPane.add(getPortLbl(), null);
			jContentPane.add(getPortTF(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes discoverBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDiscoverBtn() {
		if (discoverBtn == null) {
			discoverBtn = new JButton();
			discoverBtn.setBounds(new Rectangle(250, 5, 151, 26));
			discoverBtn.setText("Discover Devices");
			discoverBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					discover();
				}
			});
		}
		return discoverBtn;
	}
	
	private void discover() {
	    addedDevices = new ArrayList<String>();
		jContentPane.removeAll();
		jContentPane = null;
		initialize();
		this.validate();
		this.repaint();
		this.nextDeviceHeight = 40;
		if (Main.main.zeroconfLibrary == Main.LIBRARY_APPLE) {
		    Main.main.appleSerialOSCDiscovery();
		} else if (Main.main.zeroconfLibrary == Main.LIBRARY_JMDNS){
            Main.main.jmdnsSerialOSCDiscovery();	    
		}
	}

	/**
	 * This method initializes closeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setBounds(new Rectangle(405, 5, 76, 26));
			closeButton.setText("Close");
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					close();
				}
			});
		}
		return closeButton;
	}
	
	private void close() {
		this.dispose();
	}

	public void addDevice(final SerialOSCDevice device) {
		JLabel deviceLabel = new JLabel();
		String label = device.getDeviceName() + " (" + device.getSerial() + ") [" + device.getHostName() + ":" + device.getPort() + "]";
		for (int i = 0; i < addedDevices.size(); i++) {
		    if (addedDevices.get(i).compareTo(label) == 0) {
		        return;
		    }
		}
		addedDevices.add(label);
		deviceLabel.setText(label);
		deviceLabel.setBounds(new Rectangle(10, nextDeviceHeight, 400, 20));
		jContentPane.add(deviceLabel);
		
		if (device instanceof SerialOSCMonome) {
    		MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration("/" + device.getSerial());
    		if (monomeConfig == null || (monomeConfig != null && monomeConfig.serialOSCHostname != null && !monomeConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
    			final JButton addButton = new JButton();
    			addButton.setBounds(new Rectangle(400, nextDeviceHeight, 76, 20));
    			addButton.setText("Add");
    			addButton.addActionListener(new java.awt.event.ActionListener() {
    				public void actionPerformed(java.awt.event.ActionEvent e) {
    					addMonome(addButton, (SerialOSCMonome) device);
    				}
    			});
    			jContentPane.add(addButton);
    		}
		} else if (device instanceof SerialOSCArc) {
            ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + device.getSerial());
            if (arcConfig == null || (arcConfig != null && arcConfig.serialOSCHostname != null && !arcConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
                final JButton addButton = new JButton();
                addButton.setBounds(new Rectangle(400, nextDeviceHeight, 76, 20));
                addButton.setText("Add");
                addButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        addArc(addButton, (SerialOSCArc) device);
                    }
                });
                jContentPane.add(addButton);
            }
		}
		
		nextDeviceHeight += 25;
		this.validate();
		this.repaint();
	}
	
	protected void addArc(JButton addButton, SerialOSCArc arc) {
        jContentPane.remove(addButton);
        Main.main.startArc(arc);
        this.validate();
        this.repaint();
    }

    private void addMonome(JButton addButton, SerialOSCMonome monome) {
		jContentPane.remove(addButton);
		Main.main.startMonome(monome);
		this.validate();
		this.repaint();
	}

    /**
     * This method initializes libSelect	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getLibSelect() {
        if (libSelect == null) {
            libSelect = new JComboBox();
            libSelect.setBounds(new Rectangle(130, 5, 116, 26));
            libSelect.addItem("Apple");
            libSelect.addItem("JmDNS");
            libSelect.setSelectedIndex(Main.main.zeroconfLibrary);
            libSelect.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String mode = (String) libSelect.getSelectedItem();
                    if (mode.compareTo("Apple") == 0) {
                        Main.main.zeroconfLibrary = Main.LIBRARY_APPLE;
                    } else if (mode.compareTo("JmDNS") == 0) {
                        Main.main.zeroconfLibrary = Main.LIBRARY_JMDNS;
                    }
                }
            });
        }
        return libSelect;
    }

    /**
     * This method initializes portLbl	
     * 	
     * @return javax.swing.JLabel	
     */
    private JLabel getPortLbl() {
        if (portLbl == null) {
            portLbl = new JLabel();
            portLbl.setText("Port");
            portLbl.setBounds(new Rectangle(5, 5, 41, 26));
        }
        return portLbl;
    }

    /**
     * This method initializes portTF	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getPortTF() {
        if (portTF == null) {
            portTF = new JTextField();
            portTF.setBounds(new Rectangle(50, 5, 76, 26));
            portTF.setText("" + Main.main.configuration.oscListenPort);
            portTF.addCaretListener(new javax.swing.event.CaretListener() {
                public void caretUpdate(javax.swing.event.CaretEvent e) {
                    try {
                        int newPort = Integer.parseInt(portTF.getText());
                        if (newPort >= 1024 && newPort <= 65535) {
                            Main.main.configuration.oscListenPort = newPort;
                        }
                    } catch (NumberFormatException ex) {
                        
                    }
                }
            });
        }
        return portTF;
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
