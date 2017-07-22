package org.monome.pages.ableton;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.monome.pages.Main;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

/**
 * The AbletonOSCListener object listens for OSC messages from Ableton and updates the AbletonState object.
 * 
 * @author Tom Dinchak
 *
 */
public class AbletonOSCListener implements OSCListener {

	/* (non-Javadoc)
	 * @see com.illposed.osc.OSCListener#acceptMessage(java.util.Date, com.illposed.osc.OSCMessage)
	 */
	public void acceptMessage(Date arg0, OSCMessage msg) {
		Object[] args = msg.getArguments();		
		if (msg.getAddress().compareTo("/live/devicelist") == 0) {
			int trackId = ((Integer) args[0]).intValue();
			for (int i = 1; i < args.length; i += 2) {
				if (((String) args[i+1]).compareTo("Looper") == 0) {
					int deviceId = ((Integer) args[i]).intValue();
					
					Main.main.configuration.abletonState.getTrack(trackId).createLooper(deviceId);
					
					Object sendArgs[] = new Object[2];
					sendArgs[0] = trackId;
					sendArgs[1] = deviceId;
					OSCMessage sendMsg = new OSCMessage("/live/device", sendArgs);
					try {
						Main.main.configuration.getAbletonOSCPortOut().send(sendMsg);
					} catch (IOException e) {
						e.printStackTrace();
					}					
				}
			}
		}
		
		if (msg.getAddress().compareTo("/live/device/allparam") == 0 || msg.getAddress().compareTo("/live/device/param") == 0) {
			int trackId = ((Integer) args[0]).intValue();
			if (Main.main.configuration.abletonState.getTrack(trackId) == null) {
				return;
			}
			int deviceId = ((Integer) args[1]).intValue();
			AbletonLooper looper = Main.main.configuration.abletonState.getTrack(trackId).getLooper(deviceId);
			if (looper != null) {
				for (int i = 2; i < args.length; i += 3) {
					if (((String) args[i+2]).compareTo("State") == 0) {
					    if (args[i+1] instanceof Integer) {
    						if (((Integer)args[i+1]).intValue() != looper.getState()) {
    							looper.setState(((Integer)args[i+1]).intValue());
    							Main.main.configuration.redrawAbletonPages();
    						}
					    } if (args[i+1] instanceof Float) {
                            if (((Float)args[i+1]).intValue() != looper.getState()) {
                                looper.setState(((Float)args[i+1]).intValue());
                                Main.main.configuration.redrawAbletonPages();
                            }					        
					    }
					}
				}
			}
		}

		if (msg.getAddress().compareTo("/live/track") == 0) {
			int trackId = ((Integer) args[0]).intValue();
			((AbletonOSCControl) Main.main.configuration.getAbletonControl()).refreshTrackInfo(trackId);
		}
		
		if (msg.getAddress().compareTo("/live/name/track") == 0) {
			int trackId = ((Integer) args[0]).intValue();
			HashMap<Integer, AbletonTrack> tracks = Main.main.configuration.abletonState.getTracks();
			for (int i = trackId + 1; i < tracks.size(); i++) {
				Main.main.configuration.abletonState.removeTrack(i);
			}
			((AbletonOSCControl) Main.main.configuration.getAbletonControl()).refreshTrackInfo(trackId);
			//Main.main.configuration.redrawAbletonPages();
		}
		
		if (msg.getAddress().contains("/live/track/info")) {
			
			int trackId = ((Integer) args[0]).intValue();
			int armed = ((Integer) args[1]).intValue();
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackId);
			if (track == null) {
				track = Main.main.configuration.abletonState.createTrack(trackId);
			}
			track.setArm(armed);
			for (int i=2; i < args.length; i+=3) {
				int clipId = ((Integer) args[i]).intValue();
				int clipState = ((Integer) args[i+1]).intValue();
				float length = ((Float) args[i+2]).floatValue();
				AbletonClip clip = track.getClip(clipId);
				if (clip == null) {
					clip = track.createClip(clipId);
				}
				clip.setState(clipState);
				clip.setLength(length);
			}
			Main.main.configuration.redrawAbletonPages();
			Object sendArgs[] = new Object[1];
			sendArgs[0] = trackId;
			OSCMessage sendMsg = new OSCMessage("/live/devicelist", sendArgs);
			try {
				Main.main.configuration.getAbletonOSCPortOut().send(sendMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (msg.getAddress().contains("/live/clip/info")) {
			int trackId = ((Integer) args[0]).intValue();
			int clipId = ((Integer) args[1]).intValue();
			int clipState = ((Integer) args[2]).intValue();
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackId);
			if (track == null) {
				track = Main.main.configuration.abletonState.createTrack(trackId);
			}
			AbletonClip clip = track.getClip(clipId);
			if (clip == null) {
				clip = track.createClip(clipId);
			}
			clip.setState(clipState);
			Main.main.configuration.redrawAbletonPages();
		}
		
        if (msg.getAddress().contains("/live/state")) {
            float tempo = ((Float) args[0]).floatValue();
            int overdub = ((Integer) args[1]).intValue();
            Main.main.configuration.abletonState.setTempo(tempo);
            Main.main.configuration.abletonState.setOverdub(overdub);
    		Main.main.configuration.redrawAbletonPages();
        }
        
        if (msg.getAddress().contains("/live/scene")) {
        	int selectedScene = ((Integer) args[0]).intValue();
        	Main.main.configuration.abletonState.setSelectedScene(selectedScene);
    		Main.main.configuration.redrawAbletonPages();
        }
        
        if (msg.getAddress().contains("/live/arm")) {
        	int trackId = ((Integer) args[0]).intValue();
        	int armState = ((Integer) args[1]).intValue();
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackId);
			if (track == null) {
				track = Main.main.configuration.abletonState.createTrack(trackId);
			}
        	track.setArm(armState);
			Main.main.configuration.redrawAbletonPages();
        }

        if (msg.getAddress().contains("/live/solo")) {
        	int trackId = ((Integer) args[0]).intValue();
        	int soloState = ((Integer) args[1]).intValue();
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackId);
			if (track == null) {
				track = Main.main.configuration.abletonState.createTrack(trackId);
			}
        	track.setSolo(soloState);
			Main.main.configuration.redrawAbletonPages();
        }
		
        if (msg.getAddress().contains("/live/mute")) {
        	int trackId = ((Integer) args[0]).intValue();
        	int muteState = ((Integer) args[1]).intValue();
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackId);
			if (track == null) {
				track = Main.main.configuration.abletonState.createTrack(trackId);
			}
        	track.setMute(muteState);
			Main.main.configuration.redrawAbletonPages();
        }
        
        if (msg.getAddress().contains("/live/tempo")) {
        	float tempo = ((Float) args[0]).floatValue();
        	Main.main.configuration.abletonState.setTempo(tempo);
        }
        
        if (msg.getAddress().contains("/live/overdub")) {
        	int overdub = ((Integer) args[0]).intValue();
        	Main.main.configuration.abletonState.setOverdub(overdub - 1);
			Main.main.configuration.redrawAbletonPages();
        }
        
        if (msg.getAddress().contains("/live/refresh")) {
        	Main.main.configuration.getAbletonControl().refreshAbleton();
        }
        
        if (msg.getAddress().contains("/live/reset")) {
        	Main.main.configuration.getAbletonControl().refreshAbleton();
        }
        
	}
}