package org.monome.pages.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

public class JMonomeDisplay extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private int sizeX;
	private int sizeY;
	private int[][] ledState = new int[32][32];
	private int[][] pressState = new int[32][32];
		
	public JMonomeDisplay(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		setPreferredSize(new Dimension((sizeX * 14) + 4, (sizeY * 14) + 4));
	}
	
	public void paintComponent(Graphics g) {
		paintMonome(g);
	}
	
	public Dimension getSize() {
		Dimension dim = new Dimension((sizeX * 15), (sizeY * 15));
		return dim;
	}
	
	public void paint(Graphics g, Component c) {
		paintMonome(g);
	}
	
    public void press(int x, int y, int state) {
    	if (x < 16 && x >= 0 && y < 16 && y >= 0) {
    		pressState[x][y] = state;
    		repaint();
    	}
    }
	
	public void paintMonome(Graphics g) {
		Dimension pSize = this.getParent().getSize();
		double side = Math.min(pSize.getWidth(), pSize.getHeight());
		int xSize = (int) side / sizeX;
		int ySize = (int) side / sizeY;
		
		int sideSize = Math.max(xSize, ySize);
		
		int rectDistance = 2 + (sideSize / 10);
		int fillDistance = 3 + (sideSize / 10);
		int circleDistance = 4 + (sideSize / 10);
		int circleSize = sideSize - (sideSize / 10) - 6;
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				if (ledState[x][y] == 0) {
					g.setColor(Color.GRAY);
					g.drawRect((x * sideSize) + rectDistance, (y * sideSize) + rectDistance, sideSize - rectDistance, sideSize - rectDistance);
				} else if (ledState[x][y] == 1) {
					g.setColor(Color.GRAY);
					g.drawRect((x * sideSize) + rectDistance, (y * sideSize) + rectDistance, sideSize - rectDistance, sideSize - rectDistance);
					g.setColor(Color.ORANGE);
					g.fillRect((x * sideSize) + fillDistance, (y * sideSize) + fillDistance, sideSize - fillDistance, sideSize - fillDistance);
				}
				if (pressState[x][y] == 1) {
					g.setColor(Color.BLACK);
					g.drawOval((x * sideSize) + circleDistance, (y * sideSize) + circleDistance, circleSize, circleSize); 
				}
			}
		}
	}
	
	public void setLedState(int[][] ledState) {
		this.ledState = ledState;
		repaint();
	}
}
