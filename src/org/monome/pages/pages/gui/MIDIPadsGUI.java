package org.monome.pages.pages.gui;

import javax.swing.JPanel;

import org.monome.pages.pages.MIDIPadsPage;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.JButton;

public class MIDIPadsGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private JLabel pageLabel = null;
	
	/**
	 * The MIDIPadsPage this GUI is attached to
	 */
	private MIDIPadsPage page;
	private JLabel midiStartLBL = null;
	private JTextField midiStartTF = null;
	private JLabel velocityFactorLBL = null;
	private JTextField velocityFactorTF = null;
	private JLabel delayTimeLBL = null;
	private JTextField delayTimeTF = null;
	private JLabel midiChannelLBL = null;
	private JTextField midiChannelTF = null;
	private JButton updatePrefsBtn = null;

	/**
	 * This is the default constructor
	 * @param padsPage 
	 * @param padsPage 
	 */
	public MIDIPadsGUI(MIDIPadsPage padsPage) {
		super();
		this.page = padsPage;
		initialize();
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		pageLabel = new JLabel();
		pageLabel.setBounds(new Rectangle(10, 10, 161, 21));
		pageLabel.setText("MIDI Pads Page");
		setName("MIDI Pads Page");
		this.setSize(181, 181);
		this.setLayout(null);
		this.add(pageLabel, null);
		this.add(getMidiStartLBL(), null);
		this.add(getMidiStartTF(), null);
		this.add(getVelocityFactorLBL(), null);
		this.add(getVelocityFactorTF(), null);
		this.add(getDelayTimeLBL(), null);
		this.add(getDelayTimeTF(), null);
		this.add(getMidiChannelLBL(), null);
		this.add(getMidiChannelTF(), null);
		this.add(getUpdatePrefsBtn(), null);
	}

	/**
	 * This method initializes midiStartLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMidiStartLBL() {
		if (midiStartLBL == null) {
			midiStartLBL = new JLabel();
			midiStartLBL.setText("MIDI Start Note");
			midiStartLBL.setBounds(new Rectangle(15, 40, 101, 21));
		}
		return midiStartLBL;
	}

	/**
	 * This method initializes midiStartTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMidiStartTF() {
		if (midiStartTF == null) {
			midiStartTF = new JTextField();
			midiStartTF.setBounds(new Rectangle(120, 40, 36, 21));
		}
		return midiStartTF;
	}

	/**
	 * This method initializes velocityFactorLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getVelocityFactorLBL() {
		if (velocityFactorLBL == null) {
			velocityFactorLBL = new JLabel();
			velocityFactorLBL.setText("Velocity Factor");
			velocityFactorLBL.setBounds(new Rectangle(15, 65, 101, 21));
		}
		return velocityFactorLBL;
	}

	/**
	 * This method initializes velocityFactorTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getVelocityFactorTF() {
		if (velocityFactorTF == null) {
			velocityFactorTF = new JTextField();
			velocityFactorTF.setBounds(new Rectangle(120, 65, 36, 21));
		}
		return velocityFactorTF;
	}

	/**
	 * This method initializes delayTimeLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getDelayTimeLBL() {
		if (delayTimeLBL == null) {
			delayTimeLBL = new JLabel();
			delayTimeLBL.setText("Delay Time");
			delayTimeLBL.setBounds(new Rectangle(15, 90, 101, 21));
		}
		return delayTimeLBL;
	}

	/**
	 * This method initializes delayTimeTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getDelayTimeTF() {
		if (delayTimeTF == null) {
			delayTimeTF = new JTextField();
			delayTimeTF.setBounds(new Rectangle(120, 90, 36, 21));
		}
		return delayTimeTF;
	}

	/**
	 * This method initializes midiChannelLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMidiChannelLBL() {
		if (midiChannelLBL == null) {
			midiChannelLBL = new JLabel();
			midiChannelLBL.setText("MIDI Channel");
			midiChannelLBL.setBounds(new Rectangle(15, 115, 101, 21));
		}
		return midiChannelLBL;
	}

	/**
	 * This method initializes midiChannelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMidiChannelTF() {
		if (midiChannelTF == null) {
			midiChannelTF = new JTextField();
			midiChannelTF.setBounds(new Rectangle(120, 115, 36, 21));
		}
		return midiChannelTF;
	}

	/**
	 * This method initializes updatePrefsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpdatePrefsBtn() {
		if (updatePrefsBtn == null) {
			updatePrefsBtn = new JButton();
			updatePrefsBtn.setBounds(new Rectangle(10, 145, 161, 21));
			updatePrefsBtn.setText("Update Preferences");
			updatePrefsBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						int midiStartNote = Integer.parseInt(midiStartTF.getText()); 
						int velocityFactor = Integer.parseInt(velocityFactorTF.getText());
						int delayTime = Integer.parseInt(delayTimeTF.getText());
						int midiChannel = Integer.parseInt(midiChannelTF.getText()) - 1;
						if (midiChannel < 0 || midiChannel > 15) {
							midiChannel = 0;
						}
						page.setMidiStartNote(midiStartNote);
						page.setVelocityFactor(velocityFactor);
						page.setDelayTime(delayTime);
						page.setMidiChannel(midiChannel);
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		return updatePrefsBtn;
	}

	public void setMidiStartNote(int midiStartNote) {
		midiStartTF.setText("" + midiStartNote);
	}

	public void setDelayTime(int delayTime) {
		delayTimeTF.setText("" + delayTime);
	}

	public void setMidiChannel(int midiChannel) {
		midiChannelTF.setText("" + midiChannel);
	}

	public void setVelocityFactor(int velocityFactor) {
		velocityFactorTF.setText("" + velocityFactor);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
