package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.machinedrum.MachineDrum;
import org.monome.pages.pages.gui.MachineDrumInterfaceGUI;
import org.w3c.dom.Element;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
/**
 * The Machine Drum Interface page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/MachineDrumInterfacePage
 *   
 * @author Tom Dinchak
 *
 */
public class MachineDrumInterfacePage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * The MonomeConfiguration object this page belongs to
	 */
	MonomeConfiguration monome;
	
	private MachineDrumInterfaceGUI gui;

	/**
	 * The index of this page (the page number) 
	 */
	private int index;

	/**
	 * Utility class for sending MIDI messages to the MachineDrum 
	 */
	private MachineDrum machinedrum;

	/**
	 * How often random param changes are sent. 
	 */
	private int speed = 100;
	
	/**
	 * Random number generator 
	 */
	private Random generator;
	
	/**
	 * The name of the page 
	 */
	private String pageName = "Machine Drum Interface";
	
	private ArrayList<LoadedModule> loadedModules;

    private Dimension origGuiDimension;

	/**
	 * @param monome The MonomeConfiguration this page belongs to
	 * @param index The index of this page (the page number)
	 */
	public MachineDrumInterfacePage(MonomeConfiguration monome, int index) {
		this.machinedrum = new MachineDrum();
		this.monome = monome;
		this.index = index;
		this.generator = new Random();
		this.loadedModules = new ArrayList<LoadedModule>();
		this.loadedModules.add(new LoadedModule(new MDMPatternManager(this, 0), 0, 0));
		this.loadedModules.add(new LoadedModule(new MDMKitRandomizer(this, 1), 8, 0));
		this.loadedModules.add(new LoadedModule(new MDMKitEditor(this, 2), 0, 8));
		this.loadedModules.add(new LoadedModule(new MDMLFOManager(this, 3), 8, 8));
		gui = new MachineDrumInterfaceGUI(this);
		gui.mod1CB.setSelectedIndex(0);
		gui.mod2CB.setSelectedIndex(1);
		gui.mod3CB.setSelectedIndex(2);
		gui.mod4CB.setSelectedIndex(3);
        origGuiDimension = gui.getSize();
    }
	
    public Dimension getOrigGuiDimension() {
        return origGuiDimension;
    }

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getName()
	 */	
	public String getName() {		
		return pageName;
	}
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#setName()
	 */
	public void setName(String name) {
		this.pageName = name;
		this.gui.setName(name);
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handlePress(int, int, int)
	 */

	/**
	 * Translate monome x/y to a MachineDrum machine number
	 * 
	 * @param x The x coordinate on the monome
	 * @param y The y coordinate on the monome
	 * @return The MachineDrum machine number
	 */
	public int getMachineNum(int x, int y) {
		return (y * 8) + x;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		for (int i = 0; i < this.loadedModules.size(); i++) {
			loadedModules.get(i).module.handleReset();
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		if (!gui.syncCB.isSelected()) {
			return;
		}
		if (message instanceof ShortMessage) {
			ShortMessage shortMessage = (ShortMessage) message;
			switch (shortMessage.getCommand()) {
				case 0xF0:
						// midi clock message
					if (shortMessage.getChannel() == 0x08 ||
						// midi start message
						shortMessage.getChannel() == 0x0A ||
						// midi stop message
						shortMessage.getChannel() == 0x0C) {
							monome.sendMidi(shortMessage, index);
					}
					break;
				default:
					monome.sendMidi(shortMessage, index);
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String xml = "";
		xml += "      <name>Machine Drum Interface</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		xml += "      <speed>" + this.speed + "</speed>\n";
		String syncEnabled = "false";
		if (this.gui.syncCB.isSelected()) {
			syncEnabled = "true";
		}
		xml += "      <syncEnabled>" + syncEnabled + "</syncEnabled>\n";
		xml += "      <module1>" + gui.mod1CB.getSelectedIndex() + "</module1>\n";
		xml += "      <module2>" + gui.mod2CB.getSelectedIndex() + "</module2>\n";
		xml += "      <module3>" + gui.mod3CB.getSelectedIndex() + "</module3>\n";
		xml += "      <module4>" + gui.mod4CB.getSelectedIndex() + "</module4>\n";
		return xml;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getPanel()
	 */
	public JPanel getPanel() {
		return gui;
	}

	/**
	 * @param speed Sets the speed to send random parameter changes or auto morph, lower is faster
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
		this.gui.getSpeedTF().setText(String.valueOf(speed));
	}
	
	/**
	 * @param speed Sets the speed to send random parameter changes or auto morph, lower is faster
	 */
	public int getSpeed() {
		return this.speed;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getCacheDisabled()
	 */
	public boolean getCacheDisabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#destroyPage()
	 */
	public void destroyPage() {
		return;
	}

	public void setIndex(int index) {
		this.index = index;
		setName(this.pageName);
	}

	public void handleADC(int adcNum, float value) {
		// TODO Auto-generated method stub
		
	}
	
	public void handleADC(float x, float y) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isTiltPage() {
		// TODO Auto-generated method stub
		return false;
	}
	/*
	public ADCOptions getAdcOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAdcOptions(ADCOptions options)  {
		// TODO Auto-generated method stub
		
	}
	*/

	public void configure(Element pageElement) {
		this.setName(this.monome.readConfigValue(pageElement, "pageName"));
		this.setSpeed(Integer.parseInt(this.monome.readConfigValue(pageElement, "speed")));
		if (this.monome.readConfigValue(pageElement, "syncEnabled") != null) {
			if (this.monome.readConfigValue(pageElement, "syncEnabled").equalsIgnoreCase("true")) {
				gui.syncCB.setSelected(true);
			} else {
				gui.syncCB.setSelected(false);
			}
		}
		this.loadedModules = new ArrayList<LoadedModule>();
		for (int i = 0; i < 4; i++) {
			try {
				if (this.monome.readConfigValue(pageElement, "module" + (i + 1)) == null) {
					continue;
				}
				int option = Integer.parseInt(this.monome.readConfigValue(pageElement, "module" + (i + 1)));
				if (i == 0) {
					gui.mod1CB.setSelectedIndex(option);
				} else if (i == 1) {
					gui.mod2CB.setSelectedIndex(option);					
				} else if (i == 2) {
					gui.mod3CB.setSelectedIndex(option);					
				} else if (i == 3) {
					gui.mod4CB.setSelectedIndex(option);					
				}
				loadModule(option, i);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateModulePrefs() {
		this.loadedModules = new ArrayList<LoadedModule>();
		loadModule(gui.mod1CB.getSelectedIndex(), 0);
		loadModule(gui.mod2CB.getSelectedIndex(), 1);
		loadModule(gui.mod3CB.getSelectedIndex(), 2);
		loadModule(gui.mod4CB.getSelectedIndex(), 3);
		this.redrawDevice();
	}
	
	public void loadModule(int option, int moduleNumber) {
		int x = (moduleNumber % 2) * 8;
		int y = (moduleNumber / 2) * 8;
		if (option == 0) {
			this.loadedModules.add(new LoadedModule(new MDMPatternManager(this, moduleNumber), x, y));
		} else if (option == 1) {
			this.loadedModules.add(new LoadedModule(new MDMKitRandomizer(this, moduleNumber), x, y));			
		} else if (option == 2) {
			this.loadedModules.add(new LoadedModule(new MDMKitEditor(this, moduleNumber), x, y));			
		} else if (option == 3) {
			this.loadedModules.add(new LoadedModule(new MDMLFOManager(this, moduleNumber), x, y));			
		}
	}

	public int getIndex() {
		return index;
	}
	
	public void handleAbletonEvent() {
	}
	
	public void led(int x, int y, int value, int index) {
		LoadedModule module = loadedModules.get(index);
		this.monome.led(x + module.xOffset, y + module.yOffset, value, this.index);
	}
	
	public void redrawDevice() {
		for (int i = 0; i < this.loadedModules.size(); i++) {
			loadedModules.get(i).module.redrawMonome();
		}
	}

	public void handlePress(int x, int y, int value) {
		for (int i = 0; i < this.loadedModules.size(); i++) {
			LoadedModule lm = loadedModules.get(i);
			if (x >= lm.xOffset && y >= lm.yOffset &&
				x < lm.xOffset + 8 && y < lm.yOffset + 8) {
					MachineDrumModule module = lm.module;
					module.handlePress(x - lm.xOffset, y - lm.yOffset, value);			
			}
		}
	}
	
	public void handleTick(MidiDevice device) {
		if (!gui.syncCB.isSelected()) {
			return;
		}
		for (int i = 0; i < this.loadedModules.size(); i++) {
			loadedModules.get(i).module.handleTick();
		}
	}
	
	public class LoadedModule {
		MachineDrumModule module;
		int xOffset;
		int yOffset;
		
		public LoadedModule(MachineDrumModule module, int xOffset, int yOffset) {
			this.module = module;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
		}

		public MachineDrumModule getModule() {
			return module;
		}

		public void setModule(MachineDrumModule module) {
			this.module = module;
		}

		public int getXOffset() {
			return xOffset;
		}

		public void setXOffset(int offset) {
			xOffset = offset;
		}

		public int getYOffset() {
			return yOffset;
		}

		public void setYOffset(int offset) {
			yOffset = offset;
		}
	}
	
	public interface MachineDrumModule {
		public void handlePress(int x, int y, int value);	
		public void handleTick();
		public void handleReset();
		public void redrawMonome();
	}
	
	public class MDMLFOManager implements MachineDrumModule {
		MachineDrumInterfacePage page;
		int index;
		
		int lfo;
		int[] paramNum = new int[16];
		int[][] paramValue = new int[16][8];
		
		public MDMLFOManager(MachineDrumInterfacePage page, int index) {
			this.page = page;
			this.index = index;
			for (int i = 0; i < 16; i++) {
				paramValue[i][0] = i;
			}
		}

		public void handlePress(int x, int y, int value) {
			if (value == 1) {
				boolean sendLfoChange = false;
				if (y < 2) {
					this.lfo = x + (y * 8);
				} else if (y < 3) {
					this.paramNum[this.lfo] = x;
				} else {
					this.paramValue[this.lfo][this.paramNum[this.lfo]] = (x + ((y - 3) * 8));
					if (this.paramValue[this.lfo][this.paramNum[this.lfo]] > 127) {
						this.paramValue[this.lfo][this.paramNum[this.lfo]] = 127;
					}
					sendLfoChange = true;
				}
				
				if (sendLfoChange) {
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							int sendVal = paramValue[this.lfo][this.paramNum[this.lfo]];
							// track num 0-15
							if (paramNum[this.lfo] == 0) {
								sendVal = sendVal % 16;
							}
							// track param 0-23
							if (paramNum[this.lfo] == 1) {
								sendVal = sendVal % 24;
							}
							// waveshapes 0-5
							if (paramNum[this.lfo] == 2 || paramNum[this.lfo] == 3) {
								sendVal = sendVal % 6;
							}
							// update 0-2
							if (paramNum[this.lfo] == 4) {
								sendVal = sendVal % 3;
							}
							// speed, depth, shmix 0-127
							if (paramNum[this.lfo] > 4) {
								sendVal = (int)((double) sendVal * 3.2);
								if (sendVal > 127) {
									sendVal = 127;
								}
							}
							machinedrum.sendAssignLFO(recv, this.lfo, paramNum[this.lfo], sendVal);
						}
					}
				}
				this.redrawMonome();
			}
		}

		public void handleTick() {
		}

		public void redrawMonome() {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (y < 2) {
						int checkLfo = x + (y * 8);
						if (checkLfo == lfo) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y < 3) {
						if (x == this.paramNum[lfo]) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else {
						int checkVal = (x + ((y - 3) * 8));
						if (checkVal == this.paramValue[lfo][this.paramNum[lfo]]) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					}
				}
			}
		}

		public void handleReset() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class MDMKitEditor implements MachineDrumModule {
		MachineDrumInterfacePage page;
		int index;

		int curMachine;
		int curParam;
		int[][] paramValues = new int[16][24];
		int incDecVal = 1;
		
		public MDMKitEditor(MachineDrumInterfacePage page, int index) {
			this.page = page;
			this.index = index;
			for (int m = 0; m < 16; m++) {
				for (int p = 0; p < 24; p++) {
					paramValues[m][p] = 63;
				}
			}
		}

		public void handlePress(int x, int y, int value) {
			if (value == 1) {
				if (y < 2) {
					curMachine = x + (y * 8);
				} else if (y < 5) {
					curParam = x + ((y - 2) * 8);
				} else if (y == 5) {
					int sendVal = x * 18;
					if (sendVal > 127) {
						sendVal = 127;
					}
					paramValues[curMachine][curParam] = sendVal;
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.sendParamChange(recv, curMachine, curParam, sendVal);
						}
					}
				} else if (y == 6) {
					if (x < 4) {
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendKitLoad(recv, x);
								machinedrum.requestKit(recv, x);
							}
						}
					} else {
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendKitSave(recv, x - 4);
							}
						}
					}
				} else if (y == 7) {
					if (x == 0) {
						paramValues[curMachine][curParam] -= incDecVal;
						if (paramValues[curMachine][curParam] < 0) {
							paramValues[curMachine][curParam] = 0;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendParamChange(recv, curMachine, curParam, paramValues[curMachine][curParam]);
							}
						}
					} else if (x == 1) {
						paramValues[curMachine][curParam] += incDecVal;
						if (paramValues[curMachine][curParam] > 127) {
							paramValues[curMachine][curParam] = 127;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendParamChange(recv, curMachine, curParam, paramValues[curMachine][curParam]);
							}
						}
					} else if (x == 2) {
						if (incDecVal == 1) {
							incDecVal = 3;
						} else {
							incDecVal = 1;
						}
					} else if (x == 3) {
						paramValues[curMachine][curParam] = 63;
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendParamChange(recv, curMachine, curParam, paramValues[curMachine][curParam]);
							}
						}						
					} else if (x == 4) {
						String[] choices = machinedrum.getMachineChoices(curMachine);
						int choice = machinedrum.getMachine(choices[generator.nextInt(choices.length)]);
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendAssignMachine(recv, curMachine, choice);
							}
						}						
					} else if (x == 5) {
						int choice = machinedrum.getRandomMachineNumber();
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendAssignMachine(recv, curMachine, choice);
							}
						}						
					} else if (x == 6) {
						for (int p = 0; p < 24; p++) {
							int val = generator.nextInt(128);
							paramValues[curMachine][p] = val;
							String[] midiOutOptions = monome.getMidiOutOptions(page.index);
							for (int i = 0; i < midiOutOptions.length; i++) {
								if (midiOutOptions[i] == null) {
									continue;
								}
								Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
								if (recv != null) {
									machinedrum.sendParamChange(recv, curMachine, p, val);
								}
							}						
						}
					} else if (x == 7) {
						int val = generator.nextInt(128);
						paramValues[curMachine][curParam] = val;
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendParamChange(recv, curMachine, curParam, val);
							}
						}						
					}
				}
				redrawMonome();
			}
		}

		public void handleTick() {
			
		}

		public void redrawMonome() {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (y < 2) {
						int check = x + (y * 8);
						if (check == curMachine) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y < 5) {
						int check = x + ((y - 2) * 8);
						if (check == curParam) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y == 5) {
						int upTo = this.paramValues[curMachine][curParam] / 18;
						if (x <= upTo) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y == 7) {
						if (x == 2) {
							if (incDecVal == 3) {
								this.page.led(x, y, 1, index);
							} else {
								this.page.led(x, y, 0, index);
							}
						}
					} else {
						this.page.led(x, y, 0, index);
					}
				}
			}
		}

		public void handleReset() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class MDMPatternManager implements MachineDrumModule {
		MachineDrumInterfacePage page;
		int index;
		
		int patBank;
		int pattern;
		int songBank;
		int song;
		int extendedMode = 0;
		int songMode = 0;
		int global = 0;
		
		int seqOn = 0;
		int seqPos = 0;
		int[] seqPatterns = {0, 1, 2, 3, 4, 5, 6, 7};
		int[] muteState = new int[16];
		boolean playPatternNow = false;
		int tickNum = 0;
		
		public MDMPatternManager(MachineDrumInterfacePage page, int index) {
			this.page = page;
			this.index = index;
		}
		
		public void handlePress(int x, int y, int value) {
			if (value == 1) {
				if (y == 0) {
					patBank = x;
				} else if (y < 3) {
					pattern = x + ((y - 1) * 8);
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					this.seqPatterns[seqPos] = pattern;
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.setPattern(recv, (16 * patBank) + pattern);
						}
					}
				} else if (y == 3) {
					seqPos = x;
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.setPattern(recv, (16 * patBank) + this.seqPatterns[seqPos]);
						}
					}
					this.playPatternNow = true;
				} else if (y < 6) {
					int track = x + ((y - 4) * 8);
					if (this.muteState[track] == 0) {
						this.muteState[track] = 1;
					} else {
						this.muteState[track] = 0;
					}
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.setMute(recv, track, muteState[track]);
						}
					}
				} else if (y == 6) {
					song = x;
					int songNum = song * songBank;
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.setSong(recv, songNum);
						}
					}
				} else if (y == 7) {
					if (x < 4) {
						songBank = x;
					} else if (x == 4) {
						if (extendedMode == 0) {
							extendedMode = 1;
						} else {
							extendedMode = 0;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.setExtendedMode(recv, extendedMode);
							}
						}
					} else if (x == 5) {
						if (songMode == 0) {
							songMode = 1;
						} else {
							songMode = 0;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.setSongMode(recv, songMode);
							}
						}
					} else if (x == 6) {
						if (global == 0) {
							global = 1;
						} else {
							global = 0;
						}
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.setGlobal(recv, global);
							}
						}
					} else if (x == 7) {
						if (seqOn == 0) {
							seqOn = 1;
						} else {
							seqOn = 0;
						}
					}
				}
				redrawMonome();
			}
		}

		public void handleTick() {				
			if (tickNum == 0 || this.playPatternNow) {
				if (seqOn == 1) {
					pattern = this.seqPatterns[seqPos];
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							int nextSeqPos = seqPos + 1;
							if (nextSeqPos == 8) {
								nextSeqPos = 0;
							}
							machinedrum.setPattern(recv, this.seqPatterns[nextSeqPos] + (16 * patBank));
						}
					}
					redrawMonome();
				}
			}
			
			tickNum++;
			if (tickNum == 96) {
				tickNum = 0;
				seqPos++;
				if (seqPos > 7) {
					seqPos = 0;
				}
				redrawMonome();
			}
		}
		
		public void handleReset() {
			tickNum = 0;
			seqPos = 0;
			String[] midiOutOptions = monome.getMidiOutOptions(page.index);
			for (int i = 0; i < midiOutOptions.length; i++) {
				if (midiOutOptions[i] == null) {
					continue;
				}
				Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
				if (recv != null) {
					machinedrum.setPattern(recv, this.seqPatterns[0]);
				}
			}
			redrawMonome();
		}

		public void redrawMonome() {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					if (y == 0) {
						if (patBank == x) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y < 3) {
						if (pattern == x + ((y - 1) * 8)) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y == 3) {
						if (seqPos == x) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y < 6) {
						this.page.led(x, y, muteState[x + ((y - 4) * 8)], index);
					} else if (y == 6) {
						if (song == x) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					} else if (y == 7) {
						if (x == songBank) {
							this.page.led(x, y, 1, index);
						} else if (x == 4 && extendedMode == 1) {
							this.page.led(x, y, 1, index);
						} else if (x == 5 && songMode == 1) {
							this.page.led(x, y, 1, index);
						} else if (x == 6 && global == 1) {
							this.page.led(x, y, 1, index);
						} else if (x == 7 && seqOn == 1) {
							this.page.led(x, y, 1, index);
						} else {
							this.page.led(x, y, 0, index);
						}
					}
				}
			}
		}
		
	}
	
	public class MDMKitRandomizer implements MachineDrumModule {
		MachineDrumInterfacePage page;
		int index;
		
		int ticks;
		
		/**
		 * morph_machines[machine_number] - 1 if machine_number machine should be sent random parameter changes
		 */
		private int[] morph_machines = new int[16];

		/**
		 * morph_params[param_number] - 1 if the param_number paramater should be sent random changes 
		 */
		private int[] morph_params = new int[24];

		/**
		 * fx_morph[fx_number] - 1 if the fx_number fx unit should be sent random parameter changes, [0] = echo, [1] = gate, [2] = eq, [3] = compressor
		 */
		private int[] fx_morph = new int[4];

		/**
		 * true randomly enables and disables morph_machines and morph_params
		 */
		private boolean auto_morph = false;
		
		public MDMKitRandomizer(MachineDrumInterfacePage page, int index) {
			this.page = page;
			this.index = index;
		}

		public void handlePress(int x, int y, int value) {
			// only act on press events
			if (value == 1) {
				// top two rows, toggle morph_machines on and off
				if (y < 2) {
					int machine_num = getMachineNum(x, y);
					if (morph_machines[machine_num] == 1) {
						morph_machines[machine_num] = 0;
						this.page.led(x, y, 0, this.index);
					} else {
						morph_machines[machine_num] = 1;
						this.page.led(x, y, 1, this.index);
					}
					// next 3 rows, toggle morph_params on and off
				} else if (y < 5) {
					int param_num = getMachineNum(x, y - 2);
					if (morph_params[param_num] == 1) {
						morph_params[param_num] = 0;
						this.page.led(x, y, 0, this.index);
					} else {
						morph_params[param_num] = 1;
						this.page.led(x, y, 1, this.index);
					}
					// 6th row, initialize new kits
				} else if (y == 5) {
					String[] midiOutOptions = monome.getMidiOutOptions(page.index);
					for (int i = 0; i < midiOutOptions.length; i++) {
						if (midiOutOptions[i] == null) {
							continue;
						}
						Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
						if (recv != null) {
							machinedrum.initKit(recv, x);
						}
					}
					// 7th row, kit load and save
				} else if (y == 6) {
					if (x < 4) {
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendKitLoad(recv, x);
								machinedrum.requestKit(recv, x);
							}
						}
					} else {
						String[] midiOutOptions = monome.getMidiOutOptions(page.index);
						for (int i = 0; i < midiOutOptions.length; i++) {
							if (midiOutOptions[i] == null) {
								continue;
							}
							Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
							if (recv != null) {
								machinedrum.sendKitSave(recv, x - 4);
							}
						}
					}
					// last row, auto morph toggle and fx morph toggles
				} else if (y == 7) {
					if (x == 0) {
						if (auto_morph == false) {
							auto_morph = true;
							this.page.led(x, y, 1, this.index);
						} else {
							auto_morph = false;
							for (int i = 0; i < 16; i++) {
								morph_machines[i] = 0;
							}
							for (int i = 0; i < 24; i++) {
								morph_params[i] = 0;
							}
							this.redrawMonome();
						}
					} else if (x > 0 && x < 5) {
						if (fx_morph[x-1] == 0) {
							fx_morph[x-1] = 1;
						} else {
							fx_morph[x-1] = 0;
						}
						this.page.led(x, y, fx_morph[x-1], this.index);
					}
				}
			}
		}

		
		public void handleTick() {
			// count from 0 to 5 and reset
			if (ticks == 6) {
				ticks = 0;
			}

			// turn off and on random machines/params to morph
			if (auto_morph == true && generator.nextInt(page.speed) == 1) {
				int machine_num = generator.nextInt(12) + 2;
				int param_num = generator.nextInt(24);
				int x_m = machine_num % 8;
				int y_m = machine_num / 8;
				int x_p = param_num % 8;
				int y_p = (param_num / 8) + 2;

				if (morph_machines[machine_num] == 1) {
					morph_machines[machine_num] = 0;
					this.page.led(x_m, y_m, 0, this.index);
				} else {
					morph_machines[machine_num] = 1;
					this.page.led(x_m, y_m, 1, this.index);
				}
				if (morph_params[param_num] == 1) {
					morph_params[param_num] = 0;
					this.page.led(x_p, y_p, 0, this.index);
				} else {
					morph_params[param_num] = 1;
					this.page.led(x_p, y_p, 1, this.index);
				}
			}

			// send a param change to the echo effect
			if (fx_morph[0] == 1 && ticks == 0) {
				String[] midiOutOptions = monome.getMidiOutOptions(page.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						machinedrum.sendFxParam(recv, "echo", generator.nextInt(8), generator.nextInt(127));
					}
				}
			}

			// send a param change to the gate effect
			if (fx_morph[1] == 1 && ticks == 1) {
				String[] midiOutOptions = monome.getMidiOutOptions(page.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						machinedrum.sendFxParam(recv, "gate", generator.nextInt(8), generator.nextInt(127));
					}
				}
			}

			// send a param change to the eq effect
			if (fx_morph[2] == 1 && ticks == 2) {
				String[] midiOutOptions = monome.getMidiOutOptions(page.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						machinedrum.sendFxParam(recv, "eq", generator.nextInt(8), generator.nextInt(127));
					}
				}
			}

			// send a param change to the compressor effect
			if (fx_morph[3] == 1 && ticks == 3) {
				String[] midiOutOptions = monome.getMidiOutOptions(page.index);
				for (int i = 0; i < midiOutOptions.length; i++) {
					if (midiOutOptions[i] == null) {
						continue;
					}
					Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
					if (recv != null) {
						machinedrum.sendFxParam(recv, "compressor", generator.nextInt(8), generator.nextInt(127));
					}
				}
			}

			// send random parameter changes

			// for each machine
			for (int x = 0; x < 16; x++) {
				// divide out the sends so we don't saturate the midi channel
				if (ticks == 0 && (x >  2)) { continue; }
				else if (ticks == 1 && (x >  5 || x <  3)) { continue; }
				else if (ticks == 2 && (x >  8 || x <  6)) { continue; }
				else if (ticks == 3 && (x > 11 || x <  9)) { continue; }
				else if (ticks == 4 && (x > 14 || x < 12)) { continue; }
				else if (ticks == 5 && (x > 16 || x < 15)) { continue; }
				// for each morph parameter
				for (int y = 0; y < 24; y++) {
					// if the machine morph and the param morph are on and we pass a random check, send
					// a random param change
					if (morph_machines[x] == 1) {
						if (morph_params[y] == 1) {
							if (generator.nextInt(page.speed) == 1) {
								String[] midiOutOptions = monome.getMidiOutOptions(page.index);
								for (int i = 0; i < midiOutOptions.length; i++) {
									if (midiOutOptions[i] == null) {
										continue;
									}
									Receiver recv = monome.getMidiReceiver(midiOutOptions[i]);
									if (recv != null) {
										machinedrum.sendRandomParamChange(recv, x, y);
									}
								}
							}
						}
					}
				}
			}
			ticks++;
		}


		public void redrawMonome() {
			for (int x = 0; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					// redrawDevice the morph machine state (top 2 rows)
					if (y < 2) {
						int machine_num = getMachineNum(x, y);
						if (morph_machines[machine_num] == 1) {
							this.page.led(x, y, 1, this.index);
						} else {
							this.page.led(x, y, 0, this.index);
						}
						// redrawDevice the morph param state (next 3 rows)
					} else if (y < 5) {
						int param_num = getMachineNum(x, y - 2);
						if (morph_params[param_num] == 1) {
							this.page.led(x, y, 1, this.index);
						} else {
							this.page.led(x, y, 0, this.index);
						}
						// redrawDevice the bottom row (auto morph and fx toggles)
					} else if (y == 7) {
						if (x == 0) {
							if (auto_morph == true) {
								this.page.led(x, y, 1, this.index);
							} else {
								this.page.led(x, y, 0, this.index);
							}
						} else if (x > 0 && x < 5) {
							this.page.led(x, y, fx_morph[x-1], this.index);
						} else {
							this.page.led(x, y, 0, this.index);
						}
						// everything else should be off
					} else {
						this.page.led(x, y, 0, this.index);
					}
				}
			}
		}

		public void handleReset() {
			ticks = 0;
		}
		
	}

	public void onBlur() {
		// TODO Auto-generated method stub
		
	}
	
    public void handleRecordedPress(int x, int y, int val, int pattNum) {
        handlePress(x, y, val);
    }

	public void handleTilt(int n, int x, int y, int z) {
		// TODO Auto-generated method stub
		
	}


}
