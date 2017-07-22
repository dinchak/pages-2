package com.illposed.osc;

import java.math.BigInteger;
import java.util.*;

import com.illposed.osc.utility.OSCJavaToByteArrayConverter;

public class OSCBundle extends OSCPacket {

	protected Date timestamp;

	protected final Vector<OSCPacket> packets;
	public static final BigInteger SECONDS_FROM_1900_to_1970 =
		new BigInteger("2208988800");
	// 17 leap years

	/**
	 * Create a new OSCBundle, with a timestamp of now.
	 * You can add packets to the bundle with addPacket()
	 */
	public OSCBundle() {
		this(null, Calendar.getInstance().getTime());
	}
	
	/**
	 * Create an OSCBundle with the specified timestamp
	 * @param timestamp
	 */
	public OSCBundle(Date timestamp) {
		this(null, timestamp);
	}

	/**
	 * @param newPackets Array of OSCPackets to initialize this object with
	 */
	public OSCBundle(OSCPacket[] newPackets) {
		this(newPackets, Calendar.getInstance().getTime());
	}

	/**
	 * @param newPackets OscPacket[]
	 * @param time java.lang.Time
	 */
	public OSCBundle(OSCPacket[] newPackets, Date newTimestamp) {
		super();
		if (null != newPackets) {
			packets = new Vector<OSCPacket>(newPackets.length);
			for (int i = 0; i < newPackets.length; i++) {
				packets.add(newPackets[i]);
			}
		} else
			packets = new Vector<OSCPacket>();
		timestamp = newTimestamp;
		init();
	}
	
	/**
	 * Return the timestamp for this bundle
	 * @return a Date
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Set the timestamp for this bundle
	 * @param timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Add a packet to the list of packets in this bundle
	 * @param packet
	 */
	public void addPacket(OSCPacket packet) {
		packets.add(packet);
	}
	
	/**
	 * Get the packets contained in this bundle
	 * @return an array of packets
	 */
	public OSCPacket[] getPackets() {
		OSCPacket[] packetArray = new OSCPacket[packets.size()];
		packets.toArray(packetArray);
		return packetArray;
	}

	protected void computeTimeTagByteArray(OSCJavaToByteArrayConverter stream) {
		long millisecs = timestamp.getTime();
		long secsSince1970 = (long) (millisecs / 1000);
		long secs = secsSince1970 + SECONDS_FROM_1900_to_1970.longValue();
		long picosecs = (long) (millisecs - (secsSince1970 * 1000)) * 1000;
		
		stream.write((int) secs);
		stream.write((int) picosecs);

	}

	/**
	 * @param stream OscPacketByteArrayConverter
	 */
	protected void computeByteArray(OSCJavaToByteArrayConverter stream) {
		stream.write("#bundle");
		computeTimeTagByteArray(stream);
		//bjoern: renamed enum->enm for 5.0 compatibility
		Enumeration<OSCPacket> enm = packets.elements();
		OSCPacket nextElement;
		byte[] packetBytes;
		while (enm.hasMoreElements()) {
			nextElement = (OSCPacket) enm.nextElement();
			packetBytes = nextElement.getByteArray();
			stream.write(packetBytes.length);
			stream.write(packetBytes);
		}
		byteArray = stream.toByteArray();
	}

}