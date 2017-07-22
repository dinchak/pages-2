package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.LEDBlink;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.gui.MainGUI;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.gui.MIDIKeyboardGUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The MIDI Faders page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/MIDIKeyboardPage
 *   
 * @author Tom Dinchak, Stephen McLeod
 *
 */

public class MIDIKeyboardPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration that this page belongs to
	 */
	MonomeConfiguration monome;

	/**
	 * The index of this page (the page number) 
	 */
	int index;

	/**
	 * The GUI for this page
	 */
	MIDIKeyboardGUI gui;

	/**
	 * The selected MIDI channel (8x8 only)
	 */
	private int midiChannel = 0;

	/**
	 * The octave offset for each row (128 and 256 only) 
	 */
	private int[] octave = new int[16];
	
	/**
	 * The selected key 
	 */
	private int myKey = 0;
	/**
	 * The selected scale
	 */
	private int myScale = 0;

	/**
	 * The semitones between each note in the selected scale 
	 */
	public int[][] scales = { 
			{2,2,1,2,2,2,1}, //Major scale
			{2,1,2,2,1,2,2}, //Natural minor scale			
			{3,2,1,1,3,2,0}, //Blues scale
			{2,2,3,2,3,2,2}, //Major pentatonic scale
			{2,1,3,1,1,3,1}, //Hungarian minor scale
			{2,1,2,2,1,3,1}, //Harmonic minor scale		
	};
	public int[][] scalesDefault = { 
			{2,2,1,2,2,2,1}, //Major scale
			{2,1,2,2,1,2,2}, //Natural minor scale			
			{3,2,1,1,3,2,0}, //Blues scale
			{2,2,3,2,3,2,2}, //Major pentatonic scale
			{2,1,3,1,1,3,1}, //Hungarian minor scale
			{2,1,2,2,1,3,1}, //Harmonic minor scale		
	};
		
	private StringBuffer scaleStr[] = new StringBuffer[6];
	
	/* ************* alternative scales ***************
	
	E -> E
	G -> G 
	G# -> A
	A -> A#
	B -> B
	C# -> C#
	D -> D 
	
	c#, D, E, G, A, A#, B
	

	{1,2,3,2,2,1,2},  // blues 2?
	{1,2,1,3,1,2,2}, // indianish
	{1,3,2,2,2,1,1},  // enigmatic	
	{3,1,1,2,2,1,2} // blues/r&r	
	
	Major scale:    {2,2,1,2,2,2,1}
	Natural minor scale:    {2,1,2,2,1,2,2}
	Harmonic minor scale:    {2,1,2,2,1,3,1}
	Jazz minor scale:    {2,1,2,2,2,2,1}
	Hungarian minor scale:    {2,1,3,1,1,3,1}
	Blues scale:    {3,2,1,1,3,2,0}
	Major pentatonic scale:    {2,2,3,2,3,2,2}   {2,2,3,2,3}

	****************** alternative scales ***************/
	/**
	 * Natural:0, Sharp:1, Flat:-1 
	 */
	private int accidental = 0;
	
	/**
	 * Number to transpose the keyboard by
	 */
	private int transpose = 0;
	private boolean transpose1Hold = false;
	private boolean transpose2Hold = false;
	
	private int sustain = 0;
	
	private boolean [][] flashOn = new boolean[16][2];	

	/**
	 * The starting note for each key (from C-2 to B-2) 
	 */
	//private int[] keys = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
	//private int[] keys = {60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71};
	private int[] keys = {60, 62, 64, 65, 67, 69, 71};
	/**
	 * Stores the note on / off state of all MIDI notes
	 */
	private int[][] notesOn = new int[16][128];
	
	private int[][] buttonPress = new int[7][6];
		
	private Thread thread;
	private boolean blinkNow = true;
	
	private int pressCount = 0;
	
	private boolean functionLock = false;
			
	/**
	 * The name of the page 
	 */
	private String pageName = "MIDI Keyboard";

    private Dimension origGuiDimension;
	
	/**
	 * @param monome The MonomeConfiguration object this page belongs to
	 * @param index The index of this page (the page number)
	 */
	public MIDIKeyboardPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.gui = new MIDIKeyboardGUI(this);
		this.index = index;	 
		this.thread = new Thread( new Flasher() );
		this.thread.setDaemon(true);
		this.thread.start();
		
		if (this.monome.sizeX == 8)
			this.flashOn[4][0] = true;
		this.gui.resetScales();
        origGuiDimension = gui.getSize();
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }
	
	private final class Flasher implements Runnable {
		// TODO properly kill this thread when monome configuration is removed
	    public void run(){
	    	try {
	    		int i = 0;
	    		while (blinkNow) {
	    			if (monome.curPage == index){
			    		for (int x=0; x<monome.sizeX; x++) {			    			
		    				if (flashOn[x][0]) {
		    					if (flashOn[x][1]){
			    					monome.led(x, monome.sizeY-1, 1, index);
			    					flashOn[x][1] = false;
			    				} else {
			    					monome.led(x, monome.sizeY-1, 0, index);
			    					flashOn[x][1] = true;
			    				}	
		    				}
			    		}
			    		
			    		if (i>10){
			    			pressCount = 0;
			    			i = 0;
			    		}
			    		
			    		if(pressCount == 3 && !functionLock) {
			    			pressCount = 0;
			    			i=0;
			    			functionLock = true;
			    			monome.led(monome.sizeX-1, monome.sizeY-1, 1, index);
			    		} else if (pressCount == 3 && functionLock) {
			    			pressCount = 0;
			    			i=0;
			    			functionLock = false;
			    			monome.led(monome.sizeX-1, monome.sizeY-1, 0, index);
			    		}
			    		
			    		if (pressCount>0) i++;
	    			}
	    			
	    			Thread.sleep(50);
	    		}			  	
	    	} catch (InterruptedException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
	
	

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		if(x<0) x = 0;
		if(y<0) y = 0;
		
		// if this is a 128 or 256 then handle presses differently from a 64 or 40h
		if (this.monome.sizeX > 8) {
			this.handlePress256(x, y, value);			
		} else {
			this.handlePress64(x, y, value);
		}
	}

	/**
	 * Handles a button press for a 64 / 40h monome
	 * 
	 * @param x The x value of the button press received
	 * @param y The y value of the button press received
	 * @param value The type of event (1 = press, 0 = release)
	 */
	public void handlePress64(int x, int y, int value) {
		if (value == 1) {
			// select scale or key
			if (y >= 6) {
				if (y == 6 && x < 7 && !this.functionLock) {
					this.monome.led(this.myKey, 6, 0, this.index);					
					this.myKey = x;
					this.monome.led(this.myKey, y, 1, this.index);
					
				} else if (y == 6 && x == 7) {
					if (this.sustain == 0) {
						this.monome.led(x, y, 1, this.index);
						this.sustain = 1;
					} else if (this.sustain == 1) {
						this.monome.led(x, y, 0, this.index);
						this.sustain = 0;
					}
					this.doSustain();
				}				
				if (y == 7) {
					if (x == 0) {
						if (this.transpose > -2)
							this.transpose--;
						
						if (transpose2Hold)  {
							this.transpose = 0;
							this.stopNotes();
							transpose2Hold = false;
							transpose1Hold = false;
						} else {
							transpose1Hold = true;
						}
						
						transposeHelper();
						
						if(this.transpose < 0) 	{
							this.monome.led(x, 7, 1, this.index);
						} 						
					} else if(x == 1){	
						if (this.transpose < 2)
							this.transpose++;	
						
						if (transpose1Hold)  {
							this.transpose = 0;
							this.stopNotes();
							transpose2Hold = false;
							transpose1Hold = false;
						} else {
							transpose2Hold = true;
						}
						
						transposeHelper();
						
						if(this.transpose > 0) {						
							this.monome.led(x, 7, 1, this.index);
						}
					} else if (x == 2 && !this.functionLock) {
						if(this.accidental == -1) {
							this.monome.led(2, 7, 0, this.index);
							this.monome.led(3, 7, 0, this.index);
							this.accidental = 0;
						} else {
							this.monome.led(3, 7, 0, this.index);
							this.monome.led(x, y, 1, this.index);
							this.accidental = -1;
						}
					} else if (x == 3 && !this.functionLock) {						
						if(this.accidental == 1) {
							this.monome.led(2, 7, 0, this.index);
							this.monome.led(3, 7, 0, this.index);
							this.accidental = 0;
						} else	{
							this.monome.led(x, y, 1, this.index);
							this.monome.led(2, 7, 0, this.index);
							this.accidental = 1;
						}
					} else if (x < 7 && !this.functionLock){
						int i;
						
						if((this.myScale)%2 > 0) {							
							i = (this.myScale-1)/2 + 4;
							this.monome.led(i, y, 0, this.index);
							if (i != x)
								this.flashOn[i][0] = false;
						} else {
							i = this.myScale/2 + 4;
							this.monome.led(i, y, 0, this.index);
							if (i != x)
								this.flashOn[i][0] = false;
						}
												
						if (this.flashOn[x][0]) {
							this.flashOn[x][0] = false;
							this.monome.led(x, y, 1, this.index);
							this.myScale = ((x-4)*2)+1;								
						} else {		
							this.flashOn[x][0] = true;
							this.myScale = (x-4)*2;						
						}
					} else if (x == 7) {
						this.pressCount++;
					}
				}
				if (!(y == 6 && x == 7) && !(y == 7 && x < 2) && !this.functionLock) {
					this.stopNotes();
				}
					
				// select the midi channel
			} else {		
				if (x == 7 && y < 4 && !this.functionLock) {					
					this.midiChannel = y; 
					for (int i = 0; i < 4; i++) {
						if (this.midiChannel == i) {
							this.monome.led(x, i, 1, this.index);
						} else {
							this.monome.led(x, i,  0, this.index);
						}
					}
					if (!(y == 6 && x == 7) && !(y == 7 && x < 2) && !this.functionLock) this.stopNotes();
				} else if (x == 7 && y == 4) {					
					//set the offset for the adc sends
					/*
					if (this.pageADCOptions.getAdcTranspose() == 1) {
						this.monome.led(7, 4, 0, this.index);
						this.monome.led(7, 5, 0, this.index);
						this.pageADCOptions.setAdcTranspose(0);
					} else if (this.pageADCOptions.getAdcTranspose() == 2){
						this.monome.led(7, 4, 1, this.index);
						this.monome.led(7, 5, 0, this.index);
						this.pageADCOptions.setAdcTranspose(1);
					} else {
						this.monome.led(x, y, 1, this.index);
						this.pageADCOptions.setAdcTranspose(1);
					} 					
				} else if (x == 7 && y == 5) {
					this.monome.led(7, 4, 1, this.index);
					this.monome.led(7, 5, 1, this.index);
					this.pageADCOptions.setAdcTranspose(2);
					*/
				}
			}
		} else {
			if (y == 6 && x == 7) {
				// converted to toggle
				//turn off sustain
				//this.monome.led(x, y, 0, this.index);
				//this.sustain = 0;
				//this.doSustain();
			} else if (y == 7 && x == 0) {
				transpose1Hold = false;				
			} else if (y == 7 && x == 1) {
				transpose2Hold = false;				
			}
		}
			

		// for presses and releases in the keyboard area, send the note on message on press
		// and send the note off on message release
		if (y < 6 && x < 7) {
			int velocity = value * 127;
			int channel = this.midiChannel;
			int note_num = this.getNoteNumber(x) + ((y - 3) * 12);
			
			if (note_num > 127) note_num = 127;
			if (note_num < 0) note_num = 0;
			
			this.playNote(note_num, velocity, channel);
			this.notesOn[channel][note_num] = value;
			this.buttonPress[x][y] = value;
			this.monome.led(x, y, value, this.index);
		}
	}
	
	//allows for playing notes by pressing different key/accidental/scale buttons
	@SuppressWarnings("unused")
    private void retriggerNotes() {
		for (int i=0; i<128; i++) {
			this.notesOn[this.midiChannel][i] = 0;
		}
		for (int x=0; x<7; x++) {
			for (int y=0; y<6; y++) {
				if (this.buttonPress[x][y] == 1) {
					int note_num = this.getNoteNumber(x) + ((y - 3) * 12);
					if (note_num > 127) note_num = 127;
					if (note_num < 0) note_num = 0;
					
					this.notesOn[this.midiChannel][note_num] = 1;
					this.playNote(note_num, 127, this.midiChannel);
				}
			}
		}
	}
	
	private void transposeHelper () {
		if (transpose == -1) {
			flashOn[0][0] = true;
		} else if (transpose == 1) {
			flashOn[1][0] = true;
		} else {
			flashOn[1][0] = false;
			flashOn[0][0] = false;
		}
		if (this.transpose == 0) {
			this.monome.led(0, 7, 0, this.index);
			this.monome.led(1, 7, 0, this.index);
		}
	}

	/**
	 * Handles a button press for a 128 / 256 monome
	 * 
	 * @param x The x value of the button press received
	 * @param y The y value of the button press received
	 * @param value The type of event (1 = press, 0 = release)
	 */
	public void handlePress256(int x, int y, int value) {

		if (value == 1) {
			// bottom row - set the key or scale
			if (y == (this.monome.sizeY - 1)) {
				if (x < 7 && !this.functionLock) {
					this.monome.led(this.myKey, y, 0, this.index);					
					this.myKey = x;
					this.monome.led(this.myKey, y, 1, this.index);
				} else if (x == 7 && !this.functionLock) {
					if(this.accidental == -1) {
						this.monome.led(8, y, 0, this.index);
						this.monome.led(7, y, 0, this.index);
						this.accidental = 0;
					} else {
						this.monome.led(8, y, 0, this.index);
						this.monome.led(x, y, 1, this.index);
						this.accidental = -1;
					}
				} else if (x == 8 && !this.functionLock) {						
					if(this.accidental == 1) {
						this.monome.led(8, y, 0, this.index);
						this.monome.led(7, y, 0, this.index);
						this.accidental = 0;
					} else	{
						this.monome.led(x, y, 1, this.index);
						this.monome.led(7, y, 0, this.index);
						this.accidental = 1;
					}
				} else if (x < 15 && !this.functionLock) {					
					this.monome.led(this.myScale+9, y, 0, this.index);
					this.myScale = (x - 9); 
					this.monome.led(this.myScale+9, y, 1, this.index);					
				}
				
				if (x == 15) {
					this.pressCount++;
				} 
				
				this.stopNotes();
				
				// set the octave offset
			} else {

				// minus 2 octaves
				if (x == 14 && !this.functionLock) {
					if (this.octave[y] == -1) {
						return;
					} else {
						this.octave[y] -= 1;
					}
					if (this.octave[y] == 0) {
						this.monome.led(14, y, 0, this.index);
						this.monome.led(15, y, 0, this.index);
					}
					if (this.octave[y] == -1) {
						this.monome.led(14, y, 1, this.index);
						this.monome.led(15, y, 0, this.index);
					}
					return;
				}

				// plus 2 octaves
				if (x == 15 && !this.functionLock) {
					if (this.octave[y] == 1) {
						return;
					} else {
						this.octave[y] += 1;
					}
					if (this.octave[y] == 0) {
						this.monome.led(14, y, 0, this.index);
						this.monome.led(15, y, 0, this.index);
					}
					if (this.octave[y] == 1) {
						this.monome.led(14, y, 0, this.index);
						this.monome.led(15, y, 1, this.index);
					}
					return;
				}
				this.stopNotes();
			}	
		}

		// play the note
		if (y != (this.monome.sizeY - 1) && x < 14) {
			int velocity = value * 127;
			int channel = (int) Math.floor(y / 3);
			int note_num = this.getNoteNumber(x) + (this.octave[y] * 24);
			this.playNote(note_num, velocity, channel);
			
			if (note_num > 127) note_num = 127;
			if (note_num < 0) note_num = 0;
			
			if (x < 14) {
				this.monome.led(x, y, value, this.index);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {		
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		this.redrawDevice();
	}
	
	private void stopNotes() {
		ShortMessage note_out = new ShortMessage();
		for (int chan=0; chan < 16; chan++) {
			for (int i=0; i < 128; i++) {
				if (this.notesOn[chan][i] == 1) {
					try {
						note_out.setMessage(ShortMessage.NOTE_OFF, chan, i, 0);
						String[] midiOutOptions = monome.getMidiOutOptions(this.index);
						for (int j = 0; j < midiOutOptions.length; j++) {
							if (midiOutOptions[j] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[j]);
							if (recv != null) {
								recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
							}
						}
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}				
				}
			}
		}
	}

	/**
	 * Plays a MIDI note.  0 velocity will send a note off, and > 0 velocity will send a note on.
	 * 
	 * @param note_num
	 * @param velocity
	 * @param channel
	 */
	public void playNote(int note_num, int velocity, int channel) {
		ShortMessage note_out = new ShortMessage();
		try {
			if (velocity == 0) {
				note_out.setMessage(ShortMessage.NOTE_OFF, channel, note_num, velocity);				
			} else {
				note_out.setMessage(ShortMessage.NOTE_ON, channel, note_num, velocity);				
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
			
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends CC64 (sustain).  0 velocity will send a note off, and > 0 velocity will send a note on.
	 *
	 */
	public void doSustain() {		
		ShortMessage sustain_out = new ShortMessage();
		try {			
			if (this.sustain == 1) {
				sustain_out.setMessage(ShortMessage.CONTROL_CHANGE, this.midiChannel, 64, 127);
			} else {
				sustain_out.setMessage(ShortMessage.CONTROL_CHANGE, this.midiChannel, 64, 0);
			}
			String[] midiOutOptions = monome.getMidiOutOptions(this.index);
			for (int i = 0; i < midiOutOptions.length; i++) {
				if (midiOutOptions[i] == null) {
					continue;
				}
				Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
				if (recv != null) {
					recv.send(sustain_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
				}
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert a button press to a MIDI note number.
	 * 
	 * @param y The y value of the button pressed
	 * @return The MIDI note number
	 */
	public int getNoteNumber(int y) {
		int offset = 0;
		int note;
		
		int multiplier = this.transpose * 12;

		if (y >= 7) {
			y -= 7;
			offset = 12;
		}
				
		note = this.keys[this.myKey];
				
		for (int i=0; i < y; i++) {
			note += this.scales[this.myScale][i];
		}

		note += offset;		
		note += (multiplier + this.accidental);
			
		
		return note;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */	
	public void redrawDevice() {
		// for 128 / 256 monomes
		if (this.monome.sizeX > 8) {
			this.redrawMonome256();
			// for 64 / 40h monomes
		} else {
			this.redrawMonome64();
		}
	}

	/**
	 * Redraw this page on a 64 or 40h monome.
	 */
	public void redrawMonome64() {
		// everything off except the midi channel selection, the key and the scale
		for (int x=0; x < this.monome.sizeX; x++) {
			for (int y=0; y < this.monome.sizeY; y++) {
				if (x == 7 && y < 4) {
					if (this.midiChannel == y) {
						this.monome.led(x, y, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				} else if (x == 7 && y == 4) {
					/*
					if (this.pageADCOptions.getAdcTranspose() == 1) {
						this.monome.led(x, y, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
						
				} else if (x == 7 && y == 5) {
					if (this.pageADCOptions.getAdcTranspose() == 2){
						this.monome.led(7, 4, 1, this.index);
						this.monome.led(7, 5, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
					*/
				} else if (y >= 6) {
					if (y == 6 && this.myKey < 8 && this.myKey == x) {
						 this.monome.led(x, y, 1, this.index);
					} else if (y == 6 && x == 7){
						if (this.sustain == 1) 
							this.monome.led(x, y, 1, this.index);						
					} else if (y == 7 && x == 0) {
						if (this.transpose < 0)
							this.monome.led(x, y, 1, this.index);
						else
							this.monome.led(x, y, 0, this.index);
					} else if (y == 7 && x == 1){
						if (this.transpose > 0)
							this.monome.led(x, y, 1, this.index);
						else
							this.monome.led(x, y, 0, this.index);
					}  else if (y == 7 && x == 2){
						if (this.accidental == -1)
							this.monome.led(x, y, 1, this.index);
						else
							this.monome.led(x, y, 0, this.index);
					}  else if (y == 7 && x == 3){						
						if (this.accidental == 1)					
							this.monome.led(x, y, 1, this.index);
						else
							this.monome.led(x, y, 0, this.index);
					} else if (y == 7 && x < 7) {
						int i;
						if((this.myScale)%2 > 0) {
							i = (this.myScale-1)/2 + 4;
							if (i != x)
								this.monome.led(x, y, 0, this.index);
							else
								this.monome.led(x, y, 1, this.index);
						} else {
							i = this.myScale/2 + 4;
							if (i != x)
								this.monome.led(x, y, 0, this.index);
							else
								this.monome.led(x, y, 1, this.index);
						}
					} else if (y == 7 && x == 7) {
						if (this.functionLock)
							this.monome.led(x, y, 1, this.index);
						else
							this.monome.led(x, y, 0, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				} else {
					this.monome.led(x, y, 0, this.index);
				}
			}
		}
	}

	/**
	 * Redraws this page for a 128 or 256 monome.
	 */
	public void redrawMonome256() {
		// everything off except the key/scale selection and the octave offsets
		for (int x=0; x < this.monome.sizeX; x++) {
			for (int y=0; y < this.monome.sizeY; y++) {
				if (y == (this.monome.sizeY - 1)) {
					if (x == this.myKey) {
						this.monome.led(x, y, 1, this.index);
					} else if (x == 7 && this.accidental == -1) {
						this.monome.led(x, y, 1, this.index);
					} else if (x == 8 && this.accidental == 1) {
						this.monome.led(x, y, 1, this.index);
					} else if (x >= 9 && (x - 9) == this.myScale) {
						this.monome.led(x, y, 1, this.index);
					} else if (x == 15 && this.functionLock) {
						this.monome.led(x, y, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				} else if (y != (this.monome.sizeY - 1) && x == 14) {
					if (this.octave[y] == -1) {
						this.monome.led(x, y, 1, this.index);
					}
					if (this.octave[y] == 0) {
						this.monome.led(x, y, 0, this.index);
					}
				} else if (y != (this.monome.sizeY - 1) && x == 15) {
					if (this.octave[y] == 1) {
						this.monome.led(x, y, 1, this.index);
					}
					if (this.octave[y] == 0) {
						this.monome.led(x, y, 0, this.index);
					}
				} else {
					this.monome.led(x, y, 0, this.index);
				}
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */
	
	public String getName() 
	{		
		return this.pageName;
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
		return this.gui;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		if (message instanceof ShortMessage) {
			ShortMessage msg = (ShortMessage) message;
			int d1 = msg.getData1();
			int d2 = msg.getData2();
			for (int x = 0; x < this.monome.sizeX - 2; x++) {
				for (int y = 0; y < this.monome.sizeY - 1; y++) {
					int note_num = this.getNoteNumber(x) + (this.octave[y] * 24);
					if (note_num == d1) {
						if (d2 != 0) {
							new Thread(new LEDBlink(monome, x, y, 100, this.index)).start();
						}
					}
				}
			}
		}
		
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String xml = "";
		xml += "      <name>MIDI Keyboard</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <scaleStr1>" + this.scaleStr[0].toString() + "</scaleStr1>\n";
		xml += "      <scaleStr2>" + this.scaleStr[1].toString() + "</scaleStr2>\n";
		xml += "      <scaleStr3>" + this.scaleStr[2].toString() + "</scaleStr3>\n";
		xml += "      <scaleStr4>" + this.scaleStr[3].toString() + "</scaleStr4>\n";
		xml += "      <scaleStr5>" + this.scaleStr[4].toString() + "</scaleStr5>\n";
		xml += "      <scaleStr6>" + this.scaleStr[5].toString() + "</scaleStr6>\n";
		for (int i = 0; i < 16; i++) {
			xml += "      <octave" + i + ">" + this.octave[i] + "</octave" + i + ">\n";
		}
		xml += "      <myKey>" + myKey + "</myKey>\n";
		xml += "      <myScale>" + myScale + "</myScale>\n";
		xml += "      <midiChannel>" + midiChannel + "</midiChannel>\n";
		xml += "      <accidental>" + accidental + "</accidental>\n";
		xml += "      <transpose>" + transpose + "</transpose>\n";
		xml += "      <sustain>" + sustain + "</sustain>\n";
	
		/*
		xml += "      <ccoffset>" + this.pageADCOptions.getCcOffset() + "</ccoffset>\n";
		xml += "      <sendADC>" + this.pageADCOptions.isSendADC() + "</sendADC>\n";
		xml += "      <midiChannelADC>" + this.pageADCOptions.getMidiChannel() + "</midiChannelADC>\n";
		xml += "      <adcTranspose>" + this.pageADCOptions.getAdcTranspose() + "</adcTranspose>\n";
		xml += "      <recv>" + this.pageADCOptions.getRecv() + "</recv>\n"; 	
		*/
		
		return xml;
	}
	
	/**
	 * gets the available scales and displays them on the gui 
	 */
	public void getScales() {		
		for(int i=0; i<6; i++) {
			this.scaleStr[i] = new StringBuffer();
			for (int j=0; j<6; j++) {		
				this.scaleStr[i].append(Integer.toString(scales[i][j]) + ",");			
			}
			this.scaleStr[i].append(Integer.toString(scales[i][6]));			
		}
		this.gui.scaleTF1.setText(scaleStr[0].toString());
		this.gui.scaleTF2.setText(scaleStr[1].toString());  
		this.gui.scaleTF3.setText(scaleStr[2].toString());
		this.gui.scaleTF4.setText(scaleStr[3].toString());
		this.gui.scaleTF5.setText(scaleStr[4].toString());
		this.gui.scaleTF6.setText(scaleStr[5].toString());
	}
	/**
	 * sets the scales to those entered in the gui
	 */
	public void setScales(String s[]) {			
		scaleStr[0].replace(0, 20, s[0]);
		scaleStr[1].replace(0, 20, s[1]);
		scaleStr[2].replace(0, 20, s[2]);
		scaleStr[3].replace(0, 20, s[3]);
		scaleStr[4].replace(0, 20, s[4]);
		scaleStr[5].replace(0, 20, s[5]);
		
		for (int i=0; i<6; i++) {
			for (int j=0; j<7; j++) {	
				try {
					if (this.scaleStr[i].length() < 13)
						throw new NumberFormatException();
					scales[i][j] = Integer.parseInt(this.scaleStr[i].substring(j*2, j*2+1));
					if (j<5 && this.scaleStr[i].charAt(j*2+1) != ',') 
						throw new NumberFormatException();
					
				} catch (java.lang.NumberFormatException nfe) {
					JOptionPane.showMessageDialog(MainGUI.getDesktopPane(), "Scale input must be formatted \"#,#,#,#,#,#,#\".", "Input Error!", 1);
					return;
				}
			}
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
		this.blinkNow = false;
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
				this.monome.adcObj.sendCC(this.recv, this.midiChannel, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}  else {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, adcNum, value);
			}
		}
	}
	
	public void handleADC(float x, float y) {
		if (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) {
			int midi = this.pageADCOptions.getMidiChannel();
			if(midi == -1) {
				this.monome.adcObj.sendCC(this.recv, this.midiChannel, this.pageADCOptions.getCcADC(), monome, x, y);
			} else {
				this.monome.adcObj.sendCC(this.recv, midi, this.pageADCOptions.getCcADC(), monome, x, y);
			}			
		}
	}
	*/
	public boolean isTiltPage() {
		return true;
	}
	/*
	public ADCOptions getAdcOptions() {
		return this.pageADCOptions;
	}

	public void setAdcOptions(ADCOptions options) { 
		this.pageADCOptions = options;
	}
	*/
		

	public void configure(Element pageElement) {		
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		if (this.monome.readConfigValue(pageElement, "myKey") != null) {
			this.myKey = Integer.parseInt(this.monome.readConfigValue(pageElement, "myKey"));
		}
		if (this.monome.readConfigValue(pageElement, "myScale") != null) {
			this.myScale = Integer.parseInt(this.monome.readConfigValue(pageElement, "myScale"));
		}
		if (this.monome.readConfigValue(pageElement, "midiChannel") != null) {
			this.midiChannel = Integer.parseInt(this.monome.readConfigValue(pageElement, "midiChannel"));
		}
		if (this.monome.readConfigValue(pageElement, "accidental") != null) {
			this.accidental = Integer.parseInt(this.monome.readConfigValue(pageElement, "accidental"));
		}
		if (this.monome.readConfigValue(pageElement, "transpose") != null) {
			this.transpose = Integer.parseInt(this.monome.readConfigValue(pageElement, "transpose"));
		}
		if (this.monome.readConfigValue(pageElement, "sustain") != null) {
			this.sustain = Integer.parseInt(this.monome.readConfigValue(pageElement, "sustain"));
		}
		
		String s[] = new String[6];
		NodeList nl = null;
		Element el = null;
		for(int i=1; i<7; i++) {
			nl = pageElement.getElementsByTagName("scaleStr" + i);
			el = (Element) nl.item(0);
			if (el != null) {
				nl = el.getChildNodes();
				s[i-1] = ((Node) nl.item(0)).getNodeValue();					
			}
		}
		if (el != null) {
			this.setScales(s);
			this.getScales();
		}
		
		for (int i = 0; i < 16; i++) {
			nl = pageElement.getElementsByTagName("octave" + i);
			el = (Element) nl.item(0);
			if (el != null) {
				nl = el.getChildNodes();
				int octave = Integer.parseInt(((Node) nl.item(0)).getNodeValue());
				this.octave[i] = octave;
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

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}

	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	
	public void handleAbletonEvent() {
	}

	public void onBlur() {
		stopNotes();
	}
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}

}