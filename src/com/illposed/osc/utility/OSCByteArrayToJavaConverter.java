package com.illposed.osc.utility;

import java.math.BigInteger;
import java.util.Date;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;

/**
 * @author bjoern
 * @author cramakrishnan
 *
 * Modified from cramakrishnan to permit missing format strings

 *
 * Copyright (C) 2003, C. Ramakrishnan / Auracle
 * All rights reserved.
 * 
 * See license.txt (or license.rtf) for license information.
 */
public class OSCByteArrayToJavaConverter {

	byte[] bytes;
	int bytesLength;
	int streamPosition;

	/**
	 * Helper object for converting from a byte array to Java objects
	 */
	public OSCByteArrayToJavaConverter() {
		super();
	}

	public OSCPacket convert(byte[] byteArray, int bytesLen) {
		bytes = byteArray;
		this.bytesLength = bytesLen;
		streamPosition = 0;
		if (isBundle())
			return convertBundle();
		else
			return convertMessage();
	}

	private boolean isBundle() {
			// only need the first 7 to check if it is a bundle
		String bytesAsString = new String(bytes, 0, 7);
		return bytesAsString.startsWith("#bundle");
	}

	private OSCBundle convertBundle() {
		// skip the "#bundle " stuff
		streamPosition = 8;
		Date timestamp = readTimeTag();
		OSCBundle bundle = new OSCBundle(timestamp);
		OSCByteArrayToJavaConverter myConverter = new OSCByteArrayToJavaConverter();
		while (streamPosition < bytesLength) {
			// recursively read through the stream and convert packets you find
			int packetLength = ((Integer) readInteger()).intValue();
			byte[] packetBytes = new byte[packetLength];
			for (int i = 0; i < packetLength; i++)
				packetBytes[i] = bytes[streamPosition++];
			OSCPacket packet = myConverter.convert(packetBytes, packetLength);
			bundle.addPacket(packet);
		}
		return bundle;
	}

	private OSCMessage convertMessage() {
		OSCMessage message = new OSCMessage();
		message.setAddress(readString());
		char[] types = readTypes();
		if (null == types) {
			// we are done
			return message;
		}
		//moveToFourByteBoundry(); moved into readType (bjoern)
		for (int i = 0; i < types.length; i++) {
			if ('[' == types[i]) {
				// we're looking at an array -- read it in
				message.addArgument(readArray(types, i));
				// then increment i to the end of the array
				while (']' != types[i])
					i++;
			} else
				message.addArgument(readArgument(types[i]));
		}
		return message;
	}

	private String readString() {
		int strLen = lengthOfCurrentString();
		char[] stringChars = new char[strLen];
		for (int i = 0; i < strLen; i++)
			stringChars[i] = (char) bytes[streamPosition++];
		moveToFourByteBoundry();
		return new String(stringChars);
	}

	/**
	 * @return a char array with the types of the arguments
	 * TODO: (bjoern) - gracefully handle absent type information
	 */
	private char[] readTypes() {
		// the next byte should be a ","
		// (bjoern) if it is not, check if there is more data
		// if there is, assume a single int
		// if there is not, return null
		if (bytes[streamPosition] != 0x2C) {
			if(streamPosition >= (bytesLength-1))//TODO: check this
				return null;
			else 
			{
				char[] defType = new char[1];
				defType[0] = (char)'i';
				return defType;
			}
		}
		
		streamPosition++;
		// find out how long the list of types is
		int typesLen = lengthOfCurrentString();
		if (0 == typesLen) {
			return null;
		}
		// read in the types
		char[] typesChars = new char[typesLen];
		for (int i = 0; i < typesLen; i++) {
			typesChars[i] = (char) bytes[streamPosition++];
		}
		moveToFourByteBoundry(); //added (bjoern)
		return typesChars;
	}

