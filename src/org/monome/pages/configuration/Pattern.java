package org.monome.pages.configuration;

import java.io.Serializable;
import java.util.ArrayList;

import org.monome.pages.pages.Page;

public class Pattern implements Serializable {
    static final long serialVersionUID = 42L;
	
	ArrayList<Press> presses = new ArrayList<Press>();
	ArrayList<Press> queuedPresses = new ArrayList<Press>();
	int patternNum;
	PatternBank bank;
	int lastPosition = -1;
	
	public Pattern(int patternNum, PatternBank bank) {
		this.patternNum = patternNum;
		this.bank = bank;
	}

	public void recordPress(int position, int x, int y, int value, int pageNum) {
		if (value == 0) {
			int newPosition = position;
			for (int i = 0; i < presses.size(); i++) {
				Press rPress = presses.get(i);
				int[] rp = rPress.getPress();
				if (rp[0] == x && rp[1] == y && rp[2] == 1) {
					int posOffset = rPress.getPosition() - rPress.getOrigPosition();
					newPosition += posOffset;
					if (newPosition >= bank.patternLengths[patternNum]) {
						newPosition = newPosition % bank.patternLengths[patternNum];
					}
					if (newPosition < 0) {
						newPosition += bank.patternLengths[patternNum];
					}
				}
			}
		    this.presses.add(new Press(newPosition, position, x, y, value, patternNum, pageNum));
		} else {
			if (bank.getQuantization() > 1) {
				for (int quantPos = 0; quantPos <= bank.patternLengths[patternNum]; quantPos += bank.getQuantization()) {
					if (Math.abs(position - quantPos) <= bank.getQuantization() / 2) {
						lastPosition = quantPos % bank.patternLengths[patternNum];
					}
				}
			} else {
				lastPosition = position;
			}
		    this.presses.add(new Press(lastPosition, position, x, y, value, patternNum, pageNum));
		}
	}

	public ArrayList<Press> getRecordedPress(int position) {
		ArrayList<Press> returnPresses = null;
		if (this.presses.size() > 0) {
			returnPresses = new ArrayList<Press>();
			for (int i=0; i < this.presses.size(); i++) {
				presses.get(i).seenTicks++;
				if (presses.get(i).seenTicks <= bank.getQuantization()) continue;
				if (this.presses.get(i).getPosition() % bank.patternLengths[patternNum] == position) {
					returnPresses.add(this.presses.get(i));
				}
			}
		}

		return returnPresses;
	}

	public void clearPattern() {
		this.presses = new ArrayList<Press>();
		for (int i = 0; i < MonomeConfigurationFactory.getNumMonomeConfigurations(); i++) {
		    MonomeConfiguration monome = MonomeConfigurationFactory.getMonomeConfiguration(i);
		    if (monome == null) {
		    	continue;
		    }
		    for (int j = 0; j < monome.pressesInPlayback.size(); j++) {
		        Press press = monome.pressesInPlayback.get(j);
	            Page page = monome.pages.get(monome.curPage);
	            int[] iPress = press.getPress();
            	page.handleRecordedPress(iPress[0], iPress[1], 0, press.getPatternNum());
		    }
	        monome.pressesInPlayback = new ArrayList<Press>();
		}
	}

}
