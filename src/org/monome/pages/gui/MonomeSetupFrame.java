package org.monome.pages.gui;

import javax.swing.JPanel;
import javax.swing.JInternalFrame;

import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;

import javax.swing.JLabel;
import java.awt.Rectangle;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class MonomeSetupFrame extends JInternalFrame {

	private static final long serialVersionUID = 42L;
	private JPanel jContentPane = null;
	private MonomeConfiguration monome;
	private JLabel prefixLBL = null;
	private JLabel frameLBL = null;
	private JTextField prefixTF = null;
	private JLabel widthLBL = null;
	private JTextField widthTF = null;
	private JLabel heightLBL = null;
	private JTextField heightTF = null;
	private JButton saveBtn = null;
	private JButton cancelBtn = null;
	private JCheckBox altClearCB = null;
	private JLabel altClearLbl = null;
	/**
	 * This is the xxx default constructor
	 */
	public MonomeSetupFrame(MonomeConfiguration monome) {
		super();
		this.monome = monome;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(148, 231);
		this.setTitle("Monome Setup");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			altClearLbl = new JLabel();
			altClearLbl.setBounds(new Rectangle(40, 105, 91, 21));
			altClearLbl.setText("led_row Clear");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getPrefixLBL(), null);
			jContentPane.add(getFrameLBL(), null);
			jContentPane.add(getPrefixTF(), null);
			jContentPane.add(getWidthLBL(), null);
			jContentPane.add(getWidthTF(), null);
			jContentPane.add(getHeightLBL(), null);
			jContentPane.add(getHeightTF(), null);
			jContentPane.add(getSaveBtn(), null);
			jContentPane.add(getCancelBtn(), null);
			jContentPane.add(getAltClearCB(), null);
			jContentPane.add(altClearLbl, null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes prefixLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getPrefixLBL() {
		if (prefixLBL == null) {
			prefixLBL = new JLabel();
			prefixLBL.setText("Prefix");
			prefixLBL.setBounds(new Rectangle(15, 30, 46, 21));
		}
		return prefixLBL;
	}

	/**
	 * This method initializes frameLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getFrameLBL() {
		if (frameLBL == null) {
			frameLBL = new JLabel();
			frameLBL.setText("Monome Setup");
			frameLBL.setBounds(new Rectangle(5, 5, 111, 21));
		}
		return frameLBL;
	}

	/**
	 * This method initializes prefixTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPrefixTF() {
		if (prefixTF == null) {
			prefixTF = new JTextField();
			prefixTF.setBounds(new Rectangle(60, 30, 61, 21));
			prefixTF.setText(monome.prefix);
		}
		return prefixTF;
	}

	/**
	 * This method initializes widthLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getWidthLBL() {
		if (widthLBL == null) {
			widthLBL = new JLabel();
			widthLBL.setText("Width");
			widthLBL.setBounds(new Rectangle(15, 55, 46, 21));
		}
		return widthLBL;
	}

	/**
	 * This method initializes widthTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getWidthTF() {
		if (widthTF == null) {
			widthTF = new JTextField();
			widthTF.setBounds(new Rectangle(60, 55, 46, 21));
			widthTF.setText("" + monome.sizeX);
		}
		return widthTF;
	}

	/**
	 * This method initializes heightLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getHeightLBL() {
		if (heightLBL == null) {
			heightLBL = new JLabel();
			heightLBL.setText("Height");
			heightLBL.setBounds(new Rectangle(15, 80, 46, 21));
		}
		return heightLBL;
	}

	/**
	 * This method initializes heightTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getHeightTF() {
		if (heightTF == null) {
			heightTF = new JTextField();
			heightTF.setBounds(new Rectangle(60, 80, 46, 21));
			heightTF.setText("" + monome.sizeY);
		}
		return heightTF;
	}

	/**
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton();
			saveBtn.setBounds(new Rectangle(25, 130, 81, 21));
			saveBtn.setText("Save");
			saveBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						int width = Integer.parseInt(getWidthTF().getText());
						int height = Integer.parseInt(getHeightTF().getText());
						monome.sizeX = width;
						monome.sizeY = height;
						String newPrefix = getPrefixTF().getText();
						MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(newPrefix);
						if (monomeConfig == null) {
							monome.prefix = getPrefixTF().getText();
						}
						if (altClearCB.isSelected()) {
							monome.altClear = true;
						}
						monome.setFrameTitle();
						cancel();
					} catch (NumberFormatException ex) {
						cancel();
					}
				}
			});
		}
		return saveBtn;
	}

	/**
	 * This method initializes cancelBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton();
			cancelBtn.setBounds(new Rectangle(25, 155, 81, 21));
			cancelBtn.setText("Cancel");
			cancelBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel();
				}
			});
		}
		return cancelBtn;
	}
	
	private void cancel() {
		this.dispose();
	}

	/**
	 * This method initializes altClearCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getAltClearCB() {
		if (altClearCB == null) {
			altClearCB = new JCheckBox();
			altClearCB.setBounds(new Rectangle(15, 105, 21, 21));
			if (this.monome.altClear == true) {
				altClearCB.setSelected(true);
			}
		}
		return altClearCB;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
