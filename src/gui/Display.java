/**
 * 
 */
package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import sim.World;

/**
 * @author joelmanning
 *
 */
public class Display extends JComponent {
	
	private World world;
	private double x, y, width, height;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		world.forColliding(x, y, width, height, (e) -> {
			if(e instanceof Visible){
				((Visible) e).paint((Graphics2D) g); 
			}
		});
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1461500059364328660L;
	
}
