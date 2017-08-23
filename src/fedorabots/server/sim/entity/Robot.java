package fedorabots.server.sim.entity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import fedorabots.common.Constants;
import javafx.geometry.Point2D;

public class Robot extends PhysicsEntity {

	private double health = 1.0;
	private boolean healthHasChanged;
	private List<Point2D> damageAngles = new ArrayList<Point2D>(3);

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

	/**
	 * Adds the damage angle to the robot's internal list.
	 *
	 * Normally the angle is encoded as a vector with an x and y component.
	 * However if the damage comes from a vaporizer, the y component should be
	 * equal to the vaporizer's id and the magnitude should be greater than 2 *
	 * the bullet velocity.
	 */
	public void addDamageAngle(Point2D angle) {
		damageAngles.add(angle);
	}

	public List<Point2D> getDamageAngles() {
		return damageAngles;
	}

	public void clearDamageAngles() {
		damageAngles.clear();
	}


	/**
	 * @param vx the vx to set
	 * @param vy the vy to set
	 */
	protected void setVelocity(double vx, double vy) {
		double v = Math.sqrt(vx * vx + vy * vy);
		if (v > Constants.Robot.MAX_VELOCITY/1e3) {
			double angle = Math.atan2(vy, vx);
			super.setVelocity(Constants.Robot.MAX_VELOCITY/1e3 * Math.cos(angle),
							  Constants.Robot.MAX_VELOCITY/1e3 * Math.sin(angle));
		} else {
			super.setVelocity(vx, vy);
		}
	}

	@Override
	public void resolveCollision(PhysicsEntity other) {
		super.resolveCollision(other);
		if (other instanceof Bullet) {
			setHealth(health - Constants.Bullet.DAMAGE);
			addDamageAngle(new Point2D(other.getVx(), other.getVy()));
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
