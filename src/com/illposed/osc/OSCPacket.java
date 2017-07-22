package com.illposed.osc;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

public abstract class OSCPacket {

	protected boolean isByteArrayComputed;
	protected byte[] byteArray;

	public OSCPacket() {
		super();
	}

	protected void computeByteArray() {
		OSCJavaToByteArrayConverter stream = new OSCJavaToByteArrayConverter();
		computeByteArray(stream);
	}

	/**
	 * @param stream OscPacketByteArrayConverter
	 *
	 * Subclasses should implement this method to product a byte array
	 * formatted according to the OSC/SuperCollider specification.
	 */
	protected abstract void computeByteArray(OSCJavaToByteArrayConverter stream);

	/**
	 * @return byte[]
	 */
	public byte[] getByteArray() {
		if (!isByteArrayComputed) 
			computeByteArray();
		return byteArray;
	}

	protected void init() {
		
	}

}