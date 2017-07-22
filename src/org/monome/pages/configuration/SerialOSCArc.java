package org.monome.pages.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.monome.pages.Main;
import org.monome.pages.pages.Page;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class SerialOSCArc implements SerialOSCDevice, OSCListener {
    
    int port;
    String serial;
    String hostName;
    String deviceName;
    int knobs;

    public void startArc() {
        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + serial);
        if (arcConfig != null) {
            if (arcConfig.serialOSCPort == 0) {
                arcConfig.serialOSCPort = port;
            }
            OSCMessage prefixMsg = new OSCMessage();
            prefixMsg.setAddress("/sys/prefix");
            prefixMsg.addArgument("/" + serial);
            try {
                OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(hostName, port);
                outPort.send(prefixMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        //else if (Main.main.openingConfig == true) {
        //    return;
        //}
        
        OSCMessage prefixMsg = new OSCMessage();
        prefixMsg.setAddress("/sys/prefix");
        prefixMsg.addArgument("/" + serial);
        try {
            OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(hostName, port);
            outPort.send(prefixMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Configuration config = Main.main.configuration;
        if (config == null) {
            Main.main.mainFrame.getConfigurationMenu().setEnabled(true);
            Main.main.mainFrame.getMidiMenu().setEnabled(true);
            Main.main.mainFrame.getFrame().setTitle("Pages");
            Main.main.configuration = new Configuration("");
            Main.main.configuration.initAbleton();
            config = Main.main.configuration;
        }
        config.addArcConfigurationSerialOSC(ArcConfigurationFactory.getNumArcConfigurations(), "/" + serial, serial, knobs, port, hostName, false);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostname) {
        this.hostName = hostname;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public int getKnobs() {
        return knobs;
    }
    
    public void setKnobs(int knobs) {
        this.knobs = knobs;
    }

    public void acceptMessage(Date time, OSCMessage message) {
        if (message.getAddress().compareToIgnoreCase("/sys/port") == 0) {
            ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + serial);
            if (arcConfig == null) {
                arcConfig = Main.main.configuration.addArcConfigurationSerialOSC(ArcConfigurationFactory.getNumArcConfigurations(), "/" + serial, serial, knobs, port, hostName, false);
            }
            if (arcConfig.serialOSCPort == 0) {
                arcConfig.serialOSCPort = port;
                Main.main.configuration.initArcSerialOSC(arcConfig);
            }
        }
    }
}
