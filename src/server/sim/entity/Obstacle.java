package server.sim.entity;

import common.Constants;
import server.sim.world.World;

public abstract class Obstacle extends PhysicsEntity {

	public Obstacle(double x, double y) {
		super((short) 0, null, x, y, 0, Constants.Obstacle.RADIUS, Double.POSITIVE_INFINITY);
	}

    public abstract byte getObstacleType();

	@Override
	public void resolveCollision(PhysicsEntity other) {
        super.resolveCollision(other);
        if (other instanceof Bullet) {
			other.markForRemoval();
		}
    }

    /**
     * Returns the closest robot to the obstacle within a given range. If there
     * is no such robot in the range then returns null.
     *
     * The distance is measured from the center of the obstacle to the edge of the robot.
     */
    protected Robot getClosestRobotInRange(double range, World world) {
        Entity closest = world.closest(this, Robot.class::isInstance);
        double distance = Math.sqrt(Math.pow(closest.getX() - getX(), 2) + Math.pow(closest.getY() - getY(), 2));
        if (distance - closest.getRadius() <= range) {
            return (Robot) closest;
        } else {
            return null;
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
