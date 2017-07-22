package org.monome.pages.pages.gui;

import javax.swing.ButtonGroup;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JComboBox;

import org.monome.pages.pages.MIDITriggersPage;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import java.io.Serializable;

import javax.swing.JTextField;

public class MIDITriggersGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	MIDITriggersPage page;
	private JLabel pageLabel = null;
	private JComboBox rowColCB = null;
	private JLabel rowColLBL = null;
	private String[] colChoices = {"Col 1", "Col 2", "Col 3", "Col 4", "Col 5", "Col 6",
			"Col 7", "Col 8", "Col 9", "Col 10", "Col 11", "Col 12", "Col 13", "Col 14",
			"Col 15", "Col 16"};
	private String[] rowChoices = {"Row 1", "Row 2", "Row 3", "Row 4", "Row 5", "Row 6",
			"Row 7", "Row 8", "Row 9", "Row 10", "Row 11", "Row 12", "Row 13", "Row 14",
			"Row 15", "Row 16"};
	public JRadioButton rowRB = null;
	private JLabel rowLBL = null;
	public JRadioButton colRB = null;
	private JLabel colLBL = null;
	public JCheckBox onAndOffCB = null;
	private JLabel onAndOffLBL = null;
	private JComboBox modeCB = null;
	private JLabel modeLBL = null;
	private boolean ignoreModeCB = false;
	public JCheckBox ccCB = null;
	private JLabel ccLbl = null;
	private JTextField velocityTF = null;
	private JLabel velocityLBL = null;
	/**
	 * This is the default constructor
	 */
	public MIDITriggersGUI(MIDITriggersPage page) {
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
		velocityLBL = new JLabel();
		velocityLBL.setBounds(new Rectangle(95, 185, 96, 21));
		velocityLBL.setText("Velocity");
		ccLbl = new JLabel();
		ccLbl.setBounds(new Rectangle(60, 160, 131, 21));
		ccLbl.setText("MIDI CC Mode");
		modeLBL = new JLabel();
		modeLBL.setBounds(new Rectangle(10, 105, 33, 26));
		modeLBL.setText("Mode");
		pageLabel = new JLabel();
		pageLabel.setBounds(new Rectangle(5, 5, 186, 21));
		this.setSize(213, 222);
		this.setLayout(null);
		this.add(pageLabel, null);
		setName("MIDI Triggers Page");
		this.add(getRowColCB(), null);
		this.add(getRowColLBL(), null);
		this.add(getRowRB(), null);
		this.add(getRowLBL(), null);
		this.add(getColRB(), null);
		this.add(getColLBL(), null);
		this.add(getOnAndOffCB(), null);
		this.add(getOnAndOffLBL(), null);
		this.add(getModeCB(), null);
		this.add(modeLBL, null);
		this.add(getCcCB(), null);
		this.add(ccLbl, null);
		this.add(getVelocityTF(), null);
		this.add(velocityLBL, null);
		ButtonGroup colRowBG = new ButtonGroup();
		colRowBG.add(getRowRB());
		colRowBG.add(getColRB());
		rowRB.doClick();
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}
		
	private void colMode() {
		rowColCB.removeAllItems();
		for (int i = 0; i < colChoices.length; i++) {
			rowColCB.addItem(colChoices[i]);
		}
		rowColLBL.setText("Col");
		page.redrawDevice();
	}
	
	private void rowMode() {
		rowColCB.removeAllItems();
		for (int i = 0; i < rowChoices.length; i++) {
			rowColCB.addItem(rowChoices[i]);
		}
		rowColLBL.setText("Row");
		page.redrawDevice();
	}

	/**
	 * This method initializes rowColCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getRowColCB() {
		if (rowColCB == null) {
			rowColCB = new JComboBox();
			rowColCB.setBounds(new Rectangle(45, 30, 96, 21));
			rowColCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String label = (String) rowColCB.getSelectedItem();
					if (label == null) {
						return;
					}
					String[] pieces;
					if (rowRB.isSelected()) {
						pieces = label.split("Row ");
					} else {
						pieces = label.split("Col ");
					}
					int index = Integer.parseInt(pieces[1]) - 1;
					onAndOffCB.setSelected(page.onAndOff[index]);
					ccCB.setSelected(page.ccMode[index]);
					ignoreModeCB = true;
					modeCB.setSelectedIndex(page.mode[index]);
					velocityTF.setText("" + page.velocity[index]);
				}
			});
		}
		return rowColCB;
	}

	/**
	 * This method initializes rowColLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getRowColLBL() {
		if (rowColLBL == null) {
			rowColLBL = new JLabel();
			rowColLBL.setText("Row");
			rowColLBL.setBounds(new Rectangle(20, 30, 26, 21));
		}
		return rowColLBL;
	}

	/**
	 * This method initializes rowRB	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRowRB() {
		if (rowRB == null) {
			rowRB = new JRadioButton();
			rowRB.setBounds(new Rectangle(45, 60, 21, 21));
			rowRB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					rowMode();
				}
			});
		}
		return rowRB;
	}

	/**
	 * This method initializes rowLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getRowLBL() {
		if (rowLBL == null) {
			rowLBL = new JLabel();
			rowLBL.setText("Rows");
			rowLBL.setBounds(new Rectangle(70, 60, 46, 21));
		}
		return rowLBL;
	}

	/**
	 * This method initializes colRB	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getColRB() {
		if (colRB == null) {
			colRB = new JRadioButton();
			colRB.setBounds(new Rectangle(45, 80, 21, 21));
			colRB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					colMode();
				}
			});
		}
		return colRB;
	}

	/**
	 * This method initializes colLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getColLBL() {
		if (colLBL == null) {
			colLBL = new JLabel();
			colLBL.setText("Cols");
			colLBL.setBounds(new Rectangle(70, 80, 46, 21));
		}
		return colLBL;
	}

	/**
	 * This method initializes onAndOffCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOnAndOffCB() {
		if (onAndOffCB == null) {
			onAndOffCB = new JCheckBox();
			onAndOffCB.setBounds(new Rectangle(35, 135, 21, 21));
			onAndOffCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String label = (String) rowColCB.getSelectedItem();
					if (label == null) {
						return;
					}
					String[] pieces;
					if (rowRB.isSelected()) {
						pieces = label.split("Row ");
					} else {
						pieces = label.split("Col ");
					}
					int index = Integer.parseInt(pieces[1]) - 1;
					page.onAndOff[index] = onAndOffCB.isSelected();
				}
			});
		}
		return onAndOffCB;
	}

	/**
	 * This method initializes onAndOffLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getOnAndOffLBL() {
		if (onAndOffLBL == null) {
			onAndOffLBL = new JLabel();
			
			onAndOffLBL.setText("Toggles On And Off");
			onAndOffLBL.setBounds(new Rectangle(60, 135, 131, 21));
		}
		return onAndOffLBL;
	}

	/**
	 * This method initializes modeCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getModeCB() {
		if (modeCB == null) {
			modeCB = new JComboBox();
			modeCB.setBounds(new Rectangle(45, 105, 151, 25));
			modeCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (ignoreModeCB == true) {
						ignoreModeCB = false;
						return;
					}
					String mode = (String) modeCB.getSelectedItem();
					int index = getRowColIndex();
					if (mode.compareTo("Triggers") == 0) {
						page.mode[index] = page.MODE_TRIGGERS;
					} else if (mode.compareTo("Toggles") == 0) {
						page.mode[index] = page.MODE_TOGGLES;
					} else if (mode.compareTo("Ableton Clip") == 0) {
						page.mode[index] = page.MODE_CLIP_OVERLAY; 
					} else if (mode.compareTo("Ableton Looper") == 0) {
						page.mode[index] = page.MODE_LOOPER_OVERLAY;
					}
					page.redrawDevice();
				}
			});
			modeCB.addItem("Triggers");
			modeCB.addItem("Toggles");
			modeCB.addItem("Ableton Clip");
			modeCB.addItem("Ableton Looper");
		}
		return modeCB;
	}
	
	public int getRowColIndex() {
		String label = (String) rowColCB.getSelectedItem();
		int index = 0;
		if (label == null) {
			return index;
		}
		String[] pieces;
		if (rowRB.isSelected()) {
			pieces = label.split("Row ");
		} else {
			pieces = label.split("Col ");
		}
		if (pieces.length < 2) {
			return index;
		}
		index = Integer.parseInt(pieces[1]) - 1;
		return index;
	}

	/**
	 * This method initializes ccCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCcCB() {
		if (ccCB == null) {
			ccCB = new JCheckBox();
			ccCB.setBounds(new Rectangle(35, 160, 21, 21));
			ccCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String label = (String) rowColCB.getSelectedItem();
					if (label == null) {
						return;
					}
					String[] pieces;
					if (rowRB.isSelected()) {
						pieces = label.split("Row ");
					} else {
						pieces = label.split("Col ");
					}
					int index = Integer.parseInt(pieces[1]) - 1;
					page.ccMode[index] = ccCB.isSelected();
				}
			});
		}
		return ccCB;
	}

	/**
	 * This method initializes velocityTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getVelocityTF() {
		if (velocityTF == null) {
			velocityTF = new JTextField();
			velocityTF.setBounds(new Rectangle(40, 185, 46, 21));
			velocityTF.addCaretListener(new javax.swing.event.CaretListener() {
				public void caretUpdate(javax.swing.event.CaretEvent e) {
					String value = velocityTF.getText();
					try {
						int velocity = Integer.parseInt(value);
						if (velocity >= 0 && velocity <= 127) {
							String label = (String) rowColCB.getSelectedItem();
							if (label == null) {
								return;
							}
							String[] pieces;
							if (rowRB.isSelected()) {
								pieces = label.split("Row ");
							} else {
								pieces = label.split("Col ");
							}
							int index = Integer.parseInt(pieces[1]) - 1;
							page.velocity[index] = velocity;
						}
					} catch (NumberFormatException ex) {
						
					}
				}
			});
		}
		return velocityTF;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
