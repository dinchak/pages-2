package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonClip;
import org.monome.pages.ableton.AbletonLooper;
import org.monome.pages.ableton.AbletonState;
import org.monome.pages.ableton.AbletonTrack;
import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.gui.MIDITriggersGUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * The MIDI Triggers page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/MIDITriggersPage
 *   
 * @author Tom Dinchak
 *
 */
public class MIDITriggersPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * Toggles mode constant
	 */
	public final int MODE_TOGGLES = 1;

	/**
	 * Triggers mode constant
	 */
	public final int MODE_TRIGGERS = 0;

	/**
	 * Rows orientation constant
	 */
	private static final int ORIENTATION_ROWS = 2;

	/**
	 * Columns orientation constant
	 */
	private static final int ORIENTATION_COLUMNS = 3;

	/**
	 * The toggled state of each button (on or off)
	 */
	private int[][] toggleValues = new int[16][16];
	public final int MODE_CLIP_OVERLAY = 2;
	public final int MODE_LOOPER_OVERLAY = 3;
	
	
	public int[] mode = new int[16];
	public int[] velocity = new int[16];
	public boolean[] onAndOff = new boolean[16];
	public boolean[] ccMode = new boolean[16];
	private int tickNum = 0;

	/**
	 * The MonomeConfiguration object this page belongs to
	 */
	MonomeConfiguration monome;
	
	MIDITriggersGUI gui;

	/**
	 * The index of this page (the page number) 
	 */
	private int index;
		
	/**
	 * The name of the page 
	 */
	private String pageName = "MIDI Triggers";

    private Dimension origGuiDimension;
	
	/**
	 * @param monome The MonomeConfiguration this page belongs to
	 * @param index The index of this page (the page number)
	 */
	public MIDITriggersPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		gui = new MIDITriggersGUI(this);
		for (int i = 0; i < 16; i++) {
			onAndOff[i] = false;
			ccMode[i] = false;
			velocity[i] = 127;
			for (int j = 0; j < 16; j++) {
				toggleValues[i][j] = 0;
			}
		}
		gui.onAndOffCB.setSelected(true);
        origGuiDimension = gui.getSize();
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */	
	public String getName() {		
		return pageName;
	}
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/**
	 * Find out of toggle mode is enabled for a row/column.
	 * 
	 * @param index The index of the row/column
	 * @return The mode of the checkbox (toggles or triggers)
	 */
	private int getMode(int index) {
		return this.mode[index];
	}

	/**
	 * Get the current orientation setting.
	 * 
	 * @return The current orientation (rows or columns)
	 */
	private int getOrientation() {
		// default to rows
		if (this.gui == null) {
			return ORIENTATION_ROWS;
		}
		if (this.gui.rowRB == null) {
			return ORIENTATION_ROWS;
		}
		if (this.gui.rowRB.isSelected()) {
			return ORIENTATION_ROWS;
		} else {
			return ORIENTATION_COLUMNS;
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		int a = x;
		int b = y;

		if (this.getOrientation() == ORIENTATION_COLUMNS) {
			a = y;
			b = x;
		}

		if (this.getMode(b) == MODE_TOGGLES) {
			if (value == 1) {
				if (this.toggleValues[a][b] == 1) {
					this.toggleValues[a][b] = 0;
					this.monome.led(x, y, 0, this.index);
					if (onAndOff[b] == true) {
						this.playNote(a, b, 1);
					}
					this.playNote(a, b, 0);					
					// note on
				} else {
					this.toggleValues[a][b] = 1;
					this.monome.led(x, y, 1, this.index);
					this.playNote(a, b, 1);
					if (onAndOff[b] == true) {
						this.playNote(a, b, 0);
					}
					// note off
				}
			}
		} else {
			this.playNote(a, b, value);
		}
	}

	/**
	 * Converts a button press into a MIDI note event
	 * 
	 * @param x The x value of the button pressed
	 * @param y The y value of the button pressed
	 * @param value The state, 1 = pressed, 0 = released
	 */
	public void playNote(int x, int y, int value) {
	    /*
		int note_num = ((x * this.monome.sizeX) + y) % 128;
		if (getOrientation() == ORIENTATION_COLUMNS) {
			note_num = ((y * this.monome.sizeY) + x) % 128;
		}
        int channel = note_num / 128;
        */
	    int note_num = x + 12;
	    int channel = y;
		boolean ccMode = this.ccMode[index];
		int velocity = value * this.velocity[index];
		ShortMessage note_out = new ShortMessage();
		try {
			if (velocity == 0) {
				if (ccMode) {
					note_out.setMessage(ShortMessage.CONTROL_CHANGE, channel, note_num, velocity);
				} else {
					note_out.setMessage(ShortMessage.NOTE_OFF, channel, note_num, velocity);
				}
			} else {
				if (ccMode) {
					note_out.setMessage(ShortMessage.CONTROL_CHANGE, channel, note_num, velocity);
				} else {
					note_out.setMessage(ShortMessage.NOTE_ON, channel, note_num, velocity);
				}
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		String[] midiOutOptions = monome.getMidiOutOptions(this.index);
		for (int i = 0; i < midiOutOptions.length; i++) {
			if (midiOutOptions[i] == null) {
				continue;
			}
			Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
			if (recv != null) {
				recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {
		tickNum++;
		if (tickNum == 96) {
			tickNum = 0;
		}
		int numRowsCols = this.monome.sizeX;
		if (getOrientation() == ORIENTATION_COLUMNS) {
			numRowsCols = this.monome.sizeY;
		}
		for (int b = 0; b < numRowsCols; b++) {
			if (mode[b] == this.MODE_CLIP_OVERLAY) {
				int playingFlashState = 0;
				int triggeredFlashState = 0;
				if (tickNum % 24 < 12) {
					playingFlashState = 1;
				} else {
					playingFlashState = 0;
				}
				
				if (tickNum % 12 < 6) {
					triggeredFlashState = 1;
				} else {
					triggeredFlashState = 0;
				}
				this.handleClipRedraw(b, playingFlashState, triggeredFlashState);
			}
		}
		
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		for (int x = 0; x < this.monome.sizeX; x++) {
			for (int y = 0; y < this.monome.sizeY; y++) {
				int a = x;
				int b = y;
				if (this.getOrientation() == ORIENTATION_COLUMNS) {
					a = y;
					b = x;
				}
				if (this.getMode(b) == MODE_TOGGLES) {
					if (this.toggleValues[a][b] == 1) {
						this.monome.led(x, y, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				} else if (this.getMode(b) == MODE_TRIGGERS) {
					//this.monome.led(x, y, 0, this.index);
				} else if (this.getMode(b) == MODE_LOOPER_OVERLAY) {
					handleLooperRedraw(b);
				} else if (this.getMode(b) == MODE_CLIP_OVERLAY) {
					handleClipRedraw(b, 1, 1);
				}
			}
		}
	}
	
	private void handleClipRedraw(int b, int playingFlashState, int triggeredFlashState) {
		int trackNum = 0;
		for (int i = 0; i < b; i++) {
			if (i == b) {
				break;
			}
			if (this.getMode(i) == MODE_CLIP_OVERLAY) {
				trackNum++;
			}
		}
		AbletonState abletonState = Main.main.configuration.abletonState;
		HashMap<Integer, AbletonTrack> tracks = abletonState.getTracks();
		if (trackNum >= tracks.size()) {
			return;
		}

		AbletonTrack track = tracks.get(trackNum);
		if (track == null) {
			return;
		}
		HashMap<Integer, AbletonClip> clips = track.getClips();
		Set<Integer> keySet = clips.keySet();
		Iterator<Integer> it = keySet.iterator();
		while (it.hasNext()) {
			int clipId = it.next();
			AbletonClip clip = clips.get(clipId);
			if (clip.getState() == AbletonClip.STATE_EMPTY) {
				if (getOrientation() == ORIENTATION_COLUMNS) {
					this.monome.led(b, clipId, 0, this.index);
				} else {
					this.monome.led(clipId, b, 0, this.index);
				}
			} else if (clip.getState() == AbletonClip.STATE_STOPPED) {
				if (getOrientation() == ORIENTATION_COLUMNS) {
					this.monome.led(b, clipId, 1, this.index);
				} else {
					this.monome.led(clipId, b, 1, this.index);
				}
			} else if (clip.getState() == AbletonClip.STATE_PLAYING) {
				if (getOrientation() == ORIENTATION_COLUMNS) {
					this.monome.led(b, clipId, playingFlashState, this.index);
				} else {
					this.monome.led(clipId, b, playingFlashState, this.index);
				}
			} else if (clip.getState() == AbletonClip.STATE_TRIGGERED) {
				if (getOrientation() == ORIENTATION_COLUMNS) {
					this.monome.led(b, clipId, triggeredFlashState, this.index);
				} else {
					this.monome.led(clipId, b, triggeredFlashState, this.index);
				}
			}
		}
	}
	
	private void handleLooperRedraw(int b) {
		int looperNum = 0;
		for (int i = 0; i < b; i++) {
			if (i == b) {
				break;
			}
			if (this.getMode(i) == MODE_LOOPER_OVERLAY) {
				looperNum++;
			}
		}
		AbletonState abletonState = Main.main.configuration.abletonState;
		HashMap<Integer, AbletonTrack> tracks = abletonState.getTracks();
		int foundLoopersNum = -1;
		for (int i = 0; i < tracks.size(); i++) {
			AbletonTrack track = tracks.get(new Integer(i));
			if (track == null) {
				continue;
			}
			HashMap<Integer, AbletonLooper> loopers = track.getLoopers();
			Set<Integer> keySet = loopers.keySet();
			Iterator<Integer> it = keySet.iterator();
			//for (int j = 0; j < loopers.size(); j++) {
			while (it.hasNext()) {
				foundLoopersNum++;
				Integer deviceId = it.next();
				if (foundLoopersNum != looperNum) {
					continue;
				}
				AbletonLooper looper = loopers.get(deviceId);
				if (looper.getState() == 0) {
					if (getOrientation() == ORIENTATION_COLUMNS) {
						this.monome.led(b, 0, 0, this.index);
						this.monome.led(b, 1, 0, this.index);
					} else {
						this.monome.led(0, b, 0, this.index);
						this.monome.led(1, b, 0, this.index);
					}
				} else if (looper.getState() == 1) {
					if (getOrientation() == ORIENTATION_COLUMNS) {
						this.monome.led(b, 0, 0, this.index);
						this.monome.led(b, 1, 1, this.index);
					} else {
						this.monome.led(0, b, 0, this.index);
						this.monome.led(1, b, 1, this.index);
					}
				} else if (looper.getState() == 2) {
					if (getOrientation() == ORIENTATION_COLUMNS) {
						this.monome.led(b, 0, 1, this.index);
						this.monome.led(b, 1, 0, this.index);
					} else {
						this.monome.led(0, b, 1, this.index);
						this.monome.led(1, b, 0, this.index);
					}
				} else if (looper.getState() == 3) {
					if (getOrientation() == ORIENTATION_COLUMNS) {
						this.monome.led(b, 0, 1, this.index);
						this.monome.led(b, 1, 1, this.index);
					} else {
						this.monome.led(0, b, 1, this.index);
						this.monome.led(1, b, 1, this.index);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		ShortMessage msg = (ShortMessage) message;
		//int note_num = msg.getData1() + (msg.getChannel() * 128);
		/*
		int x = note_num / monome.sizeX;
		int y = note_num % monome.sizeY;
		if (getOrientation() == ORIENTATION_COLUMNS) {
	        x = note_num % monome.sizeX;
	        y = note_num / monome.sizeY;
		}
		*/
		int x = msg.getData1() - 12;
		int y = msg.getChannel();
		if (getOrientation() == ORIENTATION_COLUMNS) {
			x = msg.getChannel();
			y = msg.getData1() - 12;
		}
		if (x >= 0 && x < this.monome.sizeX && y >= 0 && y < this.monome.sizeY) {
			if (msg.getCommand() == ShortMessage.NOTE_ON) {
				this.toggleValues[x][y] = 1;
				this.monome.led(x, y, 1, this.index);
			} else {
				this.toggleValues[x][y] = 0;
				this.monome.led(x, y, 0, this.index);
			}
		}
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String mode;
		if (this.gui.rowRB.isSelected()) {
			mode = "rows";
		} else {
			mode = "columns";
		}

		String xml = "";
		xml += "      <name>MIDI Triggers</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <rowcolmode>" + mode + "</rowcolmode>\n";

		/*
		xml += "      <ccoffset>" + this.pageADCOptions.getCcOffset() + "</ccoffset>\n";
		xml += "      <sendADC>" + this.pageADCOptions.isSendADC() + "</sendADC>\n";
		xml += "      <midiChannelADC>" + this.pageADCOptions.getMidiChannel() + "</midiChannelADC>\n";
		xml += "      <adcTranspose>" + this.pageADCOptions.getAdcTranspose() + "</adcTranspose>\n";
		xml += "      <recv>" + this.pageADCOptions.getRecv() + "</recv>\n";
		*/ 	
		
		for (int i=0; i < 16; i++) {
			if (this.mode[i] == MODE_TOGGLES) {
				mode = "toggles";
			} else if (this.mode[i] == MODE_TRIGGERS) {
				mode = "triggers";
			} else if (this.mode[i] == MODE_CLIP_OVERLAY) {
				mode = "clipoverlay";
			} else if (this.mode[i] == MODE_LOOPER_OVERLAY) {
				mode = "looperoverlay";
			}
			xml += "      <mode>" + mode + "</mode>\n";
		}
		for (int i=0; i < 16; i++) {
			String state;
			if (this.onAndOff[i]) {
				state = "on";
			} else {
				state = "off";
			}
			xml += "      <onandoff>" + state + "</onandoff>\n";
			if (this.ccMode[i]) {
				state = "on";
			} else {
				state = "off";
			}
			xml += "      <ccMode>" + state + "</ccMode>\n";
			xml += "      <velocity>" + this.velocity[i] + "</velocity>\n";
		}
		return xml;

	}


	/**
	 * Sets the mode / orientation of the page to rows or columns mode
	 * 
	 * @param mode "rows" for row mode, "columns" for column mode
	 */
	public void setRowColMode(String mode) {
		if (mode == null) {
			return;
		}
		if (mode.equals("rows")) {
			this.gui.rowRB.doClick();
		} else if (mode.equals("columns")) {
			this.gui.colRB.doClick();
		}

	}

	/**
	 * Used when loading configuration to enable checkboxes for rows/columns that should be toggles.
	 * 
	 * @param l 
	 */
	public void setMode(int l, String value) {
		int rowColIndex = gui.getRowColIndex();
		if (value.compareTo("triggers") == 0) {
			this.mode[l] = MODE_TRIGGERS;
			if (rowColIndex == l) {
				this.gui.getModeCB().setSelectedIndex(0);
			}
		} else if (value.compareTo("toggles") == 0) {
			this.mode[l] = MODE_TOGGLES;
			if (rowColIndex == l) {
				this.gui.getModeCB().setSelectedIndex(1);
			}
		} else if (value.compareTo("clipoverlay") == 0) {
			this.mode[l] = MODE_CLIP_OVERLAY;
			if (rowColIndex == l) {
				this.gui.getModeCB().setSelectedIndex(2);
			}
		} else if (value.compareTo("looperoverlay") == 0) {
			this.mode[l] = MODE_LOOPER_OVERLAY;
			if (rowColIndex == l) {
				this.gui.getModeCB().setSelectedIndex(3);
			}
		}
	}
	
	/**
	 * Used when loading configuration to enable checkboxes for rows/columns that should be on and off.
	 * 
	 * @param l 
	 */
	public void enableOnAndOff(int l) {
		this.onAndOff[l] = true;
		if (l == 0) {
			gui.onAndOffCB.setSelected(true);
		}
	}
	
	public void enableCcMode(int l) {
		this.ccMode[l] = true;
		if (l == 0) {
			gui.ccCB.setSelected(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getCacheDisabled()
	 */
	public boolean getCacheDisabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#destroyPage()
	 */
	public void destroyPage() {
		return;
	}
		
	public void setIndex(int index) {
		this.index = index;
	}

	/*
	public void handleADC(int adcNum, float value) {
		if (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) {
			int midi = this.pageADCOptions.getMidiChannel();
			if(midi == -1) {
				this.monome.adcObj.sendCC(this.recv, 0, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}  else {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}
		}
	}
	
	public void handleADC(float x, float y) {
		if (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) {
			int midi = this.pageADCOptions.getMidiChannel();
			if(midi == -1) {
				this.monome.adcObj.sendCC(this.recv, 0, this.pageADCOptions.getCcADC(), monome, x, y);
			} else {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, x, y);
			}			
		}
	}
	*/
	public boolean isTiltPage() {
		return false;
	}
	
	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setRowColMode(this.monome.readConfigValue(pageElement, "rowcolmode"));
						
		NodeList seqNL = pageElement.getElementsByTagName("mode");
		for (int l=0; l < seqNL.getLength(); l++) {
			Element el = (Element) seqNL.item(l);
			if (el != null) {
				NodeList nl = el.getChildNodes();
				String mode = ((Node) nl.item(0)).getNodeValue();
				this.setMode(l, mode);
			}
		}
		
		seqNL = pageElement.getElementsByTagName("onandoff");
		for (int l=0; l < seqNL.getLength(); l++) {
			Element el = (Element) seqNL.item(l);
			if (el != null) {
				NodeList nl = el.getChildNodes();
				String mode = ((Node) nl.item(0)).getNodeValue();
				if (mode.compareTo("on") == 0) {
					this.enableOnAndOff(l);
				}
			}
		}

		seqNL = pageElement.getElementsByTagName("ccMode");
		for (int l=0; l < seqNL.getLength(); l++) {
			Element el = (Element) seqNL.item(l);
			if (el != null) {
				NodeList nl = el.getChildNodes();
				String mode = ((Node) nl.item(0)).getNodeValue();
				if (mode.compareTo("on") == 0) {
					this.enableCcMode(l);
				}
			}
		}
		
		seqNL = pageElement.getElementsByTagName("velocity");
		for (int l=0; l < seqNL.getLength(); l++) {
			Element el = (Element) seqNL.item(l);
			if (el != null) {
				NodeList nl = el.getChildNodes();
				String value = ((Node) nl.item(0)).getNodeValue();
				try {
					int velocity = Integer.parseInt(value);
					this.velocity[l] = velocity;
				} catch (NumberFormatException ex) {
					
				}
			}
		}
		/*
		nl = pageElement.getElementsByTagName("ccoffset");
		el = (Element) nl.item(0);
		if (el != null) {
			nl = el.getChildNodes();
			String	ccOffset = ((Node) nl.item(0)).getNodeValue();
			this.pageADCOptions.setCcOffset(Integer.parseInt(ccOffset));
		}	
		
		nl = pageElement.getElementsByTagName("sendADC");
		el = (Element) nl.item(0);
		if (el != null) {
			nl = el.getChildNodes();
			String	sendADC = ((Node) nl.item(0)).getNodeValue();
			this.pageADCOptions.setSendADC(Boolean.parseBoolean(sendADC));
		}
		
		nl = pageElement.getElementsByTagName("adcTranspose");
		el = (Element) nl.item(0);
		if (el != null) {
			nl = el.getChildNodes();
			String	adcTranspose = ((Node) nl.item(0)).getNodeValue();
			this.pageADCOptions.setAdcTranspose(Integer.parseInt(adcTranspose));
		}
		
		nl = pageElement.getElementsByTagName("midiChannelADC");
		el = (Element) nl.item(0);
		if (el != null) {
			nl = el.getChildNodes();
			String	midiChannelADC = ((Node) nl.item(0)).getNodeValue();
			this.pageADCOptions.setMidiChannel(Integer.parseInt(midiChannelADC));
		}		
		*/

		this.redrawDevice();
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}

	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return gui;
	}

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}

	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	
	public void handleAbletonEvent() {
	}

	public void onBlur() {
		// TODO Auto-generated method stub
		
	}
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}


}