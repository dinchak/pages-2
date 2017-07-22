package org.monome.pages.gui;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.monome.pages.Main;
import org.monome.pages.configuration.Configuration;

public class MonomeSerialSetupFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel hostLabel = null;
	private JLabel inPortLabel = null;
	private JLabel outPortLabel = null;
	private JTextField host = null;
	private JTextField inPort = null;
	private JTextField outPort = null;
	private JButton saveButton = null;
	private JButton cancelButton = null;
	private JButton autoConfigButton = null;

	/**
	 * This is the MonomeSerialSetupFrame default constructor
	 */
	public MonomeSerialSetupFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(237, 204);
		this.setTitle("Monome Serial Setup");
		this.setContentPane(getJContentPane());
		this.setResizable(true);
		this.pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			outPortLabel = new JLabel();
			outPortLabel.setBounds(new Rectangle(25, 70, 96, 21));
			outPortLabel.setText("Listen Port");
			inPortLabel = new JLabel();
			inPortLabel.setBounds(new Rectangle(25, 40, 96, 21));
			inPortLabel.setText("Host Port");
			hostLabel = new JLabel();
			hostLabel.setBounds(new Rectangle(25, 10, 96, 21));
			hostLabel.setText("Host Address");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(hostLabel, null);
			jContentPane.add(inPortLabel, null);
			jContentPane.add(outPortLabel, null);
			jContentPane.add(getHost(), null);
			jContentPane.add(getInPort(), null);
			jContentPane.add(getOutPort(), null);
			jContentPane.add(getSaveButton(), null);
			jContentPane.add(getCancelButton(), null);
			jContentPane.add(getAutoConfigButton(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes host	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getHost() {
		if (host == null) {
			host = new JTextField();
			host.setBounds(new Rectangle(120, 10, 91, 21));
			Configuration config = Main.main.configuration;
			host.setText(config.getMonomeHostname());
		}
		return host;
	}

	/**
	 * This method initializes inPort	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInPort() {
		if (inPort == null) {
			inPort = new JTextField();
			inPort.setBounds(new Rectangle(120, 40, 56, 21));
			Configuration config = Main.main.configuration;
			inPort.setText("" + config.getMonomeSerialOSCInPortNumber());
		}
		return inPort;
	}

	/**
	 * This method initializes outPort	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOutPort() {
		if (outPort == null) {
			outPort = new JTextField();
			outPort.setBounds(new Rectangle(120, 70, 56, 21));
			Configuration config = Main.main.configuration;
			outPort.setText("" + config.getMonomeSerialOSCOutPortNumber());
		}
		return outPort;
	}

	/**
	 * This method initializes saveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setBounds(new Rectangle(30, 100, 76, 21));
			saveButton.setText("Save");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					save();
				}
			});
		}
		return saveButton;
	}
	
	private void save() {
		int inport = Integer.parseInt(this.inPort.getText());
		int outport = Integer.parseInt(this.outPort.getText());
		String hostname = this.host.getText();
		
		Configuration config = Main.main.configuration;
		config.stopMonomeSerialOSC();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		config.setMonomeSerialOSCInPortNumber(inport);
		config.setMonomeSerialOSCOutPortNumber(outport);
		config.setMonomeHostname(hostname);
		config.startMonomeSerialOSC();
		
		MainGUI.getNewMonomeItem().setEnabled(true);
		
		this.dispose();
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setBounds(new Rectangle(120, 100, 76, 21));
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel();
				}
			});
		}
		return cancelButton;
	}
	
	private void cancel() {
		this.dispose();
	}

	/**
	 * This method initializes autoConfigButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAutoConfigButton() {
		if (autoConfigButton == null) {
			autoConfigButton = new JButton();
			autoConfigButton.setBounds(new Rectangle(30, 130, 166, 21));
			autoConfigButton.setText("Discover Monomes");
			autoConfigButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					discover();
				}
			});
		}
		return autoConfigButton;
	}
	
	private void discover() {
		int inport = Integer.parseInt(this.inPort.getText());
		int outport = Integer.parseInt(this.outPort.getText());
		String hostname = this.host.getText();
		
		Configuration config = Main.main.configuration;
		config.setMonomeSerialOSCInPortNumber(inport);
		config.setMonomeSerialOSCOutPortNumber(outport);
		config.setMonomeHostname(hostname);
		config.discoverMonomes();
		MainGUI.getNewMonomeItem().setEnabled(true);
		this.dispose();
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

