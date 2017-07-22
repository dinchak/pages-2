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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.monome.pages.Main;
import com.illposed.osc.OSCMessage;

/**
 * Implementation of the AbletonControl interface for control via the LiveAPI.
 * The latest version of the LiveAPI can be found at:
 * http://monome.q3f.org/changeset/latest/trunk/LiveOSC?old_path=/&filename=LiveOSC&format=zip
 * 
 * Documentation is available here:
 * http://monome.q3f.org/wiki/LiveOSC
 * 
 * @author Tom Dinchak
 *
 */
public class AbletonOSCControl implements AbletonControl, Serializable {	
    static final long serialVersionUID = 42L;

    /* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#armTrack(int)
	 */
	public void armTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		// 1 = arm
		args[1] = new Integer(1);
		OSCMessage msg = new OSCMessage("/live/arm", args);
		// send the message 5 times because Ableton doesn't always respond to
		// this for some reason
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setArm(1);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#disarmTrack(int)
	 */
	public void disarmTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		// 0 = disarm
		args[1] = new Integer(0);
		OSCMessage msg = new OSCMessage("/live/arm", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setArm(0);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#playClip(int, int)
	 */
	public void playClip(int track, int clip) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(clip);
		OSCMessage msg = new OSCMessage("/live/play/clipslot", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// listener will handle updating us on the state
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#stopClip(int, int)
	 */
	public void stopClip(int track, int clip) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(clip);
		OSCMessage msg = new OSCMessage("/live/stop/clip", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// listener will handle updating us on the state
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#redo()
	 */
	public void redo() {
		OSCMessage msg = new OSCMessage("/live/redo");
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// refresh everything because anything can happen (!)
		this.refreshAbleton();
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#setOverdub(int)
	 */
	public void setOverdub(int overdub) {
		Object args[] = new Object[1];
		args[0] = new Integer(overdub);
		OSCMessage msg = new OSCMessage("/live/overdub", args);
		
		// send the message 5 times because Ableton doesn't always respond to
		// this for some reason
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			abletonState.setOverdub(overdub);
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#setTempo(float)
	 */
	public void setTempo(float tempo) {
		if (tempo + 1.0 > 999.0) {
			tempo = (float) 998.0;
		}
		Object args[] = new Object[1];
		args[0] = new Float(tempo + 1.0);
		
		OSCMessage msg = new OSCMessage("/live/tempo", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			abletonState.setTempo(tempo);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Sends "/live/stop/track track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void stopTrack(int track) {
		Object args[] = new Object[1];
		args[0] = new Integer(track);
		OSCMessage msg = new OSCMessage("/live/stop/track", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// listener will handle updating us on the state
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#muteTrack(int)
	 */
	public void muteTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(1);
		OSCMessage msg = new OSCMessage("/live/mute", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setMute(1);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#unmuteTrack(int)
	 */
	public void unmuteTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(0);
		OSCMessage msg = new OSCMessage("/live/mute", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setMute(0);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.ableton.AbletonControl#soloTrack(int)
	 */
	public void soloTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(1);
		OSCMessage msg = new OSCMessage("/live/solo", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setSolo(1);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void unsoloTrack(int track) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Integer(0);
		OSCMessage msg = new OSCMessage("/live/solo", args);
		try {
			for (int i = 0; i < 5; i++) {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			}
			// update ableton state
			AbletonState abletonState = Main.main.configuration.getAbletonState();
			AbletonTrack abletonTrack = abletonState.getTrack(track);
			if (abletonTrack != null) {
				abletonTrack.setSolo(0);
			}
			// redrawDevice all ableton pages
			Main.main.configuration.redrawAbletonPages();			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Sends "/live/track/view track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void viewTrack(int track) {
		if (Main.main.configuration.getAbletonIgnoreViewTrack() == false) {
			Object args[] = new Object[1];
			args[0] = new Integer(track);
			OSCMessage msg = new OSCMessage("/live/track/view", args);
			try {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void trackJump(int track, float amount) {
		Object args[] = new Object[2];
		args[0] = new Integer(track);
		args[1] = new Float(amount);
		OSCMessage msg = new OSCMessage("/live/track/jump", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends "/live/undo" to LiveOSC. 
	 */
	public void undo() {
		OSCMessage msg = new OSCMessage("/live/undo");
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.refreshAbleton();
	}
	
	public void launchScene(int scene_num) {
		Object args[] = new Object[1];
		args[0] = new Integer(scene_num);
		OSCMessage msg = new OSCMessage("/live/play/scene", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.refreshState();
	}

	public void refreshClipInfo(int trackNum, int clipNum) {
		Object args[] = new Object[2];
		args[0] = trackNum;
		args[1] = clipNum;
		OSCMessage msg = new OSCMessage("/live/clip/info", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void refreshAllTracks() {
		OSCMessage msg = new OSCMessage("/live/track/info");
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		HashMap<Integer, AbletonTrack> tracks = Main.main.configuration.abletonState.getTracks();
		Iterator<Integer> i = tracks.keySet().iterator();
		while (i.hasNext()) {
			Integer trackNum = i.next();
			Object args[] = new Object[1];
			args[0] = trackNum;
			msg = new OSCMessage("/live/mute", args);
			OSCMessage msg2 = new OSCMessage("/live/solo", args);
			try {
				Main.main.configuration.getAbletonOSCPortOut().send(msg);
				Main.main.configuration.getAbletonOSCPortOut().send(msg2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void refreshTrackInfo(int trackNum) {
		Object args[] = new Object[1];
		args[0] = trackNum;
		OSCMessage msg = new OSCMessage("/live/track/info", args);
		OSCMessage msg2 = new OSCMessage("/live/mute", args);
		OSCMessage msg3 = new OSCMessage("/live/solo", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
			Main.main.configuration.getAbletonOSCPortOut().send(msg2);
			Main.main.configuration.getAbletonOSCPortOut().send(msg3);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void refreshState() {
		OSCMessage msg  = new OSCMessage("/live/state");
		OSCMessage msg2 = new OSCMessage("/live/tempo");
		OSCMessage msg3 = new OSCMessage("/live/overdub");
		OSCMessage msg4 = new OSCMessage("/live/scene");
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
			Main.main.configuration.getAbletonOSCPortOut().send(msg2);
			Main.main.configuration.getAbletonOSCPortOut().send(msg3);
			Main.main.configuration.getAbletonOSCPortOut().send(msg4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refreshAbleton() {
		Main.main.configuration.abletonState.reset();
		refreshAllTracks();
		refreshState();
		Main.main.configuration.redrawAbletonPages();
	}

	public void setSelection(int widthOffset, int sceneOffset, int width,
			int height) {
		Object args[] = new Object[4];
		args[0] = widthOffset;
		args[1] = sceneOffset;
		args[2] = width;
		args[3] = height;
		OSCMessage msg = new OSCMessage("/live/selection", args);
		try {
			Main.main.configuration.getAbletonOSCPortOut().send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}	
}
