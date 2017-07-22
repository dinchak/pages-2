package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.midi.MidiDeviceFactory;
import org.monome.pages.pages.gui.MIDISequencerPolyGUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase Fa license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * The MIDI Faders page. Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/MIDISequencerPagePoly
 * 
 * @author Tom Dinchak
 * 
 */
public class MIDISequencerPolyPage implements Page, Serializable {
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
	MIDISequencerPolyGUI gui;

	/**
	 * The current MIDI clock tick number (from 0 to 6)
	 */
	private int tickNum0 = 0;
	private int tickNum1 = 0;
	private int tickFlash0 = 0;
	private int tickFlash1 = 0;
	private int tickFlashRefresh = 1;

	/**
	 * The current position in the sequence (from 0 to 31)
	 */
	private int sequencePosition0 = 0;
	private int sequencePosition1 = 0;

	/**
	 * The selected pattern (0 to 3)
	 */
	private int pattern = 0;

	/**
	 * 1 = bank mode on
	 */
	private int bankMode = 0;

	/**
	 * sequence[bank_number][width][height] - the currently programmed sequences
	 */
	private int[][][] sequence = new int[256][64][16];
	private boolean[] bankSel = new boolean[256];
	private boolean heldNotes[][] = new boolean[256][16];
	private int heldNotesNum[][] = new int[256][16];
	private int[] patlength = new int[256];
	private int[] patHold = new int[256];
	private int[] patGate = new int[256];
	private int[] patOctgUp = new int[256];
	private int[] patSpeed = new int[256];
	private int patLengthMode = 0;
	private int patHoldMode = 0;
	private int patGateMode = 0;
	private boolean holdLength = false;
	private boolean holdHold = false;
	private boolean holdGate = false;
	private boolean hold0 = false;
	private boolean hold1 = false;
	private boolean hold5 = false;
	private boolean hold6 = false;
	private boolean[] gate = new boolean[256];
	private int[] rowMode = new int[16];
	private boolean[] noteSwitchs = new boolean[16];
	private int quantize1 = 16;
	private int quantize2 = 32;
	private int bankSize0 = 32;
	private int bankSize1 = 32;
	private int[] quantValue = new int[7];
	private int[] globalPitchValue = new int[7];
	private int globalPitch0 = 0;
	private int globalPitch1 = 0;
	private int[] globalLength = new int[4];
	private int rowGlobalQuant0 = 0;
	private int rowGlobalQuant1 = 1;
	private int rowGlobalPitch0 = 4;
	private int rowGlobalPitch1 = 5;
	private int rowGlobalMLR0 = 2;
	private int rowGlobalMLR1 = 3;
	private int rowGlobalLoop0 = 6;
	private int rowGlobalLoop1 = 7;
	private int loopStart0 = 0;
	private int loopStartPrec0 = 0;
	private int loopStart1 = 0;
	private int loopStartPrec1 = 0;
	private boolean stopNotesRequest = false;
	private boolean onOff0 = true;
	private boolean onOff1 = true;
	private int globalPitch0oct = 0;
	private int globalPitch1oct = 0;
	private int globalPitchOctValue = 12;
	private boolean globalHold0 = false;
	private boolean globalHold1 = false;
	private int[] globalMLRSize0 = new int[3];
	private int[] globalMLRSize1 = new int[3];
	private int globalMLRSizeValue0 = 4;
	private int globalMLRSizeValue1 = 4;

	/**
	 * flashSequence[bank_number][width][height] - the flashing state of leds
	 */
	private int[][][] flashSequence = new int[256][64][16];
	private int[] flashSequenceBank = new int[256];

	/**
	 * noteNumbers[row] - midi note numbers that are sent for each row
	 */
	public int[] noteNumbers = new int[16];

	/**
	 * 64/40h/128 only, 1 = edit the 2nd page of sequence lanes
	 */
	private int depth = 0;

	/**
	 * 1 = bank clear mode enabled
	 */
	private int bankClearMode = 0;

	/**
	 * 1 = bank copy mode enabled
	 */
	private int bankCopyMode = 0;

	/**
	 * Currently selected bank number
	 */
	private int bank = 0;

	/**
	 * The size of each bank in steps
	 */
	public int bankSize = 32;

	/**
	 * 1 = pattern copy mode enabled
	 */
	private int copyMode = 0;

	/**
	 * 1 = pattern clear mode enabled
	 */
	private int clearMode = 0;

	/**
	 * Random number generator
	 */
	private Random generator = new Random();

	public String midiChannel = "1";

	// tilt stuff
	// private ADCOptions pageADCOptions = new ADCOptions();

	/**
	 * The name of the page
	 */
	private String pageName = "MIDI Sequencer Poly";

    private Dimension origGuiDimension;

	/**
	 * @param monome
	 *            The MonomeConfiguration that this page belongs to
	 * @param index
	 *            The index of this page (the page number)
	 */
	public MIDISequencerPolyPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		this.gui = new MIDISequencerPolyGUI(this);
		gui.channelTF.setText(midiChannel);
		gui.bankSizeTF.setText(""+bankSize);
		// setup default notes
		this.noteNumbers[0] = this.noteToMidiNumber("C-1");
		this.noteNumbers[1] = this.noteToMidiNumber("D-1");
		this.noteNumbers[2] = this.noteToMidiNumber("E-1");
		this.noteNumbers[3] = this.noteToMidiNumber("F-1");
		this.noteNumbers[4] = this.noteToMidiNumber("G-1");
		this.noteNumbers[5] = this.noteToMidiNumber("A-1");
		this.noteNumbers[6] = this.noteToMidiNumber("B-1");
		this.noteNumbers[7] = this.noteToMidiNumber("C-2");
		this.noteNumbers[8] = this.noteToMidiNumber("D-2");
		this.noteNumbers[9] = this.noteToMidiNumber("E-2");
		this.noteNumbers[10] = this.noteToMidiNumber("F-2");
		this.noteNumbers[11] = this.noteToMidiNumber("G-2");
		this.noteNumbers[12] = this.noteToMidiNumber("A-2");
		this.noteNumbers[13] = this.noteToMidiNumber("B-2");
		this.noteNumbers[14] = this.noteToMidiNumber("C-3");
		this.noteNumbers[15] = this.noteToMidiNumber("D-3");

		// init pattern config
		int globalHold = 0;
		if (this.gui.getHoldModeCB().isSelected())
			globalHold = 1;
		else
			globalHold = 0;
		for (int i = 0; i < 256; i++) {
			this.patlength[i] = 4 * this.monome.sizeX;
			this.patHold[i] = globalHold;
			this.patGate[i] = 0;
			this.patOctgUp[i] = 0;
			this.patSpeed[i] = 0;
		}
		for (int i = 0; i < 15; i++) {
			this.rowMode[i] = 0;
		}
		this.rowMode[1] = 1;

		for (int i = 0; i < 15; i++) {
			noteSwitchs[i] = true;
			this.patGate[this.monome.sizeX
			             * (this.monome.sizeY - 3 + (i / (this.monome.sizeX - 1)))
			             + (i %

			            		 (this.monome.sizeX - 1))] = 0;
		}

		this.quantValue[0] = 2;
		this.quantValue[1] = 4;
		this.quantValue[2] = 8;
		this.quantValue[3] = 16;
		this.quantValue[4] = 24;
		this.quantValue[5] = 32;
		this.quantValue[6] = 64;

		this.globalPitchValue[0] = -6;
		this.globalPitchValue[1] = -4;
		this.globalPitchValue[2] = -2;
		this.globalPitchValue[3] = -0;
		this.globalPitchValue[4] = 2;
		this.globalPitchValue[5] = 4;
		this.globalPitchValue[6] = 6;

		this.globalLength[0] = 1;
		this.globalLength[1] = 2;
		this.globalLength[2] = 4;
		this.globalLength[3] = 32;

		this.globalMLRSize0[0] = 1;
		this.globalMLRSize0[1] = 2;
		this.globalMLRSize0[2] = 4;
		this.globalMLRSize1[0] = 1;
		this.globalMLRSize1[1] = 2;
		this.globalMLRSize1[2] = 4;

