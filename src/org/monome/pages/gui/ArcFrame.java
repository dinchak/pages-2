package org.monome.pages.gui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.monome.pages.Main;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.PagesRepository;
import org.monome.pages.pages.ArcPage;
import org.monome.pages.pages.BasePage;
import org.monome.pages.pages.Page;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.Serializable;

public class ArcFrame extends JInternalFrame implements Serializable, DeviceFrame {
    static final long serialVersionUID = 42L;

    private JPanel jContentPane = null;
    private JMenuBar arcMenuBar = null;
    private JMenu pageMenu = null;  //  @jve:decl-index=0:visual-constraint="365,110"
    private JMenuItem newPageItem = null;
    private JMenu configurationMenu = null;
    private JPanel currentPanel = null;
    public int index = 0;
    private JMenu midiMenu = null;
    private JMenu midiInMenu = null;
    private JMenu midiOutMenu = null;
    private JMenu pageChangeMidiInMenu = null;
    private JMenuItem noInputDevicesEnabledItem;
    private JMenuItem noInputDevicesEnabledItem2;
    private JMenuItem noOutputDevicesEnabledItem;
    private JMenuItem prevPageItem = null;
    private JMenuItem nextPageItem = null;
    private JMenuItem deletePageItem = null;
    private JMenuItem renamePageItem = null;
    private JMenuItem pageChangeConfigurationItem = null;
    private JMenuItem removeArcItem = null;
    private PageChangeConfigurationFrame pccFrame = null;
    private JMenu showPageMenu = null;
    /**
     * This is the xxx default constructor
     */
    public ArcFrame(int index) {
        super();
        this.index = index;
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    void initialize() {
        this.setSize(300, 200);
        this.setJMenuBar(getMonomeMenuBar());
        this.setContentPane(getJContentPane());
        this.setResizable(true);
        this.setVisible(true);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    public JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
        }
        return jContentPane;
    }

    /**
     * This method initializes monomeMenuBar    
     *  
     * @return javax.swing.JMenuBar 
     */
    private JMenuBar getMonomeMenuBar() {
        if (arcMenuBar == null) {
            arcMenuBar = new JMenuBar();
            arcMenuBar.add(getPageMenu());
            arcMenuBar.add(getConfigurationMenu());
            arcMenuBar.add(getMidiMenu());
        }
        return arcMenuBar;
    }

    /**
     * This method initializes pageMenu 
     *  
     * @return javax.swing.JMenu    
     */
    private JMenu getPageMenu() {
        if (pageMenu == null) {
            pageMenu = new JMenu();
            pageMenu.setText("Page");
            pageMenu.add(getNewPageItem());
            pageMenu.add(getDeletePageItem());
            pageMenu.addSeparator();
            pageMenu.add(getPrevPageItem());
            pageMenu.add(getNextPageItem());
            pageMenu.add(getShowPageMenu());
            pageMenu.setMnemonic(KeyEvent.VK_P);
        }
        return pageMenu;
    }
    
    /**
     * This method initializes prevPageItem 
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getPrevPageItem() {
        if (prevPageItem == null) {
            prevPageItem = new JMenuItem();
            prevPageItem.setText("Previous Page");
            prevPageItem.setMnemonic(KeyEvent.VK_P);
            prevPageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                    int prevIndex = arcConfig.curPage - 1;
                    if (prevIndex >= 0) {
                        arcConfig.switchPage(arcConfig.pages.get(prevIndex), prevIndex, true);
                    }
                }
            });
        }
        return prevPageItem;
    }

    /**
     * This method initializes nextPageItem 
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getNextPageItem() {
        if (nextPageItem == null) {
            nextPageItem = new JMenuItem();
            nextPageItem.setText("Next Page");
            nextPageItem.setMnemonic(KeyEvent.VK_N);
            nextPageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                    int nextIndex = arcConfig.curPage + 1;
                    if (nextIndex < arcConfig.pages.size()) {
                        arcConfig.switchPage(arcConfig.pages.get(nextIndex), nextIndex, true);
                    }
                }
            });
        }
        return nextPageItem;
    }

    /**
     * This method initializes deletePageItem   
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getDeletePageItem() {
        if (deletePageItem == null) {
            deletePageItem = new JMenuItem();
            deletePageItem.setText("Delete Page...");
            deletePageItem.setMnemonic(KeyEvent.VK_D);
            deletePageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(
                            MainGUI.getDesktopPane(),
                            "Are you sure you want to delete this page?",
                            "Delete Page",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                            );
                    if (confirm == 0) {
                        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                        arcConfig.deletePage(arcConfig.curPage);
                    }
                }
            });
        }
        return deletePageItem;
    }

    /**
     * This method initializes newPageItem  
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getNewPageItem() {
        if (newPageItem == null) {
            newPageItem = new JMenuItem();
            newPageItem.setText("New Page...");
            newPageItem.setMnemonic(KeyEvent.VK_N);
            newPageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String[] options = PagesRepository.getPageNames(ArcPage.class);
                    
                    for (int i=0; i<options.length; i++) {
                        options[i] = options[i].substring(27);                  
                    }

                    String name = (String)JOptionPane.showInputDialog(
                            MainGUI.getDesktopPane(),
                            "Select a new page type",
                            "New Page",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            "");
                    if (name == null) {
                        return;
                    }
                    name = "org.monome.pages.pages.arc." + name;
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                    arcConfig.addPage(name);
                    arcConfig.curPage = arcConfig.pages.size() - 1;
                    arcConfig.switchPage(arcConfig.pages.get(arcConfig.curPage), arcConfig.curPage, true);
                }
            });
        }
        return newPageItem;
    }

    /**
     * This method initializes configurationMenu    
     *  
     * @return javax.swing.JMenu    
     */
    private JMenu getConfigurationMenu() {
        if (configurationMenu == null) {
            configurationMenu = new JMenu();
            configurationMenu.setText("Configuration");
            configurationMenu.add(getRenamePageItem());
            configurationMenu.add(getRemoveArcItem());
            configurationMenu.addSeparator();
            configurationMenu.add(getPageChangeConfigurationItem());
            configurationMenu.add(getPageChangeMidiInMenu());
            configurationMenu.setMnemonic(KeyEvent.VK_C);
        }
        return configurationMenu;
    }

    public void redrawPagePanel(BasePage page) {
        if (currentPanel != null) {
            getJContentPane().remove(currentPanel);
        }
        JPanel gui = page.getPanel();
        currentPanel = gui;
        getJContentPane().add(gui, BorderLayout.CENTER);
        Dimension guiSize = page.getOrigGuiDimension();
        getJContentPane().setSize(guiSize.width + 16, guiSize.height + 60);
        getJContentPane().validate();
        this.setSize(guiSize.width + 16, guiSize.height + 60);
        this.validate();
    }
    
    /**
     * This method initializes renamePageItem   
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getRenamePageItem() {
        if (renamePageItem == null) {
            renamePageItem = new JMenuItem();
            renamePageItem.setText("Rename Page...");
            renamePageItem.setMnemonic(KeyEvent.VK_R);
            renamePageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                    if (arcConfig.pages.size() > 0) {
                        String curName = arcConfig.pages.get(arcConfig.curPage).getName();
                        String name = (String)JOptionPane.showInputDialog(
                                (JMenuItem) e.getSource(),
                                "Enter a new name for this page",
                                "New Configuration",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                curName);
                        if (name == null || name.compareTo("") == 0) {
                            return;
                        }
                        arcConfig.pages.get(arcConfig.curPage).setName(name);
                    }
                }
            });
        }
        return renamePageItem;
    }

    /**
     * This method initializes pageChangeConfigurationItem  
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getPageChangeConfigurationItem() {
        if (pageChangeConfigurationItem == null) {
            pageChangeConfigurationItem = new JMenuItem();
            pageChangeConfigurationItem.setText("Page Change Configuration...");
            pageChangeConfigurationItem.setMnemonic(KeyEvent.VK_C);
            pageChangeConfigurationItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showPageChangeConfiguration();
                }
            });
        }
        return pageChangeConfigurationItem;
    }
    
    private void showPageChangeConfiguration() {
        if (pccFrame != null && pccFrame.isShowing()) {
            try {
                pccFrame.setSelected(true);
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            }
            return;
        }
        
        pccFrame = new PageChangeConfigurationFrame(ArcConfigurationFactory.getArcConfiguration(index));
        pccFrame.setSize(new Dimension(212, 320));
        pccFrame.setVisible(true);
        Main.main.mainFrame.add(pccFrame);
        try {
            pccFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This method initializes midiMenu 
     *  
     * @return javax.swing.JMenu    
     */
    private JMenu getMidiMenu() {
        if (midiMenu == null) {
            midiMenu = new JMenu();
            midiMenu.setText("MIDI");
            midiMenu.add(getMidiInMenu());
            midiMenu.add(getMidiOutMenu());
            midiMenu.setEnabled(false);
            midiMenu.setMnemonic(KeyEvent.VK_M);
            
        }
        return midiMenu;
    }
    
