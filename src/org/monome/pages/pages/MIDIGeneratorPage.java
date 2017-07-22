package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MIDIGenerator;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.Press;
import org.monome.pages.pages.gui.MIDIGeneratorGUI;
import org.w3c.dom.Element;

public class MIDIGeneratorPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	private MonomeConfiguration monome;
	private MIDIGeneratorGUI gui;
	private int index;
	private String pageName = "MIDI Generator";
	
	private int numHeld = 0;
	private ArrayList<Press> held = new ArrayList<Press>();
	private ArrayList<MIDIGenerator> generators = new ArrayList<MIDIGenerator>();
	private boolean running = false;
	private int tickNum;
	
	public String scale;
	public int patternLength;
	public int quantization;
	public int startNote;
	public int maxNote;
	public int numNotes;
	public int chance;
	public int maxRadius;
	public int midiChannel;
	private int[][] noteMap = new int[32][32];
    private Dimension origGuiDimension;
	
	public MIDIGeneratorPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		
		maxRadius = 2;
		numNotes = 10;
		quantization = 6;
		chance = 5;
		patternLength = 384;
		scale = "2,2,1,2,2,2,1";
		startNote = 31;
		maxNote = 74;
		midiChannel = 1;
		generateNoteMap();
		gui = new MIDIGeneratorGUI(this);
        origGuiDimension = gui.getSize();
    }
	
    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }
	
	public void generateNoteMap() {
		int nextNote = startNote;
		int scaleStep = 0;
		String[] scaleOffsets = scale.split(",");
		for (int x = 0; x < monome.sizeX; x++) {
			for (int y = 0; y < monome.sizeY; y++) {
				noteMap[x][y] = nextNote;
				nextNote += Integer.parseInt(scaleOffsets[scaleStep]);
				scaleStep++;
				if (nextNote > maxNote) {
					nextNote = startNote;
					scaleStep = 0;
				}
				if (scaleStep == scaleOffsets.length) {
					scaleStep = 0;
				}
			}
		}
	}
	
	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		
		this.scale = this.monome.readConfigValue(pageElement, "scale");
		this.gui.getScaleTF().setText(scale);
		
		this.patternLength = Integer.parseInt(this.monome.readConfigValue(pageElement, "patternLength"));
		this.gui.getPatternLengthTF().setText("" + this.patternLength);
		
		this.quantization = Integer.parseInt(this.monome.readConfigValue(pageElement, "quantization"));
		this.gui.getQuantizationTF().setText("" + this.quantization);
		
		this.startNote = Integer.parseInt(this.monome.readConfigValue(pageElement, "startNote"));
		this.gui.getStartNoteTF().setText("" + this.startNote);
		
		this.maxNote = Integer.parseInt(this.monome.readConfigValue(pageElement, "maxNote"));
		this.gui.getMaxNoteTF().setText("" + this.maxNote);
		
		this.numNotes = Integer.parseInt(this.monome.readConfigValue(pageElement, "numNotes"));
		this.gui.getNumNotesTF().setText(""+this.numNotes);
		
		this.numNotes = Integer.parseInt(this.monome.readConfigValue(pageElement, "chance"));
		this.gui.getChanceTF().setText(""+this.chance);
		
		this.maxRadius = Integer.parseInt(this.monome.readConfigValue(pageElement, "maxRadius"));
		this.gui.getMaxRadiusTF().setText("" + this.maxRadius);
		
		this.midiChannel = Integer.parseInt(this.monome.readConfigValue(pageElement, "midiChannel"));
		this.gui.getMidiChannelTF().setText("" + this.midiChannel);				
	}

	public void destroyPage() {

	}

	public boolean getCacheDisabled() {
		return false;
	}

	public int getIndex() {
		return this.index;
	}

	public String getName() {
		return this.pageName;
	}

	public JPanel getPanel() {
		return gui;
	}

	public void handleADC(int adcNum, float value) {

	}

	public void handleADC(float x, float y) {

	}

	public void handlePress(int x, int y, int value) {
		if (value == 1) {
			numHeld++;
			if (numHeld == 1) {
				held = new ArrayList<Press>();
			}
			int position = (int) ((float) tickNum / (float) quantization) * quantization; 
			held.add(new Press(position, position, x, y, 1, -1, -1));
			
			MIDIGenerator mg = new MIDIGenerator(monome, index, x, y, maxRadius, chance, numNotes, noteMap, midiChannel);
			generators.add(mg);
			running = false;
		} else {
			numHeld--;
			if (numHeld == 0) {
				running = true;
			}
		}
	}

	public void handleReset() {
		tickNum = 0;
	}

	public void handleTick(MidiDevice device) {
		tickNum++;
		if (tickNum % quantization != 0) {
			return;
		}
		for (int i = 0; i < generators.size(); i++) {
			MIDIGenerator mg = generators.get(i);
			if (mg != null) {
				if (mg.notes == 0) {
					boolean doNoteOff = true;
					for (int j = 0; j < generators.size(); j++) {
						if (mg != generators.get(j) && generators.get(j).lastX == mg.lastX && generators.get(j).lastY == mg.lastY) {
							doNoteOff = false;
							break;
						}
					}
					if (doNoteOff) {
						mg.noteOff();
					}
					generators.remove(mg);
					continue;
				}
				mg.run();
			}
		}
		if (tickNum == patternLength) {
			tickNum = 0;
		}
		if (running) {
			for (int i = 0; i < held.size(); i++) {
				Press press = held.get(i);
				int position = press.getPosition();
				if (position != tickNum) {
					continue;
				}
				int[] values = press.getPress();
				int x = values[0];
				int y = values[1];
				MIDIGenerator mg = new MIDIGenerator(monome, index, x, y, maxRadius, chance, numNotes, noteMap, midiChannel);
				generators.add(mg);
			}
		}
	}

	public boolean isTiltPage() {
		return false;
	}

	public void redrawDevice() {
		this.monome.clear(0, index);
	}

	public void send(MidiMessage message, long timeStamp) {

	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	public String toXml() {
		String xml = "";
		xml += "      <name>MIDI Generator</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <scale>" + this.scale + "</scale>\n";
		xml += "      <patternLength>" + this.patternLength + "</patternLength>\n";
		xml += "      <quantization>" + this.quantization + "</quantization>\n";
		xml += "      <startNote>" + this.startNote + "</startNote>\n";
		xml += "      <maxNote>" + this.maxNote + "</maxNote>\n";
		xml += "      <numNotes>" + this.numNotes + "</numNotes>\n";
		xml += "      <chance>" + this.chance + "</chance>\n";
		xml += "      <maxRadius>" + this.maxRadius + "</maxRadius>\n";
		xml += "      <midiChannel>" + this.midiChannel + "</midiChannel>\n";
		return xml;
	}
	
	/**
	 * Convert a MIDI note number to a string, ie. "C-3".
	 * 
	 * @param noteNum The MIDI note number to convert
	 * @return The converted representation of the MIDI note number (ie. "C-3")
	 */
	public String numberToMidiNote(int noteNum) {
		int n = noteNum % 12;
		String note = "";
		switch (n) {
		case 0:
			note = "C"; break;
		case 1:
			note = "C#"; break;
		case 2:
			note = "D"; break;
		case 3:
			note = "D#"; break;
		case 4:
			note = "E"; break;
		case 5:
			note = "F"; break;
		case 6:
			note = "F#"; break;
		case 7:
			note = "G"; break;
		case 8: 
			note = "G#"; break;
		case 9:
			note = "A"; break;
		case 10:
			note = "A#"; break;
		case 11:
			note = "B"; break;
		}

		int o = (noteNum / 12) - 2;
		note = note.concat("-" + String.valueOf(o));
		return note;
	}

	/**
	 * Converts a note name to a MIDI note number (ie. "C-3").
	 * 
	 * @param convert_note The note to convert (ie. "C-3")
	 * @return The MIDI note value of that note
	 */
	public int noteToMidiNumber(String convert_note) {		
		for (int n=0; n < 12; n++) {
			String note = "";
			switch (n) {
			case 0:
				note = "C"; break;
			case 1:
				note = "C#"; break;
			case 2:
				note = "D"; break;
			case 3:
				note = "D#"; break;
			case 4:
				note = "E"; break;
			case 5:
				note = "F"; break;
			case 6:
				note = "F#"; break;
			case 7:
				note = "G"; break;
			case 8: 
				note = "G#"; break;
			case 9:
				note = "A"; break;
			case 10:
				note = "A#"; break;
			case 11:
				note = "B"; break;
			}
			for (int o=0; o < 8; o++) {
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
