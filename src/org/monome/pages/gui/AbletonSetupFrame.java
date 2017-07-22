package org.monome.pages.gui;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JTextField;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonControl;
import org.monome.pages.configuration.Configuration;
import javax.swing.JCheckBox;

public class AbletonSetupFrame extends JInternalFrame {

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
	private JButton refreshButton = null;
	private JLabel ignoreViewLabel = null;
	private JCheckBox ignoreViewCB = null;
	/**
	 * This is the xxx default constructor
	 */
	public AbletonSetupFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(231, 256);
		this.setTitle("Ableton");
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
			ignoreViewLabel = new JLabel();
			ignoreViewLabel.setBounds(new Rectangle(25, 100, 126, 21));
			ignoreViewLabel.setText("Ignore View Track");
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
			jContentPane.add(getRefreshButton(), null);
			jContentPane.add(ignoreViewLabel, null);
			jContentPane.add(getIgnoreViewCB(), null);
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
			host.setText(config.getAbletonHostname());
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
			inPort.setText("" + config.getAbletonOSCInPortNumber());
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
			outPort.setText("" + config.getAbletonOSCOutPortNumber());
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
			saveButton.setBounds(new Rectangle(30, 130, 76, 21));
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
		config.setAbletonOSCInPortNumber(inport);
		config.setAbletonOSCOutPortNumber(outport);
		config.setAbletonHostname(hostname);
		config.setAbletonIgnoreViewTrack(this.ignoreViewCB.isSelected());
		
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
			cancelButton.setBounds(new Rectangle(120, 130, 76, 21));
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
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setBounds(new Rectangle(30, 160, 166, 21));
			refreshButton.setText("Refresh Ableton");
			refreshButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					refresh();
				}
			});
		}
		return refreshButton;
	}
	
	private void refresh() {
		AbletonControl control =
			Main.main.configuration.getAbletonControl();
		if (control != null) {
			control.refreshAbleton();
		}
	}

	/**
	 * This method initializes ignoreViewCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getIgnoreViewCB() {
		if (ignoreViewCB == null) {
			ignoreViewCB = new JCheckBox();
			ignoreViewCB.setBounds(new Rectangle(155, 100, 21, 21));
			Configuration config = Main.main.configuration;
			ignoreViewCB.setSelected(config.getAbletonIgnoreViewTrack());
		}
		return ignoreViewCB;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
