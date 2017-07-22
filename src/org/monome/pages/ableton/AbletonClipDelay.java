/*
 *  AbletonClipDelay.java
 * 
 *  Copyright (c) 2009, Tom Dinchak
 * 
 *  This file is part of Pages.
 *
 *  Pages is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Pages is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Pages; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package org.monome.pages.ableton;

import org.monome.pages.Main;
import org.monome.pages.configuration.Configuration;

/**
 * Delays sending a play clip command to Ableton, used by the Live Looper page to cut loops.
 * 
 * @author Tom Dinchak
 *
 */
public class AbletonClipDelay implements Runnable {
	
	/**
	 * Amount of time to delay in ms.
	 */
	private int delay;

	/**
	 * The track number where the clip lives, from left to right starting at 0.
	 */
	private int track;

	/**
	 * The clip number, from top to bottom starting at 0. 
	 */
	private int clip;

	public AbletonClipDelay(int delay, int track, int clip) {
		this.delay = delay;
		this.track = track;
		this.clip = clip;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Configuration configuration = Main.main.configuration;
		try {
			Thread.sleep(this.delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		configuration.getAbletonControl().playClip(track, clip);
	}

}
