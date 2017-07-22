package org.monome.pages.configuration;

import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.monome.pages.configuration.MIDIPageChangeRule;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.gui.MonomeFrame;
import org.monome.pages.pages.Page;


public class FakeMonomeConfiguration extends MonomeConfiguration {	
    static final long serialVersionUID = 42L;
    QuadrantConfiguration quadConf;
	MonomeConfiguration parent;
	int quadNum;
	int pageIndex;

	public FakeMonomeConfiguration(int index, String prefix, String serial,
			int sizeX, int sizeY, boolean usePageChangeButton,
			boolean useMIDIPageChanging,
			ArrayList<MIDIPageChangeRule> midiPageChangeRules,
			MonomeFrame monomeFrame, QuadrantConfiguration quadConf, 
			int pageIndex, MonomeConfiguration parent, int quadNum) {
		super(index, prefix, serial, sizeX, sizeY, usePageChangeButton,
				useMIDIPageChanging, midiPageChangeRules, monomeFrame);
		this.quadConf = quadConf;
		this.parent = parent;
		this.quadNum = quadNum;
		this.pageIndex = pageIndex;
		// TODO Auto-generated constructor stub
	}
	
	public Page addPage(String className) {
		return super.addPage(className);
	}

	public synchronized void clear(int state, int index) {
		if (quadConf == null) {
			return;
		}
		int[] quad = quadConf.getQuad(quadNum);
		for (int y = quad[2]; y < quad[3]; y++) {
			ArrayList<Integer> intArgs = new ArrayList<Integer>();
			intArgs.add(y);
			for (int numArgs = 0; numArgs < (quad[1] - quad[0]) / 8; numArgs++) {
				intArgs.add(state * 255);
			}
			
			this.led_row(intArgs, index);
		}
	}

	public void clearMonome() {
		super.clearMonome();
	}

	public void deletePage(int i) {
		super.deletePage(i);
	}

	public void destroyPage() {
		super.destroyPage();
	}

	public void drawPatternState() {
		super.drawPatternState();
	}

	public synchronized void frame(int x, int y, int[] values, int index) {
		super.frame(x, y, values, index);
	}

	public String[] getMidiOutOptions(int index) {
		return parent.getMidiOutOptions(pageIndex);
	}

	public Receiver getMidiReceiver(String midiDeviceName) {
		return super.getMidiReceiver(midiDeviceName);
	}

	public Transmitter getMidiTransmitter(String midiDeviceName) {
		return super.getMidiTransmitter(midiDeviceName);
	}

	public synchronized void handlePress(int x, int y, int value) {
		int[] quad = quadConf.getQuad(quadNum);
		x -= quad[0];
		y -= quad[2];
		super.handlePress(x, y, value);
	}

	public synchronized void led_col(ArrayList<Integer> intArgs, int index) {
		int[] quad = quadConf.getQuad(quadNum);
		// how many quads to shift down
		// startY / 8
		int shifts = quad[2] / 8;
		
		// number of arguments we want to use for col message
		// endY - startY / 8
		int numArgs = ((quad[3] - quad[2]) / 8);
		ArrayList<Integer> newIntArgs = new ArrayList<Integer>();
		
		int col = intArgs.get(0);
		// offset x coordinate of column number
		// col + startX
		newIntArgs.add(col + quad[0]);
		
		// shift col message down by inserting arguments with existing led state
		for (int i = 0; i < shifts; i++) {
			int colState = 0;
			for (int y = 0; y < 8; y++) {
				colState += (parent.pageState[pageIndex][col + quad[0]][y] << y);
			}
			newIntArgs.add(new Integer(colState));
		}

		// add on existing column message
		for (int i = 1; i < numArgs + 1; i++) {
			newIntArgs.add(intArgs.get(i));
		}
		
		// add existing state after column message
		// sizeY - endY
		int addOns = (parent.sizeY - quad[3]) / 8;
		for (int i = 0; i < addOns; i++) {
			int colState = 0;
			for (int y = quad[3]; y < parent.sizeY; y++) {
				colState += (parent.pageState[pageIndex][col + quad[0]][y] << (y - quad[3]));
			}
			newIntArgs.add(new Integer(colState));
		}
		parent.led_col(newIntArgs, pageIndex);
	}

	public synchronized void led_row(ArrayList<Integer> intArgs, int index) {
		if (intArgs.isEmpty()) {
			return;
		}
		
		int[] quad = quadConf.getQuad(quadNum);
		int shifts = quad[0] / 8;
		int numArgs = ((quad[1] - quad[0]) / 8);
		ArrayList<Integer> newIntArgs = new ArrayList<Integer>();
		int row = intArgs.get(0);
		newIntArgs.add(row + quad[2]);
		for (int i = 0; i < shifts; i++) {
			int rowState = 0;
			for (int x = 0; x < 8; x++) {
				rowState += (parent.pageState[pageIndex][x][row + quad[2]] << x);
			}
			newIntArgs.add(new Integer(rowState));
		}
		for (int i = 1; i < numArgs + 1; i++) {
			newIntArgs.add(intArgs.get(i));
		}
		int addOns = (parent.sizeX - quad[1]) / 8;
		for (int i = 0; i < addOns; i++) {
			int rowState = 0;
			for (int x = quad[1]; x < parent.sizeX; x++) {
				rowState += (parent.pageState[pageIndex][x][row + quad[2]] << (x - quad[1]));
			}
			newIntArgs.add(new Integer(rowState));
		}
		parent.led_row(newIntArgs, pageIndex);
	}

	public synchronized void led(int x, int y, int value, int index) {
		if (x >= sizeX || y >= sizeY || x < 0 || y < 0) {
			return;
		}
		int[] quad = quadConf.getQuad(quadNum);
		x += quad[0];
		y += quad[2];
		parent.led(x, y, value, pageIndex);
	}
	
	public synchronized void led_map(ArrayList<Integer> intArgs, int index) {
		
	}

	public void redrawAbletonPages() {
		super.redrawAbletonPages();
	}

	public void reset(MidiDevice device) {
		super.reset(device);
	}

	public synchronized void send(MidiDevice device, MidiMessage message,
			long timeStamp) {
		super.send(device, message, timeStamp);
	}

	public void setFrameTitle() {
		super.setFrameTitle();
	}

	public void setPatternLength(int pageNum, int length) {
		super.setPatternLength(pageNum, length);
	}

	public void setQuantization(int pageNum, int quantization) {
		super.setQuantization(pageNum, quantization);
	}

	public void switchPage(Page page, int pageIndex, boolean redrawPanel) {
		super.switchPage(page, pageIndex, redrawPanel);
	}

	public synchronized void tick(MidiDevice device) {
		for (int i=0; i < this.numPages; i++) {
			ArrayList<Press> presses = patternBanks.get(i).getRecordedPresses();
			if (presses != null) {
				for (int k=0; k < presses.size(); k++) {
					int[] press = presses.get(k).getPress();
					for (int pb = 0; pb < this.pressesInPlayback.size(); pb++) {
						if (pressesInPlayback.get(pb) == null) continue;
						int[] pbPress = pressesInPlayback.get(pb).getPress();
						if (press[0] == pbPress[0] && press[1] == pbPress[1] && press[2] == 0) {
							pressesInPlayback.remove(pb);
						}
					}
					if (press[2] == 1) {
						pressesInPlayback.add(presses.get(k));
					}
					this.pages.get(i).handleRecordedPress(press[0], press[1], press[2], presses.get(k).getPatternNum());
				}
			}
            this.patternBanks.get(i).handleTick();
			this.pages.get(i).handleTick(device);
		}
	}

	public void toggleMidiInDevice(String deviceName) {
		super.toggleMidiInDevice(deviceName);
	}

	public void toggleMidiOutDevice(String deviceName) {
		super.toggleMidiOutDevice(deviceName);
	}

	public void togglePageChangeMidiInDevice(String deviceName) {
		super.togglePageChangeMidiInDevice(deviceName);
	}

	public String toXml() {
		String xml = "";
		xml += "  <fakemonome>\n";
		xml += "    <prefix>" + this.prefix + "</prefix>\n";
		xml += "    <serial>" + this.serial + "</serial>\n";
		xml += "    <sizeX>" + this.sizeX + "</sizeX>\n";
		xml += "    <sizeY>" + this.sizeY + "</sizeY>\n";
		xml += "  </fakemonome>\n";
		return xml;
	}
}
