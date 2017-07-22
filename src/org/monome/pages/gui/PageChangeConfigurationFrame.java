package org.monome.pages.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JInternalFrame;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import org.monome.pages.Main;
import org.monome.pages.configuration.ArcConfiguration;
import org.monome.pages.configuration.ArcConfigurationFactory;
import org.monome.pages.configuration.MIDIPageChangeRule;
import org.monome.pages.configuration.MonomeConfiguration;
import org.monome.pages.pages.ArcPage;

import java.awt.Dimension;

public class PageChangeConfigurationFrame extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JLabel pageLabel = null;
	private JComboBox pageCB = null;
	private JLabel channelLBL = null;
	private JTextField channelTF = null;
	private JLabel noteLBL = null;
	private JTextField noteTF = null;
	private JCheckBox monomeChangeCB = null;
	private JCheckBox midiChangeCB = null;
	private JLabel monomeChangeLBL = null;
	private JLabel midiChangingLBL = null;
	private JPanel jContentPane;
	private JButton saveBtn = null;
	private JButton cancelBtn = null;
	private MonomeConfiguration monome;
	private ArcConfiguration arc;
	private int[] midiChannels = new int[255];
	private int[] midiNotes = new int[255];
	private int[] midiCCs = new int[255];
	private int[] midiCCVals = new int[255];
	private int[] pageChangeDelays = new int[255];
	private String[] linkedDevices = new String[255];
	private int[] linkedPages = new int[255];
	private int pageIndex = 0;
	private JLabel pageChangeTimerLBL = null;
	private JTextField pageChangeDelayTF = null;
    private JComboBox linkedDeviceCB = null;
    private JComboBox linkedPageCB = null;
	private JLabel midiCCLbl = null;
	private JTextField midiCCTF = null;
	private JLabel midiCCValLbl = null;
	private JTextField midiCCValTF = null;

	/**
	 * This is the default constructor
	 */
	public PageChangeConfigurationFrame(MonomeConfiguration monome) {
		super();
		this.monome = monome;
		getMidiChangeCB().setSelected(monome.useMIDIPageChanging);
		getMonomeChangeCB().setSelected(monome.usePageChangeButton);
		initialize();
		initializeValues();
		populateTextFields();
		this.pack();
	}
	
	public PageChangeConfigurationFrame(ArcConfiguration arc) {
        super();
        this.arc = arc;
        getMidiChangeCB().setSelected(arc.useMIDIPageChanging);
        getMonomeChangeCB().setSelected(arc.usePageChangeButton);
        initialize();
        initializeValues();
        populateTextFields();
        this.pack();
    }

    private void initializeValues() {
        if (monome != null) {
    		ArrayList<MIDIPageChangeRule> midiPageChangeRules = monome.midiPageChangeRules;
    		for (int i = 0; i < midiPageChangeRules.size(); i++) {
    			midiChannels[i] = midiPageChangeRules.get(i).getChannel();
    			midiNotes[i] = midiPageChangeRules.get(i).getNote();
    			midiCCs[i] = midiPageChangeRules.get(i).getCC();
    			midiCCVals[i] = midiPageChangeRules.get(i).getCCVal();
    			pageChangeDelays[i] = monome.pageChangeDelays[i];
    			linkedDevices[i] = midiPageChangeRules.get(i).getLinkedSerial();
    			linkedPages[i] = midiPageChangeRules.get(i).getLinkedPageIndex();
    		}
        } else if (arc != null) {
            ArrayList<MIDIPageChangeRule> midiPageChangeRules = arc.midiPageChangeRules;
            for (int i = 0; i < midiPageChangeRules.size(); i++) {
                midiChannels[i] = midiPageChangeRules.get(i).getChannel();
                midiNotes[i] = midiPageChangeRules.get(i).getNote();
    			midiCCs[i] = midiPageChangeRules.get(i).getCC();
    			midiCCVals[i] = midiPageChangeRules.get(i).getCCVal();
            }
        }
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(215, 355);
		this.setTitle("Page Change Configuration");
		this.setResizable(true);
	}
	
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			midiCCValLbl = new JLabel();
			midiCCValLbl.setBounds(new Rectangle(15, 180, 116, 21));
			midiCCValLbl.setText("MIDI CC Value");
			midiCCLbl = new JLabel();
			midiCCLbl.setBounds(new Rectangle(15, 155, 116, 21));
			midiCCLbl.setText("MIDI CC #");
			pageChangeTimerLBL = new JLabel();
			pageChangeTimerLBL.setBounds(new Rectangle(15, 205, 116, 21));
			pageChangeTimerLBL.setText("PC Button Delay");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getPageLabel(), null);
			jContentPane.add(getPageCB(), null);
			jContentPane.add(getChannelLBL(), null);
			jContentPane.add(getChannelTF(), null);
			jContentPane.add(getNoteLBL(), null);
			jContentPane.add(getNoteTF(), null);
			jContentPane.add(getMonomeChangeCB(), null);
			jContentPane.add(getMidiChangeCB(), null);
			jContentPane.add(getMonomeChangeLBL(), null);
			jContentPane.add(getMidiChangingLBL(), null);
			jContentPane.add(getSaveBtn(), null);
			jContentPane.add(getCancelBtn(), null);
			jContentPane.add(pageChangeTimerLBL, null);
			jContentPane.add(getPageChangeDelayTF(), null);
			jContentPane.add(midiCCLbl, null);
			jContentPane.add(getMidiCCTF(), null);
			jContentPane.add(midiCCValLbl, null);
			jContentPane.add(getMidiCCValTF(), null);
			if (arc == null && monome != null) {
    			jContentPane.add(getLinkedDeviceCB(), null);
    			jContentPane.add(getLinkedPageCB(), null);
			}
		}
		return jContentPane;
	}

	/**
	 * This method initializes pageLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getPageLabel() {
		if (pageLabel == null) {
			pageLabel = new JLabel();
			pageLabel.setText("Page Change Configuration");
			pageLabel.setBounds(new Rectangle(5, 5, 191, 21));
		}
		return pageLabel;
	}

	/**
	 * This method initializes pageCB	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getPageCB() {
		if (pageCB == null) {
			pageCB = new JComboBox();
			pageCB.setBounds(new Rectangle(5, 30, 176, 21));
			pageCB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					storeValues();
					populateTextFields();
				}
			});
			if (monome != null) {
    			for (int i = 0; i < monome.pages.size(); i++) {
    				pageCB.addItem("" + (i+1) + ": " + monome.pages.get(i).getName());
    			}
			} else if (arc != null) {
                for (int i = 0; i < arc.pages.size(); i++) {
                    pageCB.addItem("" + (i+1) + ": " + arc.pages.get(i).getName());
                }
			}
		}
		return pageCB;
	}
	
	private void storeValues() {
		try {
			int value = Integer.parseInt(getChannelTF().getText());
			midiChannels[pageIndex] = value - 1;
			value = Integer.parseInt(getNoteTF().getText());
			midiNotes[pageIndex] = value;
			value = Integer.parseInt(getPageChangeDelayTF().getText());
			pageChangeDelays[pageIndex] = value;
			value = Integer.parseInt(getMidiCCTF().getText());
			midiCCs[pageIndex] = value;
			value = Integer.parseInt(getMidiCCValTF().getText());
			midiCCVals[pageIndex] = value;
			String device = linkedDeviceCB.getSelectedItem().toString();
			if (device.compareTo("-- Select Device --") != 0) {
			    linkedDevices[pageIndex] = device;
			    int pageNum = linkedPageCB.getSelectedIndex();
			    linkedPages[pageIndex] = pageNum;
			} else {
			    linkedDevices[pageIndex] = null;
			    linkedPages[pageIndex] = 0;
			}
		} catch (NumberFormatException ex) {
		}
	}
	
	private void populateTextFields() {
		String pageName = (String) getPageCB().getSelectedItem();
		if (pageName != null) {
			String[] pieces = pageName.split(":");
			pageIndex = Integer.parseInt(pieces[0]) - 1;
			getChannelTF().setText(""+(midiChannels[pageIndex] + 1));
			getNoteTF().setText(""+midiNotes[pageIndex]);
			getPageChangeDelayTF().setText("" + pageChangeDelays[pageIndex]);
			getMidiCCTF().setText("" + midiCCs[pageIndex]);
			getMidiCCValTF().setText("" + midiCCVals[pageIndex]);
			if (linkedDevices[pageIndex] != null) {
    			int deviceIdx = 0;
    			for (int i = 0; i < getLinkedDeviceCB().getItemCount(); i++) {
    			    String itemName = getLinkedDeviceCB().getItemAt(i).toString();
    			    if (itemName.compareTo(linkedDevices[pageIndex]) == 0) {
    			        deviceIdx = i;
    			    }
    			}
    			getLinkedDeviceCB().setSelectedIndex(deviceIdx);
    			getLinkedPageCB().setSelectedIndex(linkedPages[pageIndex]);
			} else {
			    getLinkedDeviceCB().setSelectedIndex(0);
			}
		}
	}

	/**
	 * This method initializes channelLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getChannelLBL() {
		if (channelLBL == null) {
			channelLBL = new JLabel();
			channelLBL.setText("MIDI Channel");
			channelLBL.setBounds(new Rectangle(15, 105, 116, 21));
		}
		return channelLBL;
	}

	/**
	 * This method initializes channelTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getChannelTF() {
		if (channelTF == null) {
			channelTF = new JTextField();
			channelTF.setBounds(new Rectangle(130, 105, 51, 21));
		}
		return channelTF;
	}

	/**
	 * This method initializes noteLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getNoteLBL() {
		if (noteLBL == null) {
			noteLBL = new JLabel();
			noteLBL.setText("MIDI Note #");
			noteLBL.setBounds(new Rectangle(15, 130, 116, 21));
		}
		return noteLBL;
	}

	/**
	 * This method initializes noteTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNoteTF() {
		if (noteTF == null) {
			noteTF = new JTextField();
			noteTF.setBounds(new Rectangle(130, 130, 51, 21));
		}
		return noteTF;
	}

	/**
	 * This method initializes monomeChangeCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getMonomeChangeCB() {
		if (monomeChangeCB == null) {
			monomeChangeCB = new JCheckBox();
			monomeChangeCB.setBounds(new Rectangle(15, 235, 21, 21));
		}
		return monomeChangeCB;
	}

	/**
	 * This method initializes midiChangeCB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getMidiChangeCB() {
		if (midiChangeCB == null) {
			midiChangeCB = new JCheckBox();
			midiChangeCB.setBounds(new Rectangle(15, 260, 21, 21));
		}
		return midiChangeCB;
	}

	/**
	 * This method initializes monomeChangeLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMonomeChangeLBL() {
		if (monomeChangeLBL == null) {
			monomeChangeLBL = new JLabel();
			monomeChangeLBL.setText("Page Change Button");
			monomeChangeLBL.setBounds(new Rectangle(40, 235, 141, 21));
		}
		return monomeChangeLBL;
	}

	/**
	 * This method initializes midiChangingLBL	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getMidiChangingLBL() {
		if (midiChangingLBL == null) {
			midiChangingLBL = new JLabel();
			midiChangingLBL.setText("MIDI Page Changing");
			midiChangingLBL.setBounds(new Rectangle(40, 260, 141, 21));
		}
		return midiChangingLBL;
	}

	/**
	 * This method initializes saveBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton();
			saveBtn.setBounds(new Rectangle(20, 290, 76, 21));
			saveBtn.setText("Save");
			saveBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					storeValues();
					if (monome != null) {
    					monome.useMIDIPageChanging = getMidiChangeCB().isSelected();
    					monome.usePageChangeButton = getMonomeChangeCB().isSelected();
    					ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
    					for (int i = 0; i < monome.pages.size(); i++) {
    						MIDIPageChangeRule mpcr = new MIDIPageChangeRule(midiNotes[i], midiChannels[i], midiCCs[i], midiCCVals[i], i);
    						midiPageChangeRules.add(mpcr);
    						monome.pageChangeDelays[i] = pageChangeDelays[i];
    						mpcr.setLinkedSerial(linkedDevices[i]);
    						mpcr.setLinkedPageIndex(linkedPages[i]);
    					}
    					monome.midiPageChangeRules = midiPageChangeRules;
					} else if (arc != null) {
                        arc.useMIDIPageChanging = getMidiChangeCB().isSelected();
                        arc.usePageChangeButton = getMonomeChangeCB().isSelected();
                        ArrayList<MIDIPageChangeRule> midiPageChangeRules = new ArrayList<MIDIPageChangeRule>();
                        for (int i = 0; i < arc.pages.size(); i++) {
                            MIDIPageChangeRule mpcr = new MIDIPageChangeRule(midiNotes[i], midiChannels[i], midiCCs[i], midiCCVals[i], i);
                            midiPageChangeRules.add(mpcr);
                        }
                        arc.midiPageChangeRules = midiPageChangeRules;
					}
    				cancel();
				}
			});
		}
		return saveBtn;
	}

	/**
	 * This method initializes cancelBtn	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelBtn() {
		if (cancelBtn == null) {
			cancelBtn = new JButton();
			cancelBtn.setBounds(new Rectangle(105, 290, 76, 21));
			cancelBtn.setText("Cancel");
			cancelBtn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancel();
				}
			});
		}
		return cancelBtn;
	}
	
	private void cancel() {
		this.dispose();
	}

	/**
	 * This method initializes pageChangeDelayTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getPageChangeDelayTF() {
		if (pageChangeDelayTF == null) {
			pageChangeDelayTF = new JTextField();
			pageChangeDelayTF.setBounds(new Rectangle(130, 205, 51, 21));
		}
		return pageChangeDelayTF;
	}

    /**
     * This method initializes linkedDeviceCB	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getLinkedDeviceCB() {
        if (linkedDeviceCB == null) {
            linkedDeviceCB = new JComboBox();
            linkedDeviceCB.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String deviceSerial = linkedDeviceCB.getSelectedItem().toString();
                    ArcConfiguration arcConfig = ArcConfigurationFactory.getArcConfiguration("/" + deviceSerial);
                    if (linkedPageCB != null) {
                        while (linkedPageCB.getItemCount() > 0) {
                            linkedPageCB.removeItemAt(0);
                        }
                        if (arcConfig != null) {
                            for (int i = 0; i < arcConfig.pages.size(); i++) {
                                ArcPage page = arcConfig.pages.get(i);
                                linkedPageCB.addItem(i + ": " + page.getName());
                            }
                        }
                    }
                }
            });
            linkedDeviceCB.setBounds(new Rectangle(5, 55, 176, 21));
            linkedDeviceCB.addItem("-- Select Device --");
            for (int i = 0; i < Main.main.configuration.getArcConfigurations().size(); i++) {
                ArcConfiguration arcConfig = Main.main.configuration.getArcConfigurations().get(i);
                if (arcConfig != null) {
                    linkedDeviceCB.addItem(arcConfig.serial);
                }
            }
        }
        
        return linkedDeviceCB;
    }

    /**
     * This method initializes linkedPageCB	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getLinkedPageCB() {
        if (linkedPageCB == null) {
            linkedPageCB = new JComboBox();
            linkedPageCB.setBounds(new Rectangle(5, 80, 176, 21));
        }
        return linkedPageCB;
    }

	/**
	 * This method initializes midiCCTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMidiCCTF() {
		if (midiCCTF == null) {
			midiCCTF = new JTextField();
			midiCCTF.setBounds(new Rectangle(130, 155, 51, 21));
		}
		return midiCCTF;
	}

	/**
	 * This method initializes midiCCValTF	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMidiCCValTF() {
		if (midiCCValTF == null) {
			midiCCValTF = new JTextField();
			midiCCValTF.setBounds(new Rectangle(130, 180, 51, 21));
		}
		return midiCCValTF;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
