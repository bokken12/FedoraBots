/**
 * 
 */
package sim;

import java.awt.Color;
import java.awt.Graphics2D;

import gui.Visible;

/**
 * @author joelmanning
 *
 */
public class TestEntity extends Entity implements Visible {

	/**
	 * @param x
	 * @param y
	 * @param radius
	 */
	public TestEntity(double x, double y, double radius) {
		super(x, y, radius);
	}

	/* (non-Javadoc)
	 * @see gui.Visible#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(Graphics2D graphics) {
		graphics.setColor(Color.GREEN);
		Visible.super.paint(graphics);
	}
}
