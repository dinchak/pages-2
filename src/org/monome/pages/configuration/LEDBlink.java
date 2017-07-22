package org.monome.pages.configuration;

import org.monome.pages.configuration.MonomeConfiguration;

/**
 * A thread to blink leds cleanly.
 * 
 * @author Tom Dinchak, Julien Bayle
 *
 */

public class LEDBlink implements Runnable {
	int x, y;
	int pageIndex;
	int delay;
	boolean cancel;
		
	/**
	 * The MonomeConfiguration that the fader page this thread belongs to is on
	 */
	private MonomeConfiguration monome;
	
	/**
	 * @param monome
	 * @param x
	 * @param y
	 * @param delay
	 */
	public LEDBlink(MonomeConfiguration monome, int x, int y, int delay, int pageIndex) {
        this.monome = monome;
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.pageIndex = pageIndex;
        this.cancel = false;
        }

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.monome.led(this.x, this.y, 1, this.pageIndex);
		try {
			Thread.sleep(this.delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (cancel) {
			return;
		}
		this.monome.led(this.x, this.y, 0, this.pageIndex);
	}
	
	public void cancel() {
		this.cancel = true;
	}

}