package com.illposed.osc.utility;

import java.util.*;

import com.illposed.osc.*;

/**
 * @author cramakrishnan
 * @author bjoern
 * 
 * modified code:
 * 
 * original code:
 * Copyright (C) 2003, C. Ramakrishnan / Auracle
 * All rights reserved.
 * Modifications (C) 2005, Bjoern Hartmann
 * See license.txt (or license.rtf) for license information.
 * 
 * Dispatches OSCMessages to registered listeners.
 * 
 */

public class OSCPacketDispatcher {
	// use Hashtable for JDK1.1 compatability
	private final Hashtable<String, ArrayList<OSCListener>> addressToClassTable = new Hashtable<String, ArrayList<OSCListener>>();
	
	/**
	 * 
	 */
	public OSCPacketDispatcher() {
		super();
	}

	public void addListener(String address, OSCListener listener) {
		ArrayList<OSCListener> listeners = addressToClassTable.get(address);
		if (listeners == null) {
			listeners = new ArrayList<OSCListener>();
		}
		listeners.add(listener);
		addressToClassTable.put(address, listeners);
	}
	
	/**
	 * Remove all listeners from the dispatch table
	 * @author bjoern
	 */
	public void removeAllListeners() {
		addressToClassTable.clear();
	}
	
	/**
	 * Remove one listener from the dispatch table
	 * @param address osc address string
	 * @return true if listener was found and removed, false otherwise
	 * @author bjoern
	 */
	public boolean removeListener(String address) {
		if (addressToClassTable.containsKey(address)) {
			addressToClassTable.remove(address);
			return true;
		} else {
			return false; // key not found - can't remove
		}
	}
	public OSCListener getListener(String address) {
		if(addressToClassTable.containsKey(address)) {
			return (OSCListener) addressToClassTable.get(address);
		} else return null;
	}
	 /**
	  * Change a listener address
	  * @param oldAddress Old OSC address
	  * @param newAddress New OSC address
	  * @return true if change was successful, false if it failed either b/c oldAddress not found or new Address already exists
	  * @author bjoern
	  */
	public boolean changeListenerAddress(String oldAddress, String newAddress) {
		if(addressToClassTable.containsKey(oldAddress) && !addressToClassTable.containsKey(newAddress)) {
			addressToClassTable.put(newAddress,addressToClassTable.remove(oldAddress));
			return true;
		} else {
			return false;
		}
	}
	
	public void dispatchPacket(OSCPacket packet) {
		if (packet instanceof OSCBundle)
			dispatchBundle((OSCBundle) packet);
		else
			dispatchMessage((OSCMessage) packet);
	}
	
	public void dispatchPacket(OSCPacket packet, Date timestamp) {
		if (packet instanceof OSCBundle)
			dispatchBundle((OSCBundle) packet);
		else
			dispatchMessage((OSCMessage) packet, timestamp);
	}
	
	private void dispatchBundle(OSCBundle bundle) {
		Date timestamp = bundle.getTimestamp();
		OSCPacket[] packets = bundle.getPackets();
		for (int i = 0; i < packets.length; i++) {
			dispatchPacket(packets[i], timestamp);
		}
	}
	
	private void dispatchMessage(OSCMessage message) {
		dispatchMessage(message, null);
	}
	
	private void dispatchMessage(OSCMessage message, Date time) {
		Enumeration<String> keys = addressToClassTable.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			// this supports the OSC regexp facility, but it
			// only works in JDK 1.4 and up
			//if (key.matches(message.getAddress())) {
			//if (key.equals(message.getAddress())) {
			if(message.getAddress().matches(key)){
				ArrayList<OSCListener> listeners = addressToClassTable.get(key);
				for (int i = 0; i < listeners.size(); i++) {
					OSCListener listener = (OSCListener) listeners.get(i);
					listener.acceptMessage(time, message);
				}
			}
		}
	}
}
