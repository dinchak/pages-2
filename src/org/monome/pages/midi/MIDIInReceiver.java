package org.monome.pages.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import org.monome.pages.Main;

public class MIDIInReceiver implements Receiver {

	private MidiDevice device;
	
	public MIDIInReceiver(MidiDevice device) {
		this.device = device;
	}
	
	public void close() {
		// TODO Auto-generated method stub

	}

	public void send(MidiMessage arg0, long arg1) {
		if (Main.main.configuration != null && this.device != null) {
			Main.main.configuration.send(this.device, arg0, arg1);
		}
	}

}
