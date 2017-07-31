package server.sim.entity;

import common.Constants;
import server.sim.world.World;

/**
 * A turret is an obstacle that has a shooter and will shoot nearby robots.
 */
public class Turret extends Obstacle {

    private double totalTime = 0;

    public Turret(byte id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public byte getObstacleType() {
        return 1;
    }

    @Override
    public void tick(double length, World world) {
        super.tick(length, world);
        totalTime += length;
        if (totalTime > Constants.Obstacle.TURRET_SHOOT_FREQUENCY) {
            if (aim(world)) {
                shoot(world);
            }
            totalTime %= Constants.Obstacle.TURRET_SHOOT_FREQUENCY;
        }
    }

    /**
     * Aims the turret at the nearest robot in the radius, and returns whether
     * there is a robot to shoot at.
     */
    private boolean aim(World world) {
        Robot robot = getClosestRobotInRange(Constants.Obstacle.TURRET_RANGE, world);
        if (robot == null) {
            return false;
        }

        double angle = Math.atan2(getY() - robot.getY(), robot.getX() - getX());
        setRotation(-angle + Math.PI / 2);
        return true;
    }

    private void shoot(World world) {
        double rotation = -getRotation() + Math.PI / 2;
        double vx = Constants.Bullet.VELOCITY/1e3 * Math.cos(rotation);
        double vy = - (Constants.Bullet.VELOCITY/1e3 * Math.sin(rotation));
        double dist = (Constants.Obstacle.RADIUS + Constants.Bullet.RADIUS) * 1.1;
        double x = getX() + dist * Math.cos(rotation);
        double y = getY() - dist * Math.sin(rotation);
        world.add(new Bullet(x, y, Constants.Bullet.RADIUS, Constants.Bullet.MASS, vx, vy));
    }

}
