package server.sim.entity;

import common.Constants;

public abstract class Obstacle extends PhysicsEntity {

	public Obstacle(double x, double y, double radius) {
		super((short) 0, null, x, y, 0, radius, Double.POSITIVE_INFINITY);
	}

	@Override
	public void resolveCollision(PhysicsEntity other) {
        super.resolveCollision(other);
        if (other instanceof Bullet) {
			other.markForRemoval();
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
