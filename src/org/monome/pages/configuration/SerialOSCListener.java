package org.monome.pages.configuration;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.monome.pages.Main;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;
import org.monome.pages.configuration.SerialOSCArc;
import org.monome.pages.configuration.SerialOSCDevice;
import org.monome.pages.configuration.SerialOSCMonome;

import com.apple.dnssd.BrowseListener;
import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.ResolveListener;
import com.apple.dnssd.TXTRecord;

public class SerialOSCListener implements BrowseListener, ResolveListener {
	
	ArrayList<SerialOSCMonome> monomes;

	public void operationFailed(DNSSDService arg0, int arg1) {
		System.out.println("Operation failed: " + arg0 + " [" + arg1 + "]");
	}
	public void serviceLost(DNSSDService arg0, int arg1, int arg2, String arg3, String arg4, String arg5) {
		System.out.println("Service Lost: [" + arg3 + "] [" + arg4 + "] [" + arg5 + "]");
	}

	public void serviceFound(DNSSDService browser, int flags, int index, String serviceName, String regType, String domain) {
		try {
			DNSSDService service = DNSSD.resolve(0, DNSSD.ALL_INTERFACES, serviceName, regType, domain, this);
			Main.main.addService(service);
		} catch (DNSSDException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void serviceResolved(DNSSDService resolver, int flags, int ifIndex, String fullName, String hostName, int port, TXTRecord txtRecord) {
		String serial = fullName.substring(0, fullName.indexOf("._"));
		if (serial.indexOf("(") != -1) {
			serial = serial.substring(serial.indexOf("(")+1, serial.indexOf(")"));
		}
        SerialOSCDevice device = null;
        String deviceName = "unknown";
        System.out.println("resolved device: " + fullName);
        if (fullName.indexOf("monome\\032arc") != -1) {
            String knobs = fullName.substring(fullName.indexOf("arc") + 7, fullName.indexOf("arc") + 8);
            deviceName = "arc " + knobs;
            device = new SerialOSCArc();
            try {
                int iKnobs = Integer.parseInt(knobs);
                ((SerialOSCArc) device).setKnobs(iKnobs);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            if (fullName.indexOf("extapp") != -1) {
                return;
            }
            if (fullName.indexOf("monome\\032") != -1) {
                String monomeType = fullName.substring(fullName.indexOf("monome\\032") + 10, fullName.indexOf("\\032("));
                deviceName = "monome " + monomeType;
            }
            if (fullName.indexOf("mk\\032") != -1) {
                deviceName = "mk";
            }
            if (fullName.indexOf("arduinome") != -1) {
                deviceName = "arduinome";
            }
            if (deviceName.compareTo("unknown") != -1) {
                device = new SerialOSCMonome();
            }
        }
        if (device == null) {
        	System.out.println("Couldn't detect device with name: " + fullName);
            JOptionPane.showMessageDialog(Main.main.mainFrame, "Couldn't detect device with name: " + fullName,
                    "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        device.setPort(port);
        device.setHostName(hostName);
        device.setSerial(serial);
        device.setDeviceName(deviceName);
		if (Main.main.mainFrame.serialOscSetupFrame != null && Main.main.openingConfig == false) {
			Main.main.mainFrame.serialOscSetupFrame.addDevice(device);
		} else {
		    if (device instanceof SerialOSCMonome) {
    			MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration("/" + serial);
    			if (monomeConfig != null && (monomeConfig.serialOSCHostname == null || monomeConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
    				Main.main.startMonome((SerialOSCMonome) device);
    				monomeConfig.reload();
    			}
		    } else if (device instanceof SerialOSCArc) {
                ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + serial);
                if (arcConfig != null && (arcConfig.serialOSCHostname == null || arcConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
                    Main.main.startArc((SerialOSCArc) device);
                    arcConfig.reload();
                }
		    }
		}
	}
}
