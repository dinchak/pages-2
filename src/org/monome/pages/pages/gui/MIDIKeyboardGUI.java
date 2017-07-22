package org.monome.pages.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JTextField;
import javax.swing.JButton;

import org.monome.pages.pages.MIDIKeyboardPage;

public class MIDIKeyboardGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

	private MIDIKeyboardPage page;
	private JLabel pageLabel = null;
	private JLabel scalesLBL = null;
	public JTextField scaleTF1 = null;
	public JTextField scaleTF2 = null;
	public JTextField scaleTF3 = null;
	public JTextField scaleTF4 = null;
	public JTextField scaleTF5 = null;
	public JTextField scaleTF6 = null;
	private JButton resetScalesBtn = null;
	private JButton updatePrefsBtn = null;
	/**
	 * This is the default constructor
	 */
	public MIDIKeyboardGUI(MIDIKeyboardPage page) {
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
		this.setSize(230, 210);
		this.setLayout(null);
		this.add(getPageLabel(), null);
		setName("MIDI Keyboard Page");
		this.add(getScalesLBL(), null);
		this.add(getScaleTF1(), null);
		this.add(getScaleTF2(), null);
		this.add(getScaleTF3(), null);
		this.add(getScaleTF4(), null);
		this.add(getScaleTF5(), null);
		this.add(getScaleTF6(), null);
		this.add(getResetScalesBtn(), null);
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
			pageLabel.setBounds(new Rectangle(5, 5, 156, 16));
		}
		return pageLabel;
	}

	/**
	 * This method initializes scalesLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getScalesLBL() {
		if (scalesLBL == null) {
			scalesLBL = new JLabel();
			scalesLBL.setText("Scales");
			scalesLBL.setBounds(new Rectangle(15, 25, 56, 16));
		}
		return scalesLBL;
	}

	/**
	 * This method initializes scaleTF1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF1() {
		if (scaleTF1 == null) {
			scaleTF1 = new JTextField();
			scaleTF1.setBounds(new Rectangle(15, 45, 86, 21));
		}
		return scaleTF1;
	}

	/**
	 * This method initializes scalesTF2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF2() {
		if (scaleTF2 == null) {
			scaleTF2 = new JTextField();
			scaleTF2.setBounds(new Rectangle(110, 45, 86, 21));
		}
		return scaleTF2;
	}

	/**
	 * This method initializes scaleTF3	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF3() {
		if (scaleTF3 == null) {
			scaleTF3 = new JTextField();
			scaleTF3.setBounds(new Rectangle(15, 70, 86, 21));
		}
		return scaleTF3;
	}

	/**
	 * This method initializes scaleTF4	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF4() {
		if (scaleTF4 == null) {
			scaleTF4 = new JTextField();
			scaleTF4.setBounds(new Rectangle(110, 70, 86, 21));
		}
		return scaleTF4;
	}

	/**
	 * This method initializes scaleTF5	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF5() {
		if (scaleTF5 == null) {
			scaleTF5 = new JTextField();
			scaleTF5.setBounds(new Rectangle(15, 95, 86, 21));
		}
		return scaleTF5;
	}

	/**
	 * This method initializes scaleTF6	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getScaleTF6() {
		if (scaleTF6 == null) {
			scaleTF6 = new JTextField();
			scaleTF6.setBounds(new Rectangle(110, 95, 86, 21));
		}
		return scaleTF6;
	}

	/**
	 * This method initializes resetScalesBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getResetScalesBtn() {
		if (resetScalesBtn == null) {
			resetScalesBtn = new JButton();
			resetScalesBtn.setBounds(new Rectangle(50, 155, 111, 21));
			resetScalesBtn.setText("Reset Scales");
			resetScalesBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					resetScales();
				}
			});
		}
		return resetScalesBtn;
	}
	
	public void resetScales() {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				page.scales[i][j] = page.scalesDefault[i][j];				
			}
		}
		page.getScales();
	}

	/**
	 * This method initializes updatePrefsBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUpdatePrefsBtn() {
		if (updatePrefsBtn == null) {
			updatePrefsBtn = new JButton();
			updatePrefsBtn.setBounds(new Rectangle(30, 125, 151, 21));
			updatePrefsBtn.setText("Update Preferences");
			updatePrefsBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String s[] = new String[6];			
					s[0] = scaleTF1.getText();
					s[1] = scaleTF2.getText();
					s[2] = scaleTF3.getText();
					s[3] = scaleTF4.getText();
					s[4] = scaleTF5.getText();
					s[5] = scaleTF6.getText();					
					page.setScales(s);
				}
			});
		}
		return updatePrefsBtn;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
