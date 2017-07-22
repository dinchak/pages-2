package org.monome.pages.api;

import org.monome.pages.pages.gui.GroovyGUI;

public class GroovyErrorLog {
    
    StringBuffer errors;
    GroovyGUI gui;
    org.monome.pages.pages.arc.gui.GroovyGUI arcGui;
    
    public GroovyErrorLog(GroovyGUI gui) {
        errors = new StringBuffer();
        this.gui = gui;
    }
    
    public GroovyErrorLog(org.monome.pages.pages.arc.gui.GroovyGUI gui) {
        errors = new StringBuffer();
        this.arcGui = gui;
    }
    
    public void addError(String message) {
        errors.append(message);
        if (gui != null) {
            if (gui.errorWindow != null) {
                gui.errorWindow.appendErrorText(message);
            }
        }
        if (arcGui != null) {
            if (arcGui.errorWindow != null) {
                arcGui.errorWindow.appendErrorText(message);
            }
        }
    }
    
    public StringBuffer getErrors() {
        return errors;
    }

}