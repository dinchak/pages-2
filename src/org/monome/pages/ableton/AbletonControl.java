/*
 *  AbletonControl.java
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
 * Provides an interface for controlling Ableton Live.
 * 
 * @author Tom Dinchak
 *
 */
public interface AbletonControl {
	
	/**
	 * Sends a 'play clipslot' command to Ableton.  This can trigger recording of an empty clipslot.  
	 * 
	 * @param track the track number, left to right starting at 0
	 * @param clip The clip number, top to bottom starting at 0.
	 */
	public void playClip(int track, int clip);
	
	/**
	 * Sends a 'stop clip' command to Ableton.
	 * 
	 * @param track The track number, left to right starting at 0.
	 * @param clip The clip number, top to bottom starting at 0.
	 */
	public void stopClip(int track, int clip);
	
	/**
	 * Sends an 'arm track' command to Ableton.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void armTrack(int track);
	
	/**
	 * Sends a 'disarm track' command to Ableton.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void disarmTrack(int track);
	
	/**
	 * Sends a 'mute track' command to Ableton.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void muteTrack(int track);
	
	/**
	 * Sends an 'unmute track' command to Ableton.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void unmuteTrack(int track);
	
	/**
	 * Sends a 'stop track' command to Ableton.  This will stop any currently playing clip in the track.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void stopTrack(int track);
	
	/**
	 * Sends a 'view track' command to Ableton.  This brings up any open plugin windows and selects the track.
	 * 
	 * @param track The track number, left to right starting at 0.
	 */
	public void viewTrack(int track);
	
	/**
	 * Sends a 'jump in playing clip' command to a track in Ableton.  
	 * 
	 * @param track The track number, left to right starting at 0.
	 * @param amount The amount to jump in bars relative to the current playhead position.
	 */
	public void trackJump(int track, float amount);
	
	/**
	 * Sends a 'redo' command to Ableton.
	 */
	public void redo();
	
	/**
	 * Sends an 'undo' command to Ableton.
	 */
	public void undo();
	
	/**
	 * Sets the current overdub state.
	 * 
	 * @param overdub The new overdub state (0 = off, 1 = on).
	 */
	public void setOverdub(int overdub);
	
	/**
	 * Sets the tempo. 
	 * 
	 * @param tempo The new tempo in BPM.
	 */
	public void setTempo(float tempo);
		
	/**
	 * Launches a scene.
	 * 
	 * @param scene_num The scene number to launch, from top to bottom starting with 0.
	 */
	public void launchScene(int scene_num);

	/**
	 * Requests a complete refresh of the current state from Ableton.
	 */
	public void refreshAbleton();

	/**
	 * Sends a 'solo track' command to Ableton.
	 * 
	 * @param track
	 */
	public void soloTrack(int track);
	
	/**
	 * Sends an 'unsolo track' command to Ableton.
	 * 
	 * @param track
	 */
	public void unsoloTrack(int track);
	
	/**
	 * Sets the red ring in ableton.
	 * 
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 */
	public void setSelection(int widthOffset, int sceneOffset, int width, int height);
}
