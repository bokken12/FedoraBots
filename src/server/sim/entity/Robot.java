package server.sim.entity;

import java.awt.Color;

import common.Constants;

public class Robot extends PhysicsEntity {

	private double health = 1.0;
	private boolean healthHasChanged;

	public Robot(short id, Color color, double x, double y, double rotation, double radius,
				 double mass, double vx, double vy, double ax, double ay)
	{
		super(id, color, x, y, rotation, radius, mass, vx, vy, ax, ay);
	}

	public Robot(short id, Color color, double x, double y, double rotation, double radius, double mass) {
		super(id, color, x, y, rotation, radius, mass);
	}

	public void setHealth(double value) {
		health = Math.min(Math.max(value, 0), 1);
		healthHasChanged = true;
	}

	public double getHealth() {
		return health;
	}

	public boolean hasHealthChanged() {
		boolean status = healthHasChanged;
		healthHasChanged = false;
		return status;
	}

	@Override
	public void resolveCollision(PhysicsEntity other) {
		super.resolveCollision(other);
		if (other instanceof Bullet) {
			System.out.println("Robot-side collision");
			System.out.println(this);
			setHealth(health - Constants.Bullet.DAMAGE);
			other.markedForRemoval();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Robot" + super.toString().substring(13);
	}
}
