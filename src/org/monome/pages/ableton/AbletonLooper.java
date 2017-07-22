package org.monome.pages.ableton;

public class AbletonLooper {
	public final static int STATE_STOPPED = 0;
	public final static int STATE_RECORDING = 1;
	public final static int STATE_PLAYING = 2;
	public final static int STATE_OVERDUB = 3;
	private int state = 0;
	
	public void setState(int state) {
		this.state = state;
	}
	public int getState() {
		return state;
	}
}
