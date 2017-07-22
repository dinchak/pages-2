/*
 *  MonomeConfiguration.java
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

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.apache.commons.lang.StringEscapeUtils;

import org.monome.pages.Main;
import org.monome.pages.gui.MonomeDisplayFrame;
import org.monome.pages.gui.MonomeFrame;

import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.Page;
import org.monome.pages.pages.QuadrantsPage;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

/**
 * @author Administrator
 *
 */
public class MonomeConfiguration extends OSCDeviceConfiguration<Page> {

    /**
	 * The monome's width (ie. 8 or 16)
	 */
	public int sizeX;

	/**
	 * The monome's height (ie. 8 or 16) 
	 */
	public int sizeY;

    /**
	 * ledState[x][y] - The LED state cache for the monome
	 */
	public int[][] ledState = new int[32][32];

	/**
	 * pageState[page_num][x][y] - The LED state cache for each page
	 */
	public int[][][] pageState = new int[255][32][32];

    /**
	 * The monome's pattern banks
	 */
	public ArrayList<PatternBank> patternBanks = new ArrayList<PatternBank>();

    /**
	 * The options dropdown when creating a new page (contains a list of all page names)
	 */
	private String options[];

	/**
	 * 1 when the page change button is held down (bottom right button) 
	 */
	private int pageChangeMode = 0;

	/**
	 * true if a page has been changed while the page change button was held down 
	 */
	private boolean pageChanged = false;
	
	public int[] pageChangeDelays = new int[255];
	
	/**
	 * The current MIDI clock tick number (resets every measure, 1/96 resolution)
	 */
	private int tickNum = 0;
				
	/**
	 * true if the page change button should be active
	 */
	public boolean usePageChangeButton = true;

	/**
	 * true if MIDI page changing should be active
	 */
	public boolean useMIDIPageChanging = false;

	/**
	 * The array of devices that MIDI page change messages can come from
	 */
	String[] pageChangeMidiInDevices = new String[32];

	public int offsetX;

	public int offsetY;
	
	public boolean altClear = false;

	public int serialOSCPort;

	public transient OSCPortOut serialOSCPortOut;

	public String serialOSCHostname;

    public transient MonomeOSCListener oscListener;

    private MonomeFrame monomeFrame;
    
    public ArrayList<Press> pressesInPlayback = new ArrayList<Press>();

    /**
	 * @param index the index to assign to this MonomeConfiguration
	 * @param prefix the prefix of the monome (/40h)
	 * @param serial the serial # of the monome (if auto-discovery was used)
	 * @param sizeX the width of the monome in buttons (ie. 8)
	 * @param sizeY the height of the monome in buttons (ie. 8)
	 * @param usePageChangeButton if true enable the MIDI page change button
	 * @param useMIDIPageChanging if true use MIDI page changing rules
	 * @param midiPageChangeRules the set of MIDI page changing rules
	 * @param monomeFrame the GUI frame for this monome
	 */
	public MonomeConfiguration(int index, String prefix, String serial, int sizeX, int sizeY, boolean usePageChangeButton, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules, MonomeFrame monomeFrame) {
        super(index, prefix, serial);

        this.options = PagesRepository.getPageNames(Page.class);
		for (int i=0; i<options.length; i++) {
			options[i] = options[i].substring(17);					
		}
        this.sizeX = sizeX;
		this.sizeY = sizeY;

		this.midiPageChangeRules = midiPageChangeRules;
		this.usePageChangeButton = usePageChangeButton;
		this.useMIDIPageChanging = useMIDIPageChanging;
		this.deviceFrame = monomeFrame;
        this.monomeFrame = monomeFrame;

		if (monomeFrame != null) {
			monomeFrame.updateMidiInMenuOptions(MidiDeviceFactory.getMidiInOptions());
			monomeFrame.updateMidiOutMenuOptions(MidiDeviceFactory.getMidiOutOptions());
		}

		this.clearMonome();
	}

    @Override
    protected void onPageAdd(String className, Page page) {
        int numPatterns = this.sizeX;
        this.patternBanks.add(this.numPages, new PatternBank(numPatterns, page));

        System.out.println("MonomeConfiguration " + this.serial + ": created " + className + " page");
    }

    @Override
    protected Class getPageType() {
        return Page.class;
    }

    /**
	 * Destroys this object.
	 *
	 */
	public void destroy() {
		for (int i = 0; i < this.numPages; i++) {
			deletePage(i);
		}
		MonomeConfigurationFactory.removeMonomeConfiguration(index);
	}

    /**
	 * Redraws only the Ableton pages (for when a new event arrives)
	 */
	public void redrawAbletonPages() {
		if (this.pages.size() == 0) {
			return;
		}
		for (int i = 0; i < this.pages.size(); i++) {
			pages.get(i).handleAbletonEvent();
		}
	}
	
