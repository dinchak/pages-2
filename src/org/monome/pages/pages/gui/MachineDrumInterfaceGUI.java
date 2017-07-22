package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Rectangle;
import javax.swing.JLabel;

import org.monome.pages.pages.MachineDrumInterfacePage;
import javax.swing.JButton;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;

public class MachineDrumInterfaceGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private MachineDrumInterfacePage page = null;
	private JTextField speedTF = null;
	private JLabel speedLBL = null;
	private JLabel pageLabel = null;
	private JButton updatePrefsBtn = null;
	public JComboBox mod1CB = null;
	public String[] options = {"Pattern Sequencer", "Kit Randomizer", "Kit Editor", "LFO Editor"};
	public JComboBox mod2CB = null;
	public JComboBox mod3CB = null;
	public JComboBox mod4CB = null;
	public JCheckBox syncCB = null;
	private JLabel syncLbl = null;

	/**
	 * This is the default constructor
	 */
	public MachineDrumInterfaceGUI(MachineDrumInterfacePage page) {
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
		syncLbl = new JLabel();
		syncLbl.setBounds(new Rectangle(40, 175, 126, 26));
		syncLbl.setText("Echo MIDI");
		this.setLayout(null);
		this.add(getPageLabel(), null);
		setName("MachineDrum Interface Page");
		this.setSize(184, 240);
		this.add(getSpeedTF(), null);
		this.add(getSpeedLBL(), null);
		this.add(getUpdatePrefsBtn(), null);
		this.add(getMod1CB(), null);
		this.add(getMod2CB(), null);
		this.add(getMod3CB(), null);
		this.add(getMod4CB(), null);
		this.add(getSyncCB(), null);
		this.add(syncLbl, null);
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}

	/**
	 * This method initializes pageLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getPageLabel() {
		if (pageLabel == null) {
			pageLabel = new JLabel();			
			pageLabel.setBounds(new Rectangle(5, 5, 171, 16));
		}
		return pageLabel;
	}

	/**
	 * This method initializes speedTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getSpeedTF() {
		if (speedTF == null) {
			speedTF = new JTextField();
			speedTF.setBounds(new Rectangle(65, 25, 51, 21));
			speedTF.setText(""+page.getSpeed());
		}
		return speedTF;
	}

	/**
	 * This method initializes speedLBL1	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getSpeedLBL() {
		if (speedLBL == null) {
			speedLBL = new JLabel();
			speedLBL.setText("Speed");
			speedLBL.setBounds(new Rectangle(15, 25, 46, 21));
		}
		return speedLBL;
	}

	/**
	 * This method initializes updatePrefsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpdatePrefsBtn() {
		if (updatePrefsBtn == null) {
			updatePrefsBtn = new JButton();
			updatePrefsBtn.setBounds(new Rectangle(15, 210, 148, 21));
			updatePrefsBtn.setText("Update Preferences");
			updatePrefsBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						int val = Integer.parseInt(getSpeedTF().getText());
						if (val >= 10 && val <= 500) {
							page.setSpeed(val);
						} else {
							getSpeedTF().setText("" + page.getSpeed());
						}
					} catch (NumberFormatException ex) {
						getSpeedTF().setText("" + page.getSpeed());
					}
					
					page.updateModulePrefs();
				}
			});
		}
		return updatePrefsBtn;
	}

	/**
	 * This method initializes mod1CB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getMod1CB() {
		if (mod1CB == null) {
			mod1CB = new JComboBox(options);
			mod1CB.setBounds(new Rectangle(10, 55, 161, 25));
		}
		return mod1CB;
	}

	/**
	 * This method initializes mod2CB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getMod2CB() {
		if (mod2CB == null) {
			mod2CB = new JComboBox(options);
			mod2CB.setBounds(new Rectangle(10, 85, 161, 25));
		}
		return mod2CB;
	}

	/**
	 * This method initializes mod3CB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getMod3CB() {
		if (mod3CB == null) {
			mod3CB = new JComboBox(options);
			mod3CB.setBounds(new Rectangle(10, 115, 161, 25));
		}
		return mod3CB;
	}

	/**
	 * This method initializes mod4CB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getMod4CB() {
		if (mod4CB == null) {
			mod4CB = new JComboBox(options);
			mod4CB.setBounds(new Rectangle(10, 145, 161, 25));
		}
		return mod4CB;
	}

	/**
	 * This method initializes syncCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSyncCB() {
		if (syncCB == null) {
			syncCB = new JCheckBox();
			syncCB.setBounds(new Rectangle(15, 175, 21, 26));
			syncCB.setSelected(true);
		}
		return syncCB;
	}} //  @jve:decl-index=0:visual-constraint="10,10"
