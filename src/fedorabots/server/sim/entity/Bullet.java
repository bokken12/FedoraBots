package fedorabots.server.sim.entity;

import fedorabots.common.Constants;
import javafx.geometry.Point2D;

public class Bullet extends PhysicsEntity {

	public Bullet(double x, double y, double radius, double mass, double vx, double vy)
	{
		super((short) 0, null, x, y, 0, radius, mass, vx, vy, 0, 0);
	}

	public Bullet(double x, double y, double radius, double mass) {
		super((short) 0, null, x, y, 0, radius, mass);
	}

	@Override
	public void resolveCollision(PhysicsEntity other) {
		super.resolveCollision(other);
		if (other instanceof Robot) {
			Robot rother = (Robot) other;
			rother.setHealth(rother.getHealth() - Constants.Bullet.DAMAGE);
			rother.addDamageAngle(new Point2D(getVx(), getVy()));
			markForRemoval();
		} else if (other instanceof Bullet) {
			markForRemoval();
			other.markForRemoval();
		} else if (other instanceof Obstacle) {
			markForRemoval();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bullet" + super.toString().substring(13);
	}
}
