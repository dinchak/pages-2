package org.monome.pages.configuration;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.apache.commons.lang.StringEscapeUtils;
import org.monome.pages.gui.ArcFrame;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.ArcPage;
import org.monome.pages.pages.Page;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class ArcConfiguration extends OSCDeviceConfiguration<ArcPage> {
    /**
     * The arc GUI window
     */
    public transient ArcFrame arcFrame;
    
    /**
     * ledState[x][y] - The LED state cache for the monome
     */
    public int[][] ledState = new int[4][64];

    /**
     * pageState[page_num][x][y] - The LED state cache for each page
     */
    public int[][][] pageState = new int[255][4][64];
    
    /**
     * Enabled MIDI In devices by page 
     */
    public String[][] midiInDevices = new String[255][32];

    /**
     * Enabled MIDI In devices by page 
     */
    public String[][] midiOutDevices = new String[255][32];
    
    /**
     * Rules on which MIDI note numbers should trigger switching to which pages.
     */
    public ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
    
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


    public int knobs;
    public String serialOSCHostname;
    public int serialOSCPort;
    public transient OSCPortOut serialOSCPortOut;

    private int tickNum;
    
    public ArcConfiguration(int index, String prefix, String serial, int knobs, ArcFrame arcFrame, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules) {
        super(index, prefix, serial);

        this.knobs = knobs;
        this.arcFrame = arcFrame;
        this.deviceFrame = arcFrame;
        this.midiPageChangeRules = midiPageChangeRules;
        this.useMIDIPageChanging = useMIDIPageChanging;
        if (arcFrame != null) {
            arcFrame.updateMidiInMenuOptions(MidiDeviceFactory.getMidiInOptions());
            arcFrame.updateMidiOutMenuOptions(MidiDeviceFactory.getMidiOutOptions());
        }
        this.clearArc(-1);
    }
    
    public void initArc() {
        class InitArcAnimation implements Runnable {
            
            ArcConfiguration arcConfig;
            
            public InitArcAnimation(ArcConfiguration arcConfig) {
                this.arcConfig = arcConfig;
            }
            
            public void run() {
                for (int enc = 0; enc < arcConfig.knobs; enc++) {
                    arcConfig.all(enc, 15, -1);
                }
                for (int level = 15; level > -32; level--) {
                    try {
                        Thread.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int enc = 0; enc < arcConfig.knobs; enc++) {
                        Integer[] levels = new Integer[64];
                        for (int led = 0; led < 64; led++) {
                            int lvl = level - (led / 8) + (enc * 8);
                            if (lvl < 0) lvl = 0;
                            if (lvl > 15) lvl = 15;
                            levels[led] = lvl;
                        }
                        arcConfig.map(enc, levels, -1);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                arcConfig.clearArc(-1);
                if (arcConfig.pages.size() > curPage && curPage > -1)
                    arcConfig.pages.get(curPage).redrawDevice();
            }
        }
        
        new Thread(new InitArcAnimation(this)).start();
    }

    public void clearArc(int index) {
        if (serialOSCPortOut == null) return;
        for (int enc = 0; enc < knobs; enc++) {
            all(enc, 0, index);
        }
    }
    
    public void reload() {
        ledState = new int[4][64];
    }
    
    public void set(int enc, int led, int level, int index) {
        if (enc < 0 || enc > 3) return;
        if (level < 0) level = 0;
        if (level > 15) level = 15;
        if (serialOSCPortOut == null) return;
        led = normalizeLedNumber(led);
        if (index > -1) {
            pageState[index][enc][led] = level;
            if (curPage != index) return;
            if (ledState[enc][led] == level) return;
            ledState[enc][led] = level;
        }
        Object[] args = new Object[3];
        args[0] = new Integer(enc);
        args[1] = new Integer(led);
        args[2] = new Integer(level);
        OSCMessage msg = new OSCMessage(this.prefix + "/ring/set", args);
        try {
            serialOSCPortOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void all(int enc, int level, int index) {
        if (enc < 0 || enc > 3) return;
        if (level < 0) level = 0;
        if (level > 15) level = 15;
        if (serialOSCPortOut == null) return;
        if (index > -1) {
            for (int led = 0; led < 64; led++) {
                pageState[index][enc][led] = level;
            }
            if (curPage != index) return;
            for (int led = 0; led < 64; led++) {
                ledState[enc][led] = level;
            }
        }
        Object[] args = new Object[2];
        args[0] = new Integer(enc);
        args[1] = new Integer(level);
        OSCMessage msg = new OSCMessage(this.prefix + "/ring/all", args);
        try {
            serialOSCPortOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void map(int enc, Integer[] levels, int index) {
        if (enc < 0 || enc > 3) return;
        if (serialOSCPortOut == null) return;
        for (int led = 0; led < 64; led++) {
            if (levels[led] < 0) levels[led] = 0;
            if (levels[led] > 15) levels[led] = 15;
        }
        if (index > -1) {
            for (int led = 0; led < 64; led++) {
                pageState[index][enc][led] = levels[led];
            }
            if (curPage != index) return;
            for (int led = 0; led < 64; led++) {
                ledState[enc][led] = levels[led];
            }
        }
        Object[] args = new Object[65];
        args[0] = new Integer(enc);
        for (int i = 1; i < 65; i++) {
            args[i] = levels[i-1];
        }
        OSCMessage msg = new OSCMessage(this.prefix + "/ring/map", args);
        try {
            serialOSCPortOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void range(int enc, int x1, int x2, int level, int index) {
        if (enc < 0 || enc > 3) return;
        if (level < 0) level = 0;
        if (level > 15) level = 15;
        if (serialOSCPortOut == null) return;
        if (index > -1) {
            for (int led = x1; led <= x2; led++) {
                int normalLed = normalizeLedNumber(led);
                pageState[index][enc][normalLed] = level;
            }
            if (index != curPage) return;
            for (int led = x1; led <= x2; led++) {
                int normalLed = normalizeLedNumber(led);
                ledState[enc][normalLed] = level;
            }
        }
        Object[] args = new Object[4];
        args[0] = new Integer(enc);
        args[1] = new Integer(x1);
        args[2] = new Integer(x2);
        args[3] = new Integer(level);
        OSCMessage msg = new OSCMessage(this.prefix + "/ring/range", args);
        try {
            serialOSCPortOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private int normalizeLedNumber(int level) {
        while (level < 0) level += 64;
        level = level % 64;
        return level;
    }
    
    @Override
    protected void onPageAdd(String className, ArcPage page) {
        System.out.println("ArcConfiguration " + this.serial + ": created " + className + " page");
    }

    @Override
    protected Class getPageType() {
        return ArcPage.class;
    }

    /**
     * Destroys this object.
     *
     */
    public void destroy() {
        for (int i = 0; i < this.numPages; i++) {
            deletePage(i);
        }
        ArcConfigurationFactory.removeArcConfiguration(index);
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
                if (this.arcFrame != null) {
                    this.arcFrame.updateMidiInSelectedItems(midiInDevices[this.curPage]);
                }
                return;
            }
        }

        // if we didn't disable it, enable it
        for (int i = 0; i < this.midiInDevices[this.curPage].length; i++) {
            if (this.midiInDevices[this.curPage][i] == null) {
                this.midiInDevices[this.curPage][i] = deviceName;
                if (this.arcFrame != null) {
                    this.arcFrame.updateMidiInSelectedItems(midiInDevices[this.curPage]);
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
                this.arcFrame.updatePageChangeMidiInSelectedItems(pageChangeMidiInDevices);
                return;
            }
        }

        // if we didn't disable it, enable it
        for (int i = 0; i < this.pageChangeMidiInDevices.length; i++) {
            if (this.pageChangeMidiInDevices[i] == null) {
                this.pageChangeMidiInDevices[i] = deviceName;
                if (this.arcFrame != null) {
                    this.arcFrame.updatePageChangeMidiInSelectedItems(pageChangeMidiInDevices);
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
                if (this.arcFrame != null) {
                    this.arcFrame.updateMidiOutSelectedItems(midiOutDevices[this.curPage]);
                }
                return;
            }
        }

        // if we didn't disable it, enable it
        for (int i = 0; i < this.midiOutDevices[this.curPage].length; i++) {
            if (this.midiOutDevices[this.curPage][i] == null) {
                this.midiOutDevices[this.curPage][i] = deviceName;
                if (this.arcFrame != null) {
                    this.arcFrame.updateMidiOutSelectedItems(midiOutDevices[this.curPage]);
                }
                return;
            }
        }
    }
    
    /**
     * Sets the title bar of this ArcConfiguration's ArcFrame
     */
    public void setFrameTitle() {
        String title = "";
        if (prefix != null) {
            title += prefix;
        }
        if (serial != null) {
            title += " | " + serial;
        }
        if (knobs != 0) {
            title += " | " + knobs;
        }
        if (this.arcFrame != null) {
            arcFrame.setTitle(title);
        }
    }
    
    public void handleDelta(int enc, int delta) {
        // if we have no pages then dont handle any button presses
        if (this.pages.size() == 0) {
            return;
        }
        if (enc < 0 || enc > 3) return;
        if (this.pages.get(curPage) != null) {
            this.pages.get(curPage).handleDelta(enc, delta);
        }
    }

    public void handleKey(int enc, int value) {
        // if we have no pages then dont handle any button presses
        if (this.pages.size() == 0) {
            return;
        }
        if (enc < 0 || enc > 3) return;
        if (this.pages.get(curPage) != null) {
            this.pages.get(curPage).handleKey(enc, value);
        }
    }
    
	/**
	 * Converts the current monome configuration to XML.
	 * 
	 * @return XML representing the current monome configuration
	 */
	public String toXml() {
		String xml = "";
		xml += "  <arc>\n";
		xml += "    <prefix>" + this.prefix + "</prefix>\n";
		xml += "    <serial>" + this.serial + "</serial>\n";
		xml += "    <knobs>" + this.knobs + "</knobs>\n";
		if (this.serialOSCHostname != null) {
			xml += "    <serialOSCHostname>" + this.serialOSCHostname + "</serialOSCHostname>\n";
		}
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
				xml += "    </page>\n";
			}
		}
		xml += "  </arc>\n";
		return xml;
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
                    this.pages.get(i).handleTick();
                }
            }
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
                                    ArcPage page = this.pages.get(switchToPageIndex);
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
									ArcPage page = this.pages.get(switchToPageIndex);
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

    public void sendMidi(ShortMessage midiMsg, int pageIndex) {
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


	
}
