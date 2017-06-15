/**
 * 
 */
package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.Semaphore;

import javax.swing.JComponent;

import sim.World;

/**
 * @author joelmanning
 *
 */
public class Display extends JComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1461500059364328660L;
	
	private World world;
	private Semaphore lock;
	private double x, y;
	private int width, height;
	
	public Display(World w, Semaphore lock, int width, int height) {
		world = w;
		x = 0;
		y = 0;
		this.lock = lock;
		this.width = width;
		this.height = height;
		setSize(width, height);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// set the origin to (x, y)
		g.translate((int) -x, (int) -y);
		// paint shapes in the world that would appear (would collide with the
		// display box)
		try {
			lock.acquire();
			world.forCollidingUnsafe(x, y, width, height, (e) -> {
				e.paint((Graphics2D) g);
			});
			lock.release();
		} catch(InterruptedException e1) {
			e1.printStackTrace();
		}
		
		g.dispose();
	}
	
	/**
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}
	
	/**
	 * @return the x
	 */
	public double getXViewPoint() {
		return x;
	}
	
	/**
	 * @return the y
	 */
	public double getYViewPoint() {
		return y;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * @param x
	 *            the x to set
	 */
	public void setXViewPoint(double x) {
		this.x = x;
	}
	
	/**
	 * @param y
	 *            the y to set
	 */
	public void setYViewPoint(double y) {
		this.y = y;
	}
	
	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
}
