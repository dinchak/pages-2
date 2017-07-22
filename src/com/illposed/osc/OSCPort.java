package com.illposed.osc;

import java.net.DatagramSocket;

public abstract class OSCPort {

	protected DatagramSocket socket;
	protected int port;

	/**
	 * The port that the SuperCollider synth engine ususally listens too
	 */
	public static int defaultSCOSCPort() {
		return 57110;
	}
	
	/**
	 * The port that the SuperCollider language engine ususally listens too
	 */
	public static int defaultSCLangOSCPort() {
		return 57120;
	}
	
	/**
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		socket.close();
	}
	
	/**
	 * Close the socket and free-up resources. It's recommended that clients call
	 * this when they are done with the port.
	 */
	public void close() {
		socket.close();
	}

}
