package org.monome.pages.pages.gui;

import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.pages.AbletonSceneLauncherPage;

import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JButton;

public class AbletonSceneLauncherGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private AbletonSceneLauncherPage page = null;
	private JLabel pageLabel = null;
	private JCheckBox disableMuteCB = null;
	private JLabel disableMuteLabel = null;
	private JCheckBox disableSoloCB = null;
	private JLabel disableSoloLabel = null;
	private JCheckBox disableArmCB = null;
	private JLabel disableArmLabel = null;
	private JButton refreshButton = null;
	private JCheckBox disableStopCB = null;
	private JLabel disableStopLabel = null;
	/**
	 * This is the default constructor
	 */
	public AbletonSceneLauncherGUI(AbletonSceneLauncherPage page) {
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
		disableStopLabel = new JLabel();
		disableStopLabel.setBounds(new Rectangle(40, 105, 121, 21));
		disableStopLabel.setText("Disable Stop");
		disableArmLabel = new JLabel();
		disableArmLabel.setBounds(new Rectangle(40, 80, 121, 21));
		disableArmLabel.setText("Disable Arm");
		disableSoloLabel = new JLabel();
		disableSoloLabel.setBounds(new Rectangle(40, 55, 121, 21));
		disableSoloLabel.setText("Disable Solo");
		disableMuteLabel = new JLabel();
		disableMuteLabel.setBounds(new Rectangle(40, 30, 121, 21));
		disableMuteLabel.setText("Disable Mute");
		pageLabel = new JLabel();
		setName("Ableton Scene Launcher");
		pageLabel.setBounds(new Rectangle(5, 5, 196, 21));
		this.setLayout(null);
		this.add(pageLabel, null);
		this.add(getDisableMuteCB(), null);
		this.add(disableMuteLabel, null);
		this.add(getDisableSoloCB(), null);
		this.add(disableSoloLabel, null);
		this.add(getDisableArmCB(), null);
		this.add(disableArmLabel, null);
		this.add(getRefreshButton(), null);
		this.add(getDisableStopCB(), null);
		this.add(disableStopLabel, null);
		this.setSize(220, 215);
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}
	
	/**
	 * This method initializes disableMuteCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDisableMuteCB() {
		if (disableMuteCB == null) {
			disableMuteCB = new JCheckBox();
			disableMuteCB.setBounds(new Rectangle(15, 30, 21, 21));
			disableMuteCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (disableMuteCB.isSelected()) {
						page.numEnabledRows--;
					} else {
						page.numEnabledRows++;
					}
					page.redrawDevice();
				}
			});

		}
		return disableMuteCB;
	}

	/**
	 * This method initializes disableSoloCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDisableSoloCB() {
		if (disableSoloCB == null) {
			disableSoloCB = new JCheckBox();
			disableSoloCB.setBounds(new Rectangle(15, 55, 21, 21));
			disableSoloCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (disableSoloCB.isSelected()) {
						page.numEnabledRows--;
					} else {
						page.numEnabledRows++;
					}
					page.redrawDevice();
				}
			});
		}
		return disableSoloCB;
	}

	/**
	 * This method initializes disableArmCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDisableArmCB() {
		if (disableArmCB == null) {
			disableArmCB = new JCheckBox();
			disableArmCB.setBounds(new Rectangle(15, 80, 21, 21));
			disableArmCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (disableArmCB.isSelected()) {
						page.numEnabledRows--;
					} else {
						page.numEnabledRows++;
					}
					page.redrawDevice();
				}
			});
		}
		return disableArmCB;
	}

	/**
	 * This method initializes refreshButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setBounds(new Rectangle(15, 135, 146, 21));
			refreshButton.setText("Refresh From Ableton");
			refreshButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Main.main.configuration.getAbletonControl().refreshAbleton();
				}
			});
		}
		return refreshButton;
	}

	/**
	 * This method initializes disableStopCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDisableStopCB() {
		if (disableStopCB == null) {
			disableStopCB = new JCheckBox();
			disableStopCB.setBounds(new Rectangle(15, 105, 21, 21));
			disableStopCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (disableStopCB.isSelected()) {
						page.numEnabledRows--;
					} else {
						page.numEnabledRows++;
					}
					page.redrawDevice();
				}
			});
		}
		return disableStopCB;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
