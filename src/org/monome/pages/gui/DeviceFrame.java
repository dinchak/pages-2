package org.monome.pages.gui;

import org.monome.pages.pages.BasePage;

public interface DeviceFrame {

    public void enableMidiMenu(boolean flag);

    public void updateShowPageMenuItems(String[] pageNames);

    public void clearPage();

    public void redrawPagePanel(BasePage page);

    public void updateMidiInSelectedItems(String[] midiInDevices);

    public void updateMidiOutSelectedItems(String[] midiOutDevices);

    public void updatePageChangeMidiInSelectedItems(String[] midiInDevices);

    public void updateMidiInMenuOptions(String[] midiInMenuOptions);

    public void updateMidiOutMenuOptions(String[] midiOutMenuOptions);

    public void setTitle(String title);

    public void dispose();
}