	/**
	 * Handles a press event from the monome.
	 * 
	 * @param x The x coordinate of the button pressed.
	 * @param y The y coordinate of the button pressed.
	 * @param value The type of event (1 = press, 0 = release)
	 */
	public synchronized void handlePress(int x, int y, int value) {
		if (deviceFrame != null) {
			MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
			if (monomeDisplayFrame != null) {
				monomeDisplayFrame.press(x, y, value);
			}
		}
		
		// if we have no pages then dont handle any button presses
		if (this.pages.size() == 0) {
			return;
		}
		
		// if the monome isn't configured to handle this button then don't handle it
		// ie if you config a 256 as a 64 and hit a button out of range
		if (y >= this.sizeY || x >= this.sizeX) {
			return;
		}
				
		// stop here if we don't want to use the page change button
		if (this.usePageChangeButton == false) {
			// pass presses to the current page and record them in the pattern bank
			if (this.pages.get(curPage) != null) {
				this.patternBanks.get(curPage).recordPress(x, y, value, curPage);
				this.pages.get(curPage).handlePress(x, y, value);
			}
			return;
		}
		
		// if page change mode is on and this is a button on the bottom row then change page and return
		if (this.pageChangeMode == 1) {
			// if this is the bottom right button and we let go turn it off
			// and send the value == 1 press along to the page
			if (x == (this.sizeX - 1) && y == (this.sizeY - 1) && value == 0) {
				this.clear(0, -1);
				this.pageChangeMode = 0;
				if (this.pageChanged == false) {
					if (this.pages.get(curPage) != null) {
						this.pages.get(curPage).handlePress(x, y, 1);
						this.patternBanks.get(curPage).recordPress(x, y, 1, curPage);
						this.pages.get(curPage).handlePress(x, y, 0);
						this.patternBanks.get(curPage).recordPress(x, y, 0, curPage);
					}
				}
				if (this.pages.get(curPage) != null) {
					this.ledState = new int[32][32];
					this.pages.get(curPage).redrawDevice();
				}
				return;
			}
			int nextPage = x + ((this.sizeY - y - 1) * this.sizeX);
			int patternNum = x;
			int numPages = this.pages.size();
			if (numPages > this.sizeX - 1) {
				numPages++;
			}
			if (value == 1) {
    			if (numPages > nextPage && nextPage < (this.sizeX * this.sizeY) / 2) {
    				// offset back by one because of the page change button
    				if (nextPage > this.sizeX - 1) {
    					nextPage--;
    				}
    				this.curPage = nextPage;
    				this.switchPage(this.pages.get(this.curPage), this.curPage, true);
    			} else if (y == 0 && value == 1) {
    				this.pages.get(curPage).onBlur();
    				this.patternBanks.get(this.curPage).handlePress(patternNum);
    			}
    			this.pageChanged = true;
			}
			return;
		}

		// if this is the bottom right button and we pressed the button (value == 1), turn page change mode on
		if (x == (this.sizeX - 1) && y == (this.sizeY - 1) && value == 1) {
			this.pageChangeMode = 1;
			this.pageChanged = false;
			if (this.pageChangeDelays[this.curPage] > 0) {
				new Thread(new PageChangeTimer(this, this.pageChangeDelays[this.curPage])).start();
			}
			this.clear(0, -1);
			this.drawPatternState();
			this.pages.get(curPage).onBlur();
			return;
		}
		
		// pass presses to the current page and record them in the pattern bank
		if (this.pages.get(curPage) != null) {
			this.patternBanks.get(curPage).recordPress(x, y, value, curPage);
			this.pages.get(curPage).handlePress(x, y, value);
		}
	}

    @Override
    public void dispose() {
        if (monomeFrame != null && monomeFrame.monomeDisplayFrame != null) {
            monomeFrame.monomeDisplayFrame.dispose();
        }

        super.dispose();
    }

    class PageChangeTimer implements Runnable {

		MonomeConfiguration monome;
		int delay;
		
		public PageChangeTimer(MonomeConfiguration monome, int delay) {
			this.monome = monome;
			this.delay = delay;
		}
		
