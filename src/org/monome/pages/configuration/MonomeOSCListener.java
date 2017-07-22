package org.monome.pages.configuration;

import java.util.Date;

import org.monome.pages.configuration.MonomeConfiguration;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

/**
 * Listens for /press messages from all monomes.
 * 
 * @author Tom Dinchak
 *
 */
public class MonomeOSCListener implements OSCListener {

	/**
	 * The MonomeConfiguration that this OSCListener triggers
	 */
	MonomeConfiguration monome;

	/**
	 * @param monome The MonomeConfiguration that this OSCListener triggers
	 */
	public MonomeOSCListener(MonomeConfiguration monome) {
		this.monome = monome;
	}

	/* (non-Javadoc)
	 * @see com.illposed.osc.OSCListener#acceptMessage(java.util.Date, com.illposed.osc.OSCMessage)
	 */
	public synchronized void acceptMessage(Date time, OSCMessage message) {
		Object[] args = message.getArguments();
		if (args == null) {
			return;
		}
		// only act if the message has our monome prefix
		if (!message.getAddress().contains(monome.prefix)) {
			return;
		}
		if (message.getAddress().contains("/grid/key")) {
			if (args.length == 3) {
				int x = ((Integer) args[0]).intValue();
				int y = ((Integer) args[1]).intValue();
				int value = ((Integer) args[2]).intValue();
				monome.handlePress(x, y, value);
			}
		}
		if (message.getAddress().contains("press")) {
			if (args.length == 3) {
				int x = ((Integer) args[0]).intValue();
				int y = ((Integer) args[1]).intValue();
				int value = ((Integer) args[2]).intValue();
				monome.handlePress(x, y, value);
			}
		}
		if (message.getAddress().contains("tilt")) {
			if (args.length == 4) {
				int n = ((Integer) args[0]).intValue();
				int x = ((Integer) args[1]).intValue();
				int y = ((Integer) args[2]).intValue();
				int z = ((Integer) args[3]).intValue();
				monome.handleTilt(n, x, y, z);
			}
		}
		/*
		if (message.getAddress().contains("adc")) { 
			int adcNum = ((Integer) args[0]).intValue();
			float value = ((Float) args[1]).floatValue();
			//monome.handleADC(adcNum, value);
		}
		if (message.getAddress().contains("tilt") && !(message.getAddress().contains("mode"))) {
			float x = 0.0f;
			if (args[0] instanceof Integer) {
				x = (float) ((Integer) args[0]).intValue();
			} else if (args[0] instanceof Float) {
				x = ((Float) args[0]).floatValue();
			}
			
			float y = 0.0f;
			if (args[1] instanceof Integer) {
				y = (float) ((Integer) args[1]).intValue();
			} else if (args[1] instanceof Float) {
				y = ((Float) args[1]).floatValue();
			}
			
			//monome.handleADC(x, y);
		}
		*/
	}
}