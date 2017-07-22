package org.monome.pages.machinedrum;

import java.util.Random;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * A utility class to interact with the Elektron MachineDrum over a MIDI connection.
 * 
 * @author Tom Dinchak
 *
 */
public class MachineDrum {

	/**
	 * Random number generator
	 */
	Random generator;

	/**
	 * Control out message.
	 */
	ShortMessage ctrl_out;

	/**
	 * Constructor.
	 */
	public MachineDrum() {
		ctrl_out = new ShortMessage();
		generator = new Random();
	}
	
	public void sendParamChange(Receiver output_device, int machine_number, int param_number, int value) {
		if (output_device == null) {
			return;
		}

		// see appendix of machinedrum manual
		int midi_channel = (int) Math.floor(machine_number / 4);
		int cc = (param_number + 16) + ((machine_number % 4) * 24);
		if (cc >= 64) {
			cc += 8;
		}
		try {
			ctrl_out.setMessage(ShortMessage.CONTROL_CHANGE, midi_channel, cc, value);
			output_device.send(ctrl_out, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a random MIDI CC value from 0-127 to machine_number's param_number through the output_device.
	 * 
	 * @param output_device The MIDI output to use
	 * @param machine_number The machine number (0=BD, 1=SD, etc.)
	 * @param param_number The parameter number (0-7 = first page from upper left to bottom right, 8-15 = second page, 16-23 = third page)
	 */
	public void sendRandomParamChange(Receiver output_device, int machine_number, int param_number) {
		if (output_device == null) {
			return;
		}

		// see appendix of machinedrum manual
		int midi_channel = (int) Math.floor(machine_number / 4);
		int cc = (param_number + 16) + ((machine_number % 4) * 24);
		if (cc >= 64) {
			cc += 8;
		}
		int value = generator.nextInt(128);
		try {
			ctrl_out.setMessage(ShortMessage.CONTROL_CHANGE, midi_channel, cc, value);
			output_device.send(ctrl_out, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to load the kit specified by kit_number.
	 * 
	 * @param output_device The MIDI output to use
	 * @param kit_number The kit number to load (0 = first kit, 63 = last kit)
	 */
	public void sendKitLoad(Receiver output_device, int kit_number) {
		if (output_device == null) {
			return;
		}

		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[9];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x58;
		data[7] = (byte) (kit_number);
		data[8] = (byte) 0xF7;
		try {
			msg.setMessage(data, 9);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to save the current kit to kit slot specified by kit_number.
	 * 
	 * @param output_device The MIDI output to use
	 * @param kit_number The kit number to save the current kit into
	 */
	public void sendKitSave(Receiver output_device, int kit_number) {
		if (output_device == null) {
			return;
		}

		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[9];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x59;
		data[7] = (byte) (kit_number);
		data[8] = (byte) 0xF7;
		try {
			msg.setMessage(data, 9);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to initialize and assign a machine to a track.
	 * 
	 * @param output_device The MIDI output to use
	 * @param track The track number (0=BD, 1=SD, etc.)
	 * @param choice The machine number to assign, see the MachineDrum manual's appendix
	 */
	public void sendAssignMachine(Receiver output_device, int track, int choice) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[12];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x5b;
		data[7] = (byte) (track);
		data[8] = (byte) (choice);
		data[9] = (byte) 0xF7;
		data[10] = (byte) 0x00;
		data[11] = (byte) 0x02;
		try {
			msg.setMessage(data, 12);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void sendAssignLFO(Receiver output_device, int lfoNum, int paramNum, int paramValue) {
		if (output_device == null) {
			return;
		}
		if (lfoNum < 0 || lfoNum > 15) {
			return;
		}
		if (paramNum < 0 || paramNum > 7) {
			return;
		}
		if (paramValue < 0 || paramValue > 127) {
			return;
		}
		
		byte b1 = (byte) ((lfoNum << 3) + paramNum);
		byte b2 = (byte) paramValue;
		
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x62;
		data[7] = b1;
		data[8] = b2;
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to set the value of a global fx unit parameter. 
	 * 
	 * @param output_device The MIDI output to use
	 * @param fx The name of the global fx unit: "echo", "gate", ,"eq", or "compressor" 
	 * @param param The parameter number (0 = first paramater)
	 * @param value The new value (from 0-127)
	 */
	public void sendFxParam(Receiver output_device, String fx, int param, int value) {
		if (output_device == null) {
			return;
		}

		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		if (fx.equals("echo")) {
			data[6] = (byte) 0x5d;
		} else if (fx.equals("gate")) {
			data[6] = (byte) 0x5e;
		} else if (fx.equals("eq")) {
			data[6] = (byte) 0x5f;
		} else if (fx.equals("compressor")) {
			data[6] = (byte) 0x60;
		} else {
			return;
		}
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[7] = (byte) (param);
		data[8] = (byte) (value);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void requestKit(Receiver output_device, int kitNum) {
		if (output_device == null) {
			return;
		}

		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[9];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x53;
		data[7] = (byte) kitNum;
		data[8] = (byte) 0xF7;
		try {
			msg.setMessage(data, 9);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getMachineChoices(int slot) {
		String[] bd = {
				"TRX-BD",
				"TRX-B2",
				"EFM-BD",
				"P-I-BD",
				"E12-BD"
		};
		String[] sd = {
				"TRX-SD",
				"EFM-SD",
				"P-I-SD",
				"E12-SD",
				"E12-BR"
		};
		String[] ht = {
				"TRX-XT",
				"TRX-XC",
				"EFM-XT",
				"E12-HT",
				"E12-LT",
				"E12-BC",
				"P-I-XT"
		};
		String[] mt = {
				"TRX-XT",
				"TRX-XC",
				"EFM-XT",
				"E12-HT",
				"E12-LT",
				"E12-BC",
				"P-I-XT"
		};
		String[] lt = {
				"TRX-XT",
				"TRX-XC",
				"EFM-XT",
				"E12-HT",
				"E12-LT",
				"E12-BC",
				"P-I-XT"
		};
		String[] cp = {
				"TRX-CP",
				"EFM-CP",
				"E12-CP",
				"E12-TA",
				"E12-SH"
		};

		String[] rs = {
				"TRX-RS",
				"EFM-RS",
				"E12-RS",
				"E12-BR",
				"E12-SH",
				"P-I-RS"
		};

		String[] cb = {
				"TRX-CB",
				"TRX-CL",
				"EFM-CB",
				"E12-CB",
				"E12-TR",
				"P-I-ML"
		};

		String[] ch = {
				"GND-NS",
				"TRX-CH",
				"EFM-HH",
				"E12-CH",
				"P-I-HH"
		};

		String[] oh = {
				"P-I-ML",
				"TRX-OH",
				"EFM-HH",
				"E12-OH",
				"P-I-HH"
		};

		String[] rc = {
				"TRX-CY",
				"EFM-CY",
				"P-I-RC",
				"P-I-ML",
				"E12-RC"
		};

		String[] cc = {
				"TRX-CY",
				"EFM-CY",
				"P-I-CC",
				"P-I-ML",
				"E12-CC"
		};

		String[] m1 = {
				"TRX-MA",
				"TRX-CL",
				"TRX-XC"
		};

		String[] m2 = {
				"E12-TA",
				"E12-BC",
				"E12-TR"
		};
		
		if (slot == 0) { return bd; }
		if (slot == 1) { return sd; }
		if (slot == 2) { return ht; }
		if (slot == 3) { return mt; }
		if (slot == 4) { return lt; }
		if (slot == 5) { return cp; }
		if (slot == 6) { return rs; }
		if (slot == 7) { return cb; }
		if (slot == 8) { return ch; }
		if (slot == 9) { return oh; }
		if (slot == 10) { return rc; }
		if (slot == 11) { return cc; }
		if (slot == 12) { return m1; }
		if (slot == 13) { return m2; }
		return ch;

	}

	/**
	 * Sends a request to initialize a new drum kit.  This means initializing all tracks with machines randomly selected from a pool.
	 * 
	 * @param output_device The MIDI output device
	 * @param machinePool Which pools to use when assigning machines, currently only pool 0 will work
	 */
	public void initKit(Receiver output_device, int machinePool) {
		byte[] choice = new byte[16];
		if (machinePool == 0) {
			for (int i = 0; i < 14; i++) {
				String[] choices = getMachineChoices(i);
				choice[i] = getMachine(choices[generator.nextInt(choices.length)]);
			}
		}
		if (machinePool == 1) {
			for (int i = 0; i < 14; i++) {
				choice[i] = getRandomMachineNumber();
			}
		}
		for (int x = 0; x < 14; x++) {
			sendAssignMachine(output_device, x, choice[x]);
		}
	}

	/**
	 * Translates machine names to the number they're referenced by in Sysex messages.
	 * 
	 * @param name The name of the machine ("GND-SIN", "TRX-BD", etc.) 
	 * @return The value that refers to that machine in Sysex messages
	 */
	public byte getMachine(String name) {
		// GND-xyz
		if (name.equals("GND-EMPTY")) {
			return (byte) 0;
		}
		if (name.equals("GND-SIN")) {
			return (byte) 1;
		}
		if (name.equals("GND-NS")) {
			return (byte) 2;
		}
		if (name.equals("GND-IM")) {
			return (byte) 3;
		}

		// TRX-xy
		if (name.equals("TRX-BD")) {
			return (byte) 16;
		}
		if (name.equals("TRX-SD")) {
			return (byte) 17;
		}
		if (name.equals("TRX-XT")) {
			return (byte) 18;
		}
		if (name.equals("TRX-CP")) {
			return (byte) 19;
		}
		if (name.equals("TRX-RS")) {
			return (byte) 20;
		}
		if (name.equals("TRX-CB")) {
			return (byte) 21;
		}
		if (name.equals("TRX-CH")) {
			return (byte) 22;
		}
		if (name.equals("TRX-OH")) {
			return (byte) 23;
		}
		if (name.equals("TRX-CY")) {
			return (byte) 24;
		}
		if (name.equals("TRX-MA")) {
			return (byte) 25;
		}
		if (name.equals("TRX-CL")) {
			return (byte) 26;
		}
		if (name.equals("TRX-XC")) {
			return (byte) 27;
		}
		if (name.equals("TRX-B2")) {
			return (byte) 28;
		}

		// EFM-xy
		if (name.equals("EFM-BD")) {
			return (byte) 32;
		}
		if (name.equals("EFM-SD")) {
			return (byte) 33;
		}
		if (name.equals("EFM-XT")) {
			return (byte) 34;
		}
		if (name.equals("EFM-CP")) {
			return (byte) 35;
		}
		if (name.equals("EFM-RS")) {
			return (byte) 36;
		}
		if (name.equals("EFM-CB")) {
			return (byte) 37;
		}
		if (name.equals("EFM-HH")) {
			return (byte) 38;
		}
		if (name.equals("EFM-CY")) {
			return (byte) 39;
		}

		// E12-xy
		if (name.equals("E12-BD")) {
			return (byte) 48;
		}
		if (name.equals("E12-SD")) {
			return (byte) 49;
		}
		if (name.equals("E12-HT")) {
			return (byte) 50;
		}
		if (name.equals("E12-LT")) {
			return (byte) 51;
		}
		if (name.equals("E12-CP")) {
			return (byte) 52;
		}
		if (name.equals("E12-RS")) {
			return (byte) 53;
		}
		if (name.equals("E12-CB")) {
			return (byte) 54;
		}
		if (name.equals("E12-CH")) {
			return (byte) 55;
		}
		if (name.equals("E12-OH")) {
			return (byte) 56;
		}
		if (name.equals("E12-RC")) {
			return (byte) 57;
		}
		if (name.equals("E12-CC")) {
			return (byte) 58;
		}
		if (name.equals("E12-BR")) {
			return (byte) 59;
		}
		if (name.equals("E12-TA")) {
			return (byte) 60;
		}
		if (name.equals("E12-TR")) {
			return (byte) 61;
		}
		if (name.equals("E12-SH")) {
			return (byte) 62;
		}
		if (name.equals("E12-BC")) {
			return (byte) 63;
		}

		// P-I-xy
		if (name.equals("P-I-BD")) {
			return (byte) 64;
		}
		if (name.equals("P-I-SD")) {
			return (byte) 65;
		}
		if (name.equals("P-I-MT")) {
			return (byte) 66;
		}
		if (name.equals("P-I-ML")) {
			return (byte) 67;
		}
		if (name.equals("P-I-MA")) {
			return (byte) 68;
		}
		if (name.equals("P-I-RS")) {
			return (byte) 69;
		}
		if (name.equals("P-I-RC")) {
			return (byte) 70;
		}
		if (name.equals("P-I-CC")) {
			return (byte) 71;
		}
		if (name.equals("P-I-HH")) {
			return (byte) 72;
		}

		return (byte) 0;
	}
	
	public byte getRandomMachineNumber() {
		byte[] choices = {01, 02, 03, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
				          32, 33, 34, 35, 36, 37, 38, 39, 48, 49, 50, 51, 52, 53, 54, 55,
				          56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71,
				          72};
		return choices[this.generator.nextInt(choices.length)];
		
	}
	
	public void setPattern(Receiver output_device, int patternNum) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x71;
		data[7] = (byte) 0x04;
		data[8] = (byte) (patternNum);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void setSong(Receiver output_device, int songNum) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x71;
		data[7] = (byte) 0x08;
		data[8] = (byte) (songNum);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void setMute(Receiver output_device, int machineNum, int muted) {
		if (output_device == null) {
			return;
		}
		
		int midiChan = machineNum / 4;
		int midiCC = 12 + (machineNum % 4);
		ShortMessage msg = new ShortMessage();
		try {
			msg.setMessage(ShortMessage.CONTROL_CHANGE, midiChan, midiCC, muted);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void setExtendedMode(Receiver output_device, int mode) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x71;
		data[7] = (byte) 0x20;
		data[8] = (byte) (mode);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void setSongMode(Receiver output_device, int mode) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x71;
		data[7] = (byte) 0x10;
		data[8] = (byte) (mode);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void setGlobal(Receiver output_device, int mode) {
		if (output_device == null) {
			return;
		}
		SysexMessage msg = new SysexMessage();
		byte[] data = new byte[10];
		data[0] = (byte) 0xF0;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x3c;
		data[4] = (byte) 0x02;
		data[5] = (byte) 0x00;
		data[6] = (byte) 0x71;
		data[7] = (byte) 0x01;
		data[8] = (byte) (mode);
		data[9] = (byte) 0xF7;
		try {
			msg.setMessage(data, 10);
			output_device.send(msg, -1);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

}