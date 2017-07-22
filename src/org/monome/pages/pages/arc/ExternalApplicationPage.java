package org.monome.pages.pages.arc;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.jmdns.ServiceInfo;
import javax.sound.midi.MidiMessage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.OSCPortFactory;
import org.monome.pages.gui.MainGUI;
import org.monome.pages.pages.ArcPage;
import org.monome.pages.pages.arc.gui.ExternalApplicationGUI;
import org.w3c.dom.Element;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

/**
 * The External Application page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/ExternalApplicationPage
 * 
 * @author Tom Dinchak, Stephen McLeod
 *
 */
public class ExternalApplicationPage implements ArcPage, OSCListener, RegisterListener, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration this page belongs to
	 */
	private ArcConfiguration arc;

	/**
	 * This page's index (page number) 
	 */
	private int index;

	/**
	 * The OSC prefix the external application uses
	 */
	private String prefix = "/tml";

	/**
	 * The hostname that the external application is bound to 
	 */
	private String hostname = "localhost";

	/**
	 * The OSC input port number to receive messages from the external application 
	 */
	public int inPort = 8080;

	/**
	 * The OSCPortIn object for communication with the external application
	 */
	private OSCPortIn oscIn;

	/**
	 * The OSC output port number to send messages to the external application
	 */
	public int outPort = 8000;

	/**
	 * The OSCPortOut object for communication with the external application 
	 */
	private OSCPortOut oscOut;
	
	/**
	 * The name of the page 
	 */
	private String pageName = "External Application";
	
	private ExternalApplicationGUI gui;
	
	private HashMap<String, Integer> listenersAdded;

    private Dimension origGuiDimension;
		
	/**
	 * @param monome The MonomeConfiguration object this page belongs to
	 * @param index The index of this page (page number)
	 */
	public ExternalApplicationPage(ArcConfiguration arc, int index) {
        inPort = (int) (1024 + (Math.random() * 65411)); 
		this.arc = arc;
		this.index = index;
		listenersAdded = new HashMap<String, Integer>();
		gui = new ExternalApplicationGUI(this);
        origGuiDimension = gui.getSize();
    }

    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }

	/**
	 * Stops OSC communication with the external application
	 */
	public void stopOSC() {
		if (this.oscIn != null) {
			this.oscIn.removeListener("/sys/prefix");
			this.oscIn.removeListener("/sys/info");
			Set<String> keys = listenersAdded.keySet();
			Object[] keysArray = keys.toArray();
			for (int i = 0; i < keysArray.length; i++) {
				String prefix = (String) keysArray[i];
				this.oscIn.removeListener(prefix + "/ring/set");
				this.oscIn.removeListener(prefix + "/ring/all");
				this.oscIn.removeListener(prefix + "/ring/map");
				this.oscIn.removeListener(prefix + "/ring/range");
				listenersAdded.remove(prefix);
			}
			OSCPortFactory.getInstance().destroyOSCPortIn(this.inPort);
			this.oscIn = null;
		}
	}

	/**
	 * Initializes OSC communication with the external application
	 */
	public void initOSC() {
		if (this.oscIn == null) {
			
			this.oscIn = OSCPortFactory.getInstance().getOSCPortIn(Integer.valueOf(this.inPort));
			if (this.oscIn == null) {
				JOptionPane.showMessageDialog(MainGUI.getDesktopPane(), "External Application Page was unable to bind to port " + this.inPort + ".  Try closing any other programs that might be listening on it.", "OSC Error", JOptionPane.ERROR_MESSAGE);
				this.oscIn = null;
				return;
			}
			
			addListeners();
			if (Main.main.zeroconfLibrary == Main.LIBRARY_APPLE) {			
    			try {
    				DNSSDRegistration reg = DNSSD.register("arc-extapp-" + this.inPort + "-" + arc.serial, "_monome-osc._udp", this.inPort, this);
    				Main.main.addRegistration(reg);
    			} catch (DNSSDException e) {
    				e.printStackTrace();
    			}
			} else if (Main.main.zeroconfLibrary == Main.LIBRARY_JMDNS) {
    			ServiceInfo info = ServiceInfo.create("_monome-osc._udp.local.", "arc-extapp-" + this.inPort + "-" + arc.serial, this.inPort, "extapp-" + this.inPort + "-" + arc.serial);
    			
    			try {
                    Main.main.jmdns.registerService(info);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
			}
			this.oscOut = OSCPortFactory.getInstance().getOSCPortOut(this.hostname, Integer.valueOf(this.outPort));

		}
		
	}
	
	public void addListeners() {
		if (listenersAdded.containsKey(this.prefix + " " + index)) {
			return;
		}
		this.oscIn.addListener("/sys/prefix", this);
		this.oscIn.addListener(this.prefix + "/ring/set", this);
		this.oscIn.addListener(this.prefix + "/ring/all", this);
		this.oscIn.addListener(this.prefix + "/ring/map", this);
		this.oscIn.addListener(this.prefix + "/ring/range", this);
		this.oscIn.addListener("/sys/port", this);
		this.oscIn.addListener("/sys/info", this);		
		listenersAdded.put(this.prefix + " " + index, 1);
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */
	public String getName() {		
		return pageName;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick()
	 */
	public void handleTick() {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawDevice()
	 */
	public void redrawDevice() {
		// redrawDevice the arc from the pageState, this is updated when the page isn't selected
	    for (int enc = 0; enc < arc.knobs; enc++) {
	        Integer[] levels = new Integer[64];
	        for (int led = 0; led < 64; led++) {
	            levels[led] = arc.pageState[index][enc][led];
	        }
	        arc.map(enc, levels, index);
	    }
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String disableCache = "false";
		if (gui.getDisableLedCacheCB().isSelected()) {
			disableCache = "true";
		}
		String ignorePrefix = "false";
		if (gui.getIgnorePrefixCB().isSelected()) {
			ignorePrefix = "true";
		}

		String xml = "";
		xml += "      <name>External Application</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <prefix>" + this.prefix + "</prefix>\n";
		xml += "      <oscinport>" + this.inPort + "</oscinport>\n";
		xml += "      <oscoutport>" + this.outPort + "</oscoutport>\n";
		xml += "      <hostname>" + this.hostname + "</hostname>\n";
		xml += "      <disablecache>" + disableCache + "</disablecache>\n";
		xml += "      <ignoreprefix>" + ignorePrefix + "</ignoreprefix>\n";

		return xml;
	}

	/* (non-Javadoc)
	 * @see com.illposed.osc.OSCListener#acceptMessage(java.util.Date, com.illposed.osc.OSCMessage)
	 */
	public void acceptMessage(Date arg0, OSCMessage msg) {
		Object[] args = msg.getArguments();
		if (msg.getAddress().compareTo("/sys/info") == 0) {
			try {
				OSCMessage outmsg = new OSCMessage();
				outmsg.setAddress("/sys/port");
				outmsg.addArgument(outPort);
				oscOut.send(outmsg);
				outmsg = new OSCMessage();
				outmsg.setAddress("/sys/prefix");
				outmsg.addArgument(this.prefix);
				oscOut.send(outmsg);
				outmsg = new OSCMessage();
				outmsg.setAddress("/sys/id");
				outmsg.addArgument("extpp");
				oscOut.send(outmsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
		if (msg.getAddress().compareTo("/sys/port") == 0) {
            if (gui.getIgnorePrefixCB().isSelected()) {
                return;
            }
			int port = ((Integer) args[0]).intValue();
			setOutPort("" + port);
			this.oscOut = OSCPortFactory.getInstance().getOSCPortOut(this.hostname, Integer.valueOf(this.outPort));
		}
		
		if (msg.getAddress().compareTo("/sys/prefix") == 0) {
			if (gui.getIgnorePrefixCB().isSelected()) {
				return;
			}
			if (args.length == 1) {
				this.setPrefix((String) args[0]);
			} else if (args.length == 2) {
				this.setPrefix((String) args[1]);
			}
			addListeners();
		}
		
		// only process messages from the external application
		if (!msg.getAddress().contains(this.prefix)) {
			return;
		}
		// handle an all request from the external application
		if (msg.getAddress().contains("/ring/all")) {
			int val = 0;
			int enc = 0;
			if (args.length > 0) {
				if (args[0] instanceof Integer) {
					enc = ((Integer) args[0]).intValue();
				}
				if (args[0] instanceof Float) {
					enc = ((Float) args[0]).intValue();
				}
                if (args[1] instanceof Integer) {
                    val = ((Integer) args[1]).intValue();
                }
                if (args[1] instanceof Float) {
                    val = ((Float) args[1]).intValue();
                }
			}
			arc.all(enc, val, index);
		}
        if (msg.getAddress().contains("/ring/set")) {
            int enc = 0;
            int led = 0;
            int level = 0;
            if (args.length > 0) {
                if (args[0] instanceof Integer) {
                    enc = ((Integer) args[0]).intValue();
                }
                if (args[0] instanceof Float) {
                    enc = ((Float) args[0]).intValue();
                }
                if (args[1] instanceof Integer) {
                    led = ((Integer) args[1]).intValue();
                }
                if (args[1] instanceof Float) {
                    led = ((Float) args[1]).intValue();
                }
                if (args[2] instanceof Integer) {
                    level = ((Integer) args[2]).intValue();
                }
                if (args[2] instanceof Float) {
                    level = ((Float) args[2]).intValue();
                }
            }
            arc.set(enc, led, level, index);
        }
        if (msg.getAddress().contains("/ring/range")) {
            int enc = 0;
            int x1 = 0;
            int x2 = 0;
            int level = 0;
            if (args.length > 0) {
                if (args[0] instanceof Integer) {
                    enc = ((Integer) args[0]).intValue();
                }
                if (args[0] instanceof Float) {
                    enc = ((Float) args[0]).intValue();
                }
                if (args[1] instanceof Integer) {
                    x1 = ((Integer) args[1]).intValue();
                }
                if (args[1] instanceof Float) {
                    x1 = ((Float) args[1]).intValue();
                }
                if (args[2] instanceof Integer) {
                    x2 = ((Integer) args[2]).intValue();
                }
                if (args[2] instanceof Float) {
                    x2 = ((Float) args[2]).intValue();
                }
                if (args[3] instanceof Integer) {
                    level = ((Integer) args[3]).intValue();
                }
                if (args[3] instanceof Float) {
                    level = ((Float) args[3]).intValue();
                }
            }
            arc.range(enc, x1, x2, level, index);
        }
        if (msg.getAddress().contains("/ring/map")) {
            int enc = 0;
            Integer[] levels = new Integer[64];
            if (args.length > 0) {
                if (args[0] instanceof Integer) {
                    enc = ((Integer) args[0]).intValue();
                }
                if (args[0] instanceof Float) {
                    enc = ((Float) args[0]).intValue();
                }
                for (int i = 1; i < args.length && i < 65; i++) {
                    if (args[i] instanceof Integer) {
                        levels[i-1] = (Integer) args[i];
                    }
                    if (args[i] instanceof Integer) {
                        levels[i-1] = ((Float) args[i]).intValue();
                    }
                }
            }
            arc.map(enc, levels, index);
        }
	}

	/**
	 * @param extPrefix The OSC prefix of the external application
	 */
	public void setPrefix(String extPrefix) {
		this.prefix = extPrefix;
		gui.oscPrefixTF.setText(extPrefix);
	}

	/**
	 * @param extInPort The OSC input port number to receive messages from the external application
	 */
	public void setInPort(String extInPort) {
		this.inPort = Integer.parseInt(extInPort);
		gui.oscInTF.setText(extInPort);
	}

	/**
	 * @param extOutPort The OSC output port number to send messages to the external application
	 */
	public void setOutPort(String extOutPort) {
		this.outPort = Integer.parseInt(extOutPort);
		gui.oscOutTF.setText(extOutPort);
	}

	/**
	 * @param extHostname The hostname that the external application is bound to
	 */
	public void setHostname(String extHostname) {
		this.hostname = extHostname;
		gui.oscHostnameTF.setText(extHostname);
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#destroyPage()
	 */
	public void destroyPage() {
		this.stopOSC();
	}
		
	public void setIndex(int index) {
		this.index = index;
		setName(this.pageName);
	}

	public void configure(Element pageElement) {
		this.setName(this.arc.readConfigValue(pageElement, "pageName"));
		this.setPrefix(this.arc.readConfigValue(pageElement, "prefix"));
		this.setInPort(this.arc.readConfigValue(pageElement, "oscinport"));
		this.setOutPort(this.arc.readConfigValue(pageElement, "oscoutport"));
		this.setHostname(this.arc.readConfigValue(pageElement, "hostname"));
		gui.setCacheDisabled(this.arc.readConfigValue(pageElement, "disablecache"));
		gui.setIgnorePrefix(this.arc.readConfigValue(pageElement, "ignoreprefix"));
		this.stopOSC();
		this.initOSC();
	}

	public boolean getCacheDisabled() {
		return gui.getDisableLedCacheCB().isSelected();
	}

	public int getIndex() {
		return index;
	}

	public JPanel getPanel() {
		return gui;
	}
	
	public void handleAbletonEvent() {
	}
	
	public void operationFailed(DNSSDService arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void serviceRegistered(DNSSDRegistration registration, int flags,
			String serviceName, String regType, String domain) {
		System.out.println("Service registered: " + serviceName + " / " + regType + " / " + domain);
	}

	public void onBlur() {
		// TODO Auto-generated method stub
		
	}

    public void handleDelta(int enc, int delta) {
        // pass all press messages along to the external application
        if (this.oscOut == null) {
            return;
        }
        Object args[] = new Object[2];
        args[0] = new Integer(enc);
        args[1] = new Integer(delta);
        OSCMessage msg = new OSCMessage(this.prefix + "/enc/delta", args);
        try {
            this.oscOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleKey(int enc, int value) {
        // pass all press messages along to the external application
        if (this.oscOut == null) {
            return;
        }
        Object args[] = new Object[2];
        args[0] = new Integer(enc);
        args[1] = new Integer(value);
        OSCMessage msg = new OSCMessage(this.prefix + "/enc/key", args);
        try {
            this.oscOut.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}