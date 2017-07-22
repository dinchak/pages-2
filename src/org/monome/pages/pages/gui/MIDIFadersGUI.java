package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.JButton;

import org.monome.pages.pages.MIDIFadersPage;
import javax.swing.JCheckBox;

public class MIDIFadersGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private MIDIFadersPage page;
	private JLabel pageLabel = null;
	private JTextField delayTF = null;
	private JLabel delayLBL = null;
	private JLabel ccOffsetLBL = null;
	private JTextField ccOffsetTF = null;
	private JLabel channelLBL = null;
	private JTextField channelTF = null;
	private JButton updatePrefsBtn = null;
	private JCheckBox horizontalCB = null;
	private JLabel horizontalLbl = null;

	/**
	 * This is the default constructor
	 */
	public MIDIFadersGUI(MIDIFadersPage page) {
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
		horizontalLbl = new JLabel();
		horizontalLbl.setBounds(new Rectangle(40, 105, 136, 21));
		horizontalLbl.setText("Horizontal Mode");
		this.setSize(205, 176);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		setName("MIDI Faders Page");
		this.add(getDelayTF(), null);
		this.add(getDelayLBL(), null);
		this.add(getCcOffsetLBL(), null);
		this.add(getCcOffsetTF(), null);
		this.add(getChannelLBL(), null);
		this.add(getChannelTF(), null);
		this.add(getUpdatePrefsBtn(), null);
		this.add(getHorizontalCB(), null);
		this.add(horizontalLbl, null);
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
			pageLabel.setBounds(new Rectangle(5, 5, 181, 21));
		}
		return pageLabel;
	}

	/**
	 * This method initializes delayTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getDelayTF() {
		if (delayTF == null) {
			delayTF = new JTextField();
			delayTF.setBounds(new Rectangle(80, 30, 41, 21));
		}
		return delayTF;
	}

	/**
	 * This method initializes delayLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getDelayLBL() {
		if (delayLBL == null) {
			delayLBL = new JLabel();
			delayLBL.setText("Delay");
			delayLBL.setBounds(new Rectangle(15, 30, 61, 21));
		}
		return delayLBL;
	}

	/**
	 * This method initializes ccOffsetLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getCcOffsetLBL() {
		if (ccOffsetLBL == null) {
			ccOffsetLBL = new JLabel();
			ccOffsetLBL.setText("CC Offset");
			ccOffsetLBL.setBounds(new Rectangle(15, 55, 61, 21));
		}
		return ccOffsetLBL;
	}

	/**
	 * This method initializes ccOffsetTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getCcOffsetTF() {
		if (ccOffsetTF == null) {
			ccOffsetTF = new JTextField();
			ccOffsetTF.setBounds(new Rectangle(80, 55, 41, 20));
		}
		return ccOffsetTF;
	}

	/**
	 * This method initializes channelLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getChannelLBL() {
		if (channelLBL == null) {
			channelLBL = new JLabel();
			channelLBL.setText("Channel");
			channelLBL.setBounds(new Rectangle(15, 80, 61, 21));
		}
		return channelLBL;
	}

	/**
	 * This method initializes channelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getChannelTF() {
		if (channelTF == null) {
			channelTF = new JTextField();
			channelTF.setBounds(new Rectangle(80, 80, 41, 20));
		}
		return channelTF;
	}

	/**
	 * This method initializes updatePrefsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpdatePrefsBtn() {
		if (updatePrefsBtn == null) {
			updatePrefsBtn = new JButton();
			updatePrefsBtn.setBounds(new Rectangle(10, 135, 165, 23));
			updatePrefsBtn.setText("Update Preferences");
			updatePrefsBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					page.setDelayAmount(getDelayTF().getText());
					page.setCCOffset(getCcOffsetTF().getText());
					page.setMidiChannel(getChannelTF().getText());
					page.setHorizontal(getHorizontalCB().isSelected());
					page.redrawDevice();
				}
			});
		}
		return updatePrefsBtn;
	}

	/**
	 * This method initializes horizontalCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getHorizontalCB() {
		if (horizontalCB == null) {
			horizontalCB = new JCheckBox();
			horizontalCB.setBounds(new Rectangle(15, 105, 21, 21));
		}
		return horizontalCB;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
