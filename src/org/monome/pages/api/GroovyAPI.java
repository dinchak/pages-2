package org.monome.pages.api;

import org.monome.pages.Main;
import org.monome.pages.ableton.AbletonControl;
import org.monome.pages.ableton.AbletonState;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.configuration.OSCPortFactory;
import org.monome.pages.configuration.PatternBank;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;	

public class GroovyAPI implements GroovyPageInterface {
    int pageIndex;

    MonomeConfiguration monome;
    int sizeX;
    int sizeY;
    
    ArcConfiguration arc;
    int knobs;
    
    private GroovyErrorLog errorLog;
    
    public void setMonome(MonomeConfiguration monome) {
        this.monome = monome;
        this.sizeX = monome.sizeX;
        this.sizeY = monome.sizeY;
    }
    
    public void setArc(ArcConfiguration arc) {
        this.arc = arc;
        this.knobs = arc.knobs;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void led(ArrayList<Integer> args) {
        if (monome == null) return;
        if (args.size() != 3) return;
        monome.led(args.get(0), args.get(1), args.get(2), pageIndex);
    }

    public void led(int x, int y, int val) {
        if (monome == null) return;
        monome.led(x, y, val, pageIndex);
    }

    public void row(int row, ArrayList<Integer> rows) {
        if (monome == null) return;
        ArrayList<Integer> args = new ArrayList<Integer>();
        args.add(row);
        for (int i = 0; i < rows.size(); i++) {
            args.add(rows.get(i));
        }
        monome.led_row(args, pageIndex);
    }

    public void row(int row, int val1, int val2) {
        if (monome == null) return;
        ArrayList<Integer> args = new ArrayList<Integer>();
        args.add(row);
        args.add(val1);
        args.add(val2);
        monome.led_row(args, pageIndex);
    }

    public void col(int col, ArrayList<Integer> cols) {
        if (monome == null) return;
        ArrayList<Integer> args = new ArrayList<Integer>();
        args.add(col);
        for (int i = 0; i < cols.size(); i++) {
            args.add(cols.get(i));
        }
        monome.led_col(args, pageIndex);
    }

    public void col(int col, int val1, int val2) {
        if (monome == null) return;
        ArrayList<Integer> args = new ArrayList<Integer>();
        args.add(col);
        args.add(val1);
        args.add(val2);
        monome.led_col(args, pageIndex);
    }
    
    public void set(int enc, int led, int level) {
        if (arc == null) return;
        arc.set(enc, led, level, pageIndex);
    }
    
    public void all(int enc, int level) {
        if (arc == null) return;
        arc.all(enc, level, pageIndex);
    }
    
    public void map(int enc, Integer[] levels) {
        if (arc == null) return;
        arc.map(enc, levels, pageIndex);
    }

    public void range(int enc, int x1, int x2, int level) {
        if (arc == null) return;
        arc.range(enc, x1, x2, level, pageIndex);
    }
    
    public void clear(int state) {
        if (monome != null) monome.clear(state, pageIndex);
        if (arc != null) arc.clearArc(pageIndex);
    }

    public void sendOSC(String addr, Object[] args, String host, int port) {
        OSCMessage msg = new OSCMessage();
        msg.setAddress(addr); 
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                msg.addArgument(args[i]);
            }
        }
        OSCPortOut portOut = OSCPortFactory.getInstance().getOSCPortOut(host, port); 
        if (portOut != null) {
            try {
				portOut.send(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    public void noteOut(int num, int velo, int chan, int on) {
        ShortMessage msg = new ShortMessage();
        int cmd = ShortMessage.NOTE_OFF;
        if (on == 1) {
            cmd = ShortMessage.NOTE_ON;
        }
        try {
			msg.setMessage(cmd, chan, num, velo);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		if (monome != null)
		    monome.sendMidi(msg, pageIndex);
        if (arc != null)
            arc.sendMidi(msg, pageIndex);
    }

    public void ccOut(int num, int val, int chan) {
        ShortMessage msg = new ShortMessage();
        try {
			msg.setMessage(ShortMessage.CONTROL_CHANGE, chan, num, val);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
		if (monome != null)
		    monome.sendMidi(msg, pageIndex);
		if (arc != null)
            arc.sendMidi(msg, pageIndex);
    }
    
    public void programChange(int num, int val, int chan) {
        ShortMessage msg = new ShortMessage();
        try {
            msg.setMessage(ShortMessage.PROGRAM_CHANGE, chan, num, val);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        if (monome != null)
            monome.sendMidi(msg, pageIndex);
        if (arc != null)
            arc.sendMidi(msg, pageIndex);
    }

    public void clockOut() {
        ShortMessage msg = new ShortMessage();
        try {
			msg.setMessage(0XF8);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
        if (monome != null)
            monome.sendMidi(msg, pageIndex);
        if (arc != null)
            arc.sendMidi(msg, pageIndex);
    }

    public void clockResetOut() {
        ShortMessage msg = new ShortMessage();
        try {
			msg.setMessage(0xFC);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
        if (monome != null)
            monome.sendMidi(msg, pageIndex);
        if (arc != null)
            arc.sendMidi(msg, pageIndex);
    }

	public void init() {
	}

	public void press(int x, int y, int val) {
	}
	
	public void delta(int enc, int delta) {
	}
	
	public void key(int enc, int value) {
	}
	
	public void redraw() {
	}
	
	public void note(int num, int velo, int chan, int on) {
	}
	
	public void cc(int num, int val, int chan) {
	}
	
	public void clock() {
	}
	
	public void clockReset() {
	}
	
	public MonomeConfiguration monome() {
		return monome;
	}
	
	public int sizeX() {
		return monome.sizeX;
	}
	
	public int sizeY() {
		return monome.sizeY;
	}
	
	public ArcConfiguration arc() {
	    return arc;
	}
	
	public int knobs() {
	    return knobs;
	}
	
	public void log(String message) {
	    errorLog.addError(message + "\n");
	}
	
	public AbletonState ableton() {
		return Main.main.configuration.getAbletonState();
	}
	
	public AbletonControl abletonOut() {
		return Main.main.configuration.getAbletonControl();
	}
	
	public void handleAbletonEvent() {
		return;
	}
	
    public void stop() {
    }
    
    public void setLogger(GroovyErrorLog errorLog) {
        this.errorLog = errorLog;
    }
    
    public int pageIndex() {
        return pageIndex;
    }
    
    public PatternBank patterns() {
        return monome().patternBanks.get(pageIndex);
    }
    
    public void sendCommand(Command command) {
    }

    public void recordedPress(int x, int y, int val, int pattNum) {
        press(x, y, val);
    }

	public void tilt(int n, int x, int y, int z) {
	}
}