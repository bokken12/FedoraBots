package server.sim.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

import common.Constants;
import server.sim.world.World;

public abstract class Obstacle extends PhysicsEntity {

	public Obstacle(byte id, double x, double y) {
		super(id, null, x, y, 0, Constants.Obstacle.RADIUS, Double.POSITIVE_INFINITY);
	}

    public abstract byte getObstacleType();

	@Override
	public void resolveCollision(PhysicsEntity other) {
        super.resolveCollision(other);
        if (other instanceof Bullet) {
			other.markForRemoval();
		}
    }

    protected Collection<Robot> robotsInRange(double range, World world) {
        double prevRadius = getRadius();
        setRadius(range + Constants.Robot.RADIUS);
        Collection<Robot> robots = new ArrayList<Robot>();
        world.forCollidingUnsafe(this, entity -> {
            if (entity instanceof Robot) {
                robots.add((Robot) entity);
            }
        });
        setRadius(prevRadius);
        return robots;
    }

    /**
     * Returns the closest robot to the obstacle within a given range. If there
     * is no such robot in the range then returns null.
     *
     * The distance is measured from the center of the obstacle to the edge of the robot.
     */
    protected Robot getClosestRobotInRange(double range, World world) {
        // Entity closest = world.closest(this, rob -> {
        //     if (rob instanceof Robot) {
        //         System.out.println("YAY");
        //         return true;
        //     } else {
        //         return false;
        //     }
        // });
        // world.forEachUnsafe(entity -> {
        //     if (entity instanceof Robot) {
        //         System.out.println(entity.getWorld().getClass().getName());
        //         System.out.println(entity.markedForRemoval());
        //     }
        // });

        // if (closest == null) {
        //     return null;
        // }
        // double distance = Math.sqrt(Math.pow(closest.getX() - getX(), 2) + Math.pow(closest.getY() - getY(), 2));
        // System.out.println(distance - closest.getRadius() + " | " + range);
        // if (distance - closest.getRadius() <= range) {
        //     return (Robot) closest;
        // } else {
        //     return null;
        // }
        try  {
            world.forEachUnsafe(entity -> {
                if (entity instanceof Robot) {
                    System.out.println(entity.getWorld().getClass().getName());
                    System.out.println(entity.markedForRemoval());
                }
            });
            return robotsInRange(range, world).stream().min(Comparator.comparingDouble(ent ->
                Math.sqrt(Math.pow(ent.getX() - getX(), 2) + Math.pow(ent.getY() - getY(), 2))
            )).get();
        } catch (NoSuchElementException e) {
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
