package org.monome.pages.pages;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonClip;
import org.monome.pages.ableton.AbletonTrack;
import org.monome.pages.configuration.FakeMonomeConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.pages.gui.AbletonClipLauncherGUI;
import org.w3c.dom.Element;

/**
 * The Ableton Clip Launcher page.  Usage information is available at:
 * 
 * http://code.google.com/p/monome-pages/wiki/AbletonClipLauncherPage
 *   
 * @author Tom Dinchak
 *
 */
public class AbletonClipLauncherPage implements Page, Serializable {
    static final long serialVersionUID = 42L;

	/**
	 * Reference to the MonomeConfiguration this page belongs to.
	 */
	MonomeConfiguration monome;

	/**
	 * This page's index (page number).
	 */
	private int index;
			
	/**
	 * The amount to offset the monome display of the clips
	 */
	private int clipOffset = 0;

	/**
	 * The amount to offset the monome display of the tracks
	 */
	private int trackOffset;
	
	private AbletonClipLauncherGUI gui;
	private int tickNum;

	/**
	 * The number of control rows (track arm, track stop) that are enabled currently
	 */
	public int numEnabledRows = 4;
	
	/**
	 * The name of the page 
	 */
	private String pageName = "Ableton Clip Launcher";

    private Dimension origGuiDimension;

