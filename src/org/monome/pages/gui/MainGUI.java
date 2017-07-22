/*
 *  Main.java
 * 
 *  Copyright (c) 2010, Tom Dinchak
 * 
 *  This file is part of Pages.
 *
 *  Pages is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Pages is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Pages; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.monome.pages.gui;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonState;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.Configuration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;
import org.monome.pages.configuration.OSCPortFactory;
import org.monome.pages.midi.MidiDeviceFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class MainGUI extends JFrame {
    
	private static final long serialVersionUID = 1L;
	private static JDesktopPane jDesktopPane = null;
	private MonomeSerialSetupFrame monomeSerialSetupFrame = null;
	public SerialOSCSetupFrame serialOscSetupFrame = null;
	private AbletonSetupFrame abletonSetupFrame = null;
	private static NewMonomeConfigurationFrame showNewMonomeFrame = null;
	
	private JMenuBar mainMenuBar = null;
	
	private JMenu fileMenu = null;
	private JMenuItem newItem = null;
	private JMenuItem closeItem = null;
	private JMenuItem openOldItem = null;
	private JMenuItem saveItem = null;
	private JMenuItem saveAsItem = null;
	private JMenuItem exitItem = null;
	
	private JMenu configurationMenu = null;
	private JMenuItem monomeSerialSetupItem = null;
	private JMenuItem serialOscSetupItem = null;
	private JMenuItem abletonSetupItem = null;
	private static JMenuItem newMonomeItem = null;
	
	private JMenu midiMenu = null;
	private JMenu midiInMenu = null;
	private JMenu midiOutMenu = null;
	
	public MainGUI() {
		super();
		initialize();
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		    	actionExit();
		    }
		});
	}
	
	/**
	 * Initializes the GUI.
	 * 
	 * @return void
	 */
	private void initialize() {
	    this.setSize(800, 700);
		this.setContentPane(getDesktopPane());
		this.setJMenuBar(getMainMenuBar());
		this.setTitle("Pages");
		// maximize the window
	    //this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Initializes jContentPane.
	 * 
	 * @return javax.swing.JPanel
	 */
	public static JDesktopPane getDesktopPane() {
		if (jDesktopPane == null) {
			jDesktopPane = new JDesktopPane();
			jDesktopPane.setOpaque(true);
			jDesktopPane.setVisible(true);
			jDesktopPane.setBackground(Color.GRAY);
		}
		return jDesktopPane;
	}
	
	/**
	 * Returns this.
	 * 
	 * @return this
	 */
	public JFrame getFrame() {
		return this;
	}
	
	/**
	 * This method initializes mainMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new JMenuBar();
			mainMenuBar.add(getFileMenu());
			mainMenuBar.add(getConfigurationMenu());
			mainMenuBar.add(getMidiMenu());
		}
		return mainMenuBar;
	}

	/**
	 * This method initializes fileMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getNewItem());
			fileMenu.add(getOpenItem());
			//fileMenu.add(getOpenOldItem());
			fileMenu.addSeparator();
			fileMenu.add(getCloseItem());
			fileMenu.addSeparator();
			fileMenu.add(getSaveItem());
			fileMenu.add(getSaveAsItem());
			fileMenu.addSeparator();
			fileMenu.add(getExitItem());
			fileMenu.setMnemonic(KeyEvent.VK_F);
			fileMenu.getAccessibleContext().setAccessibleDescription("File Menu");
		}
		return fileMenu;
	}

	/**
	 * This method initializes newItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getNewItem() {
		if (newItem == null) {
			newItem = new JMenuItem();
			newItem.setText("New Configuration...");
			newItem.getAccessibleContext().setAccessibleDescription("Create a new configuration");
			newItem.setMnemonic(KeyEvent.VK_N);
			newItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String name = (String)JOptionPane.showInputDialog(
							(JMenuItem) e.getSource(),
							"Enter the name of the new configuration",
							"New Configuration",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							
							"");
					
					if (name == null || name.compareTo("") == 0) {
						return;
					}
					actionClose();
					getConfigurationMenu().setEnabled(true);
					getMidiMenu().setEnabled(true);
					getFrame().setTitle("Pages : " + name);
					Main.main.configuration = new Configuration(name);
					Main.main.configuration.initAbleton();
					Main.main.setConfigurationFile(null);
				}
					
			});
		}
		return newItem;
	}
	
	/**
	 * This method initializes openItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getOpenItem() {
		if (openOldItem == null) {
			openOldItem = new JMenuItem();
			openOldItem.setText("Open Configuration...");
			openOldItem.setMnemonic(KeyEvent.VK_O);
			openOldItem.getAccessibleContext().setAccessibleDescription("Open an existing configuration file");
			openOldItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(getFrame());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						actionClose();
						File file = fc.getSelectedFile();
						actionOpenOld(file);
					}
				}
			});
		}
		return openOldItem;
	}
	
	public void actionOpen(File file) {
		Configuration configuration = null;
		FileInputStream fis;
        try {
            fis = new FileInputStream(file.getAbsolutePath());
            ObjectInputStream in = new ObjectInputStream(fis);
            configuration = (Configuration) in.readObject();
            Main.main.configuration = configuration;
            Main.main.configuration.abletonState = new AbletonState();
            configuration.initAbleton();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for (Integer key : configuration.getMonomeConfigurations().keySet()) {
            MonomeConfiguration monomeConfig = configuration.getMonomeConfigurations().get(key);
            MonomeFrame monomeFrame = new MonomeFrame(monomeConfig.index);
            monomeConfig.deviceFrame = monomeFrame;
            monomeConfig.setFrameTitle();
            if (monomeConfig.pages.size() > 0) {
                monomeFrame.enableMidiMenu(true);
            }
            getDesktopPane().add(monomeFrame);
            if (!monomeConfig.pages.isEmpty()) {
                monomeConfig.switchPage(monomeConfig.pages.get(monomeConfig.curPage), monomeConfig.curPage, true);
            }
        }       
        
		getMidiMenu().setEnabled(true);
		getNewMonomeItem().setEnabled(true);
		getConfigurationMenu().setEnabled(true);
        Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
        MidiDevice midiDevice;
        
        for (int i = 0; i < configuration.midiInDevices.length; i++) {
            if (configuration.midiInDevices[i] == null) continue;
            for (int j = 0; j < midiInfo.length; j++) {
                try {
                    midiDevice = MidiSystem.getMidiDevice(midiInfo[j]);
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                    continue;
                }
                if (configuration.midiInDevices[i].compareTo(midiDevice.getDeviceInfo().toString()) == 0) {
                    if (midiDevice.getMaxTransmitters() != 0) {
                        MidiDeviceFactory.toggleMidiInDevice(midiDevice);
                    }
                }
            }
        }
		
        for (int i = 0; i < configuration.midiOutDevices.length; i++) {
            if (configuration.midiOutDevices[i] == null) continue;
            for (int j = 0; j < midiInfo.length; j++) {
                try {
                    midiDevice = MidiSystem.getMidiDevice(midiInfo[j]);
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                    continue;
                }
                if (configuration.midiOutDevices[i].compareTo(midiDevice.getDeviceInfo().toString()) == 0) {
                    if (midiDevice.getMaxReceivers() != 0) {
                        MidiDeviceFactory.toggleMidiOutDevice(midiDevice);
                    }
                }
            }
        }
        
        HashMap<Integer, MonomeConfiguration> monomeConfigs = configuration.getMonomeConfigurations();
        for (Integer key : monomeConfigs.keySet()) {
            MonomeConfiguration monomeConfig = monomeConfigs.get(key);
            if (monomeConfig != null) {
                for (int pageNum = 0; pageNum < monomeConfig.pages.size(); pageNum++) {
                    monomeConfig.deviceFrame.updateMidiInSelectedItems(monomeConfig.midiInDevices[pageNum]);
                    monomeConfig.deviceFrame.updateMidiOutSelectedItems(monomeConfig.midiOutDevices[pageNum]);
                }
            }
        }
	}
	
	/**
	 * This method initializes openItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
    private JMenuItem getOpenOldItem() {
		if (openOldItem == null) {
			openOldItem = new JMenuItem();
			openOldItem.setText("Open Old Configuration...");
			openOldItem.setMnemonic(KeyEvent.VK_O);
			openOldItem.getAccessibleContext().setAccessibleDescription("Open an existing configuration file");
			openOldItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(getFrame());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						actionClose();
						File file = fc.getSelectedFile();
						actionOpenOld(file);
					}
				}
			});
		}
		return openOldItem;
	}
	
	public void actionOpenOld(File file) {
	    actionClose();
		Main.main.setConfigurationFile(file);
		Configuration configuration = new Configuration("Loading");
		Main.main.configuration = configuration;

		if (!configuration.readConfigurationFile(file)) {
		    Main.main.configuration = null;
		    Main.main.setConfigurationFile(null);
		    return;
		}
		
		getMidiMenu().setEnabled(true);
		getNewMonomeItem().setEnabled(true);
		getConfigurationMenu().setEnabled(true);
		
		getFrame().setTitle("Pages : " + configuration.name);
		for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
			if (monomeConfig != null && monomeConfig.pages != null && monomeConfig.pages.size() > 0) {
				monomeConfig.switchPage(monomeConfig.pages.get(monomeConfig.curPage), monomeConfig.curPage, true);
			}
		}
        for (int i = 0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
            ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i);
            if (arcConfig != null && arcConfig.pages != null && arcConfig.pages.size() > 0) {
                arcConfig.switchPage(arcConfig.pages.get(arcConfig.curPage), arcConfig.curPage, true);
            }
        }
		Main.main.openingConfig = true;
		if (Main.main.zeroconfLibrary == Main.LIBRARY_APPLE) {
		    Main.main.appleSerialOSCDiscovery();
		} else if (Main.main.zeroconfLibrary == Main.LIBRARY_JMDNS) {
		    Main.main.jmdnsSerialOSCDiscovery();
		}
	}
	
	/**
	 * This method initializes closeItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCloseItem() {
		if (closeItem == null) {
			closeItem = new JMenuItem();
			closeItem.setText("Close Configuration");
			closeItem.setMnemonic(KeyEvent.VK_C);
			closeItem.getAccessibleContext().setAccessibleDescription("Close the current configuration");
			closeItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int confirm = JOptionPane.showConfirmDialog(
							MainGUI.getDesktopPane(),
							"Are you sure you want to close this configuration?",
							"Close Configuration",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.INFORMATION_MESSAGE
							);
					if (confirm == 0) {
						actionClose();
					}
				}
			});
		}
		return closeItem;
	}
	
	/**
	 * Handles closing the configuration.  Disables MIDI devices, stops OSC listening.
	 */
	public void actionClose() {
		Configuration configuration = Main.main.configuration;
        OSCPortFactory.getInstance().destroy();
        Main.main.removeRegistrations();
		if (configuration != null) {
			for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
				if (monomeConfig != null) {
					monomeConfig.clear(0, -1);
                    monomeConfig.dispose();
				}
			}
            for (int i = 0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
                ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i);
                if (arcConfig != null) {
                    for (int knob = 0; knob < arcConfig.knobs; knob++) {
                        arcConfig.all(knob, 0, -1);
                    }
                    arcConfig.dispose();
                }
            }
			configuration.stopAbleton();
			configuration.destroyAllPages();
			MidiDeviceFactory.closeMidiDevices();
			getFrame().setTitle("Pages");
			getConfigurationMenu().setEnabled(false);
			getMidiMenu().setEnabled(false);
			for (int i = 0; i < getMidiInMenu().getItemCount(); i++) {
				getMidiInMenu().getItem(i).setSelected(false);
			}
			for (int i = 0; i < getMidiOutMenu().getItemCount(); i++) {
				getMidiOutMenu().getItem(i).setSelected(false);
			}
			configuration.stopMonomeSerialOSC();
			MonomeConfigurationFactory.removeMonomeConfigurations();
			ArcConfigurationFactory.removeArcConfigurations();
			Main.main.configuration = null;
			if (serialOscSetupFrame != null) {
			    serialOscSetupFrame.dispose();
			}
		}
		Main.main.setConfigurationFile(null);
	}

	/**
	 * This method initializes saveItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveItem() {
		if (saveItem == null) {
			saveItem = new JMenuItem();
			saveItem.setText("Save Configuration");
			saveItem.setMnemonic(KeyEvent.VK_S);
			saveItem.getAccessibleContext().setAccessibleDescription("Save current configuration to the open configuration file");
			saveItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    actionSaveOld(e);
				}
			});
		}
		return saveItem;
	}
	
	/**
	 * Saves the current configuration to the current configuration file, or displays a file chooser.
	 * 
	 * @param e the event that triggered this action 
	 */
	public void actionSave(java.awt.event.ActionEvent e) {
		File file;
		if ((file = Main.main.getConfigurationFile()) == null) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog((JMenuItem) e.getSource());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				Main.main.setConfigurationFile(file);
				saveSerialized(file);
			}
		}
	}
	
	public void saveSerialized(File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(Main.main.configuration);
            out.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

	
	/**
	 * Saves the current configuration to the current configuration file, or displays a file chooser.
	 * 
	 * @param e the event that triggered this action 
	 */
	public void actionSaveOld(java.awt.event.ActionEvent e) {
		if (Main.main.getConfigurationFile() == null) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showSaveDialog((JMenuItem) e.getSource());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				Main.main.setConfigurationFile(file);
				try {
					if (Main.main.configuration != null) {
						FileWriter fw = new FileWriter(file);
						fw.write(Main.main.configuration.toXml());
						fw.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			FileWriter fw;
			try {
				fw = new FileWriter(Main.main.getConfigurationFile());
				fw.write(Main.main.configuration.toXml());
				fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}

	/**
	 * This method initializes saveAsItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveAsItem() {
		if (saveAsItem == null) {
			saveAsItem = new JMenuItem();
			saveAsItem.setText("Save As...");
			saveAsItem.setMnemonic(KeyEvent.VK_A);
			saveAsItem.getAccessibleContext().setAccessibleDescription("Save current configuration to a new configuration file");
			saveAsItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					int returnVal = fc.showSaveDialog((JMenuItem) e.getSource());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						Main.main.setConfigurationFile(file);
						/*
                        saveSerialized(file);
						*/
						try {
							if (Main.main.configuration != null) {
								FileWriter fw = new FileWriter(file);
								fw.write(Main.main.configuration.toXml());
								fw.close();
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			});
		}
		return saveAsItem;
	}

	/**
	 * This method initializes exitItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitItem() {
		if (exitItem == null) {
			exitItem = new JMenuItem();
			exitItem.setText("Exit");
			exitItem.setMnemonic(KeyEvent.VK_X);
			exitItem.getAccessibleContext().setAccessibleDescription("Exits the program");
			exitItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					actionExit();
				}
			});
		}
		return exitItem;
	}
	
	/**
	 * Exits the application cleanly.
	 */
	public void actionExit() {
		Configuration configuration = Main.main.configuration;
		int confirm = 1;
		if (configuration != null) {
			confirm = JOptionPane.showConfirmDialog(
					MainGUI.getDesktopPane(),
					"Do you want to save before closing?",
					"Exit",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.INFORMATION_MESSAGE
					);
		}
		if (confirm == 0) {
			this.getSaveItem().doClick();
		}
		if (confirm == 0 || confirm == 1) {
			actionClose();
			System.exit(1);
		}
	}

	/**
	 * This method initializes configurationMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	public JMenu getConfigurationMenu() {
		if (configurationMenu == null) {
			configurationMenu = new JMenu();
			configurationMenu.setMnemonic(KeyEvent.VK_C);
			configurationMenu.getAccessibleContext().setAccessibleDescription("Configuration Menu");
			configurationMenu.setText("Configuration");
			configurationMenu.add(getSerialOscSetupItem());
			configurationMenu.add(getMonomeSerialSetupItem());
			configurationMenu.add(getAbletonSetupItem());
			configurationMenu.addSeparator();
			configurationMenu.add(getNewMonomeItem());
			configurationMenu.setEnabled(false);
		}
		return configurationMenu;
	}

	/**
	 * This method initializes monomeSerialSetupItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSerialOscSetupItem() {
		if (serialOscSetupItem == null) {
			serialOscSetupItem = new JMenuItem();
			serialOscSetupItem.setText("SerialOSC Setup...");
			serialOscSetupItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showSerialOscSetup();
				}
			});
		}
		return serialOscSetupItem;
	}
	
	private void showSerialOscSetup() {
		Main.main.openingConfig = false;
		if (serialOscSetupFrame != null && serialOscSetupFrame.isShowing()) {
			try {
				serialOscSetupFrame.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			return;
		}
		
		serialOscSetupFrame = new SerialOSCSetupFrame();
		serialOscSetupFrame.setSize(new Dimension(510, 250));
		serialOscSetupFrame.setVisible(true);
		jDesktopPane.add(serialOscSetupFrame);
		try {
			serialOscSetupFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		jDesktopPane.validate();
	}

	
	/**
	 * This method initializes monomeSerialSetupItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getMonomeSerialSetupItem() {
		if (monomeSerialSetupItem == null) {
			monomeSerialSetupItem = new JMenuItem();
			monomeSerialSetupItem.setText("Monome Serial Setup...");
			monomeSerialSetupItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showMonomeSerialSetup();
				}
			});
		}
		return monomeSerialSetupItem;
	}
	
	private void showMonomeSerialSetup() {
		if (monomeSerialSetupFrame != null && monomeSerialSetupFrame.isShowing()) {
			try {
				monomeSerialSetupFrame.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			return;
		}
		
		monomeSerialSetupFrame = new MonomeSerialSetupFrame();
		monomeSerialSetupFrame.setSize(new Dimension(237, 204));
		monomeSerialSetupFrame.setVisible(true);
		jDesktopPane.add(monomeSerialSetupFrame);
		try {
			monomeSerialSetupFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		jDesktopPane.validate();
	}

	/**
	 * This method initializes abletonConfigurationItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAbletonSetupItem() {
		if (abletonSetupItem == null) {
			abletonSetupItem = new JMenuItem();
			abletonSetupItem.setText("Ableton Setup...");
			abletonSetupItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showAbletonConfiguration();
				}
			});
		}
		return abletonSetupItem;
	}
	
	/**
	 * Shows the Ableton configuration frame
	 */
	private void showAbletonConfiguration() {
		if (abletonSetupFrame != null && abletonSetupFrame.isShowing()) {
			try {
				abletonSetupFrame.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			return;
		}
		
		abletonSetupFrame = new AbletonSetupFrame();
		abletonSetupFrame.setSize(new Dimension(233, 246));
		abletonSetupFrame.setVisible(true);
		jDesktopPane.add(abletonSetupFrame);
		try {
			abletonSetupFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		jDesktopPane.validate();
	}
	
	/**
	 * This method initializes newMonomeItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	public static JMenuItem getNewMonomeItem() {
		if (newMonomeItem == null) {
			newMonomeItem = new JMenuItem();
			newMonomeItem.setMnemonic(KeyEvent.VK_N);
			newMonomeItem.getAccessibleContext().setAccessibleDescription("New Monome Configuration");
			newMonomeItem.setText("New Monome Configuration...");
			newMonomeItem.setEnabled(false);
			newMonomeItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showNewMonomeConfiguration();
				}
			});
		}
		return newMonomeItem;
	}
	
	private static void showNewMonomeConfiguration() {
		if (showNewMonomeFrame != null && showNewMonomeFrame.isShowing()) {
			try {
				showNewMonomeFrame.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			return;
		}
		
		showNewMonomeFrame = new NewMonomeConfigurationFrame();
		showNewMonomeFrame.setSize(new Dimension(235, 184));
		showNewMonomeFrame.setVisible(true);
		jDesktopPane.add(showNewMonomeFrame);
		try {
			showNewMonomeFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		
		jDesktopPane.validate();
	}
	
	/**
	 * This method initializes midiMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	public JMenu getMidiMenu() {
		if (midiMenu == null) {
			midiMenu = new JMenu();
			midiMenu.setText("MIDI");
			midiMenu.add(getMidiInMenu());
			midiMenu.add(getMidiOutMenu());
			midiMenu.setEnabled(false);
		}
		return midiMenu;
	}

	/**
	 * This method initializes midiInMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getMidiInMenu() {
		if (midiInMenu == null) {
			midiInMenu = new JMenu();
			midiInMenu.setText("MIDI In");			
			midiInMenu.setMnemonic(KeyEvent.VK_I);
			midiInMenu.getAccessibleContext().setAccessibleDescription("MIDI In Menu");

			Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
			for (int i=0; i < midiInfo.length; i++) {
				try {
					MidiDevice midiDevice = MidiSystem.getMidiDevice(midiInfo[i]);
					if (midiDevice.getMaxTransmitters() != 0) {
						JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("MIDI Input: " + midiInfo[i].getName());
						cbMenuItem.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(java.awt.event.ActionEvent e) {
								String[] pieces = e.getActionCommand().split("MIDI Input: ");
								actionAddMidiInput(pieces[1]);
							}});
						midiInMenu.add(cbMenuItem);
					}
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		}
		return midiInMenu;
	}
	
	/**
	 * Handles a click on a MIDI in device.  Toggles it on or off.
	 * 
	 * @param sMidiDevice the name of the MIDI device to toggle
	 */
	public void actionAddMidiInput(String sMidiDevice) {
		Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
		MidiDevice midiDevice;

		for (int i=0; i < midiInfo.length; i++) {
			try {
				midiDevice = MidiSystem.getMidiDevice(midiInfo[i]);
				if (sMidiDevice.compareTo(midiDevice.getDeviceInfo().toString()) == 0) {
					if (midiDevice.getMaxTransmitters() != 0) {
						MidiDeviceFactory.toggleMidiInDevice(midiDevice);
						Main.main.configuration.toggleMidiInDevice(sMidiDevice);
					}
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Only enables or disables the option in the MIDI In menu.  Used when loading configuration.
	 * 
	 * @param deviceName the name of the MIDI in device
	 * @param enabled true = checked, false = not
	 */
	public void enableMidiInOption(String deviceName, boolean enabled) {
		for (int i=0; i < midiInMenu.getItemCount(); i++) {
			String name = midiInMenu.getItem(i).getText();
			String[] pieces = name.split("MIDI Input: ");
			if (pieces[1].compareTo(deviceName) == 0) {
				midiInMenu.getItem(i).setSelected(enabled);
			}
		}
	}
	
	/**
	 * This method initializes midiOutMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getMidiOutMenu() {
		if (midiOutMenu == null) {
			midiOutMenu = new JMenu();
			midiOutMenu.setText("MIDI Out");
			midiInMenu.setMnemonic(KeyEvent.VK_I);
			midiInMenu.getAccessibleContext().setAccessibleDescription("MIDI In Menu");
			
			Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
			for (int i=0; i < midiInfo.length; i++) {
				try {
					MidiDevice midiDevice = MidiSystem.getMidiDevice(midiInfo[i]);
					if (midiDevice.getMaxReceivers() != 0) {
						JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("MIDI Output: " + midiInfo[i].getName());
						cbMenuItem.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(java.awt.event.ActionEvent e) {
								String[] pieces = e.getActionCommand().split("MIDI Output: ");
								actionAddMidiOutput(pieces[1]);
							}});
						midiOutMenu.add(cbMenuItem);
					}
				} catch (MidiUnavailableException e) {
					e.printStackTrace();
				}
			}
		}
		return midiOutMenu;
	}
	
	/**
	 * Handles a click on a MIDI out device.  Toggles it on or off.
	 * 
	 * @param sMidiDevice the name of the MIDI device to toggle
	 */
	public void actionAddMidiOutput(String sMidiDevice) {
		Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
		MidiDevice midiDevice;

		for (int i=0; i < midiInfo.length; i++) {
			try {
				midiDevice = MidiSystem.getMidiDevice(midiInfo[i]);
				if (sMidiDevice.compareTo(midiDevice.getDeviceInfo().toString()) == 0) {
					if (midiDevice.getMaxReceivers() != 0) {
						MidiDeviceFactory.toggleMidiOutDevice(midiDevice);
                        Main.main.configuration.toggleMidiOutDevice(sMidiDevice);
					}
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}		
	}
	
   /**
	 * Only enables or disabled the option in the MIDI Out menu.  Used when loading configuration.
	 * 
	 * @param deviceName the name of the MIDI in device
	 * @param enabled true = checked, false = not
	 */
	public void enableMidiOutOption(String deviceName, boolean enabled) {
		for (int i=0; i < midiOutMenu.getItemCount(); i++) {
			String name = midiOutMenu.getItem(i).getText();
			String[] pieces = name.split("MIDI Output: ");
			if (pieces[1].compareTo(deviceName) == 0) {
				midiOutMenu.getItem(i).setSelected(enabled);
			}
		}
	}
					

}