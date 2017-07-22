package org.monome.pages.configuration;

import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.monome.pages.midi.MidiDeviceFactory;

public class NoteEvent implements Runnable {
	
	private Receiver recv;
	private ShortMessage msg;
	private int delayTime;

	public NoteEvent(Receiver recv, ShortMessage msg, int delayTime) {
		this.recv = recv;
		this.msg = msg;
		this.delayTime = delayTime;
	}
	
	public void run() {
		try {
			Thread.sleep(this.delayTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.recv.send(msg, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
	}
}