    public void enableMidiMenu(boolean enabled) {
        midiMenu.setEnabled(enabled);
    }

    /**
     * This method initializes midiInItem   
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenu getMidiInMenu() {
        if (midiInMenu == null) {
            midiInMenu = new JMenu();
            midiInMenu.setText("MIDI In");
            midiInMenu.setMnemonic(KeyEvent.VK_I);
            midiInMenu.add(getNoInputDevicesEnabledItem());
        }
        return midiInMenu;
    }
    
    /**
     * This method initializes midiInItem   
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenu getPageChangeMidiInMenu() {
        if (pageChangeMidiInMenu == null) {
            pageChangeMidiInMenu = new JMenu();
            pageChangeMidiInMenu.setText("Page Change MIDI In");
            pageChangeMidiInMenu.setMnemonic(KeyEvent.VK_I);
            pageChangeMidiInMenu.add(getNoInputDevicesEnabledItem2());
        }
        return pageChangeMidiInMenu;
    }

    public void updateMidiInMenuOptions(String[] midiInOptions) {
        midiInMenu.removeAll();
        pageChangeMidiInMenu.removeAll();
        for (int i=0; i < midiInOptions.length; i++) {
            midiInMenu.remove(getNoInputDevicesEnabledItem());
            pageChangeMidiInMenu.remove(getNoInputDevicesEnabledItem2());
            JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("MIDI Input: " + midiInOptions[i]);
            JCheckBoxMenuItem cbMenuItem2 = new JCheckBoxMenuItem("MIDI Input: " + midiInOptions[i]);
            cbMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String[] pieces = e.getActionCommand().split("MIDI Input: ");
                    actionToggleMidiInput(pieces[1]);
                }});
            cbMenuItem2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String[] pieces = e.getActionCommand().split("MIDI Input: ");
                    actionTogglePageChangeMidiInput(pieces[1]);
                }});
            midiInMenu.add(cbMenuItem);
            pageChangeMidiInMenu.add(cbMenuItem2);
        }
        if (midiInMenu.getItemCount() == 0) {
            midiInMenu.add(getNoInputDevicesEnabledItem());
            pageChangeMidiInMenu.add(getNoInputDevicesEnabledItem2());
        }
    }
    
    public void actionToggleMidiInput(String deviceName) {
        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
        arcConfig.toggleMidiInDevice(deviceName);
    }
    
    public void updateMidiInSelectedItems(String[] midiInDevices) {
        for (int i = 0; i < midiInMenu.getItemCount(); i++) {
            String name = midiInMenu.getItem(i).getText();
            if (name == null || name.equals("No MIDI Input Devices Enabled")) {
                continue;
            }
            String[] pieces = name.split("MIDI Input: ");
            boolean found = false;
            for (int j = 0; j < midiInDevices.length; j++) {
                if (midiInDevices[j] == null) {
                    continue;
                }
                if (pieces[1].compareTo(midiInDevices[j]) == 0) {
                    midiInMenu.getItem(i).setSelected(true);
                    found = true;
                }
            }
            if (!found) {
                midiInMenu.getItem(i).setSelected(false);
            }
        }
    }
    
    public void actionTogglePageChangeMidiInput(String deviceName) {
        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
        arcConfig.togglePageChangeMidiInDevice(deviceName);
    }
    
    public void updatePageChangeMidiInSelectedItems(String[] midiInDevices) {
        for (int i = 0; i < pageChangeMidiInMenu.getItemCount(); i++) {
            String name = pageChangeMidiInMenu.getItem(i).getText();
            if (name == null) {
                continue;
            }
            String[] pieces = name.split("MIDI Input: ");
            boolean found = false;
            for (int j = 0; j < midiInDevices.length; j++) {
                if (midiInDevices[j] == null) {
                    continue;
                }
                if (pieces[1].compareTo(midiInDevices[j]) == 0) {
                    pageChangeMidiInMenu.getItem(i).setSelected(true);
                    found = true;
                }
            }
            if (!found) {
                pageChangeMidiInMenu.getItem(i).setSelected(false);
            }
        }
    }

    /**
     * This method initializes midiOutItem  
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenu getMidiOutMenu() {
        if (midiOutMenu == null) {
            midiOutMenu = new JMenu();
            midiOutMenu.setText("MIDI Out");
            midiOutMenu.add(getNoOutputDevicesEnabledItem());
            midiOutMenu.setMnemonic(KeyEvent.VK_O);
        }
        return midiOutMenu;
    }
    
    public void updateMidiOutMenuOptions(String[] midOutOptions) {
        midiOutMenu.removeAll();
        for (int i=0; i < midOutOptions.length; i++) {
            midiOutMenu.remove(getNoOutputDevicesEnabledItem());
            JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("MIDI Output: " + midOutOptions[i]);
            cbMenuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String[] pieces = e.getActionCommand().split("MIDI Output: ");
                    actionToggleMidiOutput(pieces[1]);
                }});
            midiOutMenu.add(cbMenuItem);
        }
        if (midiOutMenu.getItemCount() == 0) {
            midiOutMenu.add(getNoOutputDevicesEnabledItem());
        }
    }
    
    public void actionToggleMidiOutput(String deviceName) {
        ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
        arcConfig.toggleMidiOutDevice(deviceName);
    }
    
    public void updateMidiOutSelectedItems(String[] midiOutDevices) {
        for (int i = 0; i < midiOutMenu.getItemCount(); i++) {
            String name = midiOutMenu.getItem(i).getText();
            if (name == null || name.equals("No MIDI Output Devices Enabled")) {
                continue;
            }
            String[] pieces = name.split("MIDI Output: ");
            boolean found = false;
            for (int j = 0; j < midiOutDevices.length; j++) {
                if (midiOutDevices[j] == null) {
                    continue;
                }
                if (pieces[1].compareTo(midiOutDevices[j]) == 0) {
                    midiOutMenu.getItem(i).setSelected(true);
                    found = true;
                }
            }
            if (!found) {
                midiOutMenu.getItem(i).setSelected(false);
            }
        }
    }

    
    private JMenuItem getNoInputDevicesEnabledItem() {
        if (noInputDevicesEnabledItem == null) {
            noInputDevicesEnabledItem = new JMenuItem();
            noInputDevicesEnabledItem.setText("No MIDI Input Devices Enabled");
            noInputDevicesEnabledItem.setEnabled(false);
        }
        return noInputDevicesEnabledItem;
    }
    
    private JMenuItem getNoInputDevicesEnabledItem2() {
        if (noInputDevicesEnabledItem2 == null) {
            noInputDevicesEnabledItem2 = new JMenuItem();
            noInputDevicesEnabledItem2.setText("No MIDI Input Devices Enabled");
            noInputDevicesEnabledItem2.setEnabled(false);
        }
        return noInputDevicesEnabledItem2;
    }
    
    private JMenuItem getNoOutputDevicesEnabledItem() {
        if (noOutputDevicesEnabledItem == null) {
            noOutputDevicesEnabledItem = new JMenuItem();
            noOutputDevicesEnabledItem.setText("No MIDI Output Devices Enabled");
            noOutputDevicesEnabledItem.setEnabled(false);
        }
        return noOutputDevicesEnabledItem;
    }

    /**
     * This method initializes removeMonomeItem 
     *  
     * @return javax.swing.JMenuItem    
     */
    private JMenuItem getRemoveArcItem() {
        if (removeArcItem == null) {
            removeArcItem = new JMenuItem();
            removeArcItem.setText("Remove Arc");
            removeArcItem.setMnemonic(KeyEvent.VK_R);
            removeArcItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ArcConfigurationFactory.removeArcConfiguration(index);
                }
            });
        }
        return removeArcItem;
    }

    /**
     * This method initializes showPageMenu 
     *  
     * @return javax.swing.JMenu    
     */
    private JMenu getShowPageMenu() {
        if (showPageMenu == null) {
            showPageMenu = new JMenu();
            showPageMenu.setText("Show Page");
        }
        return showPageMenu;
    }
    
    public void updateShowPageMenuItems(String[] pages) {
        showPageMenu.removeAll();
        for (int i = 0; i < pages.length; i++) {
            JMenuItem pageItem = new JMenuItem("" + (i + 1) + ": " + pages[i]);
            pageItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String[] pieces = e.getActionCommand().split(":");
                    int pageIndex = Integer.parseInt(pieces[0]) - 1;
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration(index);
                    arcConfig.switchPage(arcConfig.pages.get(pageIndex), pageIndex, true);
                }
            });
            showPageMenu.add(pageItem);
        }       
    }

    public void clearPage() {
        getJContentPane().removeAll();
        getJContentPane().validate();
        pack();
    }
}
