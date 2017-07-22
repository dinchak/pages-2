package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.jmdns.ServiceInfo;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.OSCPortFactory;
import org.monome.pages.gui.MainGUI;
import org.monome.pages.pages.gui.ExternalApplicationGUI;
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
public class ExternalApplicationPage implements Page, OSCListener, RegisterListener, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration this page belongs to
	 */
	private MonomeConfiguration monome;

	/**
	 * This page's index (page number) 
	 */
	private int index;

	/**
	 * The OSC prefix the external application uses
	 */
	private String prefix = "/mlr";

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
	public ExternalApplicationPage(MonomeConfiguration monome, int index) {
        inPort = (int) (1024 + (Math.random() * 65411.0));
		this.monome = monome;
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
			removeListeners();
			OSCPortFactory.getInstance().destroyOSCPortIn(this.inPort);
			this.oscIn = null;
		}
		
	}
	
	public void removeListeners() {
		if (this.oscIn != null) {
			this.oscIn.removeListener("/sys/prefix");
			this.oscIn.removeListener("/sys/info");
			this.oscIn.removeListener("/sys/port");
			this.oscIn.removeListener("/sys/host");
			this.oscIn.removeListener("/sys/rotation");
			Set<String> keys = listenersAdded.keySet();
			Object[] keysArray = keys.toArray();
			for (int i = 0; i < keysArray.length; i++) {
				String prefix = (String) keysArray[i];
				this.oscIn.removeListener(prefix + "/led");
				this.oscIn.removeListener(prefix + "/led_col");
				this.oscIn.removeListener(prefix + "/led_row");
				this.oscIn.removeListener(prefix + "/clear");
				this.oscIn.removeListener(prefix + "/frame");
				this.oscIn.removeListener(prefix + "/grid/led/set");
				this.oscIn.removeListener(prefix + "/grid/led/row");
				this.oscIn.removeListener(prefix + "/grid/led/col");
				this.oscIn.removeListener(prefix + "/grid/led/all");
				this.oscIn.removeListener("/grid/led/set");
				this.oscIn.removeListener("/grid/led/row");
				this.oscIn.removeListener("/grid/led/col");
				this.oscIn.removeListener("/grid/led/all");
				this.oscIn.removeListener("/grid/led/map");
				listenersAdded.remove(prefix);
			}
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
			removeListeners();
			addListeners();
			if (Main.main.zeroconfLibrary == Main.LIBRARY_APPLE) {			
    			try {
    				DNSSDRegistration reg = DNSSD.register("extapp-" + this.inPort + "-" + monome.serial, "_monome-osc._udp", this.inPort, this);
    				Main.main.addRegistration(reg);
    			} catch (DNSSDException e) {
    				e.printStackTrace();
    			}
			} else if (Main.main.zeroconfLibrary == Main.LIBRARY_JMDNS) {
    			ServiceInfo info = ServiceInfo.create("_monome-osc._udp.local.", "extapp-" + this.inPort + "-" + monome.serial, this.inPort, "extapp-" + this.inPort + "-" + monome.serial);
    			
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
		this.oscIn.addListener("/sys/port", this);
		this.oscIn.addListener("/sys/host", this);
		this.oscIn.addListener("/sys/info", this);
		this.oscIn.addListener("/sys/rotation", this);
		this.oscIn.addListener(this.prefix + "/led", this);
		this.oscIn.addListener(this.prefix + "/led_col", this);
		this.oscIn.addListener(this.prefix + "/led_row", this);
		this.oscIn.addListener(this.prefix + "/clear", this);
		this.oscIn.addListener(this.prefix + "/frame", this);
		this.oscIn.addListener(this.prefix + "/frame", this);
		
		this.oscIn.addListener(this.prefix + "/grid/led/set", this);
		this.oscIn.addListener(this.prefix + "/grid/led/row", this);
		this.oscIn.addListener(this.prefix + "/grid/led/col", this);
		this.oscIn.addListener(this.prefix + "/grid/led/all", this);
		this.oscIn.addListener(this.prefix + "/grid/led/map", this);
		
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
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */
	public void handlePress(int x, int y, int value) {
		// pass all press messages along to the external application
		if (this.oscOut == null) {
			return;
		}
		// System.out.println("[extapp-" + this.inPort + "]: handlePress(" + x + ", " + y + ", " + value + ")");
		Object args[] = new Object[3];
		args[0] = new Integer(x);
		args[1] = new Integer(y);
		args[2] = new Integer(value);
		OSCMessage msg = new OSCMessage(this.prefix + "/press", args);
		try {
			this.oscOut.send(msg);
			msg = new OSCMessage(this.prefix + "/grid/key", args);
			this.oscOut.send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTiltPage() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick(MidiDevice device)
	 */
	public void handleTick(MidiDevice device) {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		// redrawDevice the monome from the pageState, this is updated when the page isn't selected
		for (int x=0; x < this.monome.sizeX; x++) {
			for (int y=0; y < this.monome.sizeY; y++) {
				this.monome.led(x, y, this.monome.pageState[this.index][x][y], this.index);
			}
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
		// System.out.print("[extapp-" + this.inPort + "]: received " + msg.getAddress() + " ");
		// for (int i = 0; i < args.length; i++) {
		// 	System.out.print(args[i] + " ");
		// }
		// System.out.println();
		if (msg.getAddress().compareTo("/sys/info") == 0) {
			try {
				Thread.sleep(100);
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
				outmsg.addArgument("m40h0800");
				oscOut.send(outmsg);
				outmsg = new OSCMessage();
				outmsg.setAddress("/sys/size");
				outmsg.addArgument(monome.sizeX);
				outmsg.addArgument(monome.sizeY);
				oscOut.send(outmsg);
				outmsg = new OSCMessage();
				outmsg.setAddress("/sys/host");
				outmsg.addArgument(this.hostname);
				oscOut.send(outmsg);
				outmsg = new OSCMessage();
				outmsg.setAddress("/sys/rotation");
				outmsg.addArgument(0);
				oscOut.send(outmsg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
		if (msg.getAddress().compareTo("/sys/port") == 0) {
            if (gui.getIgnorePrefixCB().isSelected()) {
                return;
            }
            
            if (args.length > 0) {
				if (!gui.getIgnorePrefixCB().isSelected()) {
					int port = ((Integer) args[0]).intValue();
	    			setOutPort("" + port);
	    			this.oscOut = OSCPortFactory.getInstance().getOSCPortOut(this.hostname, Integer.valueOf(this.outPort));
				}
            }
			
			OSCMessage outmsg = new OSCMessage();
			outmsg.setAddress("/sys/port");
			outmsg.addArgument(this.outPort);
			try {
				oscOut.send(outmsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (msg.getAddress().compareTo("/sys/host") == 0) {
            if (gui.getIgnorePrefixCB().isSelected()) {
                return;
            }
            if (args.length > 0) {
				if (!gui.getIgnorePrefixCB().isSelected()) {
		            this.hostname = (String) args[0];
					setHostname(this.hostname);
					this.oscOut = OSCPortFactory.getInstance().getOSCPortOut(this.hostname, Integer.valueOf(this.outPort));
				}
            }
			OSCMessage outmsg = new OSCMessage();
			outmsg.setAddress("/sys/host");
			outmsg.addArgument(this.hostname);
			try {
				oscOut.send(outmsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (msg.getAddress().compareTo("/sys/prefix") == 0) {
			if (args.length > 0 && args[0] instanceof String) {
				if (!gui.getIgnorePrefixCB().isSelected()) {
					String pfx = (String) args[0];
					if (pfx.charAt(0) != '/') {
						pfx = '/' + pfx;
					}
					this.setPrefix(pfx);
				}
			}
			OSCMessage outmsg = new OSCMessage();
			outmsg.setAddress("/sys/prefix");
			outmsg.addArgument(this.prefix);
			try {
				oscOut.send(outmsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
			removeListeners();
			addListeners();
		}

		if (msg.getAddress().compareTo("/sys/rotation") == 0) {
//			OSCMessage outmsg = new OSCMessage();
//			outmsg.setAddress("/sys/rotation");
//			int out = 0;
//			if (args.length > 0) {
//				out = ((Integer) args[0]).intValue();
//			}
//			outmsg.addArgument(out);
//			try {
//				oscOut.send(outmsg);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		
		// only process messages from the external application
		if (!msg.getAddress().contains(this.prefix)) {
			return;
		}
		// handle a monome clear request from the external application
		if (msg.getAddress().contains("clear") || msg.getAddress().contains("/grid/led/all")) {
			int int_arg = 0;
			if (args.length > 0) {
				if (args[0] instanceof Integer) {
					int_arg = ((Integer) args[0]).intValue();
				}
				if (args[0] instanceof Float) {
					int_arg = ((Float) args[0]).intValue();
				}
			}
			this.monome.clear(int_arg, this.index);
		}

		// handle a monome led_col request from the external application
		else if (msg.getAddress().contains("led_col")) {
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			for (int i=0; i < args.length; i++) {
				if ((i - 1) * 8 >= monome.sizeY) {
					break;
				}
				if (args[i] instanceof Integer) {
					intArgs.add((Integer) args[i]);
				}
				if (args[i] instanceof Float) {
					intArgs.add(((Float) args[i]).intValue());
				}
			}
			this.monome.led_col(intArgs, this.index);
		}
		
		else if (msg.getAddress().contains("/grid/led/col")) {
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			for (int i=0; i < args.length; i++) {
				if ((i - 2) * 8 >= monome.sizeY) {
					break;
				}
				if (i == 1) {
					continue;
				}
				if (args[i] instanceof Integer) {
					intArgs.add((Integer) args[i]);
				}
				if (args[i] instanceof Float) {
					intArgs.add(((Float) args[i]).intValue());
				}
			}
			this.monome.led_col(intArgs, this.index);
		}

		// handle a monome led_row request from the external application
		else if (msg.getAddress().contains("led_row")) {
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			for (int i=0; i < args.length; i++) {
				if ((i - 1) * 8 >= monome.sizeX) {
					break;
				}
				if (args[i] instanceof Integer) {
					intArgs.add((Integer) args[i]);
				}
				if (args[i] instanceof Float) {
					intArgs.add(((Float) args[i]).intValue());
				}
			}
			this.monome.led_row(intArgs, this.index);
		}
		
		else if (msg.getAddress().contains("/grid/led/row")) {
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			for (int i=1; i < args.length; i++) {
				if ((i - 2) * 8 >= monome.sizeX) {
					break;
				}
				if (args[i] instanceof Integer) {
					intArgs.add((Integer) args[i]);
				}
				if (args[i] instanceof Float) {
					intArgs.add(((Float) args[i]).intValue());
				}
			}
			this.monome.led_row(intArgs, this.index);
		}
		
		else if (msg.getAddress().contains("/grid/led/map")) {
			if (args.length != 10) {
				return;
			}
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			for (int i = 0; i < 10; i++) {
				if (args[i] instanceof Integer) {
					intArgs.add((Integer) args[i]);
				} else if (args[i] instanceof Float) {
					intArgs.add(((Float) args[i]).intValue());
				}
			}
			this.monome.led_map(intArgs, this.index);
		}
		

		// handle a monome led request from the external application
		else if (msg.getAddress().contains("led") || msg.getAddress().contains("/grid/led/set")) {
			int[] int_args = {0, 0, 0};
			for (int i=0; i < args.length; i++) {
				if (i > 2) {
					continue;
				}
				if (args[i] instanceof Integer) {
					int_args[i] = ((Integer) args[i]).intValue();					
				}
				if (args[i] instanceof Float) {
					int_args[i] = ((Float) args[i]).intValue();
				}
				if (!(args[i] instanceof Integer) && !(args[i] instanceof Float)) {
					return;
				}
			}
			int_args[0] = int_args[0] % monome.sizeX;
			int_args[1] = int_args[1] % monome.sizeY;
			this.monome.led(int_args[0], int_args[1], int_args[2], this.index);
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
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setPrefix(this.monome.readConfigValue(pageElement, "prefix"));
		this.setInPort(this.monome.readConfigValue(pageElement, "oscinport"));
		this.setOutPort(this.monome.readConfigValue(pageElement, "oscoutport"));
		this.setHostname(this.monome.readConfigValue(pageElement, "hostname"));
		gui.setCacheDisabled(this.monome.readConfigValue(pageElement, "disablecache"));
		gui.setIgnorePrefix(this.monome.readConfigValue(pageElement, "ignoreprefix"));
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

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}

	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
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
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// pass all tilt messages along to the external application
		if (this.oscOut == null) {
			return;
		}
		Object args[] = new Object[4];
		args[0] = new Integer(n);
		args[1] = new Integer(x);
		args[2] = new Integer(y);
		args[3] = new Integer(z);
		OSCMessage msg = new OSCMessage(this.prefix + "/tilt", args);
		try {
			this.oscOut.send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}