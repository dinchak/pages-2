package org.monome.pages.gui;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import javax.swing.JLabel;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JTextField;
import javax.swing.JButton;

import org.monome.pages.Main;
import org.monome.pages.configuration.Configuration;
import org.monome.pages.configuration.MIDIPageChangeRule;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;
import org.monome.pages.configuration.MonomeOSCListener;

public class NewMonomeConfigurationFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel sizeXLabel = null;
	private JLabel sizeYLabel = null;
	private JLabel Prefix = null;
	private JTextField sizeY = null;
	private JTextField sizeX = null;
	private JTextField prefix = null;
	private JButton saveButton = null;
	private JButton cancelButton = null;
	
	/**
	 * This is the default constructor
	 */
	public NewMonomeConfigurationFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(235, 184);
		this.setContentPane(getJContentPane());
		this.setTitle("New Monome Configuration");
		this.setResizable(true);
		this.pack();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			Prefix = new JLabel();
			Prefix.setBounds(new Rectangle(25, 70, 96, 21));
			Prefix.setText("Prefix");
			sizeYLabel = new JLabel();
			sizeYLabel.setBounds(new Rectangle(25, 40, 96, 21));
			sizeYLabel.setText("Size Y (Height)");
			sizeXLabel = new JLabel();
			sizeXLabel.setBounds(new Rectangle(25, 10, 96, 21));
			sizeXLabel.setText("Size X (Width)");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(sizeXLabel, null);
			jContentPane.add(sizeYLabel, null);
			jContentPane.add(Prefix, null);
			jContentPane.add(getSizeY(), null);
			jContentPane.add(getSizeX(), null);
			jContentPane.add(getPrefix(), null);
			jContentPane.add(getSaveButton(), null);
			jContentPane.add(getCancelButton(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes sizeY	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSizeY() {
		if (sizeY == null) {
			sizeY = new JTextField();
			sizeY.setBounds(new Rectangle(120, 40, 36, 21));
			sizeY.setText("8");
		}
		return sizeY;
	}

	/**
	 * This method initializes sizeX	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSizeX() {
		if (sizeX == null) {
			sizeX = new JTextField();
			sizeX.setBounds(new Rectangle(120, 10, 36, 21));
			sizeX.setText("8");
		}
		return sizeX;
	}

	/**
	 * This method initializes prefix	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPrefix() {
		if (prefix == null) {
			prefix = new JTextField();
			prefix.setBounds(new Rectangle(120, 70, 71, 21));
			prefix.setText("/40h");
		}
		return prefix;
	}

	/**
	 * This method initializes saveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setBounds(new Rectangle(30, 100, 76, 21));
			saveButton.setText("Save");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					save();
				}
			});
		}
		return saveButton;
	}
	
	private void save() {
		int sizeX = Integer.parseInt(this.sizeX.getText());
		int sizeY = Integer.parseInt(this.sizeY.getText());
		String prefix = this.prefix.getText();
		ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
				
		Configuration config = Main.main.configuration;
		config.addMonomeConfiguration(MonomeConfigurationFactory.getNumMonomeConfigurations(), prefix, "no serial", sizeX, sizeY, true, false, midiPageChangeRules);
        if (config.monomeSerialOSCPortIn == null) {
            config.startMonomeSerialOSC();
        } else {
            MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(MonomeConfigurationFactory.getNumMonomeConfigurations() - 1);
            MonomeOSCListener oscListener = new MonomeOSCListener(monomeConfig);
            config.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/press", oscListener);
            config.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/tilt", oscListener);
            monomeConfig.oscListener = oscListener;
        }
		this.dispose();
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setBounds(new Rectangle(120, 100, 76, 21));
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel();
				}
			});
		}
		return cancelButton;
	}
	
	private void cancel() {
		this.dispose();
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
