package org.monome.pages.pages;

import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;

public interface BasePage {
    /**
     * Returns the name of the page.
     *
     * @return The name of the page
     * @param optional: "type" will return page type instead of name
     */
    String getName();

    /**
	 * Sets the name of the page.
	 */
    void setName(String name);

    void setIndex(int index);

    int getIndex();

    /**
     * Should handle any cleanup needed when the page is destroyed (close open OSC ports, etc.)
     */
    void destroyPage();

    /**
     * Configure this page instance from the configuration file
     * @param pageEl
     */
    void configure(Element pageElement);

    JPanel getPanel();

    void handleAbletonEvent();

    Dimension getOrigGuiDimension();

    void onBlur();

    /**
     * Called whenever the monome needs to be redrawn from the current page state.  Should
     * turn on or off every LED on the monome, even if the button is unused.
     */
    void redrawDevice();
}
