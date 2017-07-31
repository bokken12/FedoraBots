package server.sim.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import common.Constants;
import server.sim.world.World;

public abstract class Obstacle extends PhysicsEntity {

    private boolean rotationChanged = false;
    private static Map<World, Map<Double, Entity>> rangeEntities;

    static {
        rangeEntities = new HashMap<World, Map<Double, Entity>>();
    }

	public Obstacle(byte id, double x, double y) {
        super(id, null, x, y, 0, Constants.Obstacle.RADIUS, Double.MAX_VALUE);
	}

    public abstract byte getObstacleType();

	@Override
	public void resolveCollision(PhysicsEntity other) {
        super.resolveCollision(other);
        if (other instanceof Bullet) {
			other.markForRemoval();
		}
    }

    protected Collection<Robot> robotsInRange(double range, World world, boolean cache) {
        if (rangeEntities.get(world) == null) {
            rangeEntities.put(world, new HashMap<Double, Entity>());
        }
        Entity rangeEntity;
        double radius = range + Constants.Robot.RADIUS;
        if ((rangeEntity = rangeEntities.get(world).get(radius)) == null) {
            rangeEntity = new PhysicsEntity((short) 0, null, getX(), getY(), 0, radius, 0);
            if (cache) {
                rangeEntities.get(world).put(radius, rangeEntity);
            }
        }

        world.add(rangeEntity);
        Collection<Robot> robots = new ArrayList<Robot>();
        world.forCollidingUnsafe(rangeEntity, entity -> {
            if (entity instanceof Robot) {
                robots.add((Robot) entity);
            }
        });
        world.remove(rangeEntity);
        return robots;
    }

    /**
     * Returns the closest robot to the obstacle within a given range. If there
     * is no such robot in the range then returns null.
     *
     * The distance is measured from the center of the obstacle to the edge of the robot.
     */
    protected Robot getClosestRobotInRange(double range, World world, boolean cache) {
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
            return robotsInRange(range, world, cache).stream().min(Comparator.comparingDouble(ent ->
                Math.sqrt(Math.pow(ent.getX() - getX(), 2) + Math.pow(ent.getY() - getY(), 2))
            )).get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void setRotation(double rot) {
        super.setRotation(rot);
        rotationChanged = true;
    }

    public boolean hasRotationChanged() {
        boolean res = rotationChanged;
        rotationChanged = false;
        return res;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Obstacle" + super.toString().substring(13);
	}
}
