/*
 *  Configuration.java
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

package org.monome.pages.configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;

import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonControl;
import org.monome.pages.ableton.AbletonOSCControl;
import org.monome.pages.ableton.AbletonOSCListener;
import org.monome.pages.ableton.AbletonState;
import org.monome.pages.gui.ArcFrame;
import org.monome.pages.gui.MainGUI;
import org.monome.pages.gui.MonomeFrame;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.ArcPage;
import org.monome.pages.pages.Page;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.lang.StringEscapeUtils;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

/**
 * This object stores all configuration about all current monomes and pages.  It
 * also stores global options like Ableton OSC port selection and enabled MIDI
 * devices.
 * 
 * @author Tom Dinchak
 *
 */
public class Configuration implements Serializable {
    static final long serialVersionUID = 42L;
    
	/**
	 * The name of the configuration.
	 */
	public String name;
	
	public int oscListenPort = 12345;

	/**
	 * The port number to receive OSC messages from MonomeSerial.
	 */
	public int monomeSerialOSCInPortNumber = 8000;

	/**
	 * The OSCPortIn object to receive messages from MonomeSerial.
	 */
	public transient OSCPortIn monomeSerialOSCPortIn;

	/**
	 * The port number to send OSC messages to MonomeSerial. 
	 */
	public int monomeSerialOSCOutPortNumber = 8080;

	/**
	 * The OSCPortOut object to send messages to MonomeSerial.
	 */
	public transient OSCPortOut monomeSerialOSCPortOut;

	/**
	 * The hostname that MonomeSerial is bound to.
	 */
	public String monomeHostname = "localhost";

	/**
	 * The OSC listener that checks for discovery events (/sys/report responses)
	 */
	private transient DiscoverOSCListener discoverOSCListener = null;
	
	/**
	 * The port number to receive OSC messages from Ableton.
	 */
	private int abletonOSCInPortNumber = 9006;

	/**
	 * The OSCPortIn object to receive OSC messages from Ableton. 
	 */
	private transient OSCPortIn abletonOSCPortIn;

	/**
	 * The port number to send OSC messages to Ableton. 
	 */
	private int abletonOSCOutPortNumber = 9005;
	
	/**
	 * The OSCPortOut object to send OSC messages to Ableton.
	 */
	private transient OSCPortOut abletonOSCPortOut;

	/**
	 * Listens for /live/track/info and /live/tempo responses from Ableton and
	 * updates this object.  Implements the OSCListener interface.
	 */
	private transient AbletonOSCListener abletonOSCListener;

	/**
	 * The hostname that Ableton is bound to.
	 */
	private String abletonHostname = "localhost";

	/**
	 * The AbletonControl object we're using, currently only AbletonOSCControl.
	 */
	private AbletonControl abletonControl;
	
	/**
	 * The current state of Ableton is stored in this object.
	 */
	public transient AbletonState abletonState;
	
	/**
	 * True if Ableton OSC communication has been initialized.
	 */
	private transient boolean abletonInitialized = false;

	/**
	 * True if we should not send any 'View Track' commands to Ableton.
	 */
	private boolean abletonIgnoreViewTrack = true;
	
	private transient RedrawAbletonThread redrawAbletonThread;

	private transient OSCPortIn serialOSCPortIn;
	
	private HashMap<Integer, MonomeConfiguration> monomeConfigurations = null;
	
	private HashMap<Integer, ArcConfiguration> arcConfigurations = null;
	
    /**
     * Enabled MIDI In devices (global) 
     */
    public String[] midiInDevices = new String[32];

    /**
     * Enabled MIDI Out devices (global)
     */
    public String[] midiOutDevices = new String[32];

	/**
	 * @param name The name of the configuration
	 */
	public Configuration(String name) {
		this.name = name;
		this.abletonState = new AbletonState();
	}
	
	public HashMap<Integer, MonomeConfiguration> getMonomeConfigurations() {
		return monomeConfigurations;
	}
	
	public void setMonomeConfigurations(HashMap<Integer, MonomeConfiguration> monomeConfigurations) {
		this.monomeConfigurations = monomeConfigurations;
	}
	
    public HashMap<Integer, ArcConfiguration> getArcConfigurations() {
        return arcConfigurations;
    }
    
    public void setArcConfigurations(HashMap<Integer, ArcConfiguration> arcConfigurations) {
        this.arcConfigurations = arcConfigurations;
    }

