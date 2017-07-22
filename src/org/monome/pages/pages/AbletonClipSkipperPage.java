package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.swing.JPanel;

import org.apache.commons.lang.StringEscapeUtils;
import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonClip;
import org.monome.pages.ableton.AbletonTrack;
import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.pages.gui.AbletonClipSkipperGUI;
import org.w3c.dom.Element;

/**
 * The Template page, a good starting point for creating your own pages.  
 * Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/ExternalApplicationPage
 * 
 * @author Tom Dinchak
 *
 */
public class AbletonClipSkipperPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration this page belongs to
	 */
	private MonomeConfiguration monome;
	
	/**
	 * This page's index (page number) 
	 */
	private int index;

	/**
	 * This page's GUI / configuration panel 
	 */
	private AbletonClipSkipperGUI gui;

	/**
	 * The name of the selected MIDI output device 
	 */
	private String midiDeviceName;

	private int[] trackJump = {-1, -1, -1, -1, -1, -1, -1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1};

	private float[] trackMovement = new float[16];

	private int[] jumpClip = new int[16];
	
	/**
	 * The name of the page 
	 */
	private String pageName = "Ableton Clip Skipper";

    private Dimension origGuiDimension;
			
	/**
	 * @param monome The MonomeConfiguration this page belongs to
	 * @param index The index of this page (the page number)
	 */
	public AbletonClipSkipperPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		this.gui = new AbletonClipSkipperGUI(this);
        origGuiDimension = gui.getSize();
    }
	
    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */	
	public String getName() 
	{		
		return pageName;
	}
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getPanel()
	 */
	public JPanel getPanel() {
		return gui;
	}
	
	public void updateClipState(int track, int clip, int state, float length) {
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		AbletonTrack track = Main.main.configuration.abletonState.getTrack(y);
		if (track != null) {
			for (int clipNum = 0; clipNum < track.getClips().size(); clipNum++) {
				AbletonClip clip = track.getClip(clipNum);
				if (clip.getState() == AbletonClip.STATE_PLAYING) {
					int xPos = (int) ((float) (clip.getPosition() / clip.getLength()) * (float) this.monome.sizeX);
					int xDiff = x - xPos;
					float beatsPerButton = (float) (clip.getLength() / (float) this.monome.sizeX);
					float movement = xDiff * beatsPerButton;
					this.trackJump[y] = y;
					this.trackMovement[y] = movement;
					this.jumpClip[y] = clipNum;
					ArrayList<Integer> rowArgs = new ArrayList<Integer>();
					rowArgs.add(y);
					rowArgs.add(0);
					rowArgs.add(0);
					this.monome.led_row(rowArgs, this.index);
				}
			}
		}		
	}
		
	public void trackJump(int track, float amount) {
		Main.main.configuration.getAbletonControl().trackJump(track, amount);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		for (int y=0; y < this.monome.sizeY; y++) {
			ArrayList<Integer> rowArgs = new ArrayList<Integer>();
			rowArgs.add(y);
			rowArgs.add(0);
			rowArgs.add(0);
			this.monome.led_row(rowArgs, this.index);
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(y);
			if (track != null) {
				for (int clipNum = 0; clipNum < track.getClips().size(); clipNum++) {
					AbletonClip clip = track.getClip(clipNum);
					if (clip != null) {
						clip.setPosition(0.0f);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {
		for (int y = 0; y < this.monome.sizeY; y++) {
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(y);
			if (track != null) {
				for (int clipNum = 0; clipNum < track.getClips().size(); clipNum++) {
					AbletonClip clip = track.getClip(clipNum);
					if (clip != null) {
						if (clip.getState() == AbletonClip.STATE_PLAYING) {
							clip.setPosition(clip.getPosition() + (4.0f / 96.0f));
						} else {
							clip.setPosition(0.0f);
						}
						if (this.trackJump[y] != -1) {
							clip.setPosition(clip.getPosition() + this.trackMovement[y]);
							this.trackJump(this.trackJump[y], this.trackMovement[y]);
							this.trackJump[y] = -1;
						}			
						if (clip.getPosition() > clip.getLength()) {
							clip.setPosition(clip.getPosition() - clip.getLength());
						}
					}
				}
			}
		}
		this.redrawDevice();
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		for (int y = 0; y < this.monome.sizeY; y++) {
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(y);
			if (track != null) {
				int foundPlayingClip = 0;
				for (int clipNum = 0; clipNum < track.getClips().size(); clipNum++) {
					AbletonClip clip = track.getClip(clipNum);
					if (clip != null) {
						if (clip.getState() == AbletonClip.STATE_PLAYING) {
							foundPlayingClip = 1;
							int x = (int) ((float) (clip.getPosition() / clip.getLength()) * (float) this.monome.sizeX);
							for (int leds = 0; leds < this.monome.sizeX; leds++) {
								if (leds == x) {
									this.monome.led(leds, y, 1, this.index);							
								} else {
									this.monome.led(leds, y, 0, this.index);
								}
							}
						}
					} else {
						ArrayList<Integer> rowParams = new ArrayList<Integer>();
						rowParams.add(y);
						rowParams.add(0);
						rowParams.add(0);
						this.monome.led_row(rowParams, index);
					}
				}
				if (foundPlayingClip == 0) {
					for (int x=0; x < this.monome.sizeX; x++) {
						this.monome.led(x, y, 0, this.index);
					}
				}
			} else {
				ArrayList<Integer> rowParams = new ArrayList<Integer>();
				rowParams.add(y);
				rowParams.add(0);
				rowParams.add(0);
				this.monome.led_row(rowParams, index);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		// TODO add code to handle midi input from midi clock source (can be any type of input)
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String xml = "";
		xml += "      <name>Ableton Clip Skipper</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <selectedmidioutport>" + StringEscapeUtils.escapeXml(this.midiDeviceName) + "</selectedmidioutport>\n";
		return xml;
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
		setName(this.pageName);
	}

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}
	
	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	public boolean isTiltPage() {
		// TODO Auto-generated method stub
		return false;
	}	
/*
	public ADCOptions getAdcOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAdcOptions(ADCOptions options)  {
		// TODO Auto-generated method stub
		
	}
*/
	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));		
	}

	public int getIndex() {
		return index;
	}
	
	public void handleAbletonEvent() {
		redrawDevice();
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
