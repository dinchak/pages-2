package org.monome.pages.pages;

import java.awt.Dimension;

import javax.sound.midi.MidiMessage;
import javax.swing.JPanel;

import org.w3c.dom.Element;

/**
 * The ArcPage interface.  All pages in the application must implement this interface.
 * 
 * @author Tom Dinchak
 *
 */
public interface ArcPage extends BasePage {
    /**
     * Called whenever a delta event is received on the arc this page belongs to.
     * 
     * @param enc The encoder that was moved
     * @param delta The delta of the movement
     */
    public void handleDelta(int enc, int delta);
    
    /**
     * Called when a key event is received.
     */
    public void handleKey(int enc, int value);

    /**
     * Called whenever a MIDI clock tick message is received from the selected MIDI input deviec.
     */
    public void handleTick();

    /**
     * Called whenever a MIDI message is received from the MIDI input device
     * 
     * @param message The MIDI message received
     * @param timeStamp The timestamp that the message was received at
     */
    public void send(MidiMessage message, long timeStamp);

    /**
     * Called whenever a MIDI clock reset message is received on the selected MIDI input device.
     */
    public void handleReset();

    /**
     * Called when a save configuration action is requested.
     * 
     * @return XML representation of the page's current configuration.
     */
    public String toXml();
}
