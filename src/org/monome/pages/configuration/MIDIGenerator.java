package org.monome.pages.configuration;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.monome.pages.midi.MidiDeviceFactory;

public class MIDIGenerator {

	private MonomeConfiguration monome;
	private int index;
	private int x;
	private int y;
	private int radius;
	private int maxRadius;
	private int chance;
	public int notes;
	private int radiusIncrement;
	public int lastX;
	public int lastY;
	private int[][] noteMap;
	private int midiChannel;
	
	public MIDIGenerator(MonomeConfiguration monome, int index, int x, int y, int maxRadius, int chance, int notes, int[][] noteMap, int midiChannel) {
		this.monome = monome;
		this.index = index;
		this.radius = 0;
		this.maxRadius = maxRadius;
		this.chance = chance;
		this.notes = notes;
		if (this.maxRadius != 0) {
			this.radiusIncrement = this.notes / this.maxRadius;
		} else {
			this.radiusIncrement = 0;
		}
		this.x = x;
		this.y = y;
		this.lastX = -1;
		this.lastY = -1;
		this.noteMap = noteMap;
		this.midiChannel = midiChannel;
	}
	
	public void run() {
		if (lastX >= 0 && lastY >= 0) {
			noteOff();
		}
		int chance = (int) (Math.random() * this.chance);
		if (chance == 0 || radius == 0) {
			int xOffset = (int) (Math.random() * (1 + (radius * 2))) - radius;
			int yOffset = (int) (Math.random() * (1 + (radius * 2))) - radius;
			int x = this.x + xOffset;
			int y = this.y + yOffset;
			if (x < 0) {
				x = Math.abs(x % monome.sizeX);
			}
			if (y < 0) {
				y = Math.abs(y % monome.sizeY);
			}
			if (x >= monome.sizeX) {
				x = x % monome.sizeX;
			}
			if (y >= monome.sizeY ) {
				y = y % monome.sizeY;
			}
			monome.led(x, y, 1, index);
			String[] midiOutOptions = monome.getMidiOutOptions(this.index);
			for (int i = 0; i < midiOutOptions.length; i++) {
				if (midiOutOptions[i] == null) {
					continue;
				}
				Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
				ShortMessage msg = new ShortMessage();
				try {
					msg.setMessage(ShortMessage.NOTE_ON, midiChannel - 1, noteMap[x][y], 127);
					if (recv != null) {
						recv.send(msg, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
					}
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}
			lastX = x;
			lastY = y;
			if (radius == 0 && radiusIncrement != 0) {
				radius++;
			}
		}
		notes--;
		if (this.radiusIncrement != 0 && this.notes % this.radiusIncrement == 0) {
			this.radius++;
		}
	}
	
	public void noteOff() {
		this.monome.led(this.lastX, this.lastY, 0, this.index);
		String[] midiOutOptions = monome.getMidiOutOptions(this.index);
		for (int i = 0; i < midiOutOptions.length; i++) {
			if (midiOutOptions[i] == null) {
				continue;
			}
			Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
			ShortMessage msg = new ShortMessage();
			try {
				msg.setMessage(ShortMessage.NOTE_OFF, midiChannel - 1, noteMap[lastX][lastY], 0);
				if (recv != null) {
					recv.send(msg, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
				}
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		}
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


}
