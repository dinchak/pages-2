package org.monome.pages.midi;

import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.monome.pages.Main;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;

public class MidiDeviceFactory {

    /**
     * The selected MIDI input device to receive MIDI messages from.
     */
    public static ArrayList<MidiDevice> midiInDevices = new ArrayList<MidiDevice>();

    /**
     * midiInDevice's associated Transmitter object. 
     */
    private static ArrayList<Transmitter> midiInTransmitters = new ArrayList<Transmitter>();
    
    /**
     * midiInDevice's associated MIDIINReceiver object.
     */
    private static ArrayList<MIDIInReceiver> midiInReceivers = new ArrayList<MIDIInReceiver>();

    /**
     * The selected MIDI output devices.
     */
    public static ArrayList<MidiDevice> midiOutDevices = new ArrayList<MidiDevice>();

    /**
     * midiOutDevices' associated Receiver objects.
     */
    private static ArrayList<Receiver> midiOutReceivers = new ArrayList<Receiver>();

    /**
     * Returns a MIDI Transmitter object for the corresponding MIDI device name.
     * 
     * @param midiDeviceName the name of the MIDI device to get the Transmitter object for
     * @return the Transmitter object associated with the MIDI device named midiDeviceName
     */
    public static Transmitter getMIDITransmitterByName(String midiDeviceName) {
        for (int i=0; i < midiInDevices.size(); i++) {
            if (midiInDevices.get(i).getDeviceInfo().toString().compareTo(midiDeviceName) == 0) {
                Transmitter transmitter = midiInTransmitters.get(i);
                return transmitter;
            }
        }
        return null;        
    }
    
    /**
     * Returns a MIDI Receiver object for the corresponding MIDI device name.
     * 
     * @param midiDeviceName the name of the MIDI device to get the Receiver object for
     * @return the Receiver object associated with the MIDI device named midiDeviceName
     */
    public static Receiver getMIDIReceiverByName(String midiDeviceName) {
        for (int i=0; i < midiOutDevices.size(); i++) {
            if (midiOutDevices.get(i).getDeviceInfo().toString().compareTo(midiDeviceName) == 0) {
                Receiver receiver = midiOutReceivers.get(i);
                return receiver;
            }
        }
        return null;        
    }
    
    /**
     * @return The MIDI outputs that have been enabled in the main configuration.
     */
    public static String[] getMidiOutOptions() {
        String[] midiOutOptions = new String[midiOutDevices.size()];
        for (int i=0; i < midiOutDevices.size(); i++) {
            midiOutOptions[i] = midiOutDevices.get(i).getDeviceInfo().toString();
        }
        return midiOutOptions;
    }
    
    /**
     * @return The MIDI outputs that have been enabled in the main configuration.
     */
    public static String[] getMidiInOptions() {
        String[] midiOutOptions = new String[midiInDevices.size()];
        for (int i=0; i < midiInDevices.size(); i++) {
            midiOutOptions[i] = midiInDevices.get(i).getDeviceInfo().toString();
        }
        return midiOutOptions;
    }
        
    /**
     * Closes all selected MIDI devices.
     */
    public static void closeMidiDevices() {
        for (int i=0; i < midiInTransmitters.size(); i++) {
        	if (midiInTransmitters.get(i) == null) {
        		continue;
        	}
            midiInTransmitters.get(i).close();
        }
        
        for (int i=0; i < midiInDevices.size(); i++) {
        	if (midiInDevices.get(i) == null) {
        		continue;
        	}
            midiInDevices.get(i).close();
        }

        for (int i=0; i < midiOutDevices.size(); i++) {
        	if (midiOutDevices.get(i) == null) {
        		continue;
        	}
            midiOutDevices.get(i).close();
        }
    }

    /**
     * Called when a MIDI output device is selected or de-selected from the MIDI menu
     * 
     * @param midiOutDevice The MIDI output device to select or de-select
     */
    public static void toggleMidiOutDevice(MidiDevice midiOutDevice) {
        // check if the device is already enabled, if so disable it
        for (int i=0; i < midiOutDevices.size(); i++) {
            if (midiOutDevices.get(i).getDeviceInfo().getName().equals(midiOutDevice.getDeviceInfo().getName())) {
                MidiDevice outDevice = midiOutDevices.get(i);
                midiOutReceivers.remove(i);
                midiOutDevices.remove(i);
                outDevice.close();
                for (int j = 0; j < MonomeConfigurationFactory.getNumMonomeConfigurations(); j++) {
                    MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(j);
                    if (monomeConfig != null && monomeConfig.deviceFrame != null) {
                        monomeConfig.deviceFrame.updateMidiOutMenuOptions(getMidiOutOptions());
                    }
                }
                for (int j = 0; j < ArcConfigurationFactory.getNumArcConfigurations(); j++) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(j);
                    if (arcConfig != null && arcConfig.deviceFrame != null) {
                        arcConfig.deviceFrame.updateMidiOutMenuOptions(getMidiOutOptions());
                    }
                }
                Main.main.mainFrame.enableMidiOutOption(midiOutDevice.getDeviceInfo().getName(), false);
                return;
            }
        }

