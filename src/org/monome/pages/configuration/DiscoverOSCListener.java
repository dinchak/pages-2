package org.monome.pages.configuration;

import java.util.ArrayList;
import java.util.Date;

import org.monome.pages.Main;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

public class DiscoverOSCListener implements OSCListener {
	
	private boolean discoverMode = false;
	private int maxDevices = 10;
		
	public void setDiscoverMode(boolean newMode) {
		discoverMode = newMode;
	}

	public synchronized void acceptMessage(Date time, OSCMessage message) {
		
		Object[] args = message.getArguments();		
		if (discoverMode) {
			int index;
			if (!(args[0] instanceof Integer)) {
				return;
			}
			try {
				index = ((Integer) args[0]).intValue();
			} catch (NullPointerException e) {
				e.printStackTrace();
				return;
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				return;
			}
			
			if (index >= maxDevices) {
				return;
			}
			
			Configuration config = Main.main.configuration;
			
			if (message.getAddress().contains("/sys/type")) {
				if (args.length != 2) {
					return;
				}
				String type;
				try {
					type = (String) args[1];
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				int sizeX = 0;
				int sizeY = 0;
				if (type.contains("40h") || type.contains("64")) {
					sizeX = 8;
					sizeY = 8;
				} else if (type.contains("128")) {
					sizeX = 16;
					sizeY = 8;
				} else if (type.contains("256")) {
					sizeX = 16;
					sizeY = 16;
				}
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(index);
				if (monomeConfig != null) {
					monomeConfig.sizeX = sizeX;
					monomeConfig.sizeY = sizeY;
					monomeConfig.setFrameTitle();
				}
				return;
			}
			
			if (message.getAddress().contains("/sys/serial")) {
				if (args.length != 2) {
					return;
				}
				String serial;
				try {
					serial = (String) args[1];
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				
				if (serial.compareTo("/sys/serial") == 0) {
					return;
				}
				
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(index);
				if (monomeConfig != null) {
					monomeConfig.serial = serial;
					monomeConfig.setFrameTitle();				
				}
				return;
			}
			
			if (message.getAddress().contains("/sys/offset")) {
				if (args.length != 3) {
					return;
				}
				Integer X;
				Integer Y;
				try {
					 X = (Integer) args[1];
					 Y = (Integer) args[2];
				} catch (IndexOutOfBoundsException e) {
					return;
				}
								
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(index);
				if (monomeConfig != null) {
					monomeConfig.offsetX = X;
					monomeConfig.offsetY = Y;
				}
				return;
			}
			
			if (message.getAddress().contains("/sys/devices")) {
				if (args.length != 1) {
					return;
				}
				Integer numDevices;
				try {
					numDevices = (Integer) args[0];
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
					return;
				}
				this.maxDevices = numDevices;
				return;
			}
			
			if (message.getAddress().contains("/sys/prefix")) {
				if (args.length != 2) {
					return;
				}
				String prefix;
				try {
					prefix = (String) args[1];
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				
				if (prefix == null || prefix.compareTo("") == 0 || prefix.compareTo("/sys/prefix") == 0) {
					return;
				}
				
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(index);
				if (monomeConfig != null && monomeConfig.prefix.equals(prefix)) {
					return;
				}
				if (monomeConfig != null) {
					int newIndex = MonomeConfigurationFactory.getNumMonomeConfigurations();
					MonomeConfigurationFactory.moveIndex(index, newIndex);
				}
				ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
				config.addMonomeConfiguration(index, prefix, "", 0, 0, true, false, midiPageChangeRules);
			}
		}
	}
}