package org.monome.pages.configuration;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class OSCPortFactory {
	static OSCPortFactory instance;
	
	Hashtable<Integer, OSCPortIn> oscInPorts;
	Hashtable<Integer, OSCPortOut> oscOutPorts;
	
	public OSCPortFactory() {
		oscInPorts = new Hashtable<Integer, OSCPortIn>();
		oscOutPorts = new Hashtable<Integer, OSCPortOut>();
	}
	
	public static OSCPortFactory getInstance() {
		if (instance == null) {
			instance = new OSCPortFactory();
		}
		return instance;
	}
	
	public OSCPortIn getOSCPortIn(Integer portNum) {
		synchronized(oscInPorts) {
			if (oscInPorts.containsKey(portNum)) {
				return oscInPorts.get(portNum);
			}
			
			OSCPortIn newPort;
			try {
				newPort = new OSCPortIn(portNum);
				newPort.startListening();
				System.out.println("OSCFactory: began listening on port " + portNum);
				oscInPorts.put(portNum, newPort);
				return newPort;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public void destroyOSCPortIn(Integer portNum) {
		synchronized(oscInPorts) {
			if (oscInPorts.containsKey(portNum)) {
				oscInPorts.get(portNum).stopListening();
				oscInPorts.get(portNum).close();
				oscInPorts.remove(portNum);
			}
		}
	}
	
	public OSCPortOut getOSCPortOut(String hostName, Integer portNum) {
		synchronized(oscOutPorts) {
			if (oscOutPorts.containsKey(portNum)) {
				return oscOutPorts.get(portNum);
			}
			
			OSCPortOut newPort;
			try {
				newPort = new OSCPortOut(InetAddress.getByName(hostName), portNum);
				oscOutPorts.put(portNum, newPort);
				return newPort;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	
			return null;
		}
	}

    public void destroy() {
        ArrayList<Integer> portsToDestroy = new ArrayList<Integer>();
        synchronized(oscInPorts) {
            for (Integer port : oscInPorts.keySet()) {
                portsToDestroy.add(port);
            }
        }
        for (Integer port : portsToDestroy) {
            destroyOSCPortIn(port);
        }
    }

}
