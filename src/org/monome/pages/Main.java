package org.monome.pages;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PropertyConfigurator;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.Configuration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.MonomeConfigurationFactory;
import org.monome.pages.configuration.OSCPortFactory;
import org.monome.pages.configuration.SerialOSCArc;
import org.monome.pages.configuration.SerialOSCDevice;
import org.monome.pages.configuration.SerialOSCMonome;
import org.monome.pages.gui.MainGUI;
import org.monome.pages.configuration.SerialOSCListener;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class Main {

    public static final int LIBRARY_APPLE = 0;
    public static final int LIBRARY_JMDNS = 1;
    public static Logger logger = Logger.getLogger("socketLogger");

    public static Main main;

    public Configuration configuration;
    public int zeroconfLibrary = LIBRARY_APPLE;
    File configurationFile = null;
    public boolean openingConfig = false;
    private SerialOSCListener serialOSCListener = new SerialOSCListener();
    public boolean sentSerialOSCInfoMsg;
    public JmDNS jmdns;
    public ArrayList<DNSSDRegistration> dnssdRegistrations = new ArrayList<DNSSDRegistration>();
    public ArrayList<DNSSDService> dnssdServices = new ArrayList<DNSSDService>();    
    public MainGUI mainFrame = null;

    public static void main(final String[] args) {
/*
        File logConfigFile = new File("log4j.properties");
        if (logConfigFile.exists() && logConfigFile.canRead()) {
            PropertyConfigurator.configure("log4j.properties");
            StdOutErrLog.tieSystemOutAndErrToLog();
        }
*/
        File file = null;
        if (args.length > 0) {
            file = new File(args[0]);
        }
        logger.error("Pages 0.2a52 starting up\n");
        main = new Main(file);
    }
    
    public Main(final File file) {
        try {
            jmdns = JmDNS.create();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                mainFrame = new MainGUI();
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.setVisible(true);
                if (file != null && file.canRead()) {
                    mainFrame.actionOpenOld(file);
                }
            }
        });
    }
    
    public void jmdnsSerialOSCDiscovery() {
        HashMap<String, String> serials = new HashMap<String, String>();
        final ServiceInfo[] svcInfos = jmdns.list("_monome-osc._udp.local.");
        for (int i = 0; i < svcInfos.length; i++) {
            String serial = "unknown";
            String fullName = svcInfos[i].getName();
            if (fullName.indexOf("(") != -1) {
                serial = fullName.substring(fullName.indexOf("(")+1, fullName.indexOf(")"));
            }
            SerialOSCDevice device = null;
            String deviceName = "unknown";
            if (fullName.indexOf("monome arc") != -1) {
                String knobs = fullName.substring(fullName.indexOf(" arc ") + 5, fullName.indexOf(" arc ") + 6);
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
                    continue;
                }
                if (fullName.indexOf("monome") != -1) {
                    String monomeType = fullName.substring(fullName.indexOf("monome ") + 7, fullName.indexOf(" ("));
                    deviceName = "monome " + monomeType;
                }
                if (fullName.indexOf("mk") != -1) {
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
            int port = svcInfos[i].getPort();
            String hostName = "127.0.0.1";
            device.setPort(port);
            device.setHostName(hostName);
            device.setSerial(serial);
            device.setDeviceName(deviceName);

            if (serials.containsKey(serial)) {
                continue;
            }
            serials.put(device.getSerial(), device.getHostName());
            if (mainFrame.serialOscSetupFrame != null) {
                mainFrame.serialOscSetupFrame.addDevice(device);
            } else {
                if (device instanceof SerialOSCMonome) {
                    MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration("/" + serial);
                    if (monomeConfig != null && (monomeConfig.serialOSCHostname == null || monomeConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
                        startMonome((SerialOSCMonome) device);
                    }
                } else if (device instanceof SerialOSCArc) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + serial);
                    if (arcConfig != null && (arcConfig.serialOSCHostname == null || arcConfig.serialOSCHostname.equalsIgnoreCase(device.getHostName()))) {
                        startArc((SerialOSCArc) device);
                    }
                }
            }
        }
    }
    
    public void appleSerialOSCDiscovery() {
        /*
            if (serial.indexOf("(") != -1) {
                serial = serial.substring(serial.indexOf("(")+1, serial.indexOf(")"));
            }
            SerialOSCMonome monome = new SerialOSCMonome();
            monome.port = port;
            monome.serial = serial;
            monome.hostName = hostName;
            
            if (Main.mainFrame.serialOscSetupFrame != null) {
                Main.mainFrame.serialOscSetupFrame.addDevice(monome);
            } else {
                MonomeConfiguration monomeConfig = MonomeConfigurationFactory.getMonomeConfiguration("/" + serial);
                if (monomeConfig != null && (monomeConfig.serialOSCHostname == null || monomeConfig.serialOSCHostname.equalsIgnoreCase(monome.hostName))) {
                    Main.mainFrame.startMonome(monome);
                }
            }            
            */
        
        try {
            DNSSDService service = DNSSD.browse("_monome-osc._udp", serialOSCListener);
            addService(service);
        } catch (DNSSDException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class StdOutErrLog {

        public static void tieSystemOutAndErrToLog() {
            System.setOut(createLoggingProxy(System.out));
            System.setErr(createLoggingProxy(System.err));
        }

        public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
            return new PrintStream(realPrintStream) {
                public void print(final String string) {
                    try {
                        if (System.getProperty("user.name") != null) {
                            MDC.put("username", System.getProperty("user.name"));
                        }
                        if (System.getProperty("os.name") != null) {
                            MDC.put("osname", System.getProperty("os.name"));
                        }
                        if (System.getProperty("os.version") != null) {
                            MDC.put("osversion", System.getProperty("os.version"));
                        }
                        if (System.getProperty("user.country") != null) {
                            MDC.put("region", System.getProperty("user.country"));
                        }
                        logger.error(string);
                        MDC.remove("username");
                        MDC.remove("osname");
                        MDC.remove("osversion");
                        MDC.remove("region");
                    } catch (Exception e) {
                        e.printStackTrace(realPrintStream);
                    }
                }
            };
        }
    }
    
    public void startMonome(SerialOSCMonome monome) {
        if (configuration == null) {
            return;
        }
        OSCPortIn inPort = OSCPortFactory.getInstance().getOSCPortIn(configuration.oscListenPort);
        if (inPort == null) {
            JOptionPane.showMessageDialog(MainGUI.getDesktopPane(), "Unable to bind to port " + configuration.oscListenPort + ".  Try closing any other programs that might be listening on it.", "OSC Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inPort.addListener("/sys/size", monome);
        inPort.addListener("/sys/port", monome);
        inPort.addListener("/sys/id", monome);
        inPort.addListener("/sys/prefix", monome);
        inPort.addListener("/sys/host", monome);
        OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(monome.getHostName(), monome.getPort());
        OSCMessage infoMsg = new OSCMessage();
        infoMsg.setAddress("/sys/info");
        infoMsg.addArgument("127.0.0.1");
        infoMsg.addArgument(new Integer(configuration.oscListenPort));
        try {
            outPort.send(infoMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void startArc(SerialOSCArc arc) {
        if (configuration == null) {
            return;
        }
        OSCPortIn inPort = OSCPortFactory.getInstance().getOSCPortIn(configuration.oscListenPort);
        if (inPort == null) {
            JOptionPane.showMessageDialog(MainGUI.getDesktopPane(), "Unable to bind to port " + configuration.oscListenPort + ".  Try closing any other programs that might be listening on it.", "OSC Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        OSCPortOut outPort = OSCPortFactory.getInstance().getOSCPortOut(arc.getHostName(), arc.getPort());
        OSCMessage infoMsg = new OSCMessage();
        inPort.addListener("/sys/port", arc);
        inPort.addListener("/sys/prefix", arc);
        infoMsg.setAddress("/sys/info");
        infoMsg.addArgument("127.0.0.1");
        infoMsg.addArgument(new Integer(configuration.oscListenPort));
        try {
            outPort.send(infoMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the current open configuration file.  This file is used when File -> Save is clicked.
     * 
     * @return the current open configuration file
     */
    public File getConfigurationFile() {
        return configurationFile;
    }
    
    /**
     * Sets the current open configuration file.  This file is used when File -> Save is clicked.
     * 
     * @param cf the file to set to the current open configuration file.
     */
    public void setConfigurationFile(File cf) {
        configurationFile = cf;
    }

    public void addRegistration(DNSSDRegistration reg) {
        dnssdRegistrations.add(reg);
    }
    
    public void addService(DNSSDService service) {
        dnssdServices.add(service);
    }
    
    public void removeRegistrations() {
        for (DNSSDRegistration reg : dnssdRegistrations) {
            reg.stop();
        }
        for (DNSSDService service : dnssdServices) {
            service.stop();
        }
    }
}