		public void run() {
			try {
				Thread.sleep(delay);
				this.monome.pageChanged = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}

	/**
	 * Draws the pattern state on the monome when the page change key is held down.
	 * Blinking = triggered, solid = recorded, dark = empty.
	 */
	public void drawPatternState() {
		for (int x=0; x < this.sizeX; x++) {
			if (this.patternBanks.get(curPage).getPatternState(x) == PatternBank.PATTERN_STATE_TRIGGERED) {
				if (this.ledState[x][0] == 1) {
					this.led(x, 0, 0, -1);
				} else {
					this.led(x, 0, 1, -1);
				}
			} else if (this.patternBanks.get(curPage).getPatternState(x) == PatternBank.PATTERN_STATE_RECORDED) {
				this.led(x, 0, 1, -1);
			} else if (this.patternBanks.get(curPage).getPatternState(x) == PatternBank.PATTERN_STATE_EMPTY) {
				this.led(x, 0, 0, -1);
			}
		}
		
		for (int x = 0; x < this.sizeX; x++) {
			for (int y = 0; y < this.sizeY; y++) {
				int pageNum = x + (y * this.sizeX);
				// offset by one for the page change button
				if (pageNum == this.sizeX - 1) {
					continue;
				}
				if (pageNum > this.sizeX - 1) {
					pageNum--;
				}
				if (pageNum < this.numPages) {
					int ledX = x;
					int ledY = this.sizeY - y - 1;
					if (pageNum == curPage) {
						if (this.ledState[ledX][ledY] == 1) {
							this.led(ledX, ledY, 0, -1);
						} else {
							this.led(ledX, ledY, 1, -1);
						}
					} else {
						this.led(ledX, ledY, 1, -1);
					}
				}
			}
		}
	}

	/**
	 * Called every time a MIDI clock sync 'tick' is received, this triggers each page's handleTick() method
	 */
	public synchronized void tick(MidiDevice device) {
		for (int i=0; i < this.numPages; i++) {
			for (int j = 0; j < this.midiInDevices[i].length; j++) {
				if (this.midiInDevices[i][j] == null) {
					continue;
				}
				if (this.midiInDevices[i][j].compareTo(device.getDeviceInfo().getName()) == 0) {
					ArrayList<Press> presses = patternBanks.get(i).getRecordedPresses();
					if (presses != null) {
						for (int k=0; k < presses.size(); k++) {
							int[] press = presses.get(k).getPress();
							for (int pb = 0; pb < this.pressesInPlayback.size(); pb++) {
								if (pressesInPlayback.get(pb) == null) continue;
								int[] pbPress = pressesInPlayback.get(pb).getPress();
								if (press[0] == pbPress[0] && press[1] == pbPress[1] && press[2] == 0) {
									pressesInPlayback.remove(pb);
								}
							}
							if (press[2] == 1) {
								pressesInPlayback.add(presses.get(k));
							}
							this.pages.get(i).handleRecordedPress(press[0], press[1], press[2], presses.get(k).getPatternNum());
						}
					}
                    this.patternBanks.get(i).handleTick();
					this.pages.get(i).handleTick(device);
				}
			}
		}
		if (this.pageChangeMode == 1 && this.tickNum % 12 == 0) {			
			this.drawPatternState();
		}
		this.tickNum++;
		if (this.tickNum == 96) {
			this.tickNum = 0;
		}
	}

	/**
	 * Called every time a MIDI clock sync 'reset' is received, this triggers each page's handleReset() method.
	 */
	public void reset(MidiDevice device) {
		for (int i=0; i < this.numPages; i++) {
			for (int j = 0; j < this.midiInDevices[i].length; j++) {
				if (this.midiInDevices[i][j] == null) {
					continue;
				}
				if (this.midiInDevices[i][j].compareTo(device.getDeviceInfo().getName()) == 0) {
					this.pages.get(i).handleReset();
					this.patternBanks.get(i).handleReset();
				}
			}
		}
		this.tickNum = 0;
	}

	/**
	 * Called every time a MIDI message is received, the messages are passed along to each page.
	 * 
	 * @param message The MIDI message received
	 * @param timeStamp The timestamp of the MIDI message
	 */
	public synchronized void send(MidiDevice device, MidiMessage message, long timeStamp) {
		if (this.useMIDIPageChanging) {
			if (message instanceof ShortMessage) {
				ShortMessage msg = (ShortMessage) message;
				int velocity = msg.getData1();
				if (msg.getCommand() == ShortMessage.NOTE_ON && velocity > 0) {
					int channel = msg.getChannel();
					int note = msg.getData1();
					
					for (int j = 0; j < this.pageChangeMidiInDevices.length; j++) {
						if (this.pageChangeMidiInDevices[j] == null) {
							continue;
						}
						if (this.pageChangeMidiInDevices[j].compareTo(device.getDeviceInfo().getName()) == 0) {
							for (int i = 0; i < this.midiPageChangeRules.size(); i++) {
								MIDIPageChangeRule mpcr = this.midiPageChangeRules.get(i);
								if (mpcr.checkNoteRule(note, channel) == true) {
									int switchToPageIndex = mpcr.getPageIndex();
									Page page = this.pages.get(switchToPageIndex);
									this.switchPage(page, switchToPageIndex, true);
								}
							}
						}
					}
				}
				if (msg.getCommand() == ShortMessage.CONTROL_CHANGE) {
					int cc = msg.getData1();
					int ccVal = msg.getData2();
					int channel = msg.getChannel();
					for (int j = 0; j < this.pageChangeMidiInDevices.length; j++) {
						if (this.pageChangeMidiInDevices[j] == null) {
							continue;
						}
						if (this.pageChangeMidiInDevices[j].compareTo(device.getDeviceInfo().getName()) == 0) {
							for (int i = 0; i < this.midiPageChangeRules.size(); i++) {
								MIDIPageChangeRule mpcr = this.midiPageChangeRules.get(i);
								if (mpcr.checkCCRule(cc, ccVal, channel) == true) {
									int switchToPageIndex = mpcr.getPageIndex();
									Page page = this.pages.get(switchToPageIndex);
									this.switchPage(page, switchToPageIndex, true);
								}
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < this.numPages; i++) {
			for (int j = 0; j < this.midiInDevices[i].length; j++) {
				if (this.midiInDevices[i][j] == null) {
					continue;
				}
				if (this.midiInDevices[i][j].compareTo(device.getDeviceInfo().getName()) == 0) {
					this.pages.get(i).send(message, timeStamp);
				}
			}
		}
	}
	
	/**
	 * Turns a MIDI In device on or off for the current page.
	 * 
	 * @param deviceName the MIDI device name
	 */
	public void toggleMidiInDevice(String deviceName) {
		if (curPage < 0 || curPage > 254) {
			return;
		}
		for (int i = 0; i < this.midiInDevices[this.curPage].length; i++) {
			// if this device was enabled, disable it
			if (this.midiInDevices[this.curPage][i] == null) {
				continue;
			}
			if (this.midiInDevices[this.curPage][i].compareTo(deviceName) == 0) {
				midiInDevices[this.curPage][i] = new String();
				if (this.deviceFrame != null) {
					this.deviceFrame.updateMidiInSelectedItems(midiInDevices[this.curPage]);
				}
				return;
			}
		}

		// if we didn't disable it, enable it
		for (int i = 0; i < this.midiInDevices[this.curPage].length; i++) {
			if (this.midiInDevices[this.curPage][i] == null) {
				this.midiInDevices[this.curPage][i] = deviceName;
				if (this.deviceFrame != null) {
					this.deviceFrame.updateMidiInSelectedItems(midiInDevices[this.curPage]);
				}
				return;
			}
		}
	}
	
	/**
	 * Toggles a MIDI In device as able to receive page change rules.
	 * 
	 * @param deviceName the name of the MIDI device
	 */
	public void togglePageChangeMidiInDevice(String deviceName) {
		for (int i = 0; i < this.pageChangeMidiInDevices.length; i++) {
			// if this device was enabled, disable it
			if (this.pageChangeMidiInDevices[i] == null) {
				continue;
			}
			if (this.pageChangeMidiInDevices[i].compareTo(deviceName) == 0) {
				pageChangeMidiInDevices[i] = new String();
				this.deviceFrame.updatePageChangeMidiInSelectedItems(pageChangeMidiInDevices);
				return;
			}
		}

		// if we didn't disable it, enable it
		for (int i = 0; i < this.pageChangeMidiInDevices.length; i++) {
			if (this.pageChangeMidiInDevices[i] == null) {
				this.pageChangeMidiInDevices[i] = deviceName;
				if (this.deviceFrame != null) {
					this.deviceFrame.updatePageChangeMidiInSelectedItems(pageChangeMidiInDevices);
				}
				return;
			}
		}
	}
	
	/**
	 * Toggles a MIDI Out device on or off for the current page.
	 * 
	 * @param deviceName the name of the MIDI device
	 */
	public void toggleMidiOutDevice(String deviceName) {
		if (curPage < 0 || curPage > 254) {
			return;
		}
		for (int i = 0; i < this.midiOutDevices[this.curPage].length; i++) {
			// if this device was enabled, disable it
			if (this.midiOutDevices[this.curPage][i] == null) {
				continue;
			}
			if (this.midiOutDevices[this.curPage][i].compareTo(deviceName) == 0) {
				midiOutDevices[this.curPage][i] = new String();
				if (this.deviceFrame != null) {
					this.deviceFrame.updateMidiOutSelectedItems(midiOutDevices[this.curPage]);
				}
				return;
			}
		}

		// if we didn't disable it, enable it
		for (int i = 0; i < this.midiOutDevices[this.curPage].length; i++) {
			if (this.midiOutDevices[this.curPage][i] == null) {
				this.midiOutDevices[this.curPage][i] = deviceName;
				if (this.deviceFrame != null) {
					this.deviceFrame.updateMidiOutSelectedItems(midiOutDevices[this.curPage]);
				}
				return;
			}
		}
	}

	/**
	 * Sends a /led x y value command to the monome if index is the selected page.
	 * 
	 * @param x The x coordinate of the led
	 * @param y The y coordinate of the led
	 * @param value The value of the led (1 = on, 0 = off)
	 * @param index The index of the page making the request
	 */
	public void led(int x, int y, int value, int index) {
		if (x < 0 || y < 0 || value < 0 || x >= this.sizeX || y >= this.sizeY || value > 1) {
			return;
		}
				
		if (index > -1) {
			this.pageState[index][x][y] = value;
	
			if (index != this.curPage) {
				return;
			}
			
			if (this.pageChangeMode == 1) {
				return;
			}
	
			if (this.pages.size() <= index || this.pages.get(index) == null) {
				return;
			}
	
			if (this.pages.get(index) != null && this.pages.get(index).getCacheDisabled() == false) {
				if (this.ledState[x][y] == value) {
					return;
				}	
			}
		}

		this.ledState[x][y] = value;
		
		if (this.deviceFrame != null) {
			MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
			if (monomeDisplayFrame != null) {
				monomeDisplayFrame.setLedState(ledState);
			}
		}
		
		Object args[] = new Object[3];
		args[0] = new Integer(x);
		args[1] = new Integer(y);
		args[2] = new Integer(value);
		OSCMessage msg;
		try {
			if (this.serialOSCPort == 0) {
				msg = new OSCMessage(this.prefix + "/led", args);
				if (Main.main.configuration.monomeSerialOSCPortOut != null) {
					Main.main.configuration.monomeSerialOSCPortOut.send(msg);
				}
			} else {
				msg = new OSCMessage(this.prefix + "/grid/led/set", args);
				serialOSCPortOut.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clear the monome.
	 */
	public void clearMonome() {
		this.clear(0, 0);
	}

	/**
	 * Sends a led_col message to the monome if index is the selected page.
	 * 
	 * @param col The column to effect
	 * @param value1 The first 8 bits of the value
	 * @param value2 The second 8 bits of the value
	 * @param index The index of the page making the call
	 */
	public void led_col(ArrayList<Integer> intArgs, int index) {
		int col = intArgs.get(0);
		if (col < 0 || col >= sizeX) {
			return;
		}
		int[] values = {0, 0, 0, 0};
		int numValues = 0;
		for (int i = 0; i < intArgs.size(); i++) {
			if (i > 4) {
				break;
			}
			values[i] = intArgs.get(i);
			numValues++;
		}
		int fullvalue = (values[3] << 16) + (values[2] << 8) + values[1];
		if (index > -1) {
			for (int y=0; y < (intArgs.size() - 1) * 8; y++) {
				if (y >= sizeY) {
					break;
				}
				int bit = (fullvalue >> (this.sizeY - y - 1)) & 1;
				this.pageState[index][col][sizeY - y - 1] = bit;
			}
	
			if (index != this.curPage) {
				return;
			}
			
			if (this.pageChangeMode == 1) {
				return;
			}
	
			for (int y=0; y < (intArgs.size() - 1) * 8; y++) {
				int bit = (fullvalue >> (this.sizeY - y - 1)) & 1;
				this.ledState[col][y] = bit;
			}
			
			if (this.deviceFrame != null) {
				MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
				if (monomeDisplayFrame != null) {
					monomeDisplayFrame.setLedState(ledState);
				}
			}
		}

		Object args[] = new Object[numValues];
		args[0] = new Integer(col);
		for (int i = 1; i < numValues; i++) {
			args[i] = (Integer) intArgs.get(i);
		}
		OSCMessage msg;
		try {
			if (this.serialOSCPort == 0) {
				msg = new OSCMessage(this.prefix + "/led_col", args);
				if (Main.main.configuration.monomeSerialOSCPortOut != null) {
					Main.main.configuration.monomeSerialOSCPortOut.send(msg);
				}
			} else {
				Object newArgs[] = new Object[numValues + 1];
				newArgs[0] = intArgs.get(0);
				newArgs[1] = new Integer(0);
				if (intArgs.size() > 1) {
					newArgs[2] = intArgs.get(1);
				}
				if (intArgs.size() > 2) {
					newArgs[3] = intArgs.get(2);
				}
				msg = new OSCMessage(this.prefix + "/grid/led/col", newArgs);
				serialOSCPortOut.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void led_map(ArrayList<Integer> intArgs, int index) {
		int xOffset = intArgs.get(0);
		int yOffset = intArgs.get(1);
		for (int y = 0; y < 8; y++) {
			int val = intArgs.get(y + 2);
			for (int x = 0 ; x < 8; x++) {
				this.pageState[index][x + xOffset][y + yOffset] = val & (1 << x);
				if (index != this.curPage) {
					continue;
				}
				this.ledState[x + xOffset][y + yOffset] = val & (1 << x);
			}
		}
		if (index == this.curPage) {
			if (this.deviceFrame != null) {
				MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
				if (monomeDisplayFrame != null) {
					monomeDisplayFrame.setLedState(ledState);
				}
			}
		}
		if (index != this.curPage) {
			return;
		}
		Object args[] = new Object[10];
		args[0] = xOffset;
		args[1] = yOffset;
		for (int i = 2; i < 10; i++) {
			args[i] = (Integer) intArgs.get(i);
		}
		OSCMessage msg;
		try {
			if (this.serialOSCPort == 0) {
				return;
			}
			msg = new OSCMessage(this.prefix + "/grid/led/map", args);
			if (serialOSCPortOut != null) {
				serialOSCPortOut.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a led_row message to the monome if index is the selected page.
	 * 
	 * @param row The row to effect
	 * @param value1 The first 8 bits of the value
	 * @param value2 The second 8 bits of the value
	 * @param index The index of the page making the call
	 */
	public void led_row(ArrayList<Integer> intArgs, int index) {
		int row = intArgs.get(0);
		if (row < 0 || row >= sizeY) {
			return;
		}
		int[] values = {0, 0, 0, 0};
		int numValues = 0;
		for (int i = 0; i < intArgs.size(); i++) {
			if (i > 4) {
				break;
			}
			values[i] = intArgs.get(i);
			numValues++;
		}
		int fullvalue = (values[3] << 16) + (values[2] << 8) + values[1];
		if (index > -1) {
			for (int x=0; x < (intArgs.size() - 1) * 8; x++) {
				if (x >= sizeX) {
					break;
				}
				int bit = (fullvalue >> (this.sizeX - x- 1)) & 1;
				this.pageState[index][sizeX - x - 1][row] = bit;
			}
	
			if (index != this.curPage) {
				return;
			}
			
			if (this.pageChangeMode == 1) {
				return;
			}
	
			for (int x=0; x < (intArgs.size() - 1) * 8; x++) {
				int bit = (fullvalue >> (this.sizeX - x - 1)) & 1;
				this.ledState[x][row] = bit;
			}
			
			if (this.deviceFrame != null) {
				MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
				if (monomeDisplayFrame != null) {
					monomeDisplayFrame.setLedState(ledState);
				}
			}
		}

		Object args[] = new Object[numValues];
		args[0] = new Integer(row);
		for (int i = 0; i < numValues; i++) {
			args[i] = (Integer) intArgs.get(i);
		}
		OSCMessage msg;
		try {
			if (this.serialOSCPort == 0) {
				msg = new OSCMessage(this.prefix + "/led_row", args);
				if (Main.main.configuration.monomeSerialOSCPortOut != null) {
					Main.main.configuration.monomeSerialOSCPortOut.send(msg);
				}
			} else {
				Object newArgs[] = new Object[numValues + 1];
				newArgs[0] = new Integer(0);
				newArgs[1] = intArgs.get(0);
				if (intArgs.size() > 1) {
					newArgs[2] = intArgs.get(1);
				}
				if (intArgs.size() > 2) {
					newArgs[3] = intArgs.get(2);
				}
				msg = new OSCMessage(this.prefix + "/grid/led/row", newArgs);
				if (serialOSCPortOut != null) {
					serialOSCPortOut.send(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a frame message to the monome if index is the selected page
	 * TODO: implement this method
	 * 
	 * @param x 
	 * @param y
	 * @param values
	 * @param index
	 */
	public void frame(int x, int y, int[] values, int index) {
		for (int i=0; i < values.length; i++) {
		}
	}

	/**
	 * Sends a clear message to the monome if index is the selected page
	 * 
	 * @param state See monome OSC spec 
	 * @param index The index of the page making the call
	 */
	public synchronized void clear(int state, int index) {		
		if (state == 0 || state == 1) {
			
			if (index > -1) {
				for (int x = 0; x < this.sizeX; x++) {
					for (int y = 0; y < this.sizeY; y++) {
						this.pageState[index][x][y] = state;
					}
				}
	
				if (index != this.curPage) {
					return;
				}
				
				if (this.pageChangeMode == 1) {
					return;
				}
	
				for (int x = 0; x < this.sizeX; x++) {
					for (int y = 0; y < this.sizeY; y++) {
						this.ledState[x][y] = state;
					}
				}
				
				if (this.deviceFrame != null) {
					MonomeDisplayFrame monomeDisplayFrame = monomeFrame.getMonomeDisplayFrame();
					if (monomeDisplayFrame != null) {
						monomeDisplayFrame.setLedState(ledState);
					}
				}
			}

			Object args[] = new Object[1];
			args[0] = new Integer(state);
			OSCMessage msg;
			try {
				if (this.serialOSCPort == 0) {
					if (altClear) {
						for (int y = 0; y < this.sizeY; y++) {
							ArrayList<Integer> argz = new ArrayList<Integer>();
							argz.add(y);
							argz.add(0);
							argz.add(0);
							this.led_row(argz, index);
						}
					} else {
						msg = new OSCMessage(this.prefix + "/clear", args);
						Configuration configuration = Main.main.configuration;
						if (configuration != null && configuration.monomeSerialOSCPortOut != null) {
							configuration.monomeSerialOSCPortOut.send(msg);
						}
					}
				} else {
					msg = new OSCMessage(this.prefix + "/grid/led/all", args);
					if (serialOSCPortOut != null) {
						serialOSCPortOut.send(msg);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Converts the current monome configuration to XML.
	 * 
	 * @return XML representing the current monome configuration
	 */
	public String toXml() {
		String xml = "";
		xml += "  <monome>\n";
		xml += "    <prefix>" + this.prefix + "</prefix>\n";
		xml += "    <serial>" + this.serial + "</serial>\n";
		xml += "    <sizeX>" + this.sizeX + "</sizeX>\n";
		xml += "    <sizeY>" + this.sizeY + "</sizeY>\n";
		if (this.serialOSCHostname != null) {
			xml += "    <serialOSCHostname>" + this.serialOSCHostname + "</serialOSCHostname>\n";
		}
		String state = "off";
		if (altClear) {
			state = "on";
		}
		xml += "    <altClear>" + state + "</altClear>\n";
		xml += "    <usePageChangeButton>" + (this.usePageChangeButton ? "true" : "false") + "</usePageChangeButton>\n";
		xml += "    <useMIDIPageChanging>" + (this.useMIDIPageChanging ? "true" : "false") + "</useMIDIPageChanging>\n";
		for (int i = 0; i < this.pageChangeMidiInDevices.length; i++ ) {
			if (pageChangeMidiInDevices[i] == null || pageChangeMidiInDevices[i].compareTo("") == 0) {
				continue;
			}
			xml += "    <selectedpagechangemidiinport>" + StringEscapeUtils.escapeXml(pageChangeMidiInDevices[i]) + "</selectedpagechangemidiinport>\n";
		}
		for (int i = 0; i < this.midiPageChangeRules.size(); i++) {
			MIDIPageChangeRule mpcr = this.midiPageChangeRules.get(i);
			if (mpcr != null) {
				xml += "    <MIDIPageChangeRule>\n";
				xml += "      <pageIndex>" + mpcr.getPageIndex() + "</pageIndex>\n";
				xml += "      <note>" + mpcr.getNote() + "</note>\n";
				xml += "      <channel>" + mpcr.getChannel() + "</channel>\n";
				xml += "      <cc>" + mpcr.getCC() + "</cc>\n";
				xml += "      <ccVal>" + mpcr.getCCVal() + "</ccVal>\n";
                xml += "      <linkedSerial>" + mpcr.getLinkedSerial() + "</linkedSerial>\n";
                xml += "      <linkedPageIndex>" + mpcr.getLinkedPageIndex() + "</linkedPageIndex>\n";
				xml += "    </MIDIPageChangeRule>\n";
			}
		}
		
		for (int i=0; i < this.numPages; i++) {
			if (this.pages.get(i).toXml() != null) {
				xml += "    <page class=\"" + this.pages.get(i).getClass().getName() + "\">\n";
				xml += this.pages.get(i).toXml();
				for (int j=0; j < midiInDevices[i].length; j++) {
					if (midiInDevices[i][j] == null || midiInDevices[i][j].compareTo("") == 0) {
						continue;
					}
					xml += "      <selectedmidiinport>" + StringEscapeUtils.escapeXml(midiInDevices[i][j]) + "</selectedmidiinport>\n"; 
				}
				for (int j=0; j < midiOutDevices[i].length; j++) {
					if (midiOutDevices[i][j] == null || midiOutDevices[i][j].compareTo("") == 0) {
						continue;
					}
					xml += "      <selectedmidioutport>" + StringEscapeUtils.escapeXml(midiOutDevices[i][j]) + "</selectedmidioutport>\n"; 
				}
				xml += "      <pageChangeDelay>" + this.pageChangeDelays[i] + "</pageChangeDelay>\n";
				int patternLength = this.patternBanks.get(i).getPatternLength();
				int quantization = this.patternBanks.get(i).getQuantization();
				xml += "      <patternlength>" + patternLength + "</patternlength>\n";
				xml += "      <quantization>" + quantization + "</quantization>\n";
				xml += "    </page>\n";
			}
		}
		xml += "  </monome>\n";
		return xml;
	}
	
	public void setPatternLength(int pageNum, int length) {
		if (this.patternBanks.size() <= pageNum) {
			this.patternBanks.add(pageNum, new PatternBank(this.sizeX, this.pages.get(pageNum)));
		}
		this.patternBanks.get(pageNum).setPatternLength(length);
		if (this.pages.get(pageNum) instanceof QuadrantsPage) {
			QuadrantsPage page = (QuadrantsPage) this.pages.get(pageNum);
			page.setPatternLength(length);
		}
	}
	
	public void setQuantization(int pageNum, int quantization) {
		if (this.patternBanks.size() <= pageNum) {
			this.patternBanks.add(pageNum, new PatternBank(this.sizeX, this.pages.get(pageNum)));
		}
		this.patternBanks.get(pageNum).setQuantization(quantization);
		if (this.pages.get(pageNum) instanceof QuadrantsPage) {
			QuadrantsPage page = (QuadrantsPage) this.pages.get(pageNum);
			page.setQuantization(quantization);
		}
	}

	/**
	 * @return The MIDI outputs that have been enabled in the main configuration.
	 */
	public String[] getMidiOutOptions(int index) {
		return this.midiOutDevices[index];
	}

	/**
	 * The Receiver object for the MIDI device named midiDeviceName. 
	 * 
	 * @param midiDeviceName The name of the MIDI device to get the Receiver for
	 * @return The MIDI receiver
	 */
	public Receiver getMidiReceiver(String midiDeviceName) {
		return MidiDeviceFactory.getMIDIReceiverByName(midiDeviceName);
	}
	
	/**
	 * The Transmitter object for the MIDI device named midiDeviceName.
	 * 
	 * @param midiDeviceName The name of the MIDI device to get the Transmitter for
	 * @return The MIDI transmitter
	 */
	public Transmitter getMidiTransmitter(String midiDeviceName) {
		return MidiDeviceFactory.getMIDITransmitterByName(midiDeviceName);
	}

	/**
	 * Used to clean up OSC connections held by individual pages.
	 */
	public void destroyPage() {
		for (int i = 0; i < this.numPages; i++) {
			this.pages.get(i).destroyPage();
		}
	}
		
	/**
	 * Sets the title bar of this MonomeConfiguration's MonomeFrame
	 */
	public void setFrameTitle() {
		String title = "";
		if (prefix != null) {
			title += prefix;
		}
		if (serial != null) {
			title += " | " + serial;
		}
		if (sizeX != 0 && sizeY != 0) {
			title += " | " + sizeX + "x" + sizeY;
		}
		if (this.deviceFrame != null) {
			deviceFrame.setTitle(title);
		}
	}

	public void sendMidi(ShortMessage midiMsg, int index) {
		String[] midiOutOptions = getMidiOutOptions(index);
		for (int i = 0; i < midiOutOptions.length; i++) {
			if (midiOutOptions[i] == null) {
				continue;
			}
			Receiver recv = getMidiReceiver(midiOutOptions[i]);
			if (recv != null) {
				recv.send(midiMsg, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
			}
		}	
	}

    public void reload() {
        ledState = new int[32][32];
        pageState = new int[255][32][32];
        for (Page page : pages) {
            page.redrawDevice();
        }
    }

    public void initMonome() {
        class InitMonomeAnimation implements Runnable {
            
            MonomeConfiguration monomeConfig;
            
            public InitMonomeAnimation(MonomeConfiguration monomeConfig) {
                this.monomeConfig = monomeConfig;
            }
            
            public void run() {
                for (int value = 1; value >= 0; value--) {
                    int[][] leds = new int[monomeConfig.sizeX][monomeConfig.sizeY];
                    for (int led = 0; led < monomeConfig.sizeX * monomeConfig.sizeY; led++) {
                        boolean found = false;
                        int x = 0;
                        int y = 0;
                        while (!found) {
                            x = (int) (Math.random() * monomeConfig.sizeX);
                            y = (int) (Math.random() * monomeConfig.sizeY);
                            if (leds[x][y] == 0) {
                                found = true;
                                leds[x][y] = 1;
                            }
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        led(x, y, value, -1);
                    }
                }
                if (monomeConfig.pages.size() > curPage)
                	monomeConfig.pages.get(curPage).redrawDevice();
            }
        }
        
        new Thread(new InitMonomeAnimation(this)).start();
    }

	public void handleTilt(int n, int x, int y, int z) {
		if (curPage < 0) return;
		if (this.pages.size() <= curPage) return;
		if (this.pages.get(curPage) == null) return;
		this.pages.get(curPage).handleTilt(n, x, y, z);
	}
}