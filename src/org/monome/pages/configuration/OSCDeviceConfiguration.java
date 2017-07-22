package org.monome.pages.configuration;

import org.monome.pages.pages.BasePage;

public abstract class OSCDeviceConfiguration<TPage extends BasePage> extends DeviceConfiguration<TPage> {

    /**
     * The monome's prefix (ie. "/40h")
     */
    public String prefix;
    /**
     * The monome's serial number (ie. m40h0146)
     */
    public String serial;
    /**
     * The monome's index in MonomeSerial
     */
    public int index;


    public OSCDeviceConfiguration(int index, String prefix, String serial) {
        this.index = index;
        this.prefix = prefix;
        this.serial = serial;
    }

    public OSCDeviceConfiguration() {
    }
}
