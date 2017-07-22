package org.monome.pages.pages.gui;

import javax.swing.JPanel;

import org.monome.pages.pages.MIDIGeneratorPage;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.JButton;

public class MIDIGeneratorGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private MIDIGeneratorPage page;
	private JLabel pageLabel = null;
	private JLabel scaleLbl = null;
	private JTextField scaleTF = null;
	private JLabel patternLengthLbl = null;
	private JTextField patternLengthTF = null;
	private JLabel quantizationLbl = null;
	private JTextField quantizationTF = null;
	private JLabel startNoteLbl = null;
	private JTextField startNoteTF = null;
	private JLabel maxNoteLbl = null;
	private JTextField maxNoteTF = null;
	private JLabel numNotesLbl = null;
	private JTextField numNotesTF = null;
	private JLabel chanceLbl = null;
	private JTextField chanceTF = null;
	private JLabel maxRadiusLbl = null;
	private JTextField maxRadiusTF = null;
	private JLabel midiChannelLbl = null;
	private JTextField midiChannelTF = null;
	private JButton updatePrefsBtn = null;

	/**
	 * This is the default constructor
	 */
	public MIDIGeneratorGUI(MIDIGeneratorPage page) {
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
		this.setSize(230, 330);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		setName("MIDI Generator Page");
		this.add(getScaleLbl(), null);
		this.add(getScaleTF(), null);
		this.add(getPatternLengthLbl(), null);
		this.add(getPatternLengthTF(), null);
		this.add(getQuantizationLbl(), null);
		this.add(getQuantizationTF(), null);
		this.add(getStartNoteLbl(), null);
		this.add(getStartNoteTF(), null);
		this.add(getMaxNoteLbl(), null);
		this.add(getMaxNoteTF(), null);
		this.add(getNumNotesLbl(), null);
		this.add(getNumNotesTF(), null);
		this.add(getChanceLbl(), null);
		this.add(getChanceTF(), null);
		this.add(getMaxRadiusLbl(), null);
		this.add(getMaxRadiusTF(), null);
		this.add(getMidiChannelLbl(), null);
		this.add(getMidiChannelTF(), null);
		this.add(getUpdatePrefsBtn(), null);
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
			pageLabel.setName("MIDI Generator Page");
			pageLabel.setBounds(new Rectangle(5, 5, 191, 21));
		}
		return pageLabel;
	}

	/**
	 * This method initializes scaleLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getScaleLbl() {
		if (scaleLbl == null) {
			scaleLbl = new JLabel();
			scaleLbl.setText("Scale");
			scaleLbl.setBounds(new Rectangle(10, 30, 86, 21));
		}
		return scaleLbl;
	}

	/**
	 * This method initializes scaleTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getScaleTF() {
		if (scaleTF == null) {
			scaleTF = new JTextField();
			scaleTF.setText(page.scale);
			scaleTF.setBounds(new Rectangle(100, 30, 86, 21));
		}
		return scaleTF;
	}

	/**
	 * This method initializes patternLengthLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getPatternLengthLbl() {
		if (patternLengthLbl == null) {
			patternLengthLbl = new JLabel();
			patternLengthLbl.setText("Pattern Len");
			patternLengthLbl.setBounds(new Rectangle(10, 55, 86, 21));
		}
		return patternLengthLbl;
	}

	/**
	 * This method initializes patternLengthTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getPatternLengthTF() {
		if (patternLengthTF == null) {
			patternLengthTF = new JTextField();
			patternLengthTF.setText(""+page.patternLength);
			patternLengthTF.setBounds(new Rectangle(100, 55, 86, 21));
		}
		return patternLengthTF;
	}

	/**
	 * This method initializes quantizationLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getQuantizationLbl() {
		if (quantizationLbl == null) {
			quantizationLbl = new JLabel();
			quantizationLbl.setText("Quantization");
			quantizationLbl.setBounds(new Rectangle(10, 80, 86, 21));
		}
		return quantizationLbl;
	}

	/**
	 * This method initializes quantizationTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getQuantizationTF() {
		if (quantizationTF == null) {
			quantizationTF = new JTextField();
			quantizationTF.setText(""+page.quantization);
			quantizationTF.setBounds(new Rectangle(100, 80, 86, 21));
		}
		return quantizationTF;
	}

	/**
	 * This method initializes startNoteLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getStartNoteLbl() {
		if (startNoteLbl == null) {
			startNoteLbl = new JLabel();
			startNoteLbl.setText("Start Note");
			startNoteLbl.setBounds(new Rectangle(10, 105, 86, 21));
		}
		return startNoteLbl;
	}

	/**
	 * This method initializes startNoteTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getStartNoteTF() {
		if (startNoteTF == null) {
			startNoteTF = new JTextField();
			startNoteTF.setText(""+page.startNote);
			startNoteTF.setBounds(new Rectangle(100, 105, 86, 21));
		}
		return startNoteTF;
	}

	/**
	 * This method initializes maxNoteLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMaxNoteLbl() {
		if (maxNoteLbl == null) {
			maxNoteLbl = new JLabel();
			maxNoteLbl.setText("Max Note");
			maxNoteLbl.setBounds(new Rectangle(10, 130, 86, 21));
		}
		return maxNoteLbl;
	}

	/**
	 * This method initializes maxNoteTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getMaxNoteTF() {
		if (maxNoteTF == null) {
			maxNoteTF = new JTextField();
			maxNoteTF.setText("" + page.maxNote);
			maxNoteTF.setBounds(new Rectangle(100, 130, 86, 21));
		}
		return maxNoteTF;
	}

	/**
	 * This method initializes numNotesLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getNumNotesLbl() {
		if (numNotesLbl == null) {
			numNotesLbl = new JLabel();
			numNotesLbl.setText("Num Notes");
			numNotesLbl.setBounds(new Rectangle(10, 155, 86, 21));
		}
		return numNotesLbl;
	}

	/**
	 * This method initializes numNotesTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getNumNotesTF() {
		if (numNotesTF == null) {
			numNotesTF = new JTextField();
			numNotesTF.setText(""+page.numNotes);
			numNotesTF.setBounds(new Rectangle(100, 155, 86, 21));
		}
		return numNotesTF;
	}

	/**
	 * This method initializes chanceLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getChanceLbl() {
		if (chanceLbl == null) {
			chanceLbl = new JLabel();
			chanceLbl.setText("Chance");
			chanceLbl.setBounds(new Rectangle(10, 180, 86, 21));
		}
		return chanceLbl;
	}

	/**
	 * This method initializes chanceTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getChanceTF() {
		if (chanceTF == null) {
			chanceTF = new JTextField();
			chanceTF.setText(""+page.chance);
			chanceTF.setBounds(new Rectangle(100, 180, 86, 21));
		}
		return chanceTF;
	}

	/**
	 * This method initializes maxRadiusLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMaxRadiusLbl() {
		if (maxRadiusLbl == null) {
			maxRadiusLbl = new JLabel();
			maxRadiusLbl.setText("Max Radius");
			maxRadiusLbl.setBounds(new Rectangle(10, 205, 86, 21));
		}
		return maxRadiusLbl;
	}

	/**
	 * This method initializes maxRadiusTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getMaxRadiusTF() {
		if (maxRadiusTF == null) {
			maxRadiusTF = new JTextField();
			maxRadiusTF.setText(""+page.maxRadius);
			maxRadiusTF.setBounds(new Rectangle(100, 205, 86, 21));
		}
		return maxRadiusTF;
	}

	/**
	 * This method initializes midiChannelLbl	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMidiChannelLbl() {
		if (midiChannelLbl == null) {
			midiChannelLbl = new JLabel();
			midiChannelLbl.setText("MIDI Channel");
			midiChannelLbl.setBounds(new Rectangle(10, 230, 86, 21));
		}
		return midiChannelLbl;
	}

	/**
	 * This method initializes midiChannelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getMidiChannelTF() {
		if (midiChannelTF == null) {
			midiChannelTF = new JTextField();
			midiChannelTF.setText(""+page.midiChannel);
			midiChannelTF.setBounds(new Rectangle(100, 230, 86, 21));
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
			updatePrefsBtn.setBounds(new Rectangle(15, 260, 161, 21));
			updatePrefsBtn.setText("Update Preferences");
			updatePrefsBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// scale
					String[] pieces = getScaleTF().getText().split(",");
					boolean invalid = false;
					for (int i = 0; i < pieces.length; i++) {
						try {
							Integer.parseInt(pieces[i]);
						} catch (NumberFormatException ex) {
							invalid = true;
							getScaleTF().setText(page.scale);
						}
					}
					if (!invalid) {
						page.scale = getScaleTF().getText();
						page.generateNoteMap();
					}
					
					// pattern len
					invalid = false;
					int patternLen = 0;
					try {
						patternLen = Integer.parseInt(getPatternLengthTF().getText());
						if (patternLen < 1) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.patternLength = patternLen;
					}

					// quantization
					invalid = false;
					int quantization = 0;
					try {
						quantization = Integer.parseInt(getQuantizationTF().getText());
						if (quantization < 1) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.quantization = quantization;
					}
					
					// start note
					invalid = false;
					int startNote = 0;
					try {
						startNote = Integer.parseInt(getStartNoteTF().getText());
						if (startNote < 0 || startNote > 127) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.startNote = startNote;
					}

					// max note
					invalid = false;
					int maxNote = 0;
					try {
						maxNote = Integer.parseInt(getMaxNoteTF().getText());
						if (maxNote < 1 || maxNote > 127) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.maxNote = maxNote;
					}
					
					// num notes
					invalid = false;
					int numNotes = 0;
					try {
						numNotes = Integer.parseInt(getNumNotesTF().getText());
						if (numNotes < 0 || numNotes > 127) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.numNotes = numNotes;
					}

					// chance
					invalid = false;
					int chance = 0;
					try {
						chance = Integer.parseInt(getChanceTF().getText());
						if (chance < 0 || chance > 127) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.chance = chance;
					}
					
					// max radius
					invalid = false;
					int maxRadius = 0;
					try {
						maxRadius = Integer.parseInt(getMaxRadiusTF().getText());
						if (maxRadius < 0 || maxRadius > 127) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.maxRadius = maxRadius;
					}

					// midi channel
					invalid = false;
					int midiChannel = 0;
					try {
						midiChannel = Integer.parseInt(getMidiChannelTF().getText());
						if (midiChannel < 1 || midiChannel > 16) {
							invalid = true;
						}
					} catch (NumberFormatException ex) {
						invalid = true;
					}
					if (!invalid) {
						page.midiChannel = midiChannel;
					}

				}
			});
		}
		return updatePrefsBtn;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
