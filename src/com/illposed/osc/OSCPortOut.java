package com.illposed.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class OSCPortOut extends OSCPort {

	protected InetAddress address;

	/**
	 * Create an OSCPort that sends to newAddress, newPort
	 * @param newAddress InetAddress
	 * @param newPort int
	 */
	public OSCPortOut(InetAddress newAddress, int newPort)
		throws SocketException {
		socket = new DatagramSocket();
		address = newAddress;
		port = newPort;
	}

	/**
	 * Create an OSCPort that sends to newAddress, on the standard SuperCollider port
	 * @param newAddress InetAddress
	 *
	 * Default the port to the standard one for SuperCollider
	 */
	public OSCPortOut(InetAddress newAddress) throws SocketException {
		this(newAddress, defaultSCOSCPort());
	}

	/**
	 * Create an OSCPort that sends to localhost, on the standard SuperCollider port
	 * Default the address to localhost
	 * Default the port to the standard one for SuperCollider
	 */
	public OSCPortOut() throws UnknownHostException, SocketException {
		this(InetAddress.getLocalHost(), defaultSCOSCPort());
	}
	
	/**
	 * @param aPacket OSCPacket
	 */

	public void send(OSCPacket aPacket) throws IOException {
		byte[] byteArray = aPacket.getByteArray();
		DatagramPacket packet =
			new DatagramPacket(byteArray, byteArray.length, address, port);
		socket.send(packet);
	}
}
