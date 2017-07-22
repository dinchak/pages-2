package org.monome.pages.pages.arc.gui;

import javax.swing.JPanel;

import org.monome.pages.Main;
import org.monome.pages.gui.GroovyErrorConsole;
import org.monome.pages.pages.arc.GroovyPage;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.BoxLayout;

public class GroovyGUI extends JPanel implements Serializable {
    static final long serialVersionUID = 42L;

    private GroovyPage page;
    private JButton runBtn = null;  //  @jve:decl-index=0:visual-constraint="748,97"
    public JEditTextArea codePane = null;
    private JButton stopButton = null;  //  @jve:decl-index=0:visual-constraint="717,352"
    private JButton saveButton = null;  //  @jve:decl-index=0:visual-constraint="749,224"
    private JButton loadButton = null;  //  @jve:decl-index=0:visual-constraint="716,389"
    private JButton logButton = null;  //  @jve:decl-index=0:visual-constraint="708,318"
    public GroovyErrorConsole errorWindow;
    private JPanel buttonPane = null;
    /**
     * This is the default constructor
     */
    public GroovyGUI(GroovyPage page) {
        super();
        this.page = page;
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(getCodePane());
        this.setSize(669, 600);
        this.add(getButtonPane());
    }
    
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            buttonPane = new JPanel();
            buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
            buttonPane.add(getLoadButton());
            buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPane.add(getSaveButton());
            buttonPane.add(Box.createRigidArea(new Dimension(50, 0)));
            buttonPane.add(getLogButton());
            buttonPane.add(Box.createRigidArea(new Dimension(50, 0)));
            buttonPane.add(getRunBtn());
            buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
            buttonPane.add(getStopButton());
        }
        return buttonPane;
    }
    
    public void setName(String name) {
        //pageLabel.setText((page.getIndex() + 1) + ": " + name);
    }

    /**
     * This method initializes runBtn   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getRunBtn() {
        if (runBtn == null) {
            runBtn = new JButton();
            runBtn.setText("Run");
            runBtn.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    page.runCode();
                }
            });
        }
        return runBtn;
    }

    /**
     * This method initializes codePane 
     *  
     * @return javax.swing.JEditorPane  
     */
    private JEditTextArea getCodePane() {
        if (codePane == null) {
            codePane = new JEditTextArea();
            codePane.setTokenMarker(new JavaTokenMarker());
        }
        return codePane;
    }

    /**
     * This method initializes stopButton   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getStopButton() {
        if (stopButton == null) {
            stopButton = new JButton();
            stopButton.setText("Stop");
            stopButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    page.stopCode();
                }
            });
        }
        return stopButton;
    }

    /**
     * This method initializes saveButton   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setText("Save");
            saveButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    saveScript();
                }
            });
        }
        return saveButton;
    }
    
    public void saveScript() {
        JFileChooser fc = new JFileChooser();
        try {
            File f = new File(new File("./scripts").getCanonicalPath());
            fc.setCurrentDirectory(f);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                if (Main.main.configuration != null) {
                    FileWriter fw = new FileWriter(file);
                    fw.write(codePane.getText());
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method initializes loadButton   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getLoadButton() {
        if (loadButton == null) {
            loadButton = new JButton();
            loadButton.setText("Load");
            loadButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    loadScript();
                }
            });
        }
        return loadButton;
    }
    
    private void loadScript() {
        JFileChooser fc = new JFileChooser();
        try {
            File f = new File(new File("./scripts").getCanonicalPath());
            fc.setCurrentDirectory(f);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(fis));
            String code = "";
            try {
                while (in.ready()) {
                    code += in.readLine() + "\n";
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.codePane.setText(code);
            this.codePane.scrollTo(0, 0);
        }
    }

    /**
     * This method initializes logButton    
     *  
     * @return javax.swing.JButton  
     */
    private JButton getLogButton() {
        if (logButton == null) {
            logButton = new JButton();
            logButton.setText("Log Window");
            logButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (errorWindow != null && errorWindow.isShowing()) {
                        try {
                            errorWindow.setSelected(true);
                        } catch (PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                    
                    errorWindow = new GroovyErrorConsole();
                    errorWindow.setSize(new Dimension(623, 404));
                    errorWindow.setVisible(true);
                    errorWindow.setClosable(true);
                    errorWindow.setResizable(true);
                    errorWindow.setErrorText(page.errorLog.getErrors());
                    Main.main.mainFrame.add(errorWindow);
                    try {
                        errorWindow.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        ex.printStackTrace();
                    }
                    
                    Main.main.mainFrame.validate();
                }
            });
        }
        return logButton;
    }
}  //  @jve:decl-index=0:visual-constraint="89,40"
