package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.monome.pages.pages.MIDISequencerPage;
import java.io.Serializable;

public class MIDISequencerGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	MIDISequencerPage page;
	private JLabel pageLabel = null;
	public JComboBox rowCB = null;
	private JLabel rowLBL = null;
	public JTextField noteTF = null;
	private JButton saveBtn = null;
	private JLabel bankSizeLBL = null;
	public JTextField bankSizeTF = null;
	private JLabel channelLBL = null;
	public JTextField channelTF = null;
	private JLabel holdModeLBL = null;
	private JCheckBox holdModeCB = null;
	private String[] rowChoices = {"Row 1", "Row 2", "Row 3", "Row 4", "Row 5", "Row 6",
			"Row 7", "Row 8", "Row 9", "Row 10", "Row 11", "Row 12", "Row 13", "Row 14",
			"Row 15", "Row 16"};
	public JComboBox quantCB = null;
	private JLabel quantLBL = null;

	/**
	 * This is the default constructor
	 * @param page 
	 */
	public MIDISequencerGUI(MIDISequencerPage page) {
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
		quantLBL = new JLabel();
		quantLBL.setBounds(new Rectangle(15, 135, 76, 16));
		quantLBL.setText("Quantization");
		channelLBL = new JLabel();
		channelLBL.setBounds(new Rectangle(35, 80, 51, 21));
		channelLBL.setText("Channel");
		bankSizeLBL = new JLabel();
		bankSizeLBL.setBounds(new Rectangle(30, 55, 56, 21));
		bankSizeLBL.setText("Bank Size");
		this.setSize(213, 213);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		this.add(getNoteTF(), null);
		this.add(getRowCB(), null);
		this.add(getRowLBL(), null);
		this.add(getSaveBtn(), null);
		this.add(bankSizeLBL, null);
		this.add(getBankSizeTF(), null);
		this.add(channelLBL, null);
		this.add(getChannelTF(), null);
		this.add(getHoldModeLBL(), null);
		this.add(getHoldModeCB(), null);
		setName("MIDI Sequencer");
		this.add(getQuantCB(), null);
		this.add(quantLBL, null);
		for (int i = 0; i < rowChoices.length; i++) {
			rowCB.addItem(rowChoices[i]);
		}
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
			pageLabel.setBounds(new Rectangle(5, 5, 166, 21));
		}
		return pageLabel;
	}

	/**
	 * This method initializes rowCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getRowCB() {
		if (rowCB == null) {
			rowCB = new JComboBox();
			rowCB.setBounds(new Rectangle(85, 30, 71, 21));
			rowCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = rowCB.getSelectedIndex();
					String noteVal = page.numberToMidiNote(page.noteNumbers[index]);
					noteTF.setText(noteVal);
				}
			});
		}
		return rowCB;
	}

	/**
	 * This method initializes rowLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getRowLBL() {
		if (rowLBL == null) {
			rowLBL = new JLabel();
			rowLBL.setText("Row");
			rowLBL.setBounds(new Rectangle(55, 30, 31, 21));
		}
		return rowLBL;
	}

	/**
	 * This method initializes noteTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNoteTF() {
		if (noteTF == null) {
			noteTF = new JTextField();
			noteTF.setText("C-1");
			noteTF.setBounds(new Rectangle(160, 30, 36, 21));
		}
		return noteTF;
	}

	/**
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton();
			saveBtn.setBounds(new Rectangle(22, 164, 151, 21));
			saveBtn.setText("Update Preferences");
			saveBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String midiNote = noteTF.getText();
					int index = rowCB.getSelectedIndex();
					page.noteNumbers[index] = page.noteToMidiNumber(midiNote);
					if (quantCB.getSelectedIndex() == 0) {
						page.quantization = 96;
					} else if (quantCB.getSelectedIndex() == 1) {
						page.quantization = 48;
					} else if (quantCB.getSelectedIndex() == 2) {
						page.quantization = 24;
					} else if (quantCB.getSelectedIndex() == 3) {
						page.quantization = 12;
					} else if (quantCB.getSelectedIndex() == 4) {
						page.quantization = 6;
					} else if (quantCB.getSelectedIndex() == 5) {
						page.quantization = 3;
					}
					try {
						int bankSize = Integer.parseInt(bankSizeTF.getText());
						if (bankSize < 1 || bankSize > 32) {
							bankSizeTF.setText(""+page.bankSize);
							return;
						}
						page.setBankSize(bankSize);
						int midiChannel = Integer.parseInt(channelTF.getText());
						if (midiChannel < 1 || midiChannel > 16) {
							bankSizeTF.setText(page.midiChannel);
							return;
						}
						page.setMidiChannel(channelTF.getText());
					} catch (NumberFormatException ex) {
						bankSizeTF.setText(""+page.bankSize);
						return;
					}
				}
			});
		}
		return saveBtn;
	}

	/**
	 * This method initializes bankSizeTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getBankSizeTF() {
		if (bankSizeTF == null) {
			bankSizeTF = new JTextField();
			bankSizeTF.setBounds(new Rectangle(85, 55, 31, 21));
		}
		return bankSizeTF;
	}

	/**
	 * This method initializes channelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChannelTF() {
		if (channelTF == null) {
			channelTF = new JTextField();
			channelTF.setBounds(new Rectangle(85, 80, 31, 21));
		}
		return channelTF;
	}

	/**
	 * This method initializes holdModeLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getHoldModeLBL() {
		if (holdModeLBL == null) {
			holdModeLBL = new JLabel();
			holdModeLBL.setText("Hold Mode");
			holdModeLBL.setBounds(new Rectangle(25, 105, 61, 21));
		}
		return holdModeLBL;
	}

	/**
	 * This method initializes holdModeCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getHoldModeCB() {
		if (holdModeCB == null) {
			holdModeCB = new JCheckBox();
			holdModeCB.setBounds(new Rectangle(85, 105, 21, 21));
		}
		return holdModeCB;
	}

	/**
	 * This method initializes quantCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getQuantCB() {
		if (quantCB == null) {
			quantCB = new JComboBox();
			quantCB.setBounds(new Rectangle(90, 130, 66, 25));
			quantCB.addItem("1 bar");
			quantCB.addItem("1/2");
			quantCB.addItem("1/4");
			quantCB.addItem("1/8");
			quantCB.addItem("1/16");
			quantCB.addItem("1/32");
		}
		return quantCB;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
