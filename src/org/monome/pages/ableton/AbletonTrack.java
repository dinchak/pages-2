package org.monome.pages.ableton;

import java.util.HashMap;

public class AbletonTrack {
	
	private HashMap<Integer, AbletonClip> clips;
	private HashMap<Integer, AbletonLooper> loopers;
	private int arm;
	private int solo;
	private int mute;
	
	public AbletonTrack() {
		clips = new HashMap<Integer, AbletonClip>();
		loopers = new HashMap<Integer, AbletonLooper>();
		arm = 0;
		solo = 0;
		mute = 0;
	}
	
	public synchronized HashMap<Integer, AbletonClip> getClips() {
		return clips;
	}
	
	public synchronized HashMap<Integer, AbletonLooper> getLoopers() {
		return loopers;
	}
	
	public synchronized AbletonClip getClip(int i) {
		Integer key = new Integer(i);
		if (clips.containsKey(key)) {
			return clips.get(key);
		}
		return null;
	}
	
	public synchronized AbletonLooper getLooper(int i) {
		Integer key = new Integer(i);
		if (loopers.containsKey(key)) {
			return loopers.get(key);
		}
		return null;
	}

	public void setArm(int arm) {
		this.arm = arm;
	}

	public int getArm() {
		return arm;
	}

	public void setSolo(int solo) {
		this.solo = solo;
	}

	public int getSolo() {
		return solo;
	}

	public void setMute(int mute) {
		this.mute = mute;
	}

	public int getMute() {
		return mute;
	}

	public synchronized AbletonClip createClip(int clipId) {
		Integer key = new Integer(clipId);
		if (!clips.containsKey(key)) {
			AbletonClip clip = new AbletonClip();
			clips.put(key, clip);
			return clip;
		}
		return clips.get(key);
	}
	
	public synchronized AbletonLooper createLooper(int deviceId) {
		Integer key = new Integer(deviceId);
		if (!loopers.containsKey(key)) {
			AbletonLooper looper = new AbletonLooper();
			loopers.put(key, looper);
			return looper;
		}
		return loopers.get(key);
	}

}
