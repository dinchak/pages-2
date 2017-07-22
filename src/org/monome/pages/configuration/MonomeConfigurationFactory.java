package org.monome.pages.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.monome.pages.Main;
import org.monome.pages.gui.MonomeFrame;

public class MonomeConfigurationFactory {
			
	public static synchronized MonomeConfiguration getMonomeConfiguration(int index) {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		Iterator<Integer> it = configuration.getMonomeConfigurations().keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MonomeConfiguration monomeConfig = configuration.getMonomeConfigurations().get(key);
			if (monomeConfig.index == index) {
				return monomeConfig;
			}
		}
		return null;
	}
	
	public static synchronized MonomeConfiguration getMonomeConfiguration(String prefix) {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		Iterator<Integer> it = configuration.getMonomeConfigurations().keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MonomeConfiguration monomeConfig = configuration.getMonomeConfigurations().get(key);
			if (monomeConfig.prefix.compareTo(prefix) == 0) {
				return monomeConfig;
			}
		}
		return null;
	}
	
	public static synchronized MonomeConfiguration addMonomeConfiguration(int index, String prefix, String serial, int sizeX, int sizeY, boolean usePageChangeButton, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules, MonomeFrame monomeFrame) {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		MonomeConfiguration monomeConfiguration = new MonomeConfiguration(index, prefix, serial, sizeX, sizeY, usePageChangeButton, useMIDIPageChanging, midiPageChangeRules, monomeFrame);
		configuration.getMonomeConfigurations().put(index, monomeConfiguration);
		monomeConfiguration.setFrameTitle();
		return monomeConfiguration;
	}
	
	public static synchronized MonomeConfiguration addFakeMonomeConfiguration(int index, String prefix, String serial, int sizeX, int sizeY, boolean usePageChangeButton, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules, MonomeFrame monomeFrame, QuadrantConfiguration quadConf, int pageIndex, MonomeConfiguration parent, int quadNum) {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		Integer i = new Integer(configuration.getMonomeConfigurations().size());		
		FakeMonomeConfiguration monomeConfiguration = new FakeMonomeConfiguration(index, prefix, serial, sizeX, sizeY, usePageChangeButton, useMIDIPageChanging, midiPageChangeRules, monomeFrame, quadConf, pageIndex, parent, quadNum);
		configuration.getMonomeConfigurations().put(i, monomeConfiguration);
		monomeConfiguration.setFrameTitle();
		return monomeConfiguration;
	}
	
	public static synchronized void moveIndex(int oldIndex, int newIndex) {
		Configuration configuration = Main.main.configuration;
		configuration.getMonomeConfigurations().put(new Integer(newIndex), configuration.getMonomeConfigurations().get(oldIndex));
		configuration.getMonomeConfigurations().remove(new Integer(oldIndex));
	}
	
	public static synchronized int getNumMonomeConfigurations() {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		return configuration.getMonomeConfigurations().size();
	}
	
	public static synchronized void removeMonomeConfigurations() {
		Configuration configuration = Main.main.configuration;
		configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
	}
	
	public static synchronized void removeMonomeConfiguration(int index) {
		Configuration configuration = Main.main.configuration;
		MonomeConfiguration monomeConfig = configuration.getMonomeConfigurations().get(new Integer(index));

        if (monomeConfig != null) {
            monomeConfig.dispose();
        }

		configuration.getMonomeConfigurations().remove(new Integer(index));
	}

	public static synchronized boolean prefixExists(String prefix) {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			configuration.setMonomeConfigurations(new HashMap<Integer, MonomeConfiguration>());
		}
		Iterator<Integer> it = configuration.getMonomeConfigurations().keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MonomeConfiguration monomeConfig = configuration.getMonomeConfigurations().get(key);
			if (monomeConfig.prefix.compareTo(prefix) == 0) {
				return true;
			}
		}
		return false;
	}

	public static void combineMonomeConfigurations() {
		Configuration configuration = Main.main.configuration;
		if (configuration.getMonomeConfigurations() == null) {
			return;
		}
		@SuppressWarnings("unchecked")
        HashMap<Integer, MonomeConfiguration> tmpMonomeConfigurations = (HashMap<Integer, MonomeConfiguration>) configuration.getMonomeConfigurations().clone();
		Iterator<Integer> it = tmpMonomeConfigurations.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			MonomeConfiguration mainMonomeConfig = configuration.getMonomeConfigurations().get(key);
			if (mainMonomeConfig == null) {
				continue;
			}
			Iterator<Integer> it2 = tmpMonomeConfigurations.keySet().iterator();
			while (it2.hasNext()) {
				Integer key2 = it2.next();
				MonomeConfiguration checkMonomeConfig = configuration.getMonomeConfigurations().get(key2);
				if (checkMonomeConfig == null) {
					continue;
				}
				if (checkMonomeConfig.prefix.compareTo(mainMonomeConfig.prefix) == 0 && checkMonomeConfig.index != mainMonomeConfig.index) {
					mainMonomeConfig.sizeX += checkMonomeConfig.offsetX;
					mainMonomeConfig.sizeY += checkMonomeConfig.offsetY;
					mainMonomeConfig.setFrameTitle();
					removeMonomeConfiguration(key2);
				}
			}
		}
	}
}