	/**
	 * @param c type of argument
	 * @return a Java representation of the argument
	 */
	private Object readArgument(char c) {
		switch (c) {
			case 'i' :
				return readInteger();
			case 'h' :
				return readBigInteger();
			case 'f' :
				return readFloat();
			case 'd' :
				return readDouble();
			case 's' :
				return readString();
			case 'c' :
				return readChar();
			case 'T' :
				return Boolean.TRUE;
			case 'F' :
				return Boolean.FALSE;
		}

		return null;
	}

	/**
	 * @return a Character
	 */
	private Object readChar() {
		return new Character((char) bytes[streamPosition++]);
	}

	/**
	 * @return a Double
	 */
	private Object readDouble() {
		return readFloat();
	}

	/**
	 * @return a Float
	 */
	private Object readFloat() {
		byte[] floatBytes = new byte[4];
		floatBytes[0] = bytes[streamPosition++];
		floatBytes[1] = bytes[streamPosition++];
		floatBytes[2] = bytes[streamPosition++];
		floatBytes[3] = bytes[streamPosition++];
//		int floatBits =
//			(floatBytes[0] << 24)
//				| (floatBytes[1] << 16)
//				| (floatBytes[2] << 8)
//				| (floatBytes[3]);
		BigInteger floatBits = new BigInteger(floatBytes);
		return new Float(Float.intBitsToFloat(floatBits.intValue()));
	}

	/**
	 * @return a BigInteger
	 */
	private Object readBigInteger() {
		byte[] intBytes = new byte[4];
		intBytes[0] = bytes[streamPosition++];
		intBytes[1] = bytes[streamPosition++];
		intBytes[2] = bytes[streamPosition++];
		intBytes[3] = bytes[streamPosition++];
		BigInteger intBits = new BigInteger(intBytes);
		return new Integer(intBits.intValue());
	}

	/**
	 * @return an Integer
	 */
	private Object readInteger() {
		byte[] intBytes = new byte[4];
		try {
			intBytes[0] = bytes[streamPosition++];
			intBytes[1] = bytes[streamPosition++];
			intBytes[2] = bytes[streamPosition++];
			intBytes[3] = bytes[streamPosition++];
			BigInteger intBits = new BigInteger(intBytes);
			return new Integer(intBits.intValue());
		} catch (ArrayIndexOutOfBoundsException e) {
			return new Integer(0);
		}
	}
	
	/**
	 * @return a Date
	 */
	private Date readTimeTag() {
		byte[] secondBytes = new byte[8];
		byte[] picosecBytes = new byte[8];
		for (int i = 4; i < 8; i++)
			secondBytes[i] = bytes[streamPosition++];
		for (int i = 4; i < 8; i++)
			picosecBytes[i] = bytes[streamPosition++];
		BigInteger secsSince1900 = new BigInteger(secondBytes);
		long secsSince1970 =  secsSince1900.longValue() - OSCBundle.SECONDS_FROM_1900_to_1970.longValue();
		if (secsSince1970 < 0) secsSince1970 = 0; // no point maintaining times in the distant past
		BigInteger picosecs = new BigInteger(picosecBytes);
		long millisecs = (secsSince1970 * 1000) + (picosecs.longValue() / 1000);
		return new Date(millisecs);
	}

	/**
	 * @param types
	 * @param i
	 * @return an Array
	 */
	private Object[] readArray(char[] types, int i) {
		int arrayLen = 0;
		while (types[i + arrayLen] != ']')
			arrayLen++;
		Object[] array = new Object[arrayLen];
		for (int j = 0; i < arrayLen; j++) {
			array[j] = readArgument(types[i + j]);
		}
		return array;
	}

	private int lengthOfCurrentString() {
		int i = 0;
		while (bytes[streamPosition + i] != 0)
			i++;
		return i;
	}

	private void moveToFourByteBoundry() {
		// If i'm already at a 4 byte boundry, I need to move to the next one
		int mod = streamPosition % 4;
		streamPosition += (4 - mod);
	}

}
