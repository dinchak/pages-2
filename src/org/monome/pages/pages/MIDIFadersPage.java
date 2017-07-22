package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MIDIFader;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.pages.gui.MIDIFadersGUI;
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
 * The MIDI Faders page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/MIDIFadersPage
 *   
 * @author Tom Dinchak
 *
 */
public class MIDIFadersPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration that this page belongs to
	 */
	MonomeConfiguration monome;
	
	MIDIFadersGUI gui;

	/**
	 * The index of this page (the page number) 
	 */
	int index;

	/**
	 * The delay amount per MIDI CC paramater change (in ms)
	 */
	private int delayAmount;
	private int midiChannel;
	private int ccOffset;

	/**
	 * monome buttons to MIDI CC values (monome height = 16, 256 only) 
	 */
	private int[] buttonValuesLarge = {127, 118, 110, 101, 93, 84, 76, 67,
			59, 50, 42, 33, 25, 16, 8, 0 };

	/**
	 * monome buttons to MIDI CC values (monome height = 8, all monome models except 256)
	 */
	private int[] buttonValuesSmall = {127, 109, 91, 73, 54, 36, 18, 0};

	/**
	 * Which level each fader is currently at
	 */
	private int[] buttonFaders = new int[32];
	
	/**
	 * The name of the page 
	 */
	private String pageName = "MIDI Faders";
	
	private boolean horizontal;

    private Dimension origGuiDimension;
	
	/**
	 * Constructor.
	 * 
	 * @param monome The MonomeConfiguration object this page belongs to
	 * @param index The index of this page (page number)
	 */
	public MIDIFadersPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		gui = new MIDIFadersGUI(this);
		
		setDelayAmount("6");
		setCCOffset("0");
		setMidiChannel("1");
		setHorizontal(false);
        origGuiDimension = gui.getSize();
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }
	
	public void setHorizontal(boolean b) {
		this.horizontal = b;
		this.gui.getHorizontalCB().setSelected(b);
		for (int i=0; i < 16; i++) {
			if (horizontal) {
				this.buttonFaders[i] = this.monome.sizeX - 1;
			} else {
				this.buttonFaders[i] = this.monome.sizeY - 1;
			}
		}
	}
	
	public void setHorizontal(String horiz) {
		if (horiz != null && horiz.compareTo("true") == 0) {
			setHorizontal(true);
		} else {
			setHorizontal(false);
		}
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
		gui.setName(name);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {		
		int sizeY = this.monome.sizeY;
		
		if (horizontal) {
			sizeY = this.monome.sizeX;
			int tmpX = x;
			x = y;
			y = tmpX;
			//y = sizeY - y - 1;
		}
		
		if(x<0) x = 0;
		if(y<0) y = 0;
		
		int startVal = 0;
		int endVal = 0;
		int cc = this.ccOffset + x;
		
		if (value == 1) {
			int startY = this.buttonFaders[x];
			int endY = y;
			if (startY == endY) {
				return;
			}
			if (startY < 0) {
				startY = 0;
			}
			if (sizeY == 8) {
				startVal = this.buttonValuesSmall[startY];
				endVal = this.buttonValuesSmall[endY];
			} else if (sizeY == 16) {
				startVal = this.buttonValuesLarge[startY];
				endVal = this.buttonValuesLarge[endY];
			}
			if (sizeY == 8) {
				// start the first thread with null recv, this changes leds only
				MIDIFader fader = new MIDIFader(null, this.midiChannel, cc, startVal, endVal, this.buttonValuesSmall, this.monome, x, startY, endY, this.index, this.delayAmount, this.horizontal);
				new Thread(fader).start();
				String[] midiOutOptions = monome.getMidiOutOptions(this.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						// these threads send the actual midi messages
						MIDIFader midiFader = new MIDIFader(recv, this.midiChannel, cc, startVal, endVal, this.buttonValuesSmall, this.monome, x, startY, endY, this.index, this.delayAmount, this.horizontal);
						new Thread(midiFader).start();
					}
				}
			} else if (sizeY == 16) {
				MIDIFader fader = new MIDIFader(null, this.midiChannel, cc, startVal, endVal, this.buttonValuesLarge, this.monome, x, startY, endY, this.index, this.delayAmount, this.horizontal);
				new Thread(fader).start();
				String[] midiOutOptions = monome.getMidiOutOptions(this.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						MIDIFader midiFader = new MIDIFader(recv, this.midiChannel, cc, startVal, endVal, this.buttonValuesLarge, this.monome, x, startY, endY, this.index, this.delayAmount, this.horizontal);
						new Thread(midiFader).start();
					}
				}
			}
			
			this.buttonFaders[x] = y;
		} 
	}
	

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {

	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick()
	 */
	public void handleTick(MidiDevice device) {

	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		int sizeX = this.monome.sizeX;
		int sizeY = this.monome.sizeY;
		if (horizontal) {
			sizeX = this.monome.sizeY;
			sizeY = this.monome.sizeX;
		}
		for (int x=0; x < sizeX; x++) {
			for (int y=0; y < sizeY; y++) {
				if (this.buttonFaders[x] <= y) {
					if (horizontal) {
						this.monome.led(y, x, 1, this.index);
					} else {
						this.monome.led(x, y, 1, this.index);
					}
				} else {
					if (horizontal) {
						this.monome.led(y, x, 0, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		byte[] msg = message.getMessage();
		int cc = 0;
		int val = 0;
		if (msg.length != 3) {
			return;
		}
		if (message instanceof ShortMessage) {
			if (((ShortMessage) message).getCommand() != ShortMessage.CONTROL_CHANGE) {
				return;
			}
			if (((ShortMessage) message).getChannel() != this.midiChannel) {
				return;
			}
		} else {
			return;
		}
		if (msg.length == 3) {
			cc = msg[1];
			val = msg[2];
		}
		
		if (horizontal == true) {
			boolean foundFader = false;
			int y = 0;
			for (y = 0; y < monome.sizeY; y++) {
				if (y + ccOffset == cc) {
					foundFader = true;
					break;
				}
			}
			if (!foundFader) {
				return;
			}
			int endX = 0;
			for (int x = 0; x < this.monome.sizeX; x++) {
				if ((!horizontal && this.monome.sizeY == 8) || (horizontal && this.monome.sizeX == 8)) {
					if (val <= this.buttonValuesSmall[x]) {
						endX = x;
					}
				} else {
					if (val <= this.buttonValuesLarge[x]) {
						endX = x;
					}
				}
			}
			if (endX > monome.sizeX) {
				endX = monome.sizeX;
			}
	
			for (int x = this.monome.sizeX - 1; x > -1; x--) {
				if (x >= endX) {
					this.monome.led(x, y, 1, this.index);
				} else {
					this.monome.led(x, y, 0, this.index);
				}
			}
			
			this.buttonFaders[y] = endX;
		} else {
			boolean foundFader = false;
			int x = 0;
			for (x = 0; x < monome.sizeX; x++) {
				if (x + ccOffset == cc) {
					foundFader = true;
					break;
				}
			}
			if (!foundFader) {
				return;
			}
			int endY = 0;
			for (int y = 0; y < this.monome.sizeY; y++) {
				if (this.monome.sizeY == 8) {
					if (val <= this.buttonValuesSmall[y]) {
						endY = y;
					}
				} else {
					if (val <= this.buttonValuesLarge[y]) {
						endY = y;
					}
				}
			}
			if (endY > monome.sizeY) {
				endY = monome.sizeY;
			}
	
			for (int y = this.monome.sizeY - 1; y > -1; y--) {
				if (y >= endY) {
					this.monome.led(x, y, 1, this.index);
				} else {
					this.monome.led(x, y, 0, this.index);
				}
			}
			
			this.buttonFaders[x] = endY;
		}
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String xml = "";
		xml += "      <name>MIDI Faders</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <delayamount>" + this.delayAmount + "</delayamount>\n";
		xml += "      <midichannel>" + (this.midiChannel + 1) + "</midichannel>\n";
		xml += "      <ccoffset>" + this.ccOffset + "</ccoffset>\n";
		xml += "      <horizontal>" + (this.horizontal == true ? "true" : "false") + "</horizontal>\n";
		for (int i = 0; i < 16; i++) {
			xml += "      <faderPosition" + i + ">" + this.buttonFaders[i] + "</faderPosition" + i + ">\n";
		}
		/*
		xml += "      <ccoffsetADC>" + this.pageADCOptions.getCcOffset() + "</ccoffsetADC>\n";
		xml += "      <sendADC>" + this.pageADCOptions.isSendADC() + "</sendADC>\n";
		xml += "      <midiChannelADC>" + this.pageADCOptions.getMidiChannel() + "</midiChannelADC>\n";
		xml += "      <adcTranspose>" + this.pageADCOptions.getAdcTranspose() + "</adcTranspose>\n";
		xml += "      <recv>" + this.pageADCOptions.getRecv() + "</recv>\n";
		*/ 	
		return xml;
	}

	/**
	 * @param delayAmount The new delay amount (in ms)
	 */
	public void setDelayAmount(String delayAmount2) {
		try {
			int delayAmount = Integer.parseInt(delayAmount2);
			if (delayAmount < 0 || delayAmount > 10000) {
				this.gui.getDelayTF().setText(""+this.delayAmount);
				return;
			}
			this.delayAmount = delayAmount;
			this.gui.getDelayTF().setText(""+this.delayAmount);
		} catch (NumberFormatException e) {
			this.gui.getDelayTF().setText(""+this.delayAmount);
		}
	}
	
	public void setMidiChannel(String midiChannel2) {
		try {
			int midiChannel = Integer.parseInt(midiChannel2) - 1;
			if (midiChannel < 0 || midiChannel > 15) {
				this.gui.getChannelTF().setText(""+(this.midiChannel+1));
				return;
			}
			this.midiChannel = midiChannel;
			this.gui.getChannelTF().setText(midiChannel2);
		} catch (NumberFormatException e) {
			this.gui.getChannelTF().setText(""+(this.midiChannel+1));
			return;
		}
	}

	public void setCCOffset(String ccOffset2) {
		try {
			int ccOffset = Integer.parseInt(ccOffset2);
			if (ccOffset < 0 || ccOffset > 127) {
				this.gui.getCcOffsetTF().setText(""+this.ccOffset);
				return;
			}
			this.ccOffset = ccOffset;
			this.gui.getCcOffsetTF().setText(""+this.ccOffset);
		} catch (NumberFormatException e) {
			this.gui.getCcOffsetTF().setText(""+this.ccOffset);
			return;
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
			if(midi != -1) {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}  else {
				this.monome.adcObj.sendCC(this.recv, midiChannel, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}
		}
	}
	
	public void handleADC(float x, float y) {
		if (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) {
			int midi = this.pageADCOptions.getMidiChannel();
			if(midi != -1) {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, x, y);
			} else {
				this.monome.adcObj.sendCC(this.recv, midiChannel, this.pageADCOptions.getCcADC(), monome, x, y);
			}			
		}
	}
	*/
	public boolean isTiltPage() {
		return true;
	}

	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setDelayAmount(this.monome.readConfigValue(pageElement, "delayamount"));
		this.setMidiChannel(this.monome.readConfigValue(pageElement, "midichannel"));
		this.setCCOffset(this.monome.readConfigValue(pageElement, "ccoffset"));
		this.setHorizontal(this.monome.readConfigValue(pageElement, "horizontal"));
		for (int i=0; i < 16; i++) {
			NodeList nl = pageElement.getElementsByTagName("faderPosition" + i);
			Element el = (Element) nl.item(0);
			if (el != null) {
				nl = el.getChildNodes();
				String faderPos = ((Node) nl.item(0)).getNodeValue();
				this.buttonFaders[i] = Integer.parseInt(faderPos);
			}
		}
		/*
		nl = pageElement.getElementsByTagName("ccoffsetADC");
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
		
		nl = pageElement.getElementsByTagName("recv");
		el = (Element) nl.item(0);
		if (el != null) {
			nl = el.getChildNodes();
			String	recv = ((Node) nl.item(0)).getNodeValue();
			this.pageADCOptions.setRecv(recv);
		}
		*/
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
