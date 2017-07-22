package org.monome.pages.ableton;

import java.util.HashMap;

public class AbletonState {
	
	private HashMap<Integer, AbletonTrack> tracks;
	private float tempo;
	private int overdub;
	private int selectedScene;
	
	public AbletonState() {
		tracks = new HashMap<Integer, AbletonTrack>();
		tempo = 120.0f;
		overdub = 1;
		setSelectedScene(1);
	}
	
	public AbletonTrack getTrack(int i) {
		Integer key = new Integer(i);
		synchronized(tracks) {
			if (tracks.containsKey(key)) {
				return tracks.get(key);
			}
		}
		return null;
	}
	
	public HashMap<Integer, AbletonTrack> getTracks() {
		return tracks;
	}
	
	public void removeTrack(int trackId) {
		if (tracks.containsKey(new Integer(trackId))) {
			tracks.put(new Integer(trackId), null);
		}
	}
	
	public int getOverdub() {
		return overdub;
	}

	public void setOverdub(int overdub) {
		this.overdub = overdub;
	}

	public float getTempo() {
		return tempo;
	}
	
	public void setTempo(float tempo) {
		this.tempo = tempo;
	}

	public void setSelectedScene(int selectedScene) {
		this.selectedScene = selectedScene;
	}

	public int getSelectedScene() {
		return selectedScene;
	}

	public synchronized void reset() {
		synchronized(tracks) {
			this.tracks = new HashMap<Integer, AbletonTrack>();
		}
	}

	public AbletonTrack createTrack(int trackId) {
		AbletonTrack track = new AbletonTrack();
		Integer key = new Integer(trackId);
		synchronized(tracks) {
			tracks.put(key, track);
		}
		return track;
	}
}
