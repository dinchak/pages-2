package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;

import org.monome.pages.pages.ExternalApplicationPage;

import java.awt.Rectangle;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.io.Serializable;

public class ExternalApplicationGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private JLabel pageLabel = null;
	private ExternalApplicationPage page = null;
	private JLabel oscInLabel = null;
	public JTextField oscInTF = null;
	private JLabel oscOutLabel = null;
	public JTextField oscOutTF = null;
	private JLabel oscHostnameLabel = null;
	public JTextField oscHostnameTF = null;
	private JLabel oscPrefixLabel = null;
	public JTextField oscPrefixTF = null;
	private JCheckBox disableLedCacheCB = null;
	private JLabel disableLedCacheLabel = null;
	private JButton updatePreferencesButton = null;
	private JCheckBox ignorePrefixCB = null;
	private JLabel ignorePrefixLbl = null;

	/**
	 * This is the default constructor
	 */
	public ExternalApplicationGUI(ExternalApplicationPage page) {
		super();
		this.page = page;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		disableLedCacheLabel = new JLabel();
		disableLedCacheLabel.setBounds(new Rectangle(75, 155, 121, 21));
		disableLedCacheLabel.setText("Disable LED Cache");
		oscPrefixLabel = new JLabel();
		oscPrefixLabel.setBounds(new Rectangle(15, 35, 111, 21));
		oscPrefixLabel.setText("OSC Prefix");
		oscHostnameLabel = new JLabel();
		oscHostnameLabel.setBounds(new Rectangle(15, 65, 110, 21));
		oscHostnameLabel.setText("OSC Hostname");
		oscOutLabel = new JLabel();
		oscOutLabel.setBounds(new Rectangle(15, 125, 111, 21));
		oscOutLabel.setText("OSC Out Port");
		oscInLabel = new JLabel();
		oscInLabel.setBounds(new Rectangle(15, 95, 111, 21));
		oscInLabel.setText("OSC In Port");
		pageLabel = new JLabel();
		setName("External Application");
		pageLabel.setBounds(new Rectangle(5, 5, 291, 21));
		this.setSize(289, 283);
		this.setLayout(null);
		this.add(pageLabel, null);
		this.add(oscInLabel, null);
		this.add(getOscInTF(), null);
		this.add(oscOutLabel, null);
		this.add(getOscOutTF(), null);
		this.add(oscHostnameLabel, null);
		this.add(getOscHostnameTF(), null);
		this.add(oscPrefixLabel, null);
		this.add(getOscPrefixTF(), null);
		this.add(getDisableLedCacheCB(), null);
		this.add(disableLedCacheLabel, null);
		this.add(getUpdatePreferencesButton(), null);
		this.add(getIgnorePrefixCB(), null);
		this.add(getIgnorePrefixLbl(), null);
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}

	/**
	 * This method initializes oscInTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOscInTF() {
		if (oscInTF == null) {
			oscInTF = new JTextField();
			oscInTF.setText(""+page.inPort);
			oscInTF.setBounds(new Rectangle(125, 95, 76, 21));
		}
		return oscInTF;
	}

	/**
	 * This method initializes oscOutTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOscOutTF() {
		if (oscOutTF == null) {
			oscOutTF = new JTextField();
			oscOutTF.setText(""+page.outPort);
			oscOutTF.setBounds(new Rectangle(125, 125, 76, 21));
		}
		return oscOutTF;
	}

	/**
	 * This method initializes oscHostnameTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOscHostnameTF() {
		if (oscHostnameTF == null) {
			oscHostnameTF = new JTextField();
			oscHostnameTF.setText("localhost");
			oscHostnameTF.setBounds(new Rectangle(125, 65, 76, 21));
		}
		return oscHostnameTF;
	}

	/**
	 * This method initializes oscPrefixTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getOscPrefixTF() {
		if (oscPrefixTF == null) {
			oscPrefixTF = new JTextField();
			oscPrefixTF.setText("/mlr");
			oscPrefixTF.setBounds(new Rectangle(125, 35, 76, 21));
		}
		return oscPrefixTF;
	}

	/**
	 * This method initializes disableLedCacheCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDisableLedCacheCB() {
		if (disableLedCacheCB == null) {
			disableLedCacheCB = new JCheckBox();
			disableLedCacheCB.setBounds(new Rectangle(50, 155, 21, 21));
		}
		return disableLedCacheCB;
	}

	/**
	 * This method initializes updatePreferencesButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpdatePreferencesButton() {
		if (updatePreferencesButton == null) {
			updatePreferencesButton = new JButton();
			updatePreferencesButton.setBounds(new Rectangle(30, 205, 166, 25));
			updatePreferencesButton.setText("Update Preferences");
			updatePreferencesButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					page.stopOSC();
					page.setHostname(oscHostnameTF.getText());
					page.setPrefix(oscPrefixTF.getText());
					page.setInPort(oscInTF.getText());
					page.setOutPort(oscOutTF.getText());
					page.initOSC();
				}
			});
		}
		return updatePreferencesButton;
	}

	public void setCacheDisabled(String cacheDisabled) {
		if (cacheDisabled.compareTo("true") == 0) {
			disableLedCacheCB.setSelected(true);
		} else {
			disableLedCacheCB.setSelected(false);
		}
	}
	
	public void setIgnorePrefix(String ignorePrefix) {
		if (ignorePrefix == null) {
			return;
		}
		if (ignorePrefix.compareTo("true") == 0) {
			ignorePrefixCB.setSelected(true);
		} else {
			ignorePrefixCB.setSelected(false);
		}
	}

	/**
	 * This method initializes ignorePrefixCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getIgnorePrefixCB() {
		if (ignorePrefixCB == null) {
			ignorePrefixCB = new JCheckBox();
			ignorePrefixCB.setBounds(new Rectangle(50, 175, 21, 21));
		}
		return ignorePrefixCB;
	}

	/**
	 * This method initializes ignorePrefixLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getIgnorePrefixLbl() {
		if (ignorePrefixLbl == null) {
			ignorePrefixLbl = new JLabel();
			ignorePrefixLbl.setText("Ignore /sys messages");
			ignorePrefixLbl.setBounds(new Rectangle(75, 175, 160, 21));
		}
		return ignorePrefixLbl;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
