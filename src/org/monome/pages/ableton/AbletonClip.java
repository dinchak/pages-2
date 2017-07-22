/*
 *  AbletonClip.java
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

/**
 * Stores the state of a single clip slot.
 * 
 * @author Tom Dinchak 
 *
 */
public class AbletonClip {

	/**
	 * Indicates an empty clip slot. 
	 */
	public static final int STATE_EMPTY = 0;
	
	/**
	 * Indicates that the clip has been stopped.
	 */
	public static final int STATE_STOPPED = 1;
	
	/**
	 * Indicates that the clip is playing. 
	 */
	public static final int STATE_PLAYING = 2;
	
	/**
	 * Indicates that the clip has been triggered for playing or the empty clip slot has been triggered for recording. 
	 */
	public static final int STATE_TRIGGERED = 3;
	
	/**
	 * The current state of the clip.
	 */
	private int state;
	
	/**
	 * The length of the clip in bars.
	 */
	private float length;
	
	/**
	 * The current playhead position in the clip.
	 */
	private float position;
	
	public AbletonClip() {
		state = 0;
		setLength(0.0f);
		setPosition(0.0f);
	}
	
	/**
	 * Sets the current state of the clip.
	 * 
	 * @param state AbletonClip.STATE_EMPTY, AbletonClip.STATE_STOPPED, AbletonClip.STATE_PLAYING, or AbletonClip.STATE_TRIGGERED 
	 */
	public void setState(int state) {
		this.state = state;
	}
	
	/**
	 * Gets the current state of the clip.
	 * 
	 * @return state AbletonClip.STATE_EMPTY, AbletonClip.STATE_STOPPED, AbletonClip.STATE_PLAYING, or AbletonClip.STATE_TRIGGERED
	 */
	public int getState() {
		return state;
	}

	/**
	 * Sets the length of the clip.
	 * 
	 * @param length The new length.
	 */
	public void setLength(float length) {
		this.length = length;
	}

	/**
	 * Gets the length of the clip.
	 * 
	 * @return The length of the clip.
	 */
	public float getLength() {
		return length;
	}

	/**
	 * Sets the current playhead position of the clip.
	 * 
	 * @param position The current playhead position of the clip.
	 */
	public void setPosition(float position) {
		this.position = position;
	}

	/**
	 * Gets the current playhead position of the clip.
	 * 
	 * @return The current playhead position of the clip
	 */
	public float getPosition() {
		return position;
	}
	
}