        // try to enable the device
        try {
            midiOutDevice.open();
            Receiver recv = midiOutDevice.getReceiver();
            midiOutDevices.add(midiOutDevice);
            midiOutReceivers.add(recv);
            for (int j = 0; j < MonomeConfigurationFactory.getNumMonomeConfigurations(); j++) {
                MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(j);
                if (monomeConfig != null && monomeConfig.deviceFrame != null) {
                    MonomeConfigurationFactory.getMonomeConfiguration(j).deviceFrame.updateMidiOutMenuOptions(getMidiOutOptions());
                }
            }
            for (int j = 0; j < ArcConfigurationFactory.getNumArcConfigurations(); j++) {
                ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(j);
                if (arcConfig != null && arcConfig.deviceFrame != null) {
                    arcConfig.deviceFrame.updateMidiOutMenuOptions(getMidiOutOptions());
                }
            }
            Main.main.mainFrame.enableMidiOutOption(midiOutDevice.getDeviceInfo().getName(), true);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enables a MIDI in device to receive MIDI clock.
     * 
     * @param midiInDevice The MIDI input device to enable
     */
    public static void toggleMidiInDevice(MidiDevice midiInDevice) {
        // close the currently open device if we have one
        for (int i=0; i < midiInDevices.size(); i++) {
            if (midiInDevices.get(i).getDeviceInfo().getName().equals(midiInDevice.getDeviceInfo().getName())) {
                MidiDevice inDevice = midiInDevices.get(i);
                Transmitter transmitter = midiInTransmitters.get(i);
                midiInTransmitters.remove(i);
                midiInDevices.remove(i);
                midiInReceivers.remove(i);
                transmitter.close();
                inDevice.close();
                for (int j = 0; j < MonomeConfigurationFactory.getNumMonomeConfigurations(); j++) {
                    MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(j);
                    if (monomeConfig != null && monomeConfig.deviceFrame != null) {
                        monomeConfig.deviceFrame.updateMidiInMenuOptions(getMidiInOptions());
                    }
                }
                for (int j = 0; j < ArcConfigurationFactory.getNumArcConfigurations(); j++) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(j);
                    if (arcConfig != null && arcConfig.deviceFrame != null) {
                        arcConfig.deviceFrame.updateMidiInMenuOptions(getMidiInOptions());
                    }
                }
                Main.main.mainFrame.enableMidiInOption(midiInDevice.getDeviceInfo().getName(), false);
                return;
            }
        }

        // try to open the new midi in device
        try {
            midiInDevice.open();
            Transmitter transmitter = midiInDevice.getTransmitter();
            MIDIInReceiver receiver = new MIDIInReceiver(midiInDevice);
            transmitter.setReceiver(receiver);
            midiInDevices.add(midiInDevice);
            midiInTransmitters.add(transmitter);
            midiInReceivers.add(receiver);
            for (int j = 0; j < MonomeConfigurationFactory.getNumMonomeConfigurations(); j++) {
                MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration(j);
                if (monomeConfig != null && monomeConfig.deviceFrame != null) {
                    monomeConfig.deviceFrame.updateMidiInMenuOptions(getMidiInOptions());
                }
            }
            for (int j = 0; j < ArcConfigurationFactory.getNumArcConfigurations(); j++) {
                ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(j);
                if (arcConfig != null && arcConfig.deviceFrame != null) {
                    arcConfig.deviceFrame.updateMidiInMenuOptions(getMidiInOptions());
                }
            }
            Main.main.mainFrame.enableMidiInOption(midiInDevice.getDeviceInfo().getName(), true);
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    public static MidiDevice getDevice(Receiver recv) {
        for (int i = 0; i < midiOutReceivers.size(); i++) {
            if (midiOutReceivers.get(i).equals(recv)) {
                return midiOutDevices.get(i);
            }
        }
        return null;
    }
    
    public static MidiDevice getDevice(Transmitter xmitter) {
        for (int i = 0; i < midiInTransmitters.size(); i++) {
            if (midiInTransmitters.get(i).equals(xmitter)) {
                return midiInDevices.get(i);
            }
        }
        return null;
    }
}