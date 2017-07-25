package server.sim;

import java.awt.Color;

public class Robot extends PhysicsEntity {

	public Robot(short id, Color color, double x, double y, double rotation, double radius,
				 double mass, double vx, double vy, double ax, double ay)
	{
		super(id, color, x, y, rotation, radius, mass, vx, vy, ax, ay);
	}

	public Robot(short id, Color color, double x, double y, double rotation, double radius, double mass) {
		super(id, color, x, y, rotation, radius, mass);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Robot" + super.toString().substring(13);
	}
}
