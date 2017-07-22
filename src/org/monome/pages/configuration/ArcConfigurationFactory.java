package org.monome.pages.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.monome.pages.Main;
import org.monome.pages.gui.ArcFrame;
import org.monome.pages.gui.MonomeFrame;

public class ArcConfigurationFactory {

    public static synchronized ArcConfiguration getArcConfiguration(int index) {
        Configuration configuration = Main.main.configuration;
        if (configuration.getArcConfigurations() == null) {
            configuration.setArcConfigurations(new HashMap<Integer, ArcConfiguration>());
        }
        Iterator<Integer> it = configuration.getArcConfigurations().keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            ArcConfiguration arcConfig = configuration.getArcConfigurations().get(key);
            if (arcConfig.index == index) {
                return arcConfig;
            }
        }
        return null;
    }
    
    public static synchronized ArcConfiguration getArcConfiguration(String prefix) {
        Configuration configuration = Main.main.configuration;
        if (configuration.getArcConfigurations() == null) {
            configuration.setArcConfigurations(new HashMap<Integer, ArcConfiguration>());
        }
        Iterator<Integer> it = configuration.getArcConfigurations().keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            ArcConfiguration arcConfig = configuration.getArcConfigurations().get(key);
            if (arcConfig.prefix.compareTo(prefix) == 0) {
                return arcConfig;
            }
        }
        return null;
    }
    
    public static synchronized ArcConfiguration addArcConfiguration(int index, String prefix, String serial, int knobs, ArcFrame arcFrame, boolean useMIDIPageChanging, ArrayList<MIDIPageChangeRule> midiPageChangeRules) {
        Configuration configuration = Main.main.configuration;
        if (configuration.getArcConfigurations() == null) {
            configuration.setArcConfigurations(new HashMap<Integer, ArcConfiguration>());
        }
        ArcConfiguration arcConfiguration = new ArcConfiguration(index, prefix, serial, knobs, arcFrame, useMIDIPageChanging, midiPageChangeRules);
        configuration.getArcConfigurations().put(index, arcConfiguration);
        arcConfiguration.setFrameTitle();
        return arcConfiguration;
    }
    
    public static synchronized int getNumArcConfigurations() {
        Configuration configuration = Main.main.configuration;
        if (configuration.getArcConfigurations() == null) {
            configuration.setArcConfigurations(new HashMap<Integer, ArcConfiguration>());
        }
        return configuration.getArcConfigurations().size();
    }

    public static void removeArcConfiguration(int index) {
        Configuration configuration = Main.main.configuration;
        ArcConfiguration arcConfig = configuration.getArcConfigurations().get(new Integer(index));
        if (arcConfig != null && arcConfig.arcFrame != null) {
            arcConfig.arcFrame.dispose();
        }
        configuration.getArcConfigurations().remove(new Integer(index));
    }

    public static void removeArcConfigurations() {
        Main.main.configuration.setArcConfigurations(new HashMap<Integer, ArcConfiguration>());
    }

}
