/**
 * 
 */
package sim;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * @author joelmanning
 *
 */
public class TestEntity extends PhysicsEntity {

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
		graphics.drawOval((int)(getX() - getRadius()), (int)(getY() - getRadius()), (int)(getRadius() * 2), (int)(getRadius() * 2));
	}

	/* (non-Javadoc)
	 * @see sim.PhysicsEntity#tick(int, sim.World)
	 */
	@Override
	public void tick(int length, World world) {
		super.tick(length, world);
		System.out.println(toString());
	}
	
	
}