	/**
	 * @param monome The MonomeConfiguration this page belongs to
	 * @param index This page's index number
	 */
	public AbletonClipLauncherPage(MonomeConfiguration monome, int index) {
		this.monome = monome;
		this.index = index;
		gui = new AbletonClipLauncherGUI(this);
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
	public void handlePress(int x, int y, int value) {
		// only on button was pressed events
		if (value == 1) {
			// if this is the far right column then do special actions
			if (x == this.monome.sizeX - 1) {
				// minus 1 clip offset
				if (y == 0) {
					if (this.clipOffset > 0) {
						this.clipOffset -= 1;
						redrawDevice();
					}
				// plus 1 clip offset
				} else if (y == 1) {
					if ((this.clipOffset + 1) * (this.monome.sizeY - this.numEnabledRows) < 960) {
						this.clipOffset += 1;
						redrawDevice();
					}
				// minus 1 track offset
				} else if (y == 2) {
					if (this.trackOffset > 0) {
						this.trackOffset -= 1;
						redrawDevice();
					}
				// plus 1 track offset
				} else if (y == 3) {
					if ((this.trackOffset + 1) * (this.monome.sizeX - 1) < 100) {
						this.trackOffset += 1;
						redrawDevice();
					}
				} else if (y == 4) {
					this.tempoDown();
				} else if (y == 5) {
					this.tempoUp();
				} else if (y == 6) {
					if (Main.main.configuration.abletonState.getOverdub() == 1) {
						this.abletonOverdub(0);
					} else {
						this.abletonOverdub(1);
					}					
				} else if (y == 7) {
//					this.abletonUndo();
				}
			} else {
				// if this is the bottom row then arm/disarm track number x
				if (y == this.monome.sizeY - 1 && this.gui.getDisableArmCB().isSelected() == false) {
					int track_num = x + (this.trackOffset * (this.monome.sizeX - 1));
					AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
					if (track != null) {
						if (track.getArm() == 0) {
							this.armTrack(track_num);
						} else {
							this.disarmTrack(track_num);
						}
					}
				}
				// if this is the 2nd from the bottom row then solo/unsolo
				else if ((y == this.monome.sizeY - 2 && this.gui.getDisableSoloCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == false) ||
						  y == this.monome.sizeY - 1 && this.gui.getDisableSoloCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == true) {
					int track_num = x + (this.trackOffset * (this.monome.sizeX - 1));
					AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
					if (track != null) {
						if (track.getSolo() == 0) {
							this.soloTrack(track_num);
						} else {
							this.unsoloTrack(track_num);
						}
						this.viewTrack(track_num);
					}
				}
				// if this is the 3rd from the bottom row then mute or unmute the track
				else if ((y == this.monome.sizeY - 3 && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == false) ||
						 (y == this.monome.sizeY - 2 && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == true) ||
						 (y == this.monome.sizeY - 2 && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == false) ||
                         (y == this.monome.sizeY - 1 && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == true)) {
					int track_num = x + (this.trackOffset * (this.monome.sizeX - 1));
					AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
					if (track != null) {
						if (track.getMute() == 0) {
							this.muteTrack(track_num);
						} else {
							this.unmuteTrack(track_num);
						}
					}
					this.viewTrack(track_num);
				}
				
				// if this is the 4th from the bottom row then mute or unmute the track
				else if (
						 (y == this.monome.sizeY - 4 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == false) ||
						 
						 (y == this.monome.sizeY - 3 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == true && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == false) ||
						 (y == this.monome.sizeY - 3 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == false) ||
						 (y == this.monome.sizeY - 3 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == true) ||
						 
						 (y == this.monome.sizeY - 2 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == true && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == false) ||
						 (y == this.monome.sizeY - 2 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == false && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == true) ||
						 (y == this.monome.sizeY - 2 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == true && this.gui.getDisableArmCB().isSelected() == false && this.gui.getDisableSoloCB().isSelected() == true) ||
						 
                         (y == this.monome.sizeY - 1 && this.gui.getDisableStopCB().isSelected() == false && this.gui.getDisableMuteCB().isSelected() == true && this.gui.getDisableArmCB().isSelected() == true && this.gui.getDisableSoloCB().isSelected() == true)) {
					int track_num = x + (this.trackOffset * (this.monome.sizeX - 1));
					this.stopTrack(track_num);
					this.viewTrack(track_num);
				}
				// otherwise play the clip
				else {
					int clip_num = y + (this.clipOffset * (this.monome.sizeY - this.numEnabledRows));
					int track_num = x + (this.trackOffset * (this.monome.sizeX - 1));
					this.viewTrack(track_num);
					this.playClip(track_num, clip_num);
				}
			}
		}
	}

	/**
	 * Sends "/live/play/clip track clip" to LiveOSC.
	 * 
	 * @param track The track number to play (0 = first track)
	 * @param clip The clip number to play (0 = first clip)
	 */
	public void playClip(int track, int clip) {
		Main.main.configuration.getAbletonControl().playClip(track, clip);
	}
	
	public void stopClip(int track, int clip) {
		Main.main.configuration.getAbletonControl().stopClip(track, clip);
	}

	/**
	 * Sends "/live/arm track" to LiveOSC.
	 * 
	 * @param track The track number to arm (0 = first track)
	 */
	public void armTrack(int track) {
		Main.main.configuration.getAbletonControl().armTrack(track);
	}

	/**
	 * Sends "/live/redo" to LiveOSC. 
	 */
	public void abletonRedo() {
		Main.main.configuration.getAbletonControl().redo();
	}
	
	public void abletonOverdub(int overdub) {
		Main.main.configuration.getAbletonControl().setOverdub(overdub);
	}
	
	/**
	 * Sends "/live/undo" to LiveOSC. 
	 */
	public void abletonUndo() {
		Main.main.configuration.getAbletonControl().undo();
	}
	
	/**
	 * Sends "/live/tempo tempo-1" to LiveOSC. 
	 */
	public void tempoDown() {
		Main.main.configuration.getAbletonControl().setTempo(Main.main.configuration.abletonState.getTempo() - 1.0f);
	}
	
	/**
	 * Sends "/live/tempo tempo+1" to LiveOSC. 
	 */
	public void tempoUp() {
		Main.main.configuration.getAbletonControl().setTempo(Main.main.configuration.abletonState.getTempo() + 1.0f);
	}
	
	/**
	 * Sends "/live/disarm track" to LiveOSC.
	 * 
	 * @param track The track number to disarm (0 = first track)
	 */
	public void disarmTrack(int track) {
		Main.main.configuration.getAbletonControl().disarmTrack(track);
	}

	/**
	 * Sends "/live/stop/track track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void stopTrack(int track) {
		Main.main.configuration.getAbletonControl().stopTrack(track);
	}

	/**
	 * Sends "/live/track/view track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void viewTrack(int track) {
		Main.main.configuration.getAbletonControl().viewTrack(track);
	}
	
	/**
	 * Sends "/live/track/mute track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void muteTrack(int track) {
		Main.main.configuration.getAbletonControl().muteTrack(track);
	}
	
	public void soloTrack(int track) {
		Main.main.configuration.getAbletonControl().soloTrack(track);
	}
	
	public void unsoloTrack(int track) {
		Main.main.configuration.getAbletonControl().unsoloTrack(track);
	}
	
	/**
	 * Sends "/live/track/mute track" to LiveOSC.
	 * 
	 * @param track The track number to stop (0 = first track)
	 */
	public void unmuteTrack(int track) {
		Main.main.configuration.getAbletonControl().unmuteTrack(track);
	}
	
	public void refreshAbleton() {
		Main.main.configuration.getAbletonControl().refreshAbleton();
	}
	
	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleReset()
	 */
	public void handleReset() {
		tickNum = 0; 
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#handleTick()
	 */
	public void handleTick(MidiDevice device) {
		tickNum++;
		if (tickNum == 96) {
			tickNum = 0;
		}
		
		// iterate over the monome, adjust for the current offset, check
		// track state and flash if appropriate
		for (int x = 0; x < this.monome.sizeX; x++) {
			int trackNum = x + (this.trackOffset * (this.monome.sizeX - 1));
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackNum);
			if (track != null) {
				for (int y = 0; y < this.monome.sizeY - numEnabledRows; y++) {
					int clipNum = y + (this.clipOffset * (this.monome.sizeY - this.numEnabledRows));
					AbletonClip clip = track.getClip(clipNum);
					if (clip != null) {
						if (clip.getState() == AbletonClip.STATE_PLAYING) {
							if (tickNum % 24 == 0) {
								this.monome.led(x, y, 1, this.index);
							}
							if ((tickNum + 12) % 24 == 0) {
								this.monome.led(x, y, 0, this.index);
							}
						}
						if (clip.getState() == AbletonClip.STATE_TRIGGERED) {
							if (tickNum % 12 == 0) {
								this.monome.led(x, y, 1, this.index);
							}
							if ((tickNum + 6) % 12 == 0) {
								this.monome.led(x, y, 0, this.index);
							}
						}
					}
				}
			}
		}
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#redrawMonome()
	 */
	public void redrawDevice() {
		// redrawDevice the upper part of the monome (the clip state)
		for (int x = 0; x < this.monome.sizeX - 1; x++) {
			int trackNum = x + (this.trackOffset * (this.monome.sizeX - 1));
			AbletonTrack track = Main.main.configuration.abletonState.getTrack(trackNum);
			if (track != null) {
				for (int y = 0; y < (this.monome.sizeY - this.numEnabledRows); y++) {
					int clipNum = y + (this.clipOffset * (this.monome.sizeY - this.numEnabledRows));
					AbletonClip clip = track.getClip(clipNum);
					if (clip != null) {
						if (clip.getState() == AbletonClip.STATE_STOPPED) {
							this.monome.led(x, y, 1, this.index);
						} else if (clip.getState() == AbletonClip.STATE_EMPTY) {
							this.monome.led(x, y, 0, this.index);
						// clip is playing/triggered but clock is not running
						} else if (tickNum == 0) {
							this.monome.led(x, y, 1, this.index);
						}
					} else {
						this.monome.led(x, y, 0, this.index);
					}
				}
			} else {
				ArrayList<Integer> colParams = new ArrayList<Integer>();
				colParams.add(x);
				colParams.add(0);
				colParams.add(0);
				this.monome.led_col(colParams, this.index);
			}
		}
		
		// redrawDevice the track armed/disarmed state
		if (this.gui.getDisableArmCB().isSelected() == false) {
			for (int i = 0; i < this.monome.sizeX - 1; i++) {
				int track_num = i + (this.trackOffset * (this.monome.sizeX - 1));
				int yRow = this.monome.sizeY - 1;
				AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
				if (track != null) {
					if (track.getArm() == 1) {
						this.monome.led(i, yRow, 1, this.index);
					} else {
						this.monome.led(i, yRow, 0, this.index);
					}
				} else {
					this.monome.led(i, yRow, 0, this.index);
				}
			}
		}
		
		// redrawDevice the track solo/unsolo state
		if (this.gui.getDisableSoloCB().isSelected() == false) {
			for (int i = 0; i < this.monome.sizeX - 1; i++) {
				int track_num = i + (this.trackOffset * (this.monome.sizeX - 1));
				int yRow;
				if (gui.getDisableArmCB().isSelected() == false) {
					yRow = this.monome.sizeY - 2;
				} else {
					yRow = this.monome.sizeY - 1;
				}
				AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
				if (track != null) {
					if (track.getSolo() == 1) {
						this.monome.led(i, yRow, 1, this.index);
					} else {
						this.monome.led(i, yRow, 0, this.index);
					}
				} else {
					this.monome.led(i, yRow, 0, this.index);
				}
			}
		}

		// redrawDevice the track mute/unmute state
		if (this.gui.getDisableMuteCB().isSelected() == false) {
			for (int i = 0; i < this.monome.sizeX - 1; i++) {
				int track_num = i + (this.trackOffset * (this.monome.sizeX - 1));
				int yRow;
				if (gui.getDisableArmCB().isSelected() == true && gui.getDisableSoloCB().isSelected() == true) {
					yRow = this.monome.sizeY - 1;
				} else if (gui.getDisableArmCB().isSelected() == true || gui.getDisableSoloCB().isSelected() == true){
					yRow = this.monome.sizeY - 2;
				} else {
					yRow = this.monome.sizeY - 3;
				}
				AbletonTrack track = Main.main.configuration.abletonState.getTrack(track_num);
				if (track != null) {
					if (track.getMute() == 1) {
						this.monome.led(i, yRow, 0, this.index);
					} else {
						this.monome.led(i, yRow, 1, this.index);
					}
				} else {
					this.monome.led(i, yRow, 0, this.index);
				}
			}
		}
		
		// clear out stop buttons
		if (this.gui.getDisableStopCB().isSelected() == false) {
			for (int i = 0; i < this.monome.sizeX - 1; i++) {
				int yRow;
				yRow = this.monome.sizeY - this.numEnabledRows;
				this.monome.led(i, yRow, 0, this.index);				
			}
		}

		for (int y = 0; y < this.monome.sizeX; y++) {
			if (y == 6 && Main.main.configuration.abletonState.getOverdub() == 1) {
				this.monome.led(this.monome.sizeX - 1, y, 1, this.index);
			} else {
				this.monome.led(this.monome.sizeX - 1, y, 0, this.index);
			}
		}
		
		// set red ring
		if (this.monome.curPage == this.index) {
			int widthOffset = this.trackOffset * (this.monome.sizeX - 1);
			int clipOffset = (this.clipOffset * (this.monome.sizeY - this.numEnabledRows));
			int width = this.monome.sizeX - 1;
			int height = this.monome.sizeY - this.numEnabledRows;
			Main.main.configuration.getAbletonControl().setSelection(widthOffset, clipOffset, width, height);
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#send(javax.sound.midi.MidiMessage, long)
	 */
	public void send(MidiMessage message, long timeStamp) {
		return;
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#toXml()
	 */
	public String toXml() {
		String disableArm = "false";
		String disableSolo = "false";
		String disableMute = "false";
		String disableStop = "false";
		if (gui.getDisableArmCB().isSelected() == true) {
			disableArm = "true";
		}
		
		if (gui.getDisableSoloCB().isSelected() == true) {
			disableSolo = "true";
		}
		
		if (gui.getDisableMuteCB().isSelected() == true) {
			disableMute = "true";
		}
		
		if (gui.getDisableStopCB().isSelected() == true) {
			disableStop = "true";
		}
		
		String xml = "";
		xml += "      <name>Ableton Clip Launcher</name>\n";
		xml += "      <pageName>" + this.pageName + "</pageName>\n";
		
		xml += "      <disablearm>" + disableArm + "</disablearm>\n";
		xml += "      <disablesolo>" + disableSolo + "</disablesolo>\n";
		xml += "      <disablemute>" + disableMute + "</disablemute>\n";
		xml += "      <disablestop>" + disableStop + "</disablestop>\n";
		return xml;
	}
	
	public void setDisableArm(String disableArm) {
		if (disableArm.equals("true")) {
			this.gui.getDisableArmCB().doClick();
		}
	}
	
	public void setDisableSolo(String disableSolo) {
		if (disableSolo.equals("true")) {
			this.gui.getDisableSoloCB().doClick();
		}
	}
	
	public void setDisableMute(String disableMute) {
		if (disableMute.equals("true")) {
			this.gui.getDisableMuteCB().doClick();
		}
	}
	
	public void setDisableStop(String disableStop) {
		if (disableStop.equals("true")) {
			this.gui.getDisableStopCB().doClick();
		}
	}

	/* (non-Javadoc)
	 * @see org.monome.pages.Page#getCacheEnabled()
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
		this.setDisableArm(this.monome.readConfigValue(pageElement, "disablearm"));
		this.setDisableSolo(this.monome.readConfigValue(pageElement, "disablesolo"));
		this.setDisableMute(this.monome.readConfigValue(pageElement, "disablemute"));
		this.setDisableStop(this.monome.readConfigValue(pageElement, "disablestop"));		
	}

	public JPanel getPanel() {
		return gui;
	}

	public int getIndex() {
		return index;
	}

	public void handleAbletonEvent() {
		redrawDevice();
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