package org.monome.pages.pages.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.monome.pages.configuration.PagesRepository;
import org.monome.pages.configuration.QuadrantConfiguration;
import org.monome.pages.pages.Page;
import org.monome.pages.pages.QuadrantsPage;

import java.io.Serializable;

public class QuadrantsGUI extends JPanel implements ActionListener, Serializable {
    static final long serialVersionUID = 42L;

	QuadrantsPage page;
	private JLabel pageNameLBL;
	private ButtonGroup quadrantBG;
	private ArrayList<JRadioButton> quadrantRB;
	private int selectedQuadConf = 0;

	public QuadrantsGUI(QuadrantsPage page, int selectedQuadConf) {
		super();
		this.selectedQuadConf = selectedQuadConf;
		this.page = page;
		initialize();
	}
	
	private void initialize() {
		
		setLayout(new GridLayout(2, 2));		
		pageNameLBL = new JLabel("Page " + (page.getIndex() + 1) + ": Quadrants Page");
		pageNameLBL.setBounds(0, 0, 250, 14);
		add(pageNameLBL);
		setSize(400,300);
		quadrantBG = new ButtonGroup();
		quadrantRB = new ArrayList<JRadioButton>();
		
		for (int i = 0; i < page.quadrantConfigurations.size(); i++) {
			QuadrantConfiguration quadConf = page.quadrantConfigurations.get(i);
			JRadioButton rb = new JRadioButton();
			rb.setText(quadConf.getPicture());
			rb.addActionListener(this);
			if (i == this.selectedQuadConf) {
				rb.setSelected(true);
			}
			
			this.quadrantBG.add(rb);
			this.quadrantRB.add(rb);
			add(rb);
		}
		QuadrantConfiguration quadConf = page.quadrantConfigurations.get(this.selectedQuadConf);
		for (int i = 0; i < quadConf.getNumQuads(); i++) {			
			JButton newPageButton = new JButton();
			newPageButton.setText("New Page " + i);
			newPageButton.setBounds(0, 0, 100, 20);
			newPageButton.addActionListener(this);
			add(newPageButton);			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("New Page")) {
			String options[] = PagesRepository.getPageNames(Page.class);
			
			//don't know if this is the best way to do this...but I was getting tired of the long messy classnames :)
			for (int i=0; i<options.length; i++) {
				options[i] = options[i].substring(23);
			}
			String name = (String)JOptionPane.showInputDialog(
					this,
					"Select a new page type",
					"New Page",
					JOptionPane.PLAIN_MESSAGE,
					null,
					options,
					"");
			if (name == null) {
				return;
			}
			name = "org.monome.pages.pages." + name;
			page.quadrantConfigurations.get(selectedQuadConf).getMonomeConfiguration(0).addPage(name);
		} else {
			for (int i = 0; i < page.quadrantConfigurations.size(); i++) {
				QuadrantConfiguration quadConf = page.quadrantConfigurations.get(i);
				if (quadConf.getPicture().equals(e.getActionCommand())) {
					selectedQuadConf = i;
					page.redrawPage(selectedQuadConf);
				}
			}
		}

	}

}
