package org.monome.pages.configuration;

import java.io.Serializable;

public class MIDIPageChangeRule implements Serializable {
    static final long serialVersionUID = 42L;
	
	private int note;
	private int channel;
	private int cc;
	private int ccVal;
	private int pageIndex;
	private String linkedSerial;
	private int linkedPageIndex;
	
	public MIDIPageChangeRule(int note, int channel, int cc, int ccVal, int pageIndex) {
		this.note = note;
		this.channel = channel;
		this.cc = cc;
		this.ccVal = ccVal;
		this.pageIndex = pageIndex;
	}
	
	public boolean checkNoteRule(int note, int channel) {
		if (this.note == note && this.channel == channel) {
			return true;
		}
		return false;
	}
	
	public boolean checkCCRule(int cc, int val, int channel) {
		if (this.cc == cc && this.ccVal == val && (this.channel == channel || this.channel == -1)) {
			return true;
		}
		return false;
	}
	
	public int getNote() {
		return note;
	}
	
	public int getChannel() {
		return channel;
	}

	public int getPageIndex() {
		return pageIndex;
	}
	
	public String getLinkedSerial() {
	    return linkedSerial;
	}
	
	public void setLinkedSerial(String serial) {
	    linkedSerial = serial;
	}
	
	public int getLinkedPageIndex() {
	    return linkedPageIndex;
	}
	
	public void setLinkedPageIndex(int newPageIndex) {
	    linkedPageIndex = newPageIndex; 
	}

	public int getCC() {
		return cc;
	}
	
	public int getCCVal() {
		return ccVal;
	}

}
