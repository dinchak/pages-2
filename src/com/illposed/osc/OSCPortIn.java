package com.illposed.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JOptionPane;

import org.monome.pages.Main;
import org.monome.pages.gui.MainGUI;

import com.illposed.osc.utility.OSCByteArrayToJavaConverter;
import com.illposed.osc.utility.OSCPacketDispatcher;

public class OSCPortIn extends OSCPort implements Runnable {

	// state for listening
	protected boolean isListening;
	protected OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
	protected OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();
	
	/**
	 * Create an OSCPort that listens on port
	 * @param port
	 * @throws SocketException
	 */
	public OSCPortIn(int port) throws SocketException {
	    try {
    		socket = new DatagramSocket(port);
    		this.port = port;
	    } catch (Exception e) {
            JOptionPane.showMessageDialog(Main.main.mainFrame, "Error binding to port " + port + ": " + e.getMessage(),
                    "Groovy-error", JOptionPane.WARNING_MESSAGE);
	    }
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
			// buffers were 1500 bytes in size, but this was
			// increased to 1536, as this is a common MTU
		byte[] buffer = new byte[8192];
		DatagramPacket packet = new DatagramPacket(buffer, 8192);
		while (isListening) {
			try {
			    if (socket == null) continue;
				socket.receive(packet);
				OSCPacket oscPacket = converter.convert(buffer, packet.getLength());
				dispatcher.dispatchPacket(oscPacket);
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Start listening for incoming OSCPackets
	 */
	public void startListening() {
		isListening = true;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Stop listening for incoming OSCPackets
	 */
	public void stopListening() {
		isListening = false;
	}
	
	/**
	 * Am I listening for packets?
	 */
	public boolean isListening() {
		return isListening;
	}
	
	/**
	 * Register the listener for incoming OSCPackets addressed to an Address
	 * @param anAddress  the address to listen for
	 * @param listener   the object to invoke when a message comes in
	 */
	public void addListener(String anAddress, OSCListener listener) {
		//System.err.println("Adding OSC listener for "+anAddress);
		dispatcher.addListener(anAddress, listener);
	}
	/**
	 * Un-register all existing listeners
	 * @author bjoern
	 * july 2005
	 */
	public void removeAllListeners() {
		dispatcher.removeAllListeners();
	}
	public boolean removeListener(String anAddress) {
		//System.err.println("Removing OSC listener for "+anAddress);
		return dispatcher.removeListener(anAddress);
	}
	
	public OSCListener getListener(String anAddress) {
		return dispatcher.getListener(anAddress);
	}

	public boolean changeListenerAddress(String oldAddress, String newAddress) {
		return dispatcher.changeListenerAddress(oldAddress, newAddress);
	}
	/**
	 * Close the socket and free-up resources. It's recommended that clients call
	 * this when they are done with the port.
	 */
	public void close() {
	    if (socket != null) {
	        socket.close();
	    }
	}
}