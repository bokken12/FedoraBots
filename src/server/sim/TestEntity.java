/**
 *
 */
package server.sim;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
	public TestEntity(short id, double x, double y, double radius) {
		super(id, x, y, radius, 10);
	}

	/* (non-Javadoc)
	 * @see gui.Visible#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(GraphicsContext graphics) {
		graphics.setFill(Color.GREEN);
		graphics.fillOval((int)(getX() - getRadius()), (int)(getY() - getRadius()), (int)(getRadius() * 2), (int)(getRadius() * 2));
	}

	/* (non-Javadoc)
	 * @see sim.PhysicsEntity#tick(int, sim.World)
	 */
	@Override
	public void tick(int length, World world) {
		super.tick(length, world);
		//System.out.println("(" + getX() + ", " + getY() + ")");
	}


}
