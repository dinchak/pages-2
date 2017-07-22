package org.monome.pages.api;

public class Command {
    
    String cmd;
    Object param;

    public Command(String cmd, Object param) {
        this.cmd = cmd;
        this.param = param;
    }
    
    public Object getParam() {
        return param;
    }
    
    public Object getCmd() {
        return cmd;
    }
}
