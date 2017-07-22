package com.illposed.osc;

import java.io.*;
import java.util.*;

import javax.comm.*;

import com.illposed.osc.utility.*;



/**
 * Serial input/output of OSC messages.
 * Uses Pascal Stang's OSC serial format:
 * [0xBE = header byte][size byte][...message...][8bit checksum]
 * <p>
 * This software is distributed under the <a href="http://hci.stanford.edu/research/copyright.txt">
 * BSD License</a>.
 * </p>
 * 
 * @author <a href="http://bjoern.org/">Bjoern Hartmann</a> ( bjoern(AT)stanford.edu )
 */
public class OSCSerialPort implements Runnable, SerialPortEventListener {
	public static final int DEFAULT_BAUD_RATE = 115200;
	public static final int DEFAULT_DATA_BITS = SerialPort.DATABITS_8;
	public static final int DEFAULT_STOP_BITS = SerialPort.STOPBITS_1;
	public static final int DEFAULT_PARITY = SerialPort.PARITY_NONE;
	
	/**
	 * We occasionally loose bytes going out to the d.tools hardware boards.
	 * When this flag is set to true, outgoing messages are padded with two null bytes
	 * so the embedded OSC message parser can detect errors in time request a resend
	 * WARNING: it's not guaranteed that other OSC parsers will gracefully handle extra null bytes.
	 */
	public static final boolean PAD_MESSAGES = true; // pad messages with zero bytes?
	
	static CommPortIdentifier portId;
   static Enumeration<?>	      portList;
   InputStream		      inputStream;
   OutputStream		outputStream;
   SerialPort		      serialPort;
   Thread		      readThread;
	private int oscRxNextOp;
	private int oscRxMsgSize;
	private int oscRxReadBytes;
	private byte[] oscRxData;
	
	static String portName;
	static int portBaudRate = DEFAULT_BAUD_RATE;
	static int portDataBits = DEFAULT_DATA_BITS;
	static int portStopBits = DEFAULT_STOP_BITS;
	static int portParity = DEFAULT_PARITY;
	
	protected OSCByteArrayToJavaConverter converter = new OSCByteArrayToJavaConverter();
	protected OSCPacketDispatcher dispatcher = new OSCPacketDispatcher();
	private Timer timer;
	
	
	static final int OSC_RXOP_WAITFORSTART=0;
    static final int OSC_RXOP_READSIZE=1;
    static final int OSC_RXOP_READDATA=2;
    static final int OSC_RXOP_READCHKSUM=3;

 
    // Constructors
 	public OSCSerialPort(String portName) {
 		super();
 		if(openCommPort(portName,DEFAULT_BAUD_RATE,DEFAULT_DATA_BITS,DEFAULT_STOP_BITS,DEFAULT_PARITY)) {
 			readThread = new Thread(this);
 			readThread.start();
 		}
 	}
 	public OSCSerialPort(String portName, int baudRate) {
 		super();
 		if(openCommPort(portName,baudRate,DEFAULT_DATA_BITS,DEFAULT_STOP_BITS,DEFAULT_PARITY)) {
 			readThread = new Thread(this);
 			readThread.start();
 		}
 	}
 	public OSCSerialPort(String portName, int baudRate, int dataBits, int stopBits, int parity) {
 		super();
 		if(openCommPort(portName,baudRate,dataBits,stopBits,parity)) {
 			readThread = new Thread(this);
 			readThread.start();
 		}
 	}

 	
    /**
	 * 
	 */
    private boolean openCommPort(String pName, int baudRate, int dataBits, int stopBits, int parity) {
    portId=null;
   	serialPort=null;
   	try {
   		portName = pName;
   		portBaudRate = baudRate;
   		portDataBits = dataBits;
   		portStopBits = stopBits;
   		portParity = parity;
   		portId = CommPortIdentifier.getPortIdentifier(portName);
   	} catch(NoSuchPortException e) {
   		return false;
   	}
   	
   	oscRxNextOp = OSC_RXOP_WAITFORSTART;
	   try {
		    serialPort = (SerialPort) portId.open("OSCSerialPort", 2000);
		} catch (PortInUseException e) {
			System.err.println("PortInUseException!\n");
			return false;
		}
		try {
		    inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.err.println("IOException in serialPort.getInputStream()!\n");
			return false;
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.err.println("IOException in serialPort.getOutputStream()!\n");
			return false;
		}
		try {
		    serialPort.addEventListener(this);
		    serialPort.notifyOnDataAvailable(true);
			serialPort.notifyOnOutputEmpty(true);
		} catch (TooManyListenersException e) {
			System.err.println("TooManyListenersException!\n");
			return false;
		}
		
		try {
		    serialPort.setSerialPortParams(baudRate, dataBits, 
						   stopBits, 
						   parity);
		} catch (UnsupportedCommOperationException e) {
			System.err.println("UnsupportedCommOperationException!\n");
			return false;
		}
		return true;
   }
	
	public void run() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new CheckSerialPortTimerTask(),10000,10000);
		try {
		    Thread.sleep(20000);
		} catch (InterruptedException e) {
			System.err.println("InterruptedException in OSCSerialPort.run()");
		}
		
	}
	/**
	 * Handles incoming serial data.
	 * Passes bytes one by one to buildOSCByteBuffer().
	 * Ignores all other serial events.
	 */
	public void serialEvent(SerialPortEvent event) {
		//System.out.println("SerialEvent came in.");
    	switch (event.getEventType()) {
    	case SerialPortEvent.BI:
    	case SerialPortEvent.OE:
    	case SerialPortEvent.FE:
    	case SerialPortEvent.PE:
    	case SerialPortEvent.CD:
    	case SerialPortEvent.CTS:
    	case SerialPortEvent.DSR:
    	case SerialPortEvent.RI:
    	case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
    		break;
    	case SerialPortEvent.DATA_AVAILABLE:
    		try {
    			while (inputStream.available() > 0) {
    				buildOSCByteBuffer(inputStream.read());
    			}
    		} catch (IOException e) {
    			System.err.println("IOException in serialEvent!\n");
    		}
    		break;
    	}
		
	}
	
	/**
	 * Piece together an OSCByteBuffer one byte at a time.
	 * Expects OSC header 0xBE, size byte, and 8bit checksum.
	 * 
	 * @param c = current byte read from serial stream
	 */
    private void buildOSCByteBuffer(int c) {
    	switch(oscRxNextOp)
    	{
    	case OSC_RXOP_WAITFORSTART:
    		if(c==0xBE) {
				oscRxNextOp = OSC_RXOP_READSIZE;
			}
    		break;
    	case OSC_RXOP_READSIZE:
    		oscRxMsgSize = c;
			oscRxReadBytes = 0;
			oscRxData = new byte[oscRxMsgSize+1];
			oscRxNextOp = OSC_RXOP_READDATA;
    		break;
    	case OSC_RXOP_READDATA:
    		try {
    		oscRxData[oscRxReadBytes++] = (byte)c;
    		if(oscRxReadBytes == oscRxMsgSize) {
    		oscRxData[oscRxReadBytes] = 0; //terminate string
    			oscRxNextOp = OSC_RXOP_READCHKSUM;
    		}
    		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
    			//something went wrong (i.e., got an erroneous 0xBE- throw the message away
    			oscRxNextOp = OSC_RXOP_WAITFORSTART;
    		}
    		break;
    	case OSC_RXOP_READCHKSUM:
    		short oscRxChecksum = 0;
			for (int i=0; i<oscRxMsgSize; i++) {
				oscRxChecksum=(short)((oscRxChecksum + (short)(0x000000FF &(int)oscRxData[i]))% 256);
			}
			if(oscRxChecksum == c) {
				// checksum matched and we're done with this msg
				// -> fire off registered listeners
				
				OSCPacket oscPacket = converter.convert(oscRxData,oscRxMsgSize);
				//System.out.println("received valid OSC message.");
				dispatcher.dispatchPacket(oscPacket);
				oscRxNextOp = OSC_RXOP_WAITFORSTART;
			} else {
				// mismatch - throw this message away
				System.err.println("Received corrupted OSC message: calculated "+(int)oscRxChecksum+", should have been: "+(int)c);
				oscRxNextOp = OSC_RXOP_WAITFORSTART;
			}
    		break;
    	}
    }
    /**
     * Send an OSC packet out our open serial port
     * @param aPacket The OSC packet that should be sent
     * @throws IOException
     */
    public void send(OSCPacket aPacket) throws IOException {
   	 //if we have no port, try opening again
   	 try {
   		 CommPortIdentifier.getPortIdentifier(portName);
   	 } catch(NoSuchPortException e) {
   		 System.err.println("COM port does not exist  - not sending OSC message.");
			 throw new IOException();
   	 }
   	
   	int checksum=0;
    	// get byte array of OSC message
    	byte[] byteArray = aPacket.getByteArray();
    	// package into serial format:
		int length = byteArray.length;
		if(length<=0) {
			//we have an error in the byte array
			System.err.println("OSCSerialPort.send: bad byte array");
			return;
		}
		
		byte[] serialByteArray;
		if(PAD_MESSAGES) {
			serialByteArray = new byte[byteArray.length+5];
		} else {
			serialByteArray = new byte[byteArray.length+3];
		}
		
		// add header
		serialByteArray[0]=(byte)0xBE;
		// add length
		serialByteArray[1]=(byte)byteArray.length;
		// copy message
		for(int i=0; i<byteArray.length; i++) {
			serialByteArray[2+i]=byteArray[i];
			checksum=((checksum + byteArray[i])% 256);
		}
		// add checksum
		serialByteArray[byteArray.length+2]=(byte)checksum;
		//some zero bytes to pad
		if(PAD_MESSAGES) {
			serialByteArray[byteArray.length+3]=0;
			serialByteArray[byteArray.length+4]=0;
		}
		// send message
		//System.out.println("OSCSerialPort: Sending "+serialByteArray.length+" bytes out.");
		try {
			outputStream.write(serialByteArray);
		} catch (Exception e) {
			System.err.println("Encountered exception while trying to send serial message.");
			e.printStackTrace();
			if (serialPort!=null) serialPort.close();
			// try to reopen
			if(!openCommPort(portName,portBaudRate,portDataBits,portStopBits,portParity)) {
			 System.err.println("Unsuccesssfully tried to reopen COM port - not sending OSC message.");
			 if(serialPort!=null) serialPort.close();
			 throw new IOException();
			} else  {
				try {
					outputStream.write(serialByteArray);
				} catch (Exception f ) {
					System.err.println("Failed a second time.");
					throw new IOException();
				}
			}
		 

		}
	}
    
    /**
	 * Register the listener for incoming OSCPackets addressed to an Address
	 * @param anAddress  the address to listen for
	 * @param listener   the object to invoke when a message comes in
	 */
	public void addListener(String anAddress, OSCListener listener) {
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
		return dispatcher.removeListener(anAddress);
	}
	public boolean changeListenerAddress(String oldAddress, String newAddress) {
		return dispatcher.changeListenerAddress(oldAddress, newAddress);
	}
	
	public OSCListener getListener(String anAddress) {
		return dispatcher.getListener(anAddress);
	}

	/**
	 * Close the socket and free-up resources. It's recommended that clients call
	 * this when they are done with the port.
	 */
	public void close() {
		serialPort.close();
	}
	/**
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		serialPort.close();
	}

	private class CheckSerialPortTimerTask extends TimerTask {
		@Override
		public void run() {
			//set this timer to be expired
			System.err.print("Checking Serial port...");
			try{
				byte[] byteArray = new byte[1];
				byteArray[0]=0x20; //send some dummy byte
				outputStream.write(byteArray);
				System.err.println("ok.");
			}
			catch(IOException e) {
				System.err.print("error...");
				if(serialPort!=null) {
					serialPort.close();
				}
				if(openCommPort(portName,portBaudRate,portDataBits,portStopBits,portParity))
					System.err.println("recovered.");
				else
					System.err.println("could not recover.");
			}
		}
	}
}