        origGuiDimension = gui.getSize();
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }

	private void handleBottomRow(int x, int y) {
		if (x < 2) {
			if (this.monome.sizeY == 8) {
				this.depth = x;
				this.redrawDevice();
			}
		}
		if (x == 2) {
			this.stopNotes(this.bank);
			this.generateSequencerPattern();
		}
		if (x == 3) {
			this.stopNotes(this.bank);
			this.alterSequencerPattern();
		}
		if (x == 4 && this.bankClearMode == 0) {
			if (this.bankCopyMode == 1) {
				this.bankCopyMode = 0;
				this.monome.led(4, this.monome.sizeY - 1, 0, this.index);
			} else {
				this.bankCopyMode = 1;
				this.monome.led(4, this.monome.sizeY - 1, 1, this.index);
			}
		}
		if (x == 5 && this.bankCopyMode == 0) {
			if (this.bankClearMode == 1) {
				this.bankClearMode = 0;
				this.monome.led(5, this.monome.sizeY - 1, 0, this.index);
			} else {
				this.bankClearMode = 1;
				this.monome.led(5, this.monome.sizeY - 1, 1, this.index);
			}
		}
		if (x == 6) {
			bankMode = 0;
			this.redrawDevice();
		}
	}

	private void handlePatternConfig(int x, int y) {
		int v0, v1, v2;

		if (this.bankMode == 1) {
			if (y == 0 || y == 1 || y == 2) {
				if (y == 0) {
					v0 = 1;
					this.hold0 = true;
				} else {
					v0 = 0;
				}
				if (y == 1) {
					v1 = 1;
					this.hold1 = true;
				} else {
					v1 = 0;
				}
				if (y == 2) {
					v2 = 1;
				} else {
					v2 = 0;
				}
				// System.out.println("1 " + this.hold0 + " "+ this.hold1 +" "
				// +this.bankMode);

				if (this.hold0 && this.hold1) {
					this.bankMode = 2;
					this.hold0 = false;
					this.hold1 = false;
					// System.out.println("2 " +this.hold0 + " "+ this.hold1
					// +" " +this.bankMode);
					this.redrawDevice();
				}
				// this.bankMode=1;
				// System.out.println("3 " +this.hold0 + " else "+ this.hold1
				// +" " +this.bankMode);
				this.patLengthMode = this.monome.sizeX * (v0 + v1 * 2 + v2 * 4);
				this.monome.led(x, 0, v0, this.index);
				this.monome.led(x, 1, v1, this.index);
				this.monome.led(x, 2, v2, this.index);
				this.holdLength = true;

				// pattern select + length
				for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
					if (this.bankSel[iSeq]
					                 && ((this.patGate[iSeq] == 1 && this.gate

					                		 [iSeq]))) {
						this.patlength[iSeq] = this.patLengthMode;
					}
				}
			}
			// hold mode
			else if (y == 4) {
				this.holdHold = true;
				this.monome.led(x, y, 1, this.index);
				// pattern select + length
				for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
					if (this.bankSel[iSeq]
					                 && ((this.patGate[iSeq] == 1 && this.gate

					                		 [iSeq]))) {
						if (this.patHold[iSeq] == 1)
							this.patHold[iSeq] = 0;
						else

							this.patHold[iSeq] = 1;
					}
				}
				this.redrawDevice();
			}
			// gate mode
			else if (y == 3) {
				this.holdGate = true;
				this.monome.led(x, y, 1, this.index);
				for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
					if (this.bankSel[iSeq]
					                 && ((this.patGate[iSeq] == 1 && this.gate[iSeq]))) {
						if (this.patGate[iSeq] == 1)
							this.patGate[iSeq] = 0;
						else
							this.patGate

							[iSeq] = 1;
					}
				}
				this.redrawDevice();
			}// octave up
			else if (y == 5) {
				this.hold5 = true;
				// this.monome.led(x, y, 1, this.index);
				// this.stopNotes(this.bank);

				for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
					this.stopNotes(iSeq);
					if (this.bankSel[iSeq]
					                 && ((this.patGate[iSeq] == 1 && this.gate

					                		 [iSeq]))) {
						if (this.patOctgUp[iSeq] == 1)
							this.patOctgUp[iSeq] = 0;
						else

							this.patOctgUp[iSeq] = 1;
					}
					this.redrawDevice();
				}
			} else if (y == 6) {
				this.hold6 = true;
				for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
					if (this.bankSel[iSeq]
					                 && ((this.patGate[iSeq] == 1 && this.gate

					                		 [iSeq]))) {
						if (this.patSpeed[iSeq] == 0)
							this.patSpeed[iSeq] = 1;
						else

							this.patSpeed[iSeq] = 0;
					}
				}
				this.redrawDevice();
			}
		}

		else if (this.bankMode == 2) {
			if (y == 0 || y == 1) {
				if (y == 0) {
					this.hold0 = true;
				}
				if (y == 1) {
					this.hold1 = true;
				}

				if (this.hold0 && this.hold1) {
					this.bankMode = 1;
					this.hold0 = false;
					this.hold1 = false;
					this.redrawDevice();
				}
			} else if (y == this.rowGlobalMLR0) {
				this.onOff0 = !this.onOff0;
				this.redrawDevice();
				stopNotesRequest = true;
			} else if (y == this.rowGlobalMLR1) {
				this.onOff1 = !this.onOff1;
				this.redrawDevice();
				stopNotesRequest = true;
			}

			else if (y == this.rowGlobalPitch0) {
				if (this.globalPitch0oct == this.globalPitchOctValue)
					this.globalPitch0oct = 0;
				else

					this.globalPitch0oct = this.globalPitchOctValue;
				this.redrawDevice();
				stopNotesRequest = true;
			} else if (y == this.rowGlobalPitch1) {
				if (this.globalPitch1oct == this.globalPitchOctValue)
					this.globalPitch1oct = 0;
				else

					this.globalPitch1oct = this.globalPitchOctValue;
				this.redrawDevice();
				stopNotesRequest = true;
			} else if (y == this.rowGlobalLoop0) {
				this.globalHold0 = !this.globalHold0;
				this.redrawDevice();
				stopNotesRequest = true;
			} else if (y == this.rowGlobalLoop1) {
				this.globalHold1 = !this.globalHold1;
				this.redrawDevice();
				stopNotesRequest = true;
			}

		}

		/*
		 * if (y == 0||y == 1||y == 2) { if (y==0) {v0=1;this.hold0=true;} else
		 * {v0=0;} if (y==1) {v1=1;this.hold1=true;} else {v1=0;} if (y==2)
		 * {v2=1;} else {v2=0;} //System.out.println("1 " + this.hold0 + " "+
		 * this.hold1 +" " +this.bankMode);
		 * 
		 * if(this.hold0 && this.hold1 ){ if (this.bankMode==1) this.bankMode=2;
		 * else this.bankMode=1; this.hold0=false; this.hold1=false;
		 * //System.out.println("2 " +this.hold0 + " "+ this.hold1 +" "
		 * +this.bankMode); this.redrawDevice(); }else if(this.bankMode==2){
		 * this.redrawDevice(); }
		 * 
		 * else if(this.bankMode==1){ //this.bankMode=1;
		 * //System.out.println("3 " +this.hold0 + " else "+ this.hold1 +" "
		 * 
		 * +this.bankMode); this.patLengthMode=this.monome.sizeX(v0 + v12 +v24);
		 * this.monome.led(x, 0, v0, this.index); this.monome.led(x, 1, v1,
		 * this.index); this.monome.led(x, 2, v2, this.index);
		 * this.holdLength=true;
		 * 
		 * //pattern select + length for(int
		 * iSeq=0;iSeq<this.bankSel.length;iSeq++){ if (this.bankSel[iSeq] &&
		 * ((this.patGate[iSeq]==1 &&
		 * 
		 * this.gate[iSeq]))){ this.patlength[iSeq]=this.patLengthMode; } } } }
		 * //hold mode else if (y == 4){ this.holdHold=true; this.monome.led(x,
		 * y, 1, this.index); //pattern select + length for(int
		 * iSeq=0;iSeq<this.bankSel.length;iSeq++){ if (this.bankSel[iSeq] &&
		 * ((this.patGate[iSeq]==1 && this.gate[iSeq]))){ if
		 * (this.patHold[iSeq]==1) this.patHold[iSeq]=0; else this.patHold
		 * 
		 * [iSeq]=1; } } this.redrawDevice(); } //gate mode else if (y == 3){
		 * this.holdGate=true; this.monome.led(x, y, 1, this.index); for(int
		 * iSeq=0;iSeq<this.bankSel.length;iSeq++){ if (this.bankSel[iSeq] &&
		 * ((this.patGate[iSeq]==1 && this.gate[iSeq]))){ if
		 * (this.patGate[iSeq]==1) this.patGate[iSeq]=0; else
		 * this.patGate[iSeq]=1; } } this.redrawDevice(); }//octave up else if
		 * (y == 5){ this.hold5=true; //this.monome.led(x, y, 1, this.index);
		 * //this.stopNotes(this.bank);
		 * 
		 * for(int iSeq=0;iSeq<this.bankSel.length;iSeq++){
		 * this.stopNotes(iSeq); if (this.bankSel[iSeq] &&
		 * ((this.patGate[iSeq]==1 && this.gate[iSeq]))){ if
		 * (this.patOctgUp[iSeq]==1) this.patOctgUp[iSeq]=0; else
		 * 
		 * this.patOctgUp[iSeq]=1; } this.redrawDevice(); } } else if (y == 6){
		 * this.hold6=true; for(int iSeq=0;iSeq<this.bankSel.length;iSeq++){ if
		 * (this.bankSel[iSeq] && ((this.patGate[iSeq]==1 && this.gate[iSeq]))){
		 * if (this.patSpeed[iSeq]==0) this.patSpeed[iSeq]=1; else
		 * 
		 * this.patSpeed[iSeq]=0; } } this.redrawDevice(); }
		 */
	}

	private void handlePatternSelect(int x, int y) {
		if (this.bankCopyMode == 1) {
			this.bankCopyMode = 0;
			this.sequencerCopyBank(this.bank, (y * (this.monome.sizeY)) + x);
			this.redrawDevice();
		} else if (bankClearMode == 1) {
			this.bankClearMode = 0;
			this.sequencerClearBank((y * (this.monome.sizeY)) + x);
			if (this.bank == (y * (this.monome.sizeY)) + x) {
				this.stopNotes(this.monome.sizeX * y + x);
			}
			this.redrawDevice();
		}// quantize
		else if (this.bankMode == 2) {
			// System.out.println(this.hold0 + "  patsel" + this.hold1);
			if (y == rowGlobalQuant0) {
				if (x < 7) {
					this.quantize1 = this.quantValue[x];
				}
				this.tickNum0 = 0;
			} else if (y == rowGlobalQuant1) {
				if (x < 7) {
					this.quantize2 = this.quantValue[x];
				}
				this.tickNum1 = 0;
			} else if (y == rowGlobalPitch0) {
				stopNotesRequest = true;
				/*
				 * for (int i=0;i<256;i++){ this.stopNotes(i); }
				 */
				if (x < 7) {
					this.globalPitch0 = this.globalPitchValue[x];
				}
			} else if (y == rowGlobalPitch1) {
				stopNotesRequest = true;
				if (x < 7) {
					this.globalPitch1 = this.globalPitchValue[x];
				}
			} else if (y == rowGlobalLoop0) {
				if (x < 4) {
					stopNotesRequest = true;
					this.setBankSize0(this.globalLength[x]);
				}
				if (x < 4 && this.globalLength[x] == this.bankSize) {
					this.loopStart0 = 0;
					this.loopStartPrec0 = 0;
				} else if (x == 4 || x == 5 || x == 6) {
					// this.globalRandomGate0=!this.globalRandomGate0;
					// if(this.globalRandomGate16)
					// this.globalRandomGateValue16=(int)(Math.random() + 0.5);
					this.globalMLRSizeValue0 = this.globalMLRSize0[x - 4];
				}
				/*
				 * else if(x==5){
				 * //this.globalRandomNote0=!this.globalRandomNote0;
				 * //if(this.globalRandomNote16)
				 * this.globalRandomNoteValue16=(int)
				 * 
				 * (Math.random() (8)) -4; } else if(x==6){
				 * //this.globalRandomVelocity0=!this.globalRandomVelocity0;
				 * //if(this.globalRandomVelocity16)
				 * this.globalRandomVelocityValue16=(int)
				 * 
				 * (Math.random() (127+1)); }
				 */
			} else if (y == rowGlobalLoop1) {
				if (x < 4) {
					stopNotesRequest = true;
					this.setBankSize1(this.globalLength[x]);
				}
				if (x < 4 && this.globalLength[x] == this.bankSize) {
					this.loopStart1 = 0;
					this.loopStartPrec1 = 0;
				} else if (x == 4 || x == 5 || x == 6) {
					// this.globalRandomGate1=!this.globalRandomGate1;
					// if(this.globalRandomGate16)
					// this.globalRandomGateValue16=(int)(Math.random() + 0.5);
					this.globalMLRSizeValue1 = this.globalMLRSize1[x - 4];
				}
				/*
				 * else if(x==5){
				 * this.globalRandomNote1=!this.globalRandomNote1;
				 * //if(this.globalRandomNote16)
				 * this.globalRandomNoteValue16=(int)
				 * 
				 * (Math.random() (8)) -4; } else if(x==6){
				 * this.globalRandomVelocity1=!this.globalRandomVelocity1;
				 * //if(this.globalRandomVelocity16)
				 * this.globalRandomVelocityValue16=(int)
				 * 
				 * (Math.random() (127+1)); }
				 */
			} else if (y == rowGlobalMLR0) {
				int posNew = 0;
				if (this.bankSize0 == this.bankSize) {
					// x==((this.sequencePosition0/this.globalMLRSizeValue0)%(this.monome.sizeX))-1)
					posNew = this.sequencePosition0
					-

					((((this.sequencePosition0 / this.globalMLRSizeValue0) % (this.monome.sizeX))) - (x + 1))
					* this.globalMLRSizeValue0 -

					2 * this.globalMLRSizeValue0;
					if (posNew <= 0)
						posNew = 0;
					this.sequencePosition0 = posNew;
					// System.out.println(this.sequencePosition0);
					// this.sequencePosition0=x*(this.bankSize/this.monome.sizeX)/this.globalMLRSizeValue0;
					stopNotesRequest = true;
				} else
					// if (this.tickNum16 == (96/this.quantize1-1) &&
					// x<(this.monome.sizeX-this.bankSize)) this.loopStart=x;
					// if (x<=(this.monome.sizeX-this.bankSize)){
					stopNotesRequest = true;
				this.loopStart0 = x;
				// }
			} else if (y == rowGlobalMLR1) {
				int posNew = 0;
				if (this.bankSize1 == this.bankSize) {
					posNew = this.sequencePosition1
					-

					((((this.sequencePosition1 / this.globalMLRSizeValue1) % (this.monome.sizeX))) - (x + 1))
					* this.globalMLRSizeValue1 -

					2 * this.globalMLRSizeValue1;
					if (posNew <= 0)
						posNew = 0;
					this.sequencePosition1 = posNew;
					stopNotesRequest = true;
				} else
					// if (this.tickNum16 == (96/this.quantize1-1) &&
					// x<(this.monome.sizeX-this.bankSize)) this.loopStart=x;
					// if (x<=(this.monome.sizeX-this.bankSize)){
					stopNotesRequest = true;
				this.loopStart1 = x;
				// }

			}
			// System.out.println(this.quantize1 + " " + this.quantize2);
			this.redrawDevice();

		}

		else if (this.holdLength) {
			this.patlength[this.monome.sizeX * y + x] = this.patLengthMode;
			this.bank = (y * (this.monome.sizeY)) + x;
			this.stopNotes(this.monome.sizeX * y + x);
		}

		else if (this.holdHold) {
			if (this.patHold[this.monome.sizeX * y + x] == 1)
				this.patHold[this.monome.sizeX * y + x] = 0;
			else

				this.patHold[this.monome.sizeX * y + x] = 1;
			this.bank = (y * (this.monome.sizeY)) + x;
			this.redrawDevice();
		} else if (this.holdGate) {
			if (this.patGate[this.monome.sizeX * y + x] == 1)
				this.patGate[this.monome.sizeX * y + x] = 0;
			else

				this.patGate[this.monome.sizeX * y + x] = 1;
			this.bank = (y * (this.monome.sizeX)) + x;
			this.redrawDevice();
		}
		// octave +2
		else if (this.hold5 && !this.hold6) {
			this.stopNotes(y * (this.monome.sizeX) + x);
			if (this.patOctgUp[this.monome.sizeX * y + x] == 1)
				this.patOctgUp[this.monome.sizeX * y + x] = 0;
			else

				this.patOctgUp[this.monome.sizeX * y + x] = 1;
			this.bank = (y * (this.monome.sizeX)) + x;
			this.redrawDevice();
		}
		// current pattern
		else if (this.hold5 && this.hold6) {
			this.stopNotes(y * (this.monome.sizeX) + x);
			this.bank = (y * (this.monome.sizeX)) + x;
			this.redrawDevice();
		}

		else if (!this.hold5 && this.hold6) {
			this.stopNotes(y * (this.monome.sizeX) + x);
			if (this.patSpeed[this.monome.sizeX * y + x] == 0)
				this.patSpeed[this.monome.sizeX * y + x] = 1;
			else

				this.patSpeed[this.monome.sizeX * y + x] = 0;
			this.bank = (y * (this.monome.sizeX)) + x;
			this.redrawDevice();
		}

		else {
			this.gate[this.monome.sizeX * y + x] = true;
			this.bank = this.monome.sizeX * y + x;
			
			if (this.patGate[this.monome.sizeX * y + x] == 0) {
				if (this.bankSel[this.monome.sizeX * y + x] == true) {
					this.bankSel[this.monome.sizeX * y + x] = false;
					this.stopNotes(y * (this.monome.sizeY) + x);
				} else {
					this.bankSel[this.monome.sizeX * y + x] = true;
				}
				this.stopNotes(this.bank);
				this.redrawDevice();
			} else if (this.patGate[this.monome.sizeX * y + x] == 1) {
				this.bankSel[this.monome.sizeX * y + x] = true;
				this.monome.led(x, y, 1, this.index);
			}
			this.redrawDevice();
		}
	}

	private void handleSequenceEditMode(int x, int y) {
		// pattern select
		if (x < 4) {
			if (this.copyMode == 1) {
				this.copyMode = 0;
				this.sequencerCopyPattern(this.pattern, x);
			}
			if (this.clearMode == 1) {
				this.clearMode = 0;
				if (x == this.pattern) {
					this.stopNotes(this.monome.sizeX * y + x);
				}
				this.sequencerClearPattern(x);
			}
			this.pattern = x;
			this.redrawDevice();
		}
		// copy mode
		if (x == 4 && this.clearMode == 0 && this.bankMode == 0) {
			if (this.copyMode == 1) {
				this.copyMode = 0;
				this.monome.led(4, (this.monome.sizeY - 1), 0, this.index);
			} else {
				this.copyMode = 1;
				this.monome.led(4, (this.monome.sizeY - 1), 1, this.index);
			}
		}
		// clear mode
		if (x == 5 && this.copyMode == 0 && this.bankMode == 0) {
			if (this.clearMode == 1) {
				this.clearMode = 0;
				this.monome.led(5, (this.monome.sizeY - 1), 0, this.index);
			} else {
				this.clearMode = 1;
				this.monome.led(5, (this.monome.sizeY - 1), 1, this.index);
			}
		}
		// bank button
		if (x == 6 && this.copyMode == 0 && this.clearMode == 0) {
			this.bankMode = 1;
			this.redrawDevice();
		}
	}

	private void handleRecordButton(int x, int y) {
		int x_seq;
		int y_seq;

		int seqPosition = 0;
		int globalPitch;

		if (this.patSpeed[this.bank] == 0) {
			seqPosition = this.sequencePosition0;
			globalPitch = this.globalPitch0;
		} else {
			seqPosition = this.sequencePosition1;
			globalPitch = this.globalPitch1;
		}

		x_seq = (pattern * (this.monome.sizeX)) + x;
		y_seq = (depth * (this.monome.sizeY - 1)) + y;
		if (this.sequence[this.bank][x_seq][y_seq] == 0) {
			this.sequence[this.bank][x_seq][y_seq] = 1;
			this.monome.led(x, y, 1, this.index);
		} else if (this.sequence[this.bank][x_seq][y_seq] == 1) {
			this.sequence[bank][x_seq][y_seq] = 2;
			this.monome.led(x, y, 1, this.index);
		} else if (this.sequence[this.bank][x_seq][y_seq] == 2) {
			this.sequence[bank][x_seq][y_seq] = 0;

			// note off for bug when toggle note off at seq_position
			int note_num = 0;
			ShortMessage note_out = new ShortMessage();
			note_num = this.getNoteNumber(y_seq) + globalPitch
			+ this.patOctgUp[this.bank] * 24;
			if ((seqPosition % this.patlength[this.bank] == x_seq + 1 || seqPosition
					% this.patlength

					[this.bank] == x_seq)) {
				try {
					note_out.setMessage(ShortMessage.NOTE_OFF, 0, note_num, 0);
					String[] midiOutOptions = monome
					.getMidiOutOptions(this.index);
					for (int j = 0; j < midiOutOptions.length; j++) {
						if (midiOutOptions[j] == null) {
							continue;
						}
						Receiver recv = monome
						.getMidiReceiver(midiOutOptions[j]);
						if (recv != null) {
							recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
						}
					}
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}

			this.monome.led(x, y, 0, this.index);
		}
	}

	private void handleNotesSwitchs(int x, int y) {

		this.gate[this.monome.sizeX * y + x] = true;

		if (this.holdGate) {
			if (this.patGate[this.monome.sizeX * y + x] == 1)
				this.patGate[this.monome.sizeX * y + x] = 0;
			else

				this.patGate[this.monome.sizeX * y + x] = 1;
		}

		if (this.patGate[this.monome.sizeX * y + x] == 0) {
			if (this.bankSel[this.monome.sizeX * y + x] == true) {
				this.bankSel[this.monome.sizeX * y + x] = false;
			} else {
				this.bankSel[this.monome.sizeX * y + x] = true;
			}
		} else if (this.patGate[this.monome.sizeX * y + x] == 1) {
			this.bankSel[this.monome.sizeX * y + x] = true;
		}
		if (this.patGate[this.monome.sizeX * y + x] == 1) {
			this.noteSwitchs[(y - (this.monome.sizeY - 3))
			                 * (this.monome.sizeY - 1) + x] = true;
			this.monome.led(x, y, 1, this.index);
		} else if (this.noteSwitchs[(y - (this.monome.sizeY - 3))
		                            * (this.monome.sizeY - 1) + x] == true)
			this.noteSwitchs[(y - (this.monome.sizeY - 3))
			                 * (this.monome.sizeY - 1) + x] = false;
		else
			this.noteSwitchs[(y - (this.monome.sizeY - 3))
			                 * (this.monome.sizeY - 1) + x] = true;
		this.redrawDevice();
	}

	private void handleRelease(int x, int y) {
		if (x == (this.monome.sizeX - 1)) {
			if (y == 0 || y == 1 || y == 2) {
				this.holdLength = false;

				if (y == 0) {
					this.hold0 = false;
				}
				if (y == 1) {
					this.hold1 = false;
				}
				/*
				 * if (this.bankMode==2 && (y==0||y==1)){ this.redrawDevice(); }
				 */

			}
			if (y == 4) {
				this.holdHold = false;
			}
			if (y == 3) {
				this.holdGate = false;
			}
			if (y == 5) {
				this.hold5 = false;
				this.monome.led(x, y, 0, this.index);
				this.redrawDevice();
			}
			if (y == 6) {
				this.hold6 = false;
				this.monome.led(x, y, 0, this.index);
				this.redrawDevice();
			}
			this.gate[this.monome.sizeX * y + x] = false;
		}

		else if (this.bankMode == 2) {
			this.redrawDevice();
		}

		else if (this.patGate[this.monome.sizeX * y + x] == 1) {
			// release note switch
			if ((y == (this.monome.sizeY - 2) || y == (this.monome.sizeY - 3))
					&& x < this.monome.sizeX - 1) {
				this.noteSwitchs[(y - (this.monome.sizeY - 3))
				                 * (this.monome.sizeY - 1) + x] = false;
				this.monome.led(x, y, 0, this.index);
				this.bankSel[this.monome.sizeX * y + x] = false;
				this.monome.led(x, y, 0, this.index);
			}
			// release bank select
			else if (y < this.monome.sizeY - 3) {
				this.bankSel[this.monome.sizeX * y + x] = false;
				this.monome.led(x, y, 0, this.index);
				this.stopNotes(this.monome.sizeX * y + x); // bug stop other
				// hold sequence
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		// only on press events
		if (value == 1) {
			// bottom row - bank mode functions
			if ((this.bankMode == 1) || (this.bankMode == 2)) {

				if (y == (this.monome.sizeY - 1) && this.bankMode == 1) {
					this.handleBottomRow(x, y);
				} else if ((y == (this.monome.sizeY - 2) || y == (this.monome.sizeY - 3))
						&&

						x < this.monome.sizeX - 1 && this.bankMode == 1) {
					this.handleNotesSwitchs(x, y);
				}
				// pattern config
				else if (x == (this.monome.sizeX - 1)) {
					this.handlePatternConfig(x, y);
				}
				// pattern select
				else {
					this.handlePatternSelect(x, y);
				}
				// sequence edit mode
			} else {
				if (y == this.monome.sizeY - 1) {
					this.handleSequenceEditMode(x, y);

					// record button press to sequence
				} else {
					this.handleRecordButton(x, y);
				}
			}
		} else {
			if ((this.bankMode == 1 || this.bankMode == 2)) {
				// this.bankMode =1;
				this.handleRelease(x, y);
			}
		}
	}

	/**
	 * Clear a pattern in the currently selected bank.
	 * 
	 * @param dst
	 *            destination pattern to clear (0-3)
	 */
	private void sequencerClearPattern(int dst) {
		for (int x = 0; x < (this.monome.sizeX); x++) {
			for (int y = 0; y < 15; y++) {
				int x_dst = x + (dst * (this.monome.sizeX));
				sequence[bank][x_dst][y] = 0;
			}
		}
	}

	/**
	 * Copies src pattern to dst pattern.
	 * 
	 * @param src
	 *            The source pattern to copy (0-3)
	 * @param dst
	 *            The destination to copy the source pattern to (0-3)
	 */
	private void sequencerCopyPattern(int src, int dst) {
		for (int x = 0; x < (this.monome.sizeX); x++) {
			for (int y = 0; y < 15; y++) {
				int x_src = x + (src * (this.monome.sizeX));
				int x_dst = x + (dst * (this.monome.sizeX));
				sequence[bank][x_dst][y] = sequence[bank][x_src][y];
			}
		}
	}

	/**
	 * Copies src bank to dst bank.
	 * 
	 * @param src
	 *            The source bank to copy
	 * @param dst
	 *            The destination to copy the source bank to
	 */
	public void sequencerCopyBank(int src, int dst) {
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				sequence[dst][x][y] = sequence[src][x][y];
			}
		}
	}

	/**
	 * Clears a bank.
	 * 
	 * @param dst
	 *            The bank number to clear.
	 */
	public void sequencerClearBank(int dst) {
		for (int x = 0; x < 64; x++) {
			for (int y = 0; y < 16; y++) {
				sequence[dst][x][y] = 0;
			}
		}
	}

	/**
	 * Flashes LEDs for each sequence value of 2
	 */
	private void flashNotes() {
		int x_seq;
		int y_seq;
		if (this.bankMode == 0) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				x_seq = (this.pattern * (this.monome.sizeX)) + x;
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
					if (this.sequence[bank][x_seq][y_seq] == 1) {
						if (this.flashSequence[bank][x_seq][y_seq] == 0) {
							this.flashSequence[bank][x_seq][y_seq] = 1;
							this.monome.led(x, y, 1, this.index);
						} else {
							this.flashSequence[bank][x_seq][y_seq] = 0;
							this.monome.led(x, y, 0, this.index);
						}
					}
				}
			}
		} else {
			int x = 0;
			int y = 0;
			y = this.bank / this.monome.sizeX;
			x = this.bank % this.monome.sizeX;
			if (this.hold5 && this.hold6) {
				if (flashSequenceBank[this.bank] == 0) {
					flashSequenceBank[this.bank] = 1;
					this.monome.led(x, y, 1, this.index);
				} else {
					flashSequenceBank[this.bank] = 0;
					this.monome.led(x, y, 0, this.index);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {
		this.handleTickQuant(this.quantize1, 0);
		this.handleTickQuant(this.quantize2, 1);
	}

	public void handleTickQuant(int quantize, int quantPat) {
		int sequencePositionBank = 0;
		int seqPosition = 0;
		int tickNum = 0;
		int tickFlash = 0;
		int sequencePositionPrec = 0;
		int loopStartPrec = 0;
		int loopStart = 0;
		int bankSize = 0;

		//
		if (quantPat == 0) {
			seqPosition = this.sequencePosition0;
			tickNum = this.tickNum0;
			tickFlash = this.tickFlash0++;
			loopStartPrec = this.loopStartPrec0;
			loopStart = this.loopStart0;
			bankSize = this.bankSize0;
		} else {
			seqPosition = this.sequencePosition1;
			tickNum = this.tickNum1;
			tickFlash = this.tickFlash1++;
			loopStartPrec = this.loopStartPrec1;
			loopStart = this.loopStart1;
			bankSize = this.bankSize1;
		}

		if (tickFlash == this.tickFlashRefresh) {
			if (quantPat == 0) {
				this.tickFlash0 = 0;
			} else {
				this.tickFlash1 = 0;
			}

			if (this.patSpeed[this.bank] == quantPat) {
				this.flashNotes();
				if (this.bankMode == 1) {
					this.sequencerRedrawNoteSwitchs();
				}
			}
		}

		if (tickNum == (96 / quantize)) {
			tickNum = 0;
			if (quantPat == 0) {
				this.tickNum0 = 0;
			} else {
				this.tickNum1 = 0;
			}
		}
		// position relative to pattern length
		sequencePositionBank = seqPosition % this.patlength[this.bank];

		// send a note on for lit leds on this sequence position
		if (tickNum == 0) {
			// if (quantPat==0)System.out.println(seqPosition + " " +
			// this.bankSize + " " + this.loopStartPrec);
			if ((seqPosition == bankSize + loopStartPrec)
					|| (seqPosition == bankSize + loopStart)) {
				if (quantPat == 0) {
					this.sequencePosition0 = this.loopStart0;
					this.loopStartPrec0 = this.loopStart0;
					seqPosition = this.sequencePosition0;
				} else {
					this.sequencePosition1 = this.loopStart1;
					this.loopStartPrec1 = this.loopStart1;
					seqPosition = this.sequencePosition1;
				}
			}
			// System.out.println("sequencePosition " + seqPosition +
			// " sequencePositionBank " + sequencePositionBank +
			// " sequencePositionBank % (this.monome.sizeX) " +
			// sequencePositionBank %(this.monome.sizeX));
			if (sequencePositionBank >= (this.pattern * (this.monome.sizeX))
					&& sequencePositionBank <

					((this.pattern + 1) * (this.monome.sizeX))
					&& this.patSpeed[this.bank] == quantPat) {
				if (this.bankMode == 0) {
					int value2;
					if (this.monome.sizeY > 8) {
						value2 = 255;
					} else {
						value2 = 0;
					}
					ArrayList<Integer> colArgs = new ArrayList<Integer>();
					colArgs.add(sequencePositionBank % (this.monome.sizeX));
					colArgs.add(255);
					colArgs.add(value2);
					this.monome.led_col(colArgs, this.index);
					this.redrawCol(sequencePositionBank % (this.monome.sizeX),
							255);
					// System.out.println("this.patlength[this.bank] " +
					// this.patlength[this.bank]);
					if (sequencePositionBank % (this.monome.sizeX) == 0)
						// sequencePositionPrec=this.monome.sizeX*this.patlength[this.bank]/8-1;
						sequencePositionPrec = this.monome.sizeX - 1;
					else
						sequencePositionPrec = (sequencePositionBank % (this.monome.sizeX)) -

						1;

					colArgs = new ArrayList<Integer>();
					colArgs.add(sequencePositionPrec);
					colArgs.add(0);
					colArgs.add(0);
					this.monome.led_col(colArgs, this.index);
					this.redrawCol(sequencePositionPrec, 0);
				}
			}
			this.playNotes(seqPosition, 127, quantPat);
			// System.out.println("playNotes" + quantPat + " " + quantize);

		} else {
			if (sequencePositionBank % (this.monome.sizeX) == 0
					&& this.patSpeed[this.bank] == quantPat &&

					this.bankMode == 0) {
				// sequencePositionPrec=this.monome.sizeX*this.patlength[this.bank]/8-1;
				sequencePositionPrec = this.monome.sizeX - 1;
				ArrayList<Integer> colArgs = new ArrayList<Integer>();
				colArgs.add(sequencePositionPrec);
				colArgs.add(0);
				colArgs.add(0);
				this.monome.led_col(colArgs, this.index);
				this.redrawCol(sequencePositionPrec, 0);
			}
		}
		if (tickNum == (96 / quantize - 1)) {
			if (sequencePositionBank >= (this.pattern * (this.monome.sizeX))
					&& sequencePositionBank <

					((this.pattern + 1) * (this.monome.sizeX))) {
				if (this.bankMode == 0) {
					// this.monome.led_col(sequencePositionBank %
					// (this.monome.sizeX), 0, 0, this.index);
					// this.redrawCol(sequencePositionBank %
					// (this.monome.sizeX), 0);
				}
			}
			if (quantPat == 0) {
				this.sequencePosition0++;
			} else {
				this.sequencePosition1++;
			}
			if (this.bankMode == 2)
				this.redrawDevice();
		}
		if (quantPat == 0) {
			this.tickNum0++;
		} else {
			this.tickNum1++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		this.tickNum0 = 0;
		this.tickNum1 = 0;
		this.sequencePosition0 = 0;
		this.sequencePosition1 = 0;
		this.redrawDevice();
	}

	public void stopNotes(int iSeq) {
		ShortMessage note_out = new ShortMessage();

		for (int i = 0; i < 16; i++) {
			try {
				if (heldNotes[iSeq][i]) {
					// note_out.setMessage(ShortMessage.NOTE_OFF, 0, note_num,
					// 0);
					note_out.setMessage(ShortMessage.NOTE_OFF, 0,
							heldNotesNum[iSeq][i], 0);
					heldNotes[iSeq][i] = false;
					String[] midiOutOptions = monome
					.getMidiOutOptions(this.index);
					for (int j = 0; j < midiOutOptions.length; j++) {
						if (midiOutOptions[j] == null) {
							continue;
						}
						Receiver recv = monome
						.getMidiReceiver(midiOutOptions[j]);
						if (recv != null) {
							recv.send(note_out, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
						}
					}
				}
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send MIDI note messages based on the sequence position. If on = 0, note
	 * off will be sent.
	 * 
	 * @param seq_pos
	 *            The sequence position to play notes for
	 * @param on
	 *            Whether to turn notes on or off, a value of 1 means play notes
	 * @throws InterruptedException
	 */
	public void playNotes(int seq_pos, int on, int quantPat) {
		ShortMessage note_out = new ShortMessage();
		int note_num;
		int velocity;
		int seq_pos_pat = 0;
		int midiChannel = Integer.parseInt(this.midiChannel) - 1;
		int noteOnOff;
		int globalPitch = 0;
		int globalPitchOct = 0;
		boolean onOff = true;
		boolean globalHold;

		/*
		 * if(this.globalRandomGate16) globalRandomGateValue=(int)(Math.random()
		 * + 0.5); else
		 * 
		 * globalRandomGateValue=1; if(this.globalRandomNote16)
		 * globalRandomNoteValue=(int)(Math.random() (8)) -4; else
		 * 
		 * globalRandomNoteValue=0; if(this.globalRandomVelocity16)
		 * globalRandomVelocityValue=(int)(Math.random() (63+1)); else
		 * 
		 * globalRandomVelocityValue=0;
		 */

		if (stopNotesRequest) {
			for (int i = 0; i < 256; i++) {
				this.stopNotes(i);
			}
			stopNotesRequest = false;
		}

		if (quantPat == 0) {
			globalPitch = this.globalPitch0;
			globalPitchOct = this.globalPitch0oct;
			onOff = this.onOff0;
			globalHold = this.globalHold0;
		} else {
			globalPitch = this.globalPitch1;
			globalPitchOct = this.globalPitch1oct;
			onOff = this.onOff1;
			globalHold = this.globalHold1;
		}
		// System.out.println("playnotes " + quantPat + " seq_pos " + seq_pos);
		for (int y = 0; y < 16; y++) {
			if (y < 15 && !this.noteSwitchs[y])
				noteOnOff = 0;
			else
				noteOnOff = 1;
			for (int iSeq = 0; iSeq < this.bankSel.length; iSeq++) {
				// pat length
				seq_pos_pat = seq_pos % this.patlength[iSeq];
				// hold mode
				if (this.patHold[iSeq] == 1 && this.patSpeed[iSeq] == quantPat) {
					if (this.bankSel[iSeq] && on == 127 && ((this.patGate[iSeq] == 1 && this.gate[iSeq]) || (this.patGate[iSeq] == 0))) {
						note_num = this.getNoteNumber(y) + globalPitch + this.patOctgUp[iSeq] * 24 + globalPitchOct;
						if (onOff)
							velocity = ((this.sequence[iSeq][seq_pos_pat][y] * 64) - 1) * noteOnOff;
						else
							velocity = 0;

						if ((seq_pos_pat > 0 && this.sequence[iSeq][seq_pos_pat - 1][y] > 0 && velocity <= 0) || 
						    (seq_pos_pat == 0 && this.sequence[iSeq][this.patlength[iSeq] - 1][y] > 0 && velocity <= 0)) {
							try {
								if (!globalHold) {
									heldNotes[iSeq][y] = false;
									// note_out.setMessage(ShortMessage.NOTE_OFF,
									// midiChannel, note_num, 0);
									note_out.setMessage(ShortMessage.NOTE_OFF,
											midiChannel, heldNotesNum[iSeq][y],
											0);
									if (this.bankMode == 1)
										this.monome.led(y % (this.monome.sizeX - 1), this.monome.sizeY - 3 + (y / (this.monome.sizeX - 1)), 0, this.index);
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
								}
							} catch (InvalidMidiDataException e) {
								e.printStackTrace();
							}
						} else if (((seq_pos_pat > 0 && this.sequence[iSeq][seq_pos_pat - 1][y] == 0) || seq_pos_pat == 0) && velocity > 0) {
							try {
								heldNotes[iSeq][y] = true;
								note_out.setMessage(ShortMessage.NOTE_ON, midiChannel, note_num, velocity);
								heldNotesNum[iSeq][y] = note_num;
								if (this.bankMode == 1)
									this.monome
									.led(y % (this.monome.sizeX - 1), this.monome.sizeY - 3 + (y / (this.monome.sizeX - 1)), 1, this.index);
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
				// normal mode
				} else if (this.patSpeed[iSeq] == quantPat) {
					if (this.bankSel[iSeq] && on == 127 && ((this.patGate[iSeq] == 1 && this.gate[iSeq]) || (this.patGate[iSeq] == 0))) {
						note_num = this.getNoteNumber(y) + globalPitch + this.patOctgUp[iSeq] * 24 + globalPitchOct;
						if (onOff)
							velocity = ((this.sequence[iSeq][seq_pos_pat][y] * 64) - 1) * noteOnOff;
						else
							velocity = 0;
						if ((seq_pos_pat > 0 && this.sequence[iSeq][seq_pos_pat - 1][y] > 0) ||
							(seq_pos_pat == 0 && this.sequence[iSeq][this.patlength[iSeq] - 1][y] > 0)) {
							try {
								if (!globalHold) {
									heldNotes[iSeq][y] = false;
									// note_out.setMessage(ShortMessage.NOTE_OFF,
									// midiChannel, note_num, 0);
									note_out.setMessage(ShortMessage.NOTE_OFF, midiChannel, heldNotesNum[iSeq][y], 0);
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
								}
								if (this.bankMode == 1)
									this.monome.led(y % (this.monome.sizeX - 1), this.monome.sizeY - 3 + (y / (this.monome.sizeX - 1)), 0, this.index);
							} catch (InvalidMidiDataException e) {
								e.printStackTrace();
							}
						}

						if (velocity > 0) {
							try {
								heldNotes[iSeq][y] = true;
								if (note_num > -1) {
									note_out.setMessage(ShortMessage.NOTE_ON, midiChannel, note_num, velocity);
									heldNotesNum[iSeq][y] = note_num;
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
								}
								if (this.bankMode == 1)
									this.monome.led(y % (this.monome.sizeX - 1), this.monome.sizeY - 3 + (y / (this.monome.sizeX - 1)), 1, this.index);
							} catch (InvalidMidiDataException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Convert a MIDI note number to a string, ie. "C-3".
	 * 
	 * @param noteNum
	 *            The MIDI note number to convert
	 * @return The converted representation of the MIDI note number (ie. "C-3")
	 */
	public String numberToMidiNote(int noteNum) {
		int n = noteNum % 12;
		String note = "";
		switch (n) {
		case 0:
			note = "C";
			break;
		case 1:
			note = "C#";
			break;
		case 2:
			note = "D";
			break;
		case 3:
			note = "D#";
			break;
		case 4:
			note = "E";
			break;
		case 5:
			note = "F";
			break;
		case 6:
			note = "F#";
			break;
		case 7:
			note = "G";
			break;
		case 8:
			note = "G#";
			break;
		case 9:
			note = "A";
			break;
		case 10:
			note = "A#";
			break;
		case 11:
			note = "B";
			break;
		}

		int o = (noteNum / 12) - 2;
		note = note.concat("-" + String.valueOf(o));
		return note;
	}

	/**
	 * Converts a note name to a MIDI note number (ie. "C-3").
	 * 
	 * @param convert_note
	 *            The note to convert (ie. "C-3")
	 * @return The MIDI note value of that note
	 */
	public int noteToMidiNumber(String convert_note) {
		for (int n = 0; n < 12; n++) {
			String note = "";
			switch (n) {
			case 0:
				note = "C";
				break;
			case 1:
				note = "C#";
				break;
			case 2:
				note = "D";
				break;
			case 3:
				note = "D#";
				break;
			case 4:
				note = "E";
				break;
			case 5:
				note = "F";
				break;
			case 6:
				note = "F#";
				break;
			case 7:
				note = "G";
				break;
			case 8:
				note = "G#";
				break;
			case 9:
				note = "A";
				break;
			case 10:
				note = "A#";
				break;
			case 11:
				note = "B";
				break;
			}
			for (int o = 0; o < 8; o++) {
				int note_num = (o * 12) + n;
				if (note_num == 128) {
					break;
				}
				String note_string = note + "-" + String.valueOf(o - 2);
				if (note_string.compareTo(convert_note) == 0) {
					return note_num;
				}
			}
		}
		return -1;
	}

	/**
	 * Get the MIDI note number for a sequence lane (row)
	 * 
	 * @param y
	 *            The row / sequence lane to get the MIDI note number for
	 * @return The MIDI note number assigned to that row / sequence lane
	 */
	public int getNoteNumber(int y) {
		return noteNumbers[y];
	}

	/**
	 * Set row number num to midi note value value.
	 * 
	 * @param num
	 *            The row number to set (0 = Row 1)
	 * @param value
	 *            The MIDI note value to set the row to
	 */
	public void setNoteValue(int num, int value) {
		this.noteNumbers[num] = value;
		if (num == gui.rowCB.getSelectedIndex()) {
			gui.noteTF.setText(this.numberToMidiNote(value));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		int x_seq;
		int y_seq;

		// redrawDevice if we're in bank mode
		if (this.bankMode == 1) {
			for (int x = 0; x < (this.monome.sizeX - 1); x++) {
				for (int y = 0; y < (this.monome.sizeY); y++) {
					if (this.bankSel[this.monome.sizeX * y + x] == true) {
						this.monome.led(x, y, 1, this.index);
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				}
			}
			// redrawDevice the last column of sequencer bank page
			this.sequencerRedrawLastCol();
			this.sequencerRedrawNoteSwitchs();
			// redrawDevice if we're in sequence mode
		} else if (this.bankMode == 0) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				x_seq = (this.pattern * (this.monome.sizeX)) + x;
				for (int y = 0; y < (this.monome.sizeY - 1); y++) {
					y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
					int value = 0;
					if (this.sequence[bank][x_seq][y_seq] > 0) {
						value = 1;
					}
					this.monome.led(x, y, value, this.index);
				}
			}
		}

		else if (this.bankMode == 2) {
			for (int y = 0; y <= (this.monome.sizeY - 1); y++) {
				for (int x = 0; x <= (this.monome.sizeX - 1); x++) {
					if (y == rowGlobalQuant0)
						if (x < 7 && this.quantValue[x] == this.quantize1) {
							this.monome.led(x, y, 1, this.index);
						} else
							this.monome.led(x, y, 0, this.index);
					else if (y == rowGlobalQuant1)
						if (x < 7 && this.quantValue[x] == this.quantize2) {
							this.monome.led(x, y, 1, this.index);
						}

						else
							this.monome.led(x, y, 0, this.index);
					else if (y == rowGlobalPitch0) {
						if (x == this.monome.sizeX - 1) {
							if (this.globalPitch0oct == this.globalPitchOctValue)

								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else if (x < 7
								&& this.globalPitch0 == this.globalPitchValue[x])

							this.monome.led(x, y, (1), this.index);
						else
							this.monome.led(x, y, (0), this.index);
					} else if (y == rowGlobalPitch1) {
						if (x == this.monome.sizeX - 1) {
							if (this.globalPitch1oct == this.globalPitchOctValue)

								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else if (x < 7
								&& this.globalPitch1 == this.globalPitchValue[x])

							this.monome.led(x, y, (1), this.index);
						else
							this.monome.led(x, y, (0), this.index);

					} else if (y == rowGlobalLoop0) {
						if (x == this.monome.sizeX - 1) {
							if (this.globalHold0)
								this.monome.led(x, y, (1),

										this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else if (x < 4
								&& this.bankSize0 == this.globalLength[x])
							this.monome.led(x, y, (1), this.index);
						else if (x == 4 || x == 5 || x == 6) {
							if (this.globalMLRSizeValue0 == this.globalMLRSize1[x - 4])

								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else
							this.monome.led(x, y, (0), this.index);
					} else if (y == rowGlobalLoop1) {
						if (x == this.monome.sizeX - 1) {
							if (this.globalHold1)
								this.monome.led(x, y, (1),

										this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else if (x < 4
								&& this.bankSize1 == this.globalLength[x])
							this.monome.led(x, y, (1), this.index);
						else if (x == 4 || x == 5 || x == 6) {
							if (this.globalMLRSizeValue1 == this.globalMLRSize1[x - 4])

								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0), this.index);
						} else
							this.monome.led(x, y, (0), this.index);
					} else if (y == rowGlobalMLR0) {
						if (x == this.monome.sizeX - 1) {
							if (this.onOff0)
								this.monome.led(x, y, (1), this.index);

							else
								this.monome.led(x, y, (0), this.index);
						} else if (this.bankSize0 == this.bankSize)
							// if(x==this.sequencePosition0/(this.bankSize/this.monome.sizeX))
							// this.monome.led(x, y, (1), this.index); else
							// this.monome.led(x, y, (0), this.index);
							// this.sequencePosition0=x*(this.bankSize/this.monome.sizeX)/this.globalMLRSizeValue0;
							if (x == ((this.sequencePosition0 / this.globalMLRSizeValue0) %

									(this.monome.sizeX)))
								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0),

										this.index);
						else if (this.loopStart0 == x)
							this.monome.led(x, y, (1),

									this.index);
						else
							this.monome.led(x, y, (0), this.index);

					} else if (y == rowGlobalMLR1) {
						if (x == this.monome.sizeX - 1) {
							if (this.onOff1)
								this.monome.led(x, y, (1), this.index);

							else
								this.monome.led(x, y, (0), this.index);
						} else if (this.bankSize1 == this.bankSize)
							// if(x==this.sequencePosition1/(this.bankSize/this.monome.sizeX))
							// this.monome.led(x, y, (1), this.index); else
							// this.monome.led(x, y, (0), this.index);
							if (x == ((this.sequencePosition1 / this.globalMLRSizeValue0) %

									(this.monome.sizeX)))
								this.monome.led(x, y, (1), this.index);
							else
								this.monome.led(x, y, (0), this.index);
						else if (this.loopStart1 == x)
							this.monome.led(x, y, (1),

									this.index);
						else
							this.monome.led(x, y, (0), this.index);
					} else
						this.monome.led(x, y, 0, this.index);

				}
			}
		}

		// redrawDevice the bottom row
		this.sequencerRedrawBottomRow();
	}

	/**
	 * Redraws a column as the sequence position indicator passes by.
	 * 
	 * @param col
	 *            The column number to redrawDevice
	 * @param val
	 *            The value of the led_col message that triggered this redrawDevice
	 */
	private void redrawCol(int col, int val) {
		if (val == 0 && this.bankMode == 0) {
			int x_seq = (this.pattern * (this.monome.sizeX)) + col;
			for (int y = 0; y < (this.monome.sizeY - 1); y++) {
				int y_seq = (this.depth * (this.monome.sizeY - 1)) + y;
				if (this.sequence[bank][x_seq][y_seq] > 0) {
					this.monome.led(col, y, 1, this.index);
				}
			}
			if (col == this.pattern) {
				this.monome.led(col, (this.monome.sizeY - 1), 1, this.index);
			}
			if (col == 4 && this.copyMode == 1) {
				this.monome.led(col, (this.monome.sizeY - 1), 1, this.index);
			}
			if (col == 5 && this.clearMode == 1) {
				this.monome.led(col, (this.monome.sizeY - 1), 1, this.index);
			}
			if (col == 6 && bankMode == 1) {
				this.monome.led(col, (this.monome.sizeY - 1), 1, this.index);
			}
			if (col > 6 && col < (this.monome.sizeX - 1)) {
				this.monome.led(col, (this.monome.sizeY - 1), 0, this.index);
			}
			if (col == (this.monome.sizeX - 1)) {
				this.monome.led(col, (this.monome.sizeY - 1), 0, this.index);
			}
		}
	}

	/**
	 * Redraws the last column of the sequencer bank page on the monome.
	 */
	public void sequencerRedrawLastCol() {
		int v0 = 0;
		int v1 = 0;
		int v2 = 0;

		// redrawDevice patlength
		patLengthMode = patlength[this.bank];
		if (patLengthMode == this.monome.sizeX * 1) {
			v0 = 1;
		} else {
			v0 = 0;
		}
		if (patLengthMode == this.monome.sizeX * 2) {
			v1 = 1;
		} else {
			v1 = 0;
		}
		if (patLengthMode == this.monome.sizeX * 4) {
			v2 = 1;
		} else {
			v2 = 0;
		}

		this.monome.led(this.monome.sizeX - 1, 0, v0, this.index);
		this.monome.led(this.monome.sizeX - 1, 1, v1, this.index);
		this.monome.led(this.monome.sizeX - 1, 2, v2, this.index);

		// redrawDevice pat gate
		patGateMode = patGate[this.bank];
		if (patGateMode == 1) {
			this.monome.led(this.monome.sizeX - 1, 3, 0, this.index);
		} else {
			this.monome.led

			(this.monome.sizeX - 1, 3, 1, this.index);
		}

		// redrawDevice pat hold
		patHoldMode = patHold[this.bank];
		if (patHoldMode == 1) {
			this.monome.led(this.monome.sizeX - 1, 4, 1, this.index);
		} else {
			this.monome.led

			(this.monome.sizeX - 1, 4, 0, this.index);
		}

		// redrawDevice pat octUp
		if (this.patOctgUp[this.bank] == 1) {
			this.monome.led(this.monome.sizeX - 1, 5, 1, this.index);
		} else

		{
			this.monome.led(this.monome.sizeX - 1, 5, 0, this.index);
		}

		// redrawDevice pat speed
		if (this.patSpeed[this.bank] == 1) {
			this.monome.led(this.monome.sizeX - 1, 6, 1, this.index);
		} else

		{
			this.monome.led(this.monome.sizeX - 1, 6, 0, this.index);
		}

	}

	public void sequencerRedrawNoteSwitchs() {
		int x, y;
		for (int i = 0; i < 14; i++) {
			x = i % (this.monome.sizeX - 1);
			y = this.monome.sizeY - 3 + (i / (this.monome.sizeX - 1));
			if (this.noteSwitchs[i] == true) {
				this.monome.led(x, y, 1, this.index);
			} else {
				this.monome.led(x, y, 0, this.index);
			}
		}
	}

	/**
	 * Redraws the bottom row of the sequencer page on the monome.
	 */
	public void sequencerRedrawBottomRow() {
		// redrawDevice this way if we're in bank mode
		if (this.bankMode == 1) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if (x < 4) {
					if (this.depth == x) {
						this.monome.led(x, (this.monome.sizeY - 1), 1,
								this.index);
					} else {
						this.monome.led(x, (this.monome.sizeY - 1), 0,
								this.index);
					}
				}
				if (x == 4) {
					this.monome.led(x, (this.monome.sizeY - 1),
							this.bankCopyMode, this.index);
				}
				if (x == 5) {
					this.monome.led(x, (this.monome.sizeY - 1),
							this.bankClearMode,

							this.index);
				}
				if (x == 6) {
					this.monome.led(x, (this.monome.sizeY - 1), this.bankMode,
							this.index);
				}
			}
			// redrawDevice this way if we're in sequence edit mode
		} else if (this.bankMode == 0) {
			for (int x = 0; x < (this.monome.sizeX); x++) {
				if (x < 4) {
					if (this.pattern == x) {
						this.monome.led(x, (this.monome.sizeY - 1), 1,
								this.index);
					} else {
						this.monome.led(x, (this.monome.sizeY - 1), 0,
								this.index);
					}
				}
				if (x == 4) {
					if (copyMode == 1) {
						this.monome.led(x, (this.monome.sizeY - 1), 1,
								this.index);
					} else {
						this.monome.led(x, (this.monome.sizeY - 1), 0,
								this.index);
					}
				}
				if (x == 5) {
					if (clearMode == 1) {
						this.monome.led(x, (this.monome.sizeY - 1), 1,
								this.index);
					} else {
						this.monome.led(x, (this.monome.sizeY - 1), 0,
								this.index);
					}
				}
				if (x == 6) {
					if (this.bankMode == 1) {
						this.monome.led(x, (this.monome.sizeY - 1), 1,
								this.index);
					} else {
						this.monome.led(x, (this.monome.sizeY - 1), 0,
								this.index);
					}
				}
				if (x > 6) {
					this.monome.led(x, (this.monome.sizeY - 1), 0, this.index);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#getName()
	 */
	public String getName() {
		return pageName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#getPanel()
	 */
	public JPanel getPanel() {
		return gui;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		return;
	}

	/**
	 * Generates a random sequencer pattern on the current bank.
	 */
	private void generateSequencerPattern() {
		// pattern template to use
		int[][] p1 = {
				{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0,
					0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
					0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
					0, 0, 2, 0, 0, 0, 0, 0 }, // 1
					{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
						2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 2, 0,
						0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
						0, 0, 0, 0, 2, 0, 1, 0 }, // 2
						{ 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
							0, 0, 0, 0, 0, 0, 0, 0 }, // 3
							{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
								0, 0, 0, 0, 0, 0, 0, 0 }, // 4
								{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
									0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
									0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
									0, 0, 0, 0, 0, 0, 1, 0 }, // 5
									{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
										0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
										0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
										0, 0, 0, 0, 1, 0, 0, 0 }, // 6
										{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
											0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
											0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
											0, 0, 0, 0, 0, 0, 0, 1 }, // 7
											{ 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
												0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
												0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
												0, 0, 0, 0, 0, 0, 0, 0 }, // 8
												{ 2, 1, 0, 0, 2, 0, 2, 0, 2, 0, 2, 0, 2, 1, 0, 0, 1, 2, 0, 0,
													0, 1, 2, 1, 2, 0, 1, 0, 0, 2, 0, 1, 2, 1, 0, 0, 2, 0,
													2, 0, 2, 0, 2, 0, 2, 1, 0, 0, 1, 2, 0, 0, 0, 1, 2, 1,
													2, 0, 1, 0, 0, 2, 0, 1 }, // 9
													{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
														0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0, 0,
														0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0,
														0, 0, 0, 0, 0, 0, 2, 1 }, // 10
														{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
															1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0,
															0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
															0, 0, 0, 0, 0, 0, 0, 0 }, // 11
															{ 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0,
																0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
																0, 0, 0, 0, 0, 0, 0, 0 }, // 12
																{ 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0,
																	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
																	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0,
																	0, 0, 0, 0, 0, 0, 0, 0 }, // 13
																	{ 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0,
																		0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0,
																		0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0,
																		0, 0, 0, 0, 1, 0, 0, 0 } // 14
		};
		// randomly turn things on and off
		for (int x = 0; x < this.bankSize; x++) {
			for (int y = 0; y < 14; y++) {
				sequence[bank][x][y] = p1[y][x];
				if (generator.nextInt(20) == 1) {
					sequence[bank][x][y] = 1;
				}
				if (generator.nextInt(10) == 1) {
					sequence[bank][x][y] = 2;
				}
				if (generator.nextInt(6) == 1) {
					sequence[bank][x][y] = 0;
				}
			}
		}

	}

	/**
	 * Alters the current sequencer patterns.
	 */
	private void alterSequencerPattern() {
		// randomly turn things on or off
		for (int x = 0; x < this.bankSize; x++) {
			for (int y = 0; y < 15; y++) {
				if (sequence[bank][x][y] > 0) {
					if (generator.nextInt(30) == 1) {
						sequence[bank][x][y] = generator.nextInt(3);
					}
				}
				if (sequence[bank][x][y] == 0) {
					if (generator.nextInt(150) == 1) {
						sequence[bank][x][y] = generator.nextInt(3);
					}
				}

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		StringBuffer xml = new StringBuffer();
		int holdmode = 0;
		xml.append("      <name>MIDI Sequencer Poly</name>\n");
		xml.append("      <pageName>" + this.pageName + "</pageName>\n");
		if (this.gui.getHoldModeCB().isSelected() == true) {
			holdmode = 1;
		}
		xml.append("      <holdmode>" + holdmode + "</holdmode>\n");
		xml.append("      <banksize>" + this.bankSize + "</banksize>\n");
		xml.append("      <midichannel>" + this.midiChannel
				+ "</midichannel>\n");

		/*
		 * xml.append("      <ccoffset>" + this.pageADCOptions.getCcOffset() +
		 * "</ccoffset>\n"); xml.append("      <sendADC>" +
		 * this.pageADCOptions.isSendADC() + "</sendADC>\n");
		 * xml.append("      <midiChannelADC>" +
		 * this.pageADCOptions.getMidiChannel() + "</midiChannelADC>\n");
		 * xml.append("      <adcTranspose>" +
		 * this.pageADCOptions.getAdcTranspose() + "</adcTranspose>\n");
		 * xml.append("      <recv>" + this.pageADCOptions.getRecv() +
		 * "</recv>\n");
		 */

		for (int i = 0; i < 16; i++) {
			xml.append("      <row>" + String.valueOf(this.noteNumbers[i])
					+ "</row>\n");
		}
		for (int i = 0; i < 256; i++) {
			xml.append("      <sequence>");
			for (int j = 0; j < 64; j++) {
				for (int k = 0; k < 16; k++) {
					xml.append(this.sequence[i][j][k]);
				}
			}
			xml.append("</sequence>\n");
		}
		return xml.toString();
	}

	public void setBankSize(int banksize) {
		if (banksize > 64) {
			banksize = 64;
		} else if (banksize < 1) {
			banksize = 1;
		}
		this.sequencePosition0 = 0;
		this.sequencePosition1 = 0;
		this.bankSize = banksize;
		this.gui.bankSizeTF.setText(String.valueOf(banksize));
	}

	public void setBankSize0(int banksize) {
		if (banksize > 64) {
			banksize = 64;
		} else if (banksize < 1) {
			banksize = 1;
		}
		this.sequencePosition0 = 0;
		this.bankSize0 = banksize;
	}

	public void setBankSize1(int banksize) {
		if (banksize > 64) {
			banksize = 64;
		} else if (banksize < 1) {
			banksize = 1;
		}
		this.sequencePosition1 = 0;
		this.bankSize1 = banksize;
	}

	public void setMidiChannel(String midiChannel2) {
		this.midiChannel = midiChannel2;
		this.gui.channelTF.setText(midiChannel2);
	}

	/**
	 * Loads a sequence from a configuration file. Called from GUI on open
	 * configuration action.
	 * 
	 * @param l
	 * @param sequence2
	 */
	public void setSequence(int l, String sequence2) {
		int row = 0;
		int pos = 0;
		for (int i = 0; i < sequence2.length(); i++) {

			if (row == 16) {
				row = 0;
				pos++;
			}

			if (sequence2.charAt(i) == '0') {
				this.sequence[l][pos][row] = 0;
			} else if (sequence2.charAt(i) == '1') {
				this.sequence[l][pos][row] = 1;
			} else if (sequence2.charAt(i) == '2') {
				this.sequence[l][pos][row] = 2;
			}
			row++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#getCacheDisabled()
	 */
	public boolean getCacheDisabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monome.pages.Page#destroyPage()
	 */
	public void destroyPage() {
		return;
	}

	public void setHoldMode(String holdmode) {
		if (holdmode.equals("1")) {
			this.gui.getHoldModeCB().doClick();
		}
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void clearPanel() {
	}

	/*
	 * public void handleADC(int adcNum, float value) { if
	 * (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) { int
	 * midi = this.pageADCOptions.getMidiChannel(); if(midi != -1) {
	 * this.monome.adcObj.sendCC(this.recv, midi,
	 * this.pageADCOptions.getCcADC(), monome, adcNum, value); } else { int chan
	 * = Integer.parseInt(midiChannel)-1; if (chan < 0) chan = 0;
	 * this.monome.adcObj.sendCC(this.recv, chan,
	 * this.pageADCOptions.getCcADC(), monome, adcNum, value); } } }
	 * 
	 * public void handleADC(float x, float y) { if
	 * (this.pageADCOptions.isSendADC() && this.monome.adcObj.isEnabled()) { int
	 * midi = this.pageADCOptions.getMidiChannel(); if(midi != -1) {
	 * this.monome.adcObj.sendCC(this.recv, midi,
	 * this.pageADCOptions.getCcADC(), monome, x, y); } else { int chan =
	 * Integer.parseInt(midiChannel)-1; if (chan < 0) chan = 0;
	 * this.monome.adcObj.sendCC(this.recv, chan,
	 * this.pageADCOptions.getCcADC(), monome, x, y); } } }
	 */

	public boolean isTiltPage() {
		return true;
	}

	/*
	 * public ADCOptions getAdcOptions() { return this.pageADCOptions; }
	 * 
	 * public void setAdcOptions(ADCOptions options) { this.pageADCOptions =
	 * options; }
	 */

	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setHoldMode(this.monome.readConfigValue(pageElement, "holdmode"));
		this.setBankSize(Integer.parseInt(this.monome.readConfigValue(pageElement, "banksize")));
		this.setMidiChannel(this.monome.readConfigValue(pageElement, "midichannel"));
		NodeList rowNL = pageElement.getElementsByTagName("row");		
		for (int l=0; l < rowNL.getLength(); l++) {		
			Element el = (Element) rowNL.item(l);		
			NodeList nl = el.getChildNodes();		
			String midiNote = ((Node) nl.item(0)).getNodeValue();		
			this.setNoteValue(l, Integer.parseInt(midiNote));		
		}		
		
		NodeList seqNL = pageElement.getElementsByTagName("sequence");		
		for (int l=0; l < seqNL.getLength(); l++) {		
			Element el = (Element) seqNL.item(l);		
			NodeList nl = el.getChildNodes();		
			String sequence = ((Node) nl.item(0)).getNodeValue();		
			this.setSequence(l, sequence);		
		}
		/*
		 * NodeList nl = pageElement.getElementsByTagName("ccoffset"); el =
		 * (Element) nl.item(0); if (el != null) { nl = el.getChildNodes();
		 * String ccOffset = ((Node) nl.item(0)).getNodeValue();
		 * this.pageADCOptions.setCcOffset(Integer.parseInt(ccOffset)); }
		 * 
		 * nl = pageElement.getElementsByTagName("sendADC"); el = (Element)
		 * nl.item(0); if (el != null) { nl = el.getChildNodes(); String sendADC
		 * = ((Node) nl.item(0)).getNodeValue();
		 * this.pageADCOptions.setSendADC(Boolean.parseBoolean(sendADC)); }
		 * 
		 * nl = pageElement.getElementsByTagName("adcTranspose"); el = (Element)
		 * nl.item(0); if (el != null) { nl = el.getChildNodes(); String
		 * adcTranspose = ((Node) nl.item(0)).getNodeValue();
		 * this.pageADCOptions.setAdcTranspose(Integer.parseInt(adcTranspose));
		 * }
		 * 
		 * nl = pageElement.getElementsByTagName("midiChannelADC"); el =
		 * (Element) nl.item(0); if (el != null) { nl = el.getChildNodes();
		 * String midiChannelADC = ((Node) nl.item(0)).getNodeValue();
		 * this.pageADCOptions.setMidiChannel(Integer.parseInt(midiChannelADC));
		 * }
		 * 
		 * nl = pageElement.getElementsByTagName("recv"); el = (Element)
		 * nl.item(0); if (el != null) { nl = el.getChildNodes(); String recv =
		 * ((Node) nl.item(0)).getNodeValue();
		 * this.pageADCOptions.setRecv(recv); }
		 */

		this.redrawDevice();
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
		// TODO Auto-generated method stub
		
	}
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}


}
