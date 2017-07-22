package org.monome.pages.configuration;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.midi.MidiDeviceFactory;

/**
 * A thread that behaves like a MIDI fader being moved up or down.  It sends out MIDI CC messages from
 * it's starting point to it's ending point and moves at a specified speed.  It also updates a monome's
 * leds accordingly as it moves.
 * 
 * @author Tom Dinchak
 *
 */
public class MIDIFader implements Runnable {

	/**
	 * The MIDI Receiver to send on
	 */
	private Receiver recv;

	/**
	 * The MIDI channel to use
	 */
	private int channel;

	/**
	 * The MIDI control change number to use
	 */
	private int cc;

	/**
	 * The CC value to start at 
	 */
	private int startVal;

	/**
	 * The CC value to end at 
	 */
	private int endVal;

	/**
	 * The MonomeConfiguration that the fader page this thread belongs to is on
	 */
	private MonomeConfiguration monome;

	/**
	 * The column that was pressed on the monome
	 */
	private int col;

	/**
	 * The starting point Y coordinate on the monome
	 */
	private int startY;

	/**
	 * The Y coordinate to end on when the thread is complete
	 */
	private int endY;

	/**
	 * The page index of the fader page this thread belongs to
	 */
	private int pageIndex;

	/**
	 * The amount to delay between every movement of 1 MIDI CC value (in ms)
	 */
	private int delayAmount;

	/**
	 * 
	 */
	private int[] buttonValues;
	
	private boolean horizontal;

	/**
	 * @param recv
	 * @param channel
	 * @param cc
	 * @param startVal
	 * @param endVal
	 * @param buttonValues
	 * @param monome
	 * @param col
	 * @param startY
	 * @param endY
	 * @param pageIndex
	 * @param delayAmount
	 */
	public MIDIFader(Receiver recv, int channel, int cc, int startVal, int endVal, int[] buttonValues, 
			MonomeConfiguration monome, int col, int startY, int endY, int pageIndex, int delayAmount,
			boolean horizontal) {

		this.recv = recv;
		this.channel = channel;
		this.cc = cc;
		this.startVal = startVal;
		this.endVal = endVal;

		this.monome = monome;
		this.col = col;
		this.startY = startY;
		this.endY = endY;
		this.pageIndex = pageIndex;
		this.buttonValues = buttonValues;
		this.delayAmount = delayAmount;
		this.horizontal = horizontal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ShortMessage msg = new ShortMessage();
		int valueDirection;
		int buttonDirection;
		if (this.endVal > this.startVal) {
			valueDirection = 1;
		} else {
			valueDirection = -1;
		}

		if (this.endY > this.startY) {
			buttonDirection = 1;
		} else {
			buttonDirection = -1;
		}

		int msgs = this.startVal;
		int curButton = this.startY; 

		for (int i = this.startVal; i != this.endVal + valueDirection; i += valueDirection) {
			if (valueDirection == 1) {
				int curValue = this.buttonValues[curButton];
				if (msgs >= curValue) {
					if (buttonDirection == -1) {
						if (this.recv == null) {
							if (horizontal) {
								this.monome.led(curButton, this.col, 1, this.pageIndex);
							} else {
								this.monome.led(this.col, curButton, 1, this.pageIndex);
							}
						}
					} else {
						if (this.recv == null) {
							if (horizontal) {
								this.monome.led(curButton, this.col, 0, this.pageIndex);
							} else {
								this.monome.led(this.col, curButton, 0, this.pageIndex);
							}
						}
					}
					curButton += buttonDirection;
					if (curButton < 0) {
						curButton = 0;
					}
				}
			} else {
				if (msgs < this.buttonValues[curButton]) {
					if (buttonDirection == -1) {
						if (this.recv == null) {
							if (horizontal) {
								this.monome.led(curButton, this.col, 1, this.pageIndex);
							} else {
								this.monome.led(this.col, curButton, 1, this.pageIndex);
							}
						}
					} else {
						if (this.recv == null) {
							if (horizontal) {
								this.monome.led(curButton, this.col, 0, this.pageIndex);
							} else {
								this.monome.led(this.col, curButton, 0, this.pageIndex);
							}
						}
					}
					curButton += buttonDirection;
					if (curButton < 0) {
						curButton = 0;
					}
				}
			}

			try {
				msgs += valueDirection;
				msg.setMessage(ShortMessage.CONTROL_CHANGE, this.channel, this.cc, i);
				if (this.recv != null) {
					this.recv.send(msg, MidiDeviceFactory.getDevice(recv).getMicrosecondPosition());
				}
				Thread.sleep(this.delayAmount);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