	/**
	 * Called from GUI to add a new monome configuration.
	 * 
	 * @param index the index of this monome configuration
	 * @param prefix the prefix of the monome (ie. /40h)
	 * @param serial the serial # of the monome
	 * @param sizeX the width of the monome in buttons (ie 8)
	 * @param sizeY the height of the monome in buttons (ie 8)
	 * @param usePageChangeButton true if the page change button is active
	 * @param useMIDIPageChanging true if midi page change rules should be used
	 * @param midiPageChangeRules the set of midi page change rules
	 * @return the MonomeConfiguration object
	 */
	public MonomeConfiguration addMonomeConfiguration(int index, String prefix, String serial, int sizeX, int sizeY, boolean usePageChangeButton, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules) {
		MonomeFrame monomeFrame = new MonomeFrame(index);
		MainGUI.getDesktopPane().add(monomeFrame);
		try {
			monomeFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		MonomeConfiguration monome = MonomeConfigurationFactory.addMonomeConfiguration(index, prefix, serial, sizeX, sizeY, usePageChangeButton, useMIDIPageChanging, midiPageChangeRules, monomeFrame);
		this.initMonome(monome);
		return monome;
	}
	
	public MonomeConfiguration addMonomeConfigurationSerialOSC(int index, String prefix, String serial, int sizeX, int sizeY, boolean usePageChangeButton, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules, int port, String hostName) {
		MonomeFrame monomeFrame = new MonomeFrame(index);
		MainGUI.getDesktopPane().add(monomeFrame);
		try {
			monomeFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		MonomeConfiguration monome = MonomeConfigurationFactory.addMonomeConfiguration(index, prefix, serial, sizeX, sizeY, usePageChangeButton, useMIDIPageChanging, midiPageChangeRules, monomeFrame);
		monome.serialOSCPort = port;
		monome.serialOSCHostname = hostName;
		this.initMonomeSerialOSC(monome);
		return monome;
	}
	
    public ArcConfiguration addArcConfiguration(int index, String prefix, String serial, int knobs, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules) {
        ArcFrame arcFrame = new ArcFrame(index);
        try {
            arcFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        MainGUI.getDesktopPane().add(arcFrame);
        arcFrame.moveToFront();
        ArcConfiguration arc = ArcConfigurationFactory.addArcConfiguration(index, prefix, serial, knobs, arcFrame, useMIDIPageChanging, midiPageChangeRules);
        this.initArcSerialOSC(arc);
        return arc;
    }

    public ArcConfiguration addArcConfigurationSerialOSC(int index, String prefix, String serial, int knobs, int port, String hostName, boolean useMIDIPageChanging) {
        ArcFrame arcFrame = new ArcFrame(index);
        try {
            arcFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        MainGUI.getDesktopPane().add(arcFrame);
        arcFrame.moveToFront();
        ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
        ArcConfiguration arc = ArcConfigurationFactory.addArcConfiguration(index, prefix, serial, knobs, arcFrame, useMIDIPageChanging, midiPageChangeRules);
        arc.serialOSCPort = port;
        arc.serialOSCHostname = hostName;
        this.initArcSerialOSC(arc);
        return arc;
    }
	
	/**
	 * Binds to MonomeSerial input/output ports
	 */
	public void startMonomeSerialOSC() {
		if (this.monomeSerialOSCPortIn == null) {
			this.monomeSerialOSCPortIn = OSCPortFactory.getInstance().getOSCPortIn(this.monomeSerialOSCInPortNumber);
			if (this.monomeSerialOSCPortIn == null) {
			    System.out.println("Unable to bind to port " + this.monomeSerialOSCInPortNumber);
				JOptionPane.showMessageDialog(MainGUI.getDesktopPane(), "Unable to bind to port " + this.monomeSerialOSCInPortNumber + ".  Try closing any other programs that might be listening on it.", "OSC Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (this.monomeSerialOSCPortOut == null) {
			    this.monomeSerialOSCPortOut = OSCPortFactory.getInstance().getOSCPortOut(monomeHostname, this.monomeSerialOSCOutPortNumber);
			}
			discoverOSCListener = new DiscoverOSCListener();
			this.monomeSerialOSCPortIn.addListener("/sys/devices", discoverOSCListener);
			this.monomeSerialOSCPortIn.addListener("/sys/prefix", discoverOSCListener);
			this.monomeSerialOSCPortIn.addListener("/sys/type", discoverOSCListener);
			this.monomeSerialOSCPortIn.addListener("/sys/cable", discoverOSCListener);
			this.monomeSerialOSCPortIn.addListener("/sys/offset", discoverOSCListener);
			this.monomeSerialOSCPortIn.addListener("/sys/serial", discoverOSCListener);
			
			// used when loading config
			for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
				if (monomeConfig != null && monomeConfig.oscListener == null) {
					MonomeOSCListener oscListener = new MonomeOSCListener(monomeConfig);
					monomeConfig.oscListener = oscListener;
					this.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/press", oscListener);
					this.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/tilt", oscListener);
		            System.out.println("Added listener for " + monomeConfig.prefix + "/press on port " + this.monomeSerialOSCInPortNumber);
					/*
					this.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/adc", oscListener);
					this.monomeSerialOSCPortIn.addListener(monomeConfig.prefix + "/tilt", oscListener);
			
					Object args[] = new Object[1];
					args[0] = new Integer(1);
					OSCMessage msg = new OSCMessage(monomeConfig.prefix + "/tiltmode", args);
			
					try {
						this.monomeSerialOSCPortOut.send(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					*/

					initMonome(monomeConfig);
				}
			}
		
		}
		if (this.monomeSerialOSCPortOut == null) {
			this.monomeSerialOSCPortOut = OSCPortFactory.getInstance().getOSCPortOut(this.monomeHostname, this.monomeSerialOSCOutPortNumber);
		}		
	}

	/**
	 * Close MonomeSerial OSC Connections. 
	 */
	public void stopMonomeSerialOSC() {
		if (this.monomeSerialOSCPortIn != null) {
			if (this.monomeSerialOSCPortIn.isListening()) {
				this.monomeSerialOSCPortIn.removeAllListeners();
				this.monomeSerialOSCPortIn.stopListening();
				this.monomeSerialOSCPortIn.close();
			}
			OSCPortFactory.getInstance().destroyOSCPortIn(this.monomeSerialOSCInPortNumber);
			this.monomeSerialOSCPortIn = null;
		}

		if (this.monomeSerialOSCPortOut != null) {
			this.monomeSerialOSCPortOut = null;
		}
	}

	/**
	 * Calls each page's destroyPage() function.
	 */
	public void destroyAllPages() {
		for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
			if (monomeConfig != null) {
				monomeConfig.destroyPage();
			}
		}
	}
	
	/**
	 * @param inport The port number to receive OSC messages from MonomeSerial 
	 */
	public void setMonomeSerialOSCInPortNumber(int inport) {
		this.monomeSerialOSCInPortNumber = inport;
	}

	/**
	 * @return The port number to receive OSC messages from MonomeSerial
	 */
	public int getMonomeSerialOSCInPortNumber() {
		return this.monomeSerialOSCInPortNumber;
	}

	/**
	 * @param outport The port number to send OSC messages to MonomeSerial
	 */
	public void setMonomeSerialOSCOutPortNumber(int outport) {
		this.monomeSerialOSCOutPortNumber = outport;
	}

	/**
	 * @return The port number to send OSC messages to MonomeSerial
	 */
	public int getMonomeSerialOSCOutPortNumber() {
		return this.monomeSerialOSCOutPortNumber;
	}

	/**
	 * @param hostname The hostname that MonomeSerial is bound to
	 */
	public void setMonomeHostname(String hostname) {
		this.monomeHostname = hostname;
	}

	/**
	 * @return The hostname that MonomeSerial is bound to
	 */
	public String getMonomeHostname() {
		return this.monomeHostname;
	}

	/**
	 * Runs /sys/report and sets up a monome for each returned device
	 */
	public void discoverMonomes() {
		this.stopMonomeSerialOSC();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		this.startMonomeSerialOSC();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		this.discoverOSCListener.setDiscoverMode(true);
		OSCMessage msg = new OSCMessage("/sys/report");
		// this stuff is really touchy; malformed packets etc. run /sys/report 3 times to be sure.
		try {
			for (int i = 0; i < 3; i++) {
				this.monomeSerialOSCPortOut.send(msg);
				Thread.sleep(100);
			}
			MonomeConfigurationFactory.combineMonomeConfigurations();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Initializes a new monome configuration.  Starts OSC communication with MonomeSerial if needed.
	 * 
	 * @param monome The MonomeConfiguration object to initialize
	 */
	public void initMonome(MonomeConfiguration monome) {
		monome.clearMonome();
	}
	
	public void initMonomeSerialOSC(MonomeConfiguration monome) {
		monome.serialOSCPortOut = OSCPortFactory.getInstance().getOSCPortOut(monome.serialOSCHostname, monome.serialOSCPort);

		MonomeOSCListener oscListener = new MonomeOSCListener(monome);
		this.serialOSCPortIn = OSCPortFactory.getInstance().getOSCPortIn(oscListenPort);
		this.serialOSCPortIn.addListener(monome.prefix + "/grid/key", oscListener);
		this.serialOSCPortIn.addListener(monome.prefix + "/tilt", oscListener);
		
		try {
	        OSCMessage portMsg = new OSCMessage();
	        portMsg.setAddress("/sys/port");
	        portMsg.addArgument(oscListenPort);
			monome.serialOSCPortOut.send(portMsg);
			portMsg = new OSCMessage();
            portMsg.setAddress("/sys/host");
            portMsg.addArgument("127.0.0.1");
            monome.serialOSCPortOut.send(portMsg);
            // enable tilt sensors
			portMsg = new OSCMessage();
            portMsg.setAddress(monome.prefix + "/tilt/set");
            portMsg.addArgument(0);
            portMsg.addArgument(1);
            monome.serialOSCPortOut.send(portMsg);
			portMsg = new OSCMessage();
            portMsg.setAddress(monome.prefix + "/tilt/set");
            portMsg.addArgument(1);
            portMsg.addArgument(1);
            monome.serialOSCPortOut.send(portMsg);
			portMsg = new OSCMessage();
            portMsg.setAddress(monome.prefix + "/tilt/set");
            portMsg.addArgument(2);
            portMsg.addArgument(1);
            monome.serialOSCPortOut.send(portMsg);
			portMsg = new OSCMessage();
            portMsg.setAddress(monome.prefix + "/tilt/set");
            portMsg.addArgument(3);
            portMsg.addArgument(1);
            monome.serialOSCPortOut.send(portMsg);

		} catch (IOException e) {
			e.printStackTrace();
		}
		monome.clearMonome();
		monome.initMonome();
	}
	
    public void initArcSerialOSC(ArcConfiguration arc) {        
        if (arc.serialOSCPort == 0) return;
        arc.serialOSCPortOut = OSCPortFactory.getInstance().getOSCPortOut(arc.serialOSCHostname, arc.serialOSCPort);
        ArcOSCListener oscListener = new ArcOSCListener(arc);
        this.serialOSCPortIn = OSCPortFactory.getInstance().getOSCPortIn(oscListenPort);
        this.serialOSCPortIn.addListener(arc.prefix + "/enc/delta", oscListener);
        this.serialOSCPortIn.addListener(arc.prefix + "/enc/key", oscListener);
        
        try {
            OSCMessage portMsg = new OSCMessage();
            portMsg.setAddress("/sys/port");
            portMsg.addArgument(oscListenPort);
            arc.serialOSCPortOut.send(portMsg);
            portMsg = new OSCMessage();
            portMsg.setAddress("/sys/host");
            portMsg.addArgument("127.0.0.1");
            arc.serialOSCPortOut.send(portMsg);
            portMsg = new OSCMessage();
            portMsg.setAddress("/sys/prefix");
            portMsg.addArgument(arc.prefix);
            arc.serialOSCPortOut.send(portMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        arc.initArc();
    }

	
	/**
	 * Enables or disables a MIDI input device
	 * 
	 * @param sMidiDevice The name of the MIDI device to toggle
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
					}
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
    /**
     * Turns a MIDI In device on or off for the current page.
     * 
     * @param deviceName the MIDI device name
     */
    public void toggleMidiInDevice(String deviceName) {
        for (int i = 0; i < this.midiInDevices.length; i++) {
            // if this device was enabled, disable it
            if (this.midiInDevices[i] == null) {
                continue;
            }
            if (this.midiInDevices[i].compareTo(deviceName) == 0) {
                midiInDevices[i] = new String();
                return;
            }
        }

        // if we didn't disable it, enable it
        for (int i = 0; i < this.midiInDevices.length; i++) {
            if (this.midiInDevices[i] == null) {
                this.midiInDevices[i] = deviceName;
                return;
            }
        }
    }

	/**
	 * Enables or disables a MIDI output device
	 * 
	 * @param sMidiDevice The MIDI output device to enable or disable
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
					}
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}		
	}
	
    /**
     * Toggles a MIDI Out device on or off for the current page.
     * 
     * @param deviceName the name of the MIDI device
     */
    public void toggleMidiOutDevice(String deviceName) {
        for (int i = 0; i < this.midiOutDevices.length; i++) {
            // if this device was enabled, disable it
            if (this.midiOutDevices[i] == null) {
                continue;
            }
            if (this.midiOutDevices[i].compareTo(deviceName) == 0) {
                midiOutDevices[i] = new String();
                return;
            }
        }

        // if we didn't disable it, enable it
        for (int i = 0; i < this.midiOutDevices.length; i++) {
            if (this.midiOutDevices[i] == null) {
                this.midiOutDevices[i] = deviceName;
                return;
            }
        }
    }

	/**
	 * Called by MIDIInReceiver objects when a MIDI message is received.
	 * 
	 * @param device The MidiDevice the message was received from
	 * @param message The MidiMessage
	 * @param lTimeStamp The time when the message was received
	 */
	public void send(MidiDevice device, MidiMessage message, long lTimeStamp) {
		ShortMessage shortMessage;
		// pass all messages along to all monomes (who pass to all pages)
		for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
			if (monomeConfig != null) {
				monomeConfig.send(device, message, lTimeStamp);
			}
		}
        for (int i = 0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
            ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i);
            if (arcConfig != null) {
                arcConfig.send(device, message, lTimeStamp);
            }
        }
		
		// filter for midi clock ticks or midi reset messages
		if (message instanceof ShortMessage) {
			shortMessage = (ShortMessage) message;
			switch (shortMessage.getCommand()) {
			case 0xF0:
				if (shortMessage.getChannel() == 8) {
					for (int i=0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
						MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
						if (monomeConfig != null) {
							monomeConfig.tick(device);
						}
					}
			        for (int i = 0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
			            ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i);
			            if (arcConfig != null) {
			                arcConfig.tick(device);
			            }
			        }
				}
				if (shortMessage.getChannel() == 0x0C) {
					for (int i=0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
						MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
						if (monomeConfig != null) {
							monomeConfig.reset(device);
						}
					}
                    for (int i = 0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
                        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i);
                        if (arcConfig != null) {
                            arcConfig.reset(device);
                        }
                    }
				}
				break;
			default:
				break;
			}
		}		
	}
	
	/**
	 * @return AbletonControl the currently enabled Ableton Control device
	 */
	public AbletonControl getAbletonControl() {
		return this.abletonControl;
	}

	/**
	 * @return AbletonState the current Ableton State object
	 */
	public AbletonState getAbletonState() {
		return abletonState;
	}

	/**
	 * @param inport The port number to receive OSC messages from Ableton
	 */
	public void setAbletonOSCInPortNumber(int inport) {
		this.abletonOSCInPortNumber = inport;
	}

	/**
	 * @return The port number to receive OSC messages from Ableton
	 */
	public int getAbletonOSCInPortNumber() {
		return this.abletonOSCInPortNumber;
	}

	/**
	 * @param outport The port number to send OSC messages to Ableton
	 */
	public void setAbletonOSCOutPortNumber(int outport) {
		this.abletonOSCOutPortNumber = outport;
	}

	/**
	 * @return The port number to send OSC messages to Ableton
	 */
	public int getAbletonOSCOutPortNumber() {
		return this.abletonOSCOutPortNumber;
	}
	
	/**
	 * @return The OSCPortOut object to send OSC messages to Ableton
	 */
	public OSCPortOut getAbletonOSCPortOut() {
		return this.abletonOSCPortOut;
	}

	/**
	 * @param hostname The hostname that Ableton is bound to
	 */
	public void setAbletonHostname(String hostname) {
		this.abletonHostname = hostname;
	}

	/**
	 * @return The hostname that Ableton is bound to
	 */
	public String getAbletonHostname() {
		return this.abletonHostname;
	}	
	
	/**
	 * Initializes OSC communication with Ableton.
	 * 
	 * @return true if initialization was successful
	 */
	public void initAbleton() {
		if (!abletonInitialized) {
			this.initAbletonOSCMode();
			abletonInitialized = true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.abletonControl.refreshAbleton();
		}
	}
	
	/**
	 * Initializes Ableton connection using OSC
	 */
	public void initAbletonOSCMode() {
		this.abletonOSCListener = new AbletonOSCListener();
		this.initAbletonOSCOut();
		this.initAbletonOSCIn();
		this.abletonControl = new AbletonOSCControl();
	}
	
	/**
	 * Initializes the Ableton OSC out port
	 */
	public void initAbletonOSCOut() {		
		try {
			this.abletonOSCPortOut = new OSCPortOut(InetAddress.getByName(this.abletonHostname), this.abletonOSCOutPortNumber);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes the Ableton OSC in port
	 */
	public void initAbletonOSCIn() {
		try {
			if (this.abletonOSCPortIn != null) {
				this.abletonOSCPortIn.stopListening();
				this.abletonOSCPortIn.close();
			}
			this.abletonOSCPortIn = new OSCPortIn(this.abletonOSCInPortNumber);
			this.abletonOSCPortIn.addListener("/live/track", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/track/info", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/name/track", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/clip/info", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/state", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/mute", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/arm", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/solo", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/scene", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/tempo", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/overdub", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/refresh", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/reset", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/devicelist", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/device", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/device/allparam", this.abletonOSCListener);
			this.abletonOSCPortIn.addListener("/live/device/param", this.abletonOSCListener);
			this.abletonOSCPortIn.startListening();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Redraws all Ableton pages
	 */
	public void redrawAbletonPages() {
		if (redrawAbletonThread == null) {
			redrawAbletonThread = new RedrawAbletonThread();
			new Thread(redrawAbletonThread).start();
		} else {
			redrawAbletonThread.sleepCounter = 1;
		}
		/*
		for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
			if (monomeConfig != null && !(monomeConfig instanceof FakeMonomeConfiguration)) {
				monomeConfig.redrawAbletonPages();
			}
		}
		*/		
	}
	
	class RedrawAbletonThread implements Runnable {
		public int sleepCounter = 1;
		public void run() {
			try {
				while (sleepCounter == 1) {
					sleepCounter = 0;
					Thread.sleep(50);
				}
				for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
					MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i);
					if (monomeConfig != null && !(monomeConfig instanceof FakeMonomeConfiguration)) {
						monomeConfig.redrawAbletonPages();
					}
				}
				redrawAbletonThread = null;
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Close Ableton OSC connections.
	 */
	public void stopAbleton() {
		if (this.abletonOSCPortIn != null) {
			if (this.abletonOSCPortIn.isListening()) {
				this.abletonOSCPortIn.stopListening();				
			}
			this.abletonOSCPortIn.close();
		}

		if (this.abletonOSCPortOut != null) {
			this.abletonOSCPortOut.close();
		}
		
		this.abletonControl = null;
	}
	
	/**
	 * Reads a given configuration file and sets up the object appropriately.
	 * 
	 * @param file the configuration file to read
	 */
	public boolean readConfigurationFile(File file) {
		try {
		    System.out.println("Reading configuration file: " + file.getAbsolutePath());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();

			// read <name> from the configuration file
			NodeList rootNL = doc.getElementsByTagName("name");
			Element rootEL = (Element) rootNL.item(0);
			NodeList rootNL2 = rootEL.getChildNodes();
			String name = ((Node) rootNL2.item(0)).getNodeValue();
			this.name = name;

			// read <hostname> from the configuration file
			rootNL = doc.getElementsByTagName("hostname");
			rootEL = (Element) rootNL.item(0);
			rootNL2 = rootEL.getChildNodes();
			String hostname = ((Node) rootNL2.item(0)).getNodeValue();
            setMonomeHostname(hostname);
			
            // read <zeroconfLibrary> from the configuration file
            rootNL = doc.getElementsByTagName("zeroconfLibrary");
            rootEL = (Element) rootNL.item(0);
            if (rootEL != null) {
                rootNL2 = rootEL.getChildNodes();
                String zeroconfLibrary = ((Node) rootNL2.item(0)).getNodeValue();
                try {
                    Main.main.zeroconfLibrary = Integer.parseInt(zeroconfLibrary);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

			// read <oscinport> from the configuration file
			rootNL = doc.getElementsByTagName("oscinport");
			rootEL = (Element) rootNL.item(0);
			rootNL2 = rootEL.getChildNodes();
			String oscinport = ((Node) rootNL2.item(0)).getNodeValue();

			setMonomeSerialOSCInPortNumber(Integer.valueOf(oscinport).intValue());

			// read <oscoutport> from the configuration file
			rootNL = doc.getElementsByTagName("oscoutport");
			rootEL = (Element) rootNL.item(0);
			rootNL2 = rootEL.getChildNodes();
			String oscoutport = ((Node) rootNL2.item(0)).getNodeValue();

			setMonomeSerialOSCOutPortNumber(Integer.valueOf(oscoutport).intValue());

			// read <abletonhostname> from the configuration file
			rootNL = doc.getElementsByTagName("abletonhostname");
			rootEL = (Element) rootNL.item(0);
			// old versions might not have this setting
			if (rootEL != null) {
				rootNL2 = rootEL.getChildNodes();
				String abletonhostname = ((Node) rootNL2.item(0)).getNodeValue();
				setAbletonHostname(abletonhostname);
			}

			// read <abletonoscinport> from the configuration file
			rootNL = doc.getElementsByTagName("abletonoscinport");
			rootEL = (Element) rootNL.item(0);
			// old versions might not have this setting
			if (rootEL != null) {
				rootNL2 = rootEL.getChildNodes();
				String abletonoscinport = ((Node) rootNL2.item(0)).getNodeValue();
				setAbletonOSCInPortNumber(Integer.valueOf(abletonoscinport).intValue());
			}

			// read <abletonoscoutport> from the configuration file
			rootNL = doc.getElementsByTagName("abletonoscoutport");
			rootEL = (Element) rootNL.item(0);
			// old versions might not have this setting
			if (rootEL != null) {
				rootNL2 = rootEL.getChildNodes();
				String abletonoscoutport = ((Node) rootNL2.item(0)).getNodeValue();
				setAbletonOSCOutPortNumber(Integer.valueOf(abletonoscoutport).intValue());
			}
			
			// read <abletonignoreviewtrack> from the configuration file
			rootNL = doc.getElementsByTagName("abletonignoreviewtrack");
			rootEL = (Element) rootNL.item(0);
			// old versions might not have this setting
			if (rootEL != null) {
				rootNL2 = rootEL.getChildNodes();
				String abletonignoreviewtrack = ((Node) rootNL2.item(0)).getNodeValue();
				if (abletonignoreviewtrack.compareTo("true") == 0) {
					this.abletonIgnoreViewTrack = true;
				} else {
					this.abletonIgnoreViewTrack = false;
				}
			}
			initAbleton();

			// read <midiinport> from the configuration file
			rootNL = doc.getElementsByTagName("midiinport");
			for (int i=0; i < rootNL.getLength(); i++) {
				rootEL = (Element) rootNL.item(i);
				rootNL2 = rootEL.getChildNodes();
				String midiinport = ((Node) rootNL2.item(0)).getNodeValue();
				actionAddMidiInput(midiinport);
			}

			// read all <midioutport> tags from the configuration file
			rootNL = doc.getElementsByTagName("midioutport");
			for (int i=0; i < rootNL.getLength(); i++) {
				rootEL = (Element) rootNL.item(i);
				rootNL2 = rootEL.getChildNodes();
				String midioutport = ((Node) rootNL2.item(0)).getNodeValue();
				actionAddMidiOutput(midioutport);
			}

			// read in each <monome> block
			rootNL = doc.getElementsByTagName("monome");
			for (int i=0; i < rootNL.getLength(); i++) {
				Node node = rootNL.item(i);					
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element monomeElement = (Element) node;

					// set the monome prefix
					NodeList nl = monomeElement.getElementsByTagName("prefix");
					Element el = (Element) nl.item(0);
					nl = el.getChildNodes();
					String prefix = "";
					if (nl.item(0) != null) {
						prefix = ((Node) nl.item(0)).getNodeValue();
					}	
					
					// set the monome prefix
					nl = monomeElement.getElementsByTagName("serial");
					el = (Element) nl.item(0);
					String serial = "no serial";
					if (el != null) {
						nl = el.getChildNodes();
						if (nl.item(0) != null) {
							serial = ((Node) nl.item(0)).getNodeValue();
						}
					}
					
					// set the monome serialosc hostname if present
					String serialOSCHostName = null;
					nl = monomeElement.getElementsByTagName("serialOSCHostname");
					if (nl != null) {
						el = (Element) nl.item(0);
						if (el != null) {
							nl = el.getChildNodes();
							if (nl.item(0) != null) {
								serialOSCHostName = ((Node) nl.item(0)).getNodeValue();
							}
						}
					}
					
					// set the width of the monome
					nl = monomeElement.getElementsByTagName("sizeX");
					el = (Element) nl.item(0);
					nl = el.getChildNodes();
					String sizeX = ((Node) nl.item(0)).getNodeValue();

					// set the height of the monome
					nl = monomeElement.getElementsByTagName("sizeY");
					el = (Element) nl.item(0);
					nl = el.getChildNodes();
					String sizeY = ((Node) nl.item(0)).getNodeValue();
					
					// set the height of the monome
					String altClear = "";
					nl = monomeElement.getElementsByTagName("altClear");
					if (nl != null) {
						el = (Element) nl.item(0);
						if (el != null) {
							nl = el.getChildNodes();
							altClear = ((Node) nl.item(0)).getNodeValue();
						}
					}
					
					boolean boolUsePageChangeButton = true;
					nl = monomeElement.getElementsByTagName("usePageChangeButton");
					el = (Element) nl.item(0);
					if (el != null) {
						nl = el.getChildNodes();
						String usePageChangeButton = ((Node) nl.item(0)).getNodeValue();
						if (usePageChangeButton.equals("false")) {
							boolUsePageChangeButton = false;
						}
					}
					
					boolean boolUseMIDIPageChanging = false;
					nl = monomeElement.getElementsByTagName("useMIDIPageChanging");
					el = (Element) nl.item(0);
					if (el != null) {
						nl = el.getChildNodes();
						String useMIDIPageChanging = ((Node) nl.item(0)).getNodeValue();
						if (useMIDIPageChanging.equals("true")) {
							boolUseMIDIPageChanging = true;
						}
					}
					
					NodeList rootNL3 = monomeElement.getElementsByTagName("MIDIPageChangeRule");
					ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
					for (int i2=0; i2 < rootNL3.getLength(); i2++) {
						Node node2 = rootNL3.item(i2);					
						if (node2.getNodeType() == Node.ELEMENT_NODE) {
							Element monomeElement2 = (Element) node2;
						
							NodeList nl2 = monomeElement2.getElementsByTagName("pageIndex");
							Element el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String pageIndex = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("note");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String note = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("channel");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String channel = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("cc");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String cc = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("ccVal");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String ccVal = ((Node) nl2.item(0)).getNodeValue();
							
							MIDIPageChangeRule mpcr = new MIDIPageChangeRule(Integer.valueOf(note).intValue(), Integer.valueOf(channel).intValue(), Integer.valueOf(cc).intValue(), Integer.valueOf(ccVal).intValue(), Integer.valueOf(pageIndex).intValue());
							midiPageChangeRules.add(mpcr);
							
                            nl2 = monomeElement2.getElementsByTagName("linkedSerial");
                            el2 = (Element) nl2.item(0);
                            String linkedSerial = null;
                            if (el2 != null) {
                                nl2 = el2.getChildNodes();
                                linkedSerial = ((Node) nl2.item(0)).getNodeValue();
                                if (linkedSerial != null && linkedSerial.compareTo("null") != 0)
                                    mpcr.setLinkedSerial(linkedSerial);
                            }
                            
                            if (linkedSerial != null && linkedSerial.compareTo("null") != 0) {
                                nl2 = monomeElement2.getElementsByTagName("linkedPageIndex");
                                el2 = (Element) nl2.item(0);
                                if (el2 != null) {
                                    nl2 = el2.getChildNodes();
                                    String linkedPageIndex = ((Node) nl2.item(0)).getNodeValue();
                                    mpcr.setLinkedPageIndex(Integer.valueOf(linkedPageIndex).intValue());
                                }
                            }
						}
					}

					
					// create the new monome configuration and display its window
					MonomeConfiguration monomeConfig = addMonomeConfiguration(i, prefix, serial, Integer.valueOf(sizeX).intValue(), 
							Integer.valueOf(sizeY).intValue(), boolUsePageChangeButton, boolUseMIDIPageChanging, midiPageChangeRules);
					if (altClear.compareTo("on") == 0) {
						monomeConfig.altClear = true;
					}
					monomeConfig.serialOSCHostname = serialOSCHostName;
					monomeConfig.deviceFrame.updateMidiInMenuOptions(MidiDeviceFactory.getMidiInOptions());
					monomeConfig.deviceFrame.updateMidiOutMenuOptions(MidiDeviceFactory.getMidiOutOptions());
										
					String s;
					float [] min = {0,0,0,0};
					NodeList minNL = monomeElement.getElementsByTagName("min");
					for (int j=0; j < minNL.getLength(); j++) {
						el = (Element) minNL.item(j);
						if (el != null) {							
							nl = el.getChildNodes();
							s = ((Node) nl.item(0)).getNodeValue();
							min[j] = Float.parseFloat(s.trim());
							//monomeConfig.adcObj.setMin(min);
						}
					}
					
					float [] max = {1,1,1,1};
					NodeList maxNL = monomeElement.getElementsByTagName("max");
					for (int j=0; j < maxNL.getLength(); j++) {
						el = (Element) maxNL.item(j);
						if (el != null) {	
							nl = el.getChildNodes();
							s = ((Node) nl.item(0)).getNodeValue();
							max[j] = Float.parseFloat(s.trim());
							//monomeConfig.adcObj.setMax(max);
						}
					}
					
					// enable tilt
					/*
					nl = monomeElement.getElementsByTagName("adcEnabled");
					el = (Element) nl.item(0);
					if (el != null) {
						nl = el.getChildNodes();
						String enabled = ((Node) nl.item(0)).getNodeValue();
						monomeConfig.adcObj.setEnabled(Boolean.parseBoolean(enabled));
					}
					*/
					
					NodeList pcmidiNL = monomeElement.getElementsByTagName("selectedpagechangemidiinport");
					for (int k=0; k < pcmidiNL.getLength(); k++) {
						el = (Element) pcmidiNL.item(k);
						if(el != null) {
							nl = el.getChildNodes();
							String midintport = ((Node) nl.item(0)).getNodeValue();
							monomeConfig.togglePageChangeMidiInDevice(midintport);
						}
					}
					
					// read in each page of the monome
					monomeConfig.curPage = -1;
					NodeList pageNL = monomeElement.getElementsByTagName("page");
					for (int j=0; j < pageNL.getLength(); j++) {
						Node pageNode = pageNL.item(j);
						if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
							Element pageElement = (Element) pageNode;
							String pageClazz = pageElement.getAttribute("class");

							// all pages have a name
							nl = pageElement.getElementsByTagName("name");
							el = (Element) nl.item(0);
							nl = el.getChildNodes();
							String pageName = ((Node) nl.item(0)).getNodeValue();
							Page page;
							page = monomeConfig.addPage(pageClazz);
							page.setName(pageName);
							monomeConfig.curPage++;

							// most pages have midi outputs
							NodeList midiNL = pageElement.getElementsByTagName("selectedmidioutport");
							for (int k=0; k < midiNL.getLength(); k++) {
								el = (Element) midiNL.item(k);
								if(el != null) {
									nl = el.getChildNodes();
									String midioutport = ((Node) nl.item(0)).getNodeValue();
									monomeConfig.toggleMidiOutDevice(midioutport);
								}
							}
							
							// most pages have midi inputs
							midiNL = pageElement.getElementsByTagName("selectedmidiinport");
							for (int k=0; k < midiNL.getLength(); k++) {
								el = (Element) midiNL.item(k);
								if(el != null) {
									nl = el.getChildNodes();
									String midintport = ((Node) nl.item(0)).getNodeValue();
									monomeConfig.toggleMidiInDevice(midintport);
								}
							}
							page.configure(pageElement);
							
							
							int pageChangeDelay = 0;
							nl = pageElement.getElementsByTagName("pageChangeDelay");
							el = (Element) nl.item(0);
							if (el != null) {
								nl = el.getChildNodes();
								String sPageChangeDelay = ((Node) nl.item(0)).getNodeValue();
								try {
									pageChangeDelay = Integer.parseInt(sPageChangeDelay);
								} catch (NumberFormatException ex) {
									ex.printStackTrace();
								}
							}
							monomeConfig.pageChangeDelays[monomeConfig.curPage] = pageChangeDelay;
						}
					}
					
					NodeList lengthNL = monomeElement.getElementsByTagName("patternlength");
					for (int k=0; k < lengthNL.getLength(); k++) {
						el = (Element) lengthNL.item(k);
						nl = el.getChildNodes();
						String patternLength = ((Node) nl.item(0)).getNodeValue();
						int length = Integer.parseInt(patternLength);
						monomeConfig.setPatternLength(k, length);
					}
					NodeList quantifyNL = monomeElement.getElementsByTagName("quantization");
					for (int k=0; k < quantifyNL.getLength(); k++) {
						el = (Element) quantifyNL.item(k);
						nl = el.getChildNodes();
						String quantization = ((Node) nl.item(0)).getNodeValue();
						int quantify = Integer.parseInt(quantization);
						monomeConfig.setQuantization(k, quantify);
					}
		            if (serialOSCHostName == null) {
		                startMonomeSerialOSC();
		            }
				}
			}
			
			// read in each <arc> block
			rootNL = doc.getElementsByTagName("arc");
			for (int i=0; i < rootNL.getLength(); i++) {
				Node node = rootNL.item(i);					
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element monomeElement = (Element) node;

					// set the monome prefix
					NodeList nl = monomeElement.getElementsByTagName("prefix");
					Element el = (Element) nl.item(0);
					nl = el.getChildNodes();
					String prefix = "";
					if (nl.item(0) != null) {
						prefix = ((Node) nl.item(0)).getNodeValue();
					}	
					
					// set the monome prefix
					nl = monomeElement.getElementsByTagName("serial");
					el = (Element) nl.item(0);
					String serial = "no serial";
					if (el != null) {
						nl = el.getChildNodes();
						if (nl.item(0) != null) {
							serial = ((Node) nl.item(0)).getNodeValue();
						}
					}
					
					// set the monome serialosc hostname if present
					String serialOSCHostName = null;
					nl = monomeElement.getElementsByTagName("serialOSCHostname");
					if (nl != null) {
						el = (Element) nl.item(0);
						if (el != null) {
							nl = el.getChildNodes();
							if (nl.item(0) != null) {
								serialOSCHostName = ((Node) nl.item(0)).getNodeValue();
							}
						}
					}
					
					// set the number of knobs
					nl = monomeElement.getElementsByTagName("knobs");
					el = (Element) nl.item(0);
					nl = el.getChildNodes();
					String knobs = ((Node) nl.item(0)).getNodeValue();
					
					boolean boolUseMIDIPageChanging = false;
					nl = monomeElement.getElementsByTagName("useMIDIPageChanging");
					el = (Element) nl.item(0);
					if (el != null) {
						nl = el.getChildNodes();
						String useMIDIPageChanging = ((Node) nl.item(0)).getNodeValue();
						if (useMIDIPageChanging.equals("true")) {
							boolUseMIDIPageChanging = true;
						}
					}
					
					NodeList rootNL3 = monomeElement.getElementsByTagName("MIDIPageChangeRule");
					ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
					for (int i2=0; i2 < rootNL3.getLength(); i2++) {
						Node node2 = rootNL3.item(i2);					
						if (node2.getNodeType() == Node.ELEMENT_NODE) {
							Element monomeElement2 = (Element) node2;
						
							NodeList nl2 = monomeElement2.getElementsByTagName("pageIndex");
							Element el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String pageIndex = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("note");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String note = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("channel");
							el2 = (Element) nl2.item(0);
							nl2 = el2.getChildNodes();
							String channel = ((Node) nl2.item(0)).getNodeValue();
							
							nl2 = monomeElement2.getElementsByTagName("cc");
							String cc = "-1";
							if (nl2 != null) {
								el2 = (Element) nl2.item(0);
								if (el2 != null) {
									nl2 = el2.getChildNodes();
									cc = ((Node) nl2.item(0)).getNodeValue();
								}
							}
							
							String ccVal = "-1";
							nl2 = monomeElement2.getElementsByTagName("ccVal");
							if (nl2 != null) {
								el2 = (Element) nl2.item(0);
								if (el2 != null) {
									nl2 = el2.getChildNodes();
									ccVal = ((Node) nl2.item(0)).getNodeValue();
								}
							}
							
							MIDIPageChangeRule mpcr = new MIDIPageChangeRule(Integer.valueOf(note).intValue(), Integer.valueOf(channel).intValue(), Integer.valueOf(cc).intValue(), Integer.valueOf(ccVal).intValue(), Integer.valueOf(pageIndex).intValue());
							midiPageChangeRules.add(mpcr);
						}
					}

					
					// create the new arc configuration and display its window
					ArcConfiguration arcConfig = addArcConfiguration(i, prefix, serial, Integer.parseInt(knobs), boolUseMIDIPageChanging, midiPageChangeRules);
					arcConfig.serialOSCHostname = serialOSCHostName;
					arcConfig.deviceFrame.updateMidiInMenuOptions(MidiDeviceFactory.getMidiInOptions());
					arcConfig.deviceFrame.updateMidiOutMenuOptions(MidiDeviceFactory.getMidiOutOptions());

					NodeList pcmidiNL = monomeElement.getElementsByTagName("selectedpagechangemidiinport");
					for (int k=0; k < pcmidiNL.getLength(); k++) {
						el = (Element) pcmidiNL.item(k);
						if(el != null) {
							nl = el.getChildNodes();
							String midintport = ((Node) nl.item(0)).getNodeValue();
							arcConfig.togglePageChangeMidiInDevice(midintport);
						}
					}
					
					// read in each page of the arc
					arcConfig.curPage = -1;
					NodeList pageNL = monomeElement.getElementsByTagName("page");
					for (int j=0; j < pageNL.getLength(); j++) {
						Node pageNode = pageNL.item(j);
						if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
							Element pageElement = (Element) pageNode;
							String pageClazz = pageElement.getAttribute("class");

							// all pages have a name
							nl = pageElement.getElementsByTagName("name");
							el = (Element) nl.item(0);
							nl = el.getChildNodes();
							String pageName = ((Node) nl.item(0)).getNodeValue();
							ArcPage page;
							page = arcConfig.addPage(pageClazz);
							page.setName(pageName);
							arcConfig.curPage++;

							// most pages have midi outputs
							NodeList midiNL = pageElement.getElementsByTagName("selectedmidioutport");
							for (int k=0; k < midiNL.getLength(); k++) {
								el = (Element) midiNL.item(k);
								if(el != null) {
									nl = el.getChildNodes();
									String midioutport = ((Node) nl.item(0)).getNodeValue();
									arcConfig.toggleMidiOutDevice(midioutport);
								}
							}
							
							// most pages have midi inputs
							midiNL = pageElement.getElementsByTagName("selectedmidiinport");
							for (int k=0; k < midiNL.getLength(); k++) {
								el = (Element) midiNL.item(k);
								if(el != null) {
									nl = el.getChildNodes();
									String midintport = ((Node) nl.item(0)).getNodeValue();
									arcConfig.toggleMidiInDevice(midintport);
								}
							}
							page.configure(pageElement);
						}
					}
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}

	/**
	 * Converts the current configuration to a string of XML.  
	 * 
	 * @return The string of XML representing the current configuration
	 */
	public String toXml() {
		String xml;
		// main configuration
		xml  = "<configuration>\n";
		xml += "  <name>" + this.name + "</name>\n";
		xml += "  <hostname>" + this.monomeHostname + "</hostname>\n";
        xml += "  <zeroconfLibrary>" + Main.main.zeroconfLibrary + "</zeroconfLibrary>\n";
		xml += "  <oscinport>" + this.monomeSerialOSCInPortNumber + "</oscinport>\n";
		xml += "  <oscoutport>" + this.monomeSerialOSCOutPortNumber + "</oscoutport>\n";
		xml += "  <abletonhostname>" + this.abletonHostname + "</abletonhostname>\n";
		xml += "  <abletonoscinport>" + this.abletonOSCInPortNumber + "</abletonoscinport>\n";
		xml += "  <abletonoscoutport>" + this.abletonOSCOutPortNumber + "</abletonoscoutport>\n";
		String ignoreViewTrack = "false";
		if (this.abletonIgnoreViewTrack) {
			ignoreViewTrack = "true";
		}
		xml += "  <abletonignoreviewtrack>" + ignoreViewTrack + "</abletonignoreviewtrack>\n";
		for (int i=0; i < MidiDeviceFactory.midiInDevices.size(); i++) {
			xml += "  <midiinport>" + StringEscapeUtils.escapeXml(MidiDeviceFactory.midiInDevices.get(i).getDeviceInfo().toString()) + "</midiinport>\n";
		}
		for (int i=0; i < MidiDeviceFactory.midiOutDevices.size(); i++) {
			xml += "  <midioutport>" + StringEscapeUtils.escapeXml(MidiDeviceFactory.midiOutDevices.get(i).getDeviceInfo().toString()) + "</midioutport>\n";
		}

		// monome and page configuration
		for (int i=0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(i); 
			if (monomeConfig == null || monomeConfig instanceof FakeMonomeConfiguration) {
				continue;
			}
			xml += MonomeConfigurationFactory.getMonomeConfiguration(i).toXml();
		}
		
		// arc and page configuration
		for (int i=0; i < ArcConfigurationFactory.getNumArcConfigurations(); i++) {
			ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(i); 
			if (arcConfig == null) {
				continue;
			}
			xml += ArcConfigurationFactory.getArcConfiguration(i).toXml();
		}
		xml += "</configuration>\n";
		return xml;
	}

	public void setAbletonIgnoreViewTrack(boolean selected) {
		this.abletonIgnoreViewTrack = selected;
	}

	public boolean getAbletonIgnoreViewTrack() {
		return this.abletonIgnoreViewTrack;
	}	
}
