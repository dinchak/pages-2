package com.illposed.osc;

import java.util.Enumeration;
import java.util.Vector;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

public class OSCMessage extends OSCPacket {

	protected String address;
	protected final Vector<Object> arguments;

	/**
	 * Create an empty OSC Message
	 * In order to send this osc message, you need to set the address
	 * and, perhaps, some arguments.
	 */
	public OSCMessage() {
		super();
		arguments = new Vector<Object>();
	}

	/**
	 * Create an OSCMessage with an address already initialized
	 * @param newAddress The recepient of this OSC message
	 */
	public OSCMessage(String newAddress) {
		this(newAddress, null);
	}

	/**
	 * Create an OSCMessage with an address and arguments already initialized
	 * @param newAddress    The recepient of this OSC message
	 * @param newArguments  The data sent to the receiver
	 */
	public OSCMessage(String newAddress, Object[] newArguments) {
		super();
		address = newAddress;
		if (null != newArguments) {
			arguments = new Vector<Object>(newArguments.length);
			for (int i = 0; i < newArguments.length; i++) {
				arguments.add(newArguments[i]);
			}
		} else
			arguments = new Vector<Object>();
		init();
	}
	
	/**
	 * @return the address of this OSC Message
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Set the address of this messsage
	 * @param anAddress
	 */
	public void setAddress(String anAddress) {
		address = anAddress;
	}
	
	public void addArgument(Object argument) {
		arguments.add(argument);
	}
	
	public Object[] getArguments() {
		return arguments.toArray();
	}

	/**
	 * @param stream OscPacketByteArrayConverter
	 */
	protected void computeAddressByteArray(OSCJavaToByteArrayConverter stream) {
		stream.write(address);
	}

	/**
	 * @param stream OscPacketByteArrayConverter
	 */
	protected void computeArgumentsByteArray(OSCJavaToByteArrayConverter stream) {
		// SC starting at version 2.2.10 wants a comma at the beginning
		// of the arguments array.
		stream.write(',');
		if (null == arguments)
			return;
		stream.writeTypes(arguments);
		// bjoern: renamed enum->enm for 5.0 compatibility
		Enumeration<Object> enm = arguments.elements();
		while (enm.hasMoreElements()) {
			stream.write(enm.nextElement());
		}
	}

	/**
	 * @param stream OscPacketByteArrayConverter
	 */
	protected void computeByteArray(OSCJavaToByteArrayConverter stream) {
		computeAddressByteArray(stream);
		computeArgumentsByteArray(stream);
		byteArray = stream.toByteArray();
	}

}