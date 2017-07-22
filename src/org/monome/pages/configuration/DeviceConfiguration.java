package org.monome.pages.configuration;

import org.monome.pages.gui.DeviceFrame;
import org.monome.pages.pages.BasePage;
import org.monome.pages.pages.Page;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class DeviceConfiguration<TPage extends BasePage> implements Serializable {

    static final long serialVersionUID = 42L;
    /**
	 * The monome GUI window
	 */
	public transient DeviceFrame deviceFrame;
    /**
     * The pages that belong to this monome
     */
    public ArrayList<TPage> pages = new ArrayList<TPage>();
    /**
     * The number of pages this monome has
     */
    public int numPages = 0;
    /**
     * The currently selected page
     */
    public int curPage = 0;
    /**
     * Enabled MIDI In devices by page
     */
    public String[][] midiInDevices = new String[255][32];
    /**
     * Enabled MIDI In devices by page
     */
    public String[][] midiOutDevices = new String[255][32];
    
    /**
     * Rules on which MIDI note numbers should trigger switching to which pages.
     */
    public ArrayList<MIDIPageChangeRule> midiPageChangeRules;

    // Needed for Serializable
    public DeviceConfiguration() {
    }

    /**
     * Adds a new page to this monome
     *
     * @param className The class name of the page to add
     * @return The new Page object
     */
    public TPage addPage(String className) {
        TPage page;

        page = PagesRepository.getPageInstance(getPageType(), className, this, this.numPages);
        this.pages.add(this.numPages, page);

        onPageAdd(className, page);

        this.numPages++;
        if (this.deviceFrame != null) {
            this.deviceFrame.enableMidiMenu(true);
            String[] pageNames = new String[this.pages.size()];
            for (int i = 0; i < this.pages.size(); i++) {
                TPage tmpPage = this.pages.get(i);
                String pageName = tmpPage.getName();
                pageNames[i] = pageName;
            }
            this.deviceFrame.updateShowPageMenuItems(pageNames);
        }

        return page;
    }

    protected abstract void onPageAdd(String className, TPage page);

    protected abstract Class<? extends TPage> getPageType();

    /**
     * Deletes a page.
     *
     * @param i the index of the page to delete
     */
    public void deletePage(int i) {
        if (this.numPages == 0) {
            return;
        }
        this.pages.get(i).destroyPage();
        this.pages.remove(i);
        for (int x=0; x < this.pages.size(); x++) {
            this.pages.get(x).setIndex(x);
        }

        this.numPages--;
        this.curPage--;
        if (curPage <= 0) {
            curPage = 0;
        }
        if (this.numPages == 0) {
            if (this.deviceFrame != null) {
                this.deviceFrame.enableMidiMenu(false);
                deviceFrame.clearPage();
            }
        } else {
            switchPage(pages.get(curPage), curPage, true);
        }
        String[] pageNames = new String[this.pages.size()];
        for (int i1 = 0; i1 < this.pages.size(); i1++) {
            TPage tmpPage = this.pages.get(i1);
            String pageName = tmpPage.getName();
            pageNames[i1] = pageName;
        }
        if (this.deviceFrame != null) {
            this.deviceFrame.updateShowPageMenuItems(pageNames);
        }
    }

    /**
     * Switch pages on this monome.
     *
     * @param page The page to switch to
     * @param pageIndex The index of the page to switch to
     * @param redrawPanel true if the GUI panel should be redrawn
     */
    public void switchPage(TPage page, int pageIndex, boolean redrawPanel) {
        if (midiPageChangeRules != null) {
            for (int i = 0; i < midiPageChangeRules.size(); i++) {
                MIDIPageChangeRule mpcr = midiPageChangeRules.get(i);
                if (mpcr.getPageIndex() == curPage && mpcr.getLinkedSerial() != null) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + mpcr.getLinkedSerial());
                    if (arcConfig != null) {
                        int linkedPage = mpcr.getLinkedPageIndex();
                        if (arcConfig.pages.size() >= linkedPage) {
                            arcConfig.switchPage(linkedPage);
                        }
                    }
                }
            }
        }
        this.curPage = pageIndex;
        page.redrawDevice();
        if (deviceFrame != null) {
            deviceFrame.redrawPagePanel(page);
            deviceFrame.updateMidiInSelectedItems(this.midiInDevices[this.curPage]);
            deviceFrame.updateMidiOutSelectedItems(this.midiOutDevices[this.curPage]);
        }
    }

    public void switchPage(int pageIndex) {
        if (pages.size() <= pageIndex) {
            return;
        }
        TPage page = pages.get(pageIndex);
        this.curPage = pageIndex;
        page.redrawDevice();
        if (deviceFrame != null) {
            deviceFrame.redrawPagePanel(page);
            deviceFrame.updateMidiInSelectedItems(this.midiInDevices[this.curPage]);
            deviceFrame.updateMidiOutSelectedItems(this.midiOutDevices[this.curPage]);
        }
    }

    public void dispose() {
        if (deviceFrame != null) {
            deviceFrame.dispose();
        }
    }
    
    public String readConfigValue(Element pageElement, String name) {
        NodeList nameNL = pageElement.getElementsByTagName(name);
        Element el = (Element) nameNL.item(0);
        if (el != null) {
            NodeList nl = el.getChildNodes();
            String value = ((Node) nl.item(0)).getNodeValue();
            return value;           
        }
        return null;
    }
}
