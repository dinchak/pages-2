package org.monome.pages.pages.gui;

import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.pages.AbletonClipSkipperPage;

import javax.swing.JLabel;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JButton;

public class AbletonClipSkipperGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;
    
	private AbletonClipSkipperPage page = null;
	private JLabel pageLabel = null;
	private JButton refreshButton = null;
	/**
	 * This is the default constructor
	 */
	public AbletonClipSkipperGUI(AbletonClipSkipperPage page) {
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
		pageLabel = new JLabel();
		setName("Ableton Clip Skipper");
		pageLabel.setBounds(new Rectangle(5, 5, 181, 16));
		this.setLayout(null);
		this.add(pageLabel, null);
		this.add(getRefreshButton(), null);
		this.setSize(220, 91);
	}
	
	public void setName(String name) {
		pageLabel.setText((page.getIndex() + 1) + ": " + name);
	}
	
	/**
	 * This method initializes refreshButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setBounds(new Rectangle(15, 30, 162, 21));
			refreshButton.setText("Refresh From Ableton");
			refreshButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Main.main.configuration.getAbletonControl().refreshAbleton();
				}
			});
		}
		return refreshButton;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
