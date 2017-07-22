package org.monome.pages.configuration;

import java.util.Date;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

public class ArcOSCListener implements OSCListener {
    
    ArcConfiguration arcConfig;
    
    public ArcOSCListener(ArcConfiguration arcConfig) {
        this.arcConfig = arcConfig;
    }
    
    public synchronized void acceptMessage(Date time, OSCMessage message) {
        Object[] args = message.getArguments();
        /*
        System.out.println("received " + message.getAddress() + " msg");
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i].getClass().toString());
            if (args[i] instanceof Integer) {
                int val = ((Integer) args[i]).intValue();
                System.out.println("val=" + val);
            }
            if (args[i] instanceof String) {
                String val = (String) args[i];
                System.out.println("val=" + val);
            }
        }
        */
        
        if (message.getAddress().contains("/enc/delta")) {
            if (args.length == 2) {
                int enc = ((Integer) args[0]).intValue();
                int delta = ((Integer) args[1]).intValue();
                arcConfig.handleDelta(enc, delta);
            }
        }
        
        if (message.getAddress().contains("/enc/key")) {
            if (args.length == 2) {
                int enc = ((Integer) args[0]).intValue();
                int value = ((Integer) args[1]).intValue();
                arcConfig.handleKey(enc, value);
            }
        }

    }
}
