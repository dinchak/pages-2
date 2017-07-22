package org.monome.pages.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.monome.pages.Main;
import org.monome.pages.pages.Page;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class SerialOSCMonome implements SerialOSCDevice, OSCListener {
	
    int port;
	String serial;
	String hostName;
	String deviceName;
    int sizeX;
    int sizeY;

	public void acceptMessage(Date time, OSCMessage message) {
		Object args[] = message.getArguments();
		
		/*
		System.out.println("received " + message.getAddress() + " msg for serial " + serial);
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof Integer) {
				int val = ((Integer) args[i]).intValue();
				System.out.println("val=" + val);
			}
			if (args[i] instanceof String) {
				String val = (String) args[i];
				System.out.println("val=" + val);
			}
		}
		*/
		
		if (message.getAddress().compareTo("/sys/size") == 0) {
			if (args.length == 2) {
				int x = ((Integer) args[0]).intValue();
				int y = ((Integer) args[1]).intValue();
								
				MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration("/" + serial);
				if (monomeConfig != null) {
					if (monomeConfig.serialOSCPort == 0) {
						monomeConfig.serialOSCPort = port;
						Main.main.configuration.initMonomeSerialOSC(monomeConfig);
						if (monomeConfig.curPage > -1) {
							Page page = monomeConfig.pages.get(monomeConfig.curPage);
							if (page != null) {
								page.redrawDevice();
							}
						}
					}
					OSCMessage prefixMsg = new OSCMessage();
					prefixMsg.setAddress("/sys/prefix");
					prefixMsg.addArgument("/" + serial);
					try {
						OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(hostName, port);
						outPort.send(prefixMsg);
						OSCMessage tiltMsg = new OSCMessage();
						tiltMsg.setAddress("/" + serial + "/tilt");
						tiltMsg.addArgument(new Integer(0));
						tiltMsg.addArgument(new Integer(1));
						outPort.send(tiltMsg);
						tiltMsg = new OSCMessage();
						tiltMsg.setAddress("/" + serial + "/tilt");
						tiltMsg.addArgument(new Integer(1));
						tiltMsg.addArgument(new Integer(1));
						outPort.send(tiltMsg);
						if ((x == 8 && y == 16) || (x == 16 && y == 8)) {
							OSCMessage rotationMsg = new OSCMessage("/sys/rotation");
							rotationMsg.addArgument(new Integer(0));
							outPort.send(rotationMsg);
							x = 16;
							y = 8;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				} else if (Main.main.openingConfig == true) {
					return;
				}
				
				OSCMessage prefixMsg = new OSCMessage();
				prefixMsg.setAddress("/sys/prefix");
				prefixMsg.addArgument("/" + serial);
				try {
					OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(hostName, port);
					outPort.send(prefixMsg);
					OSCMessage tiltMsg = new OSCMessage();
					tiltMsg.setAddress("/" + serial + "/tilt");
					tiltMsg.addArgument(new Integer(0));
					tiltMsg.addArgument(new Integer(1));
					outPort.send(tiltMsg);
					tiltMsg = new OSCMessage();
					tiltMsg.setAddress("/" + serial + "/tilt");
					tiltMsg.addArgument(new Integer(1));
					tiltMsg.addArgument(new Integer(1));
					outPort.send(tiltMsg);
					if ((x == 8 && y == 16) || (x == 16 && y == 8)) {
						OSCMessage rotationMsg = new OSCMessage("/sys/rotation");
						rotationMsg.addArgument(new Integer(0));
						outPort.send(rotationMsg);
						x = 16;
						y = 8;
					}
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
				ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
				System.out.println("creating " + x + "x" + y + " monome with prefix /" + serial);
				config.addMonomeConfigurationSerialOSC(MonomeConfigurationFactory.getNumMonomeConfigurations(), "/" + serial, serial, x, y, true, false, midiPageChangeRules, port, hostName);
			}
		}
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
}
