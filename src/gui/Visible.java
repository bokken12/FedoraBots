/**
 * 
 */
package gui;

import java.awt.Graphics2D;

import sim.Entity;

/**
 * @author joelmanning
 *
 */
public interface Visible {
	public default void paint(Graphics2D graphics) {
		if(this instanceof Entity) {
			Entity e = (Entity) this;
			graphics.drawOval((int) (e.getX() - e.getRadius()), (int) (e.getY() - e.getRadius()),
					(int) (e.getRadius() * 2), (int) (e.getRadius() * 2));
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
