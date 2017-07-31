package server.sim.entity;

import java.util.HashSet;
import java.util.Set;

import common.Constants;
import server.sim.world.World;

/**
 * A vaporizer is an obstacle that sends out pulses of energy that deal damage
 * to nearby robots.
 *
 * It's energy pulse is controlled by its rotation. A rotation of 0 means no
 * pulse is active. A rotation of 359 degrees means it's pulse is pretty much at
 * the highest point.
 */
public class Vaporizer extends Obstacle {

    private static enum Mode {CHARGING, NOTARGET, PULSING};
    private static final double PULSE_START_TIME = Constants.Obstacle.VAPORIZER_PULSE_FREQUENCY - Constants.Obstacle.VAPORIZER_PULSE_LENGTH;

    private double totalTime = 0;
    private Set<Robot> damagedRobots;
    private Mode mode;

    public Vaporizer(byte id, double x, double y) {
        super(id, x, y);
        damagedRobots = new HashSet<Robot>();
        mode = Mode.CHARGING;
    }

    @Override
    public byte getObstacleType() {
        return 2;
    }

    @Override
    public void tick(double length, World world) {
        super.tick(length, world);
        totalTime += length;
        if (totalTime >= Constants.Obstacle.VAPORIZER_PULSE_FREQUENCY) {
            setRotation(0);
            mode = Mode.CHARGING;
            damagedRobots.clear();
            totalTime %= Constants.Obstacle.VAPORIZER_PULSE_FREQUENCY;
        } else if (totalTime > PULSE_START_TIME) {
            if (mode == Mode.CHARGING) {
                if (getClosestRobotInRange(Constants.Obstacle.VAPORIZER_RANGE, world) == null) {
                    mode = Mode.NOTARGET;
                } else {
                    mode = Mode.PULSING;
                }
            }
            if (mode == Mode.PULSING) {
                double pulsePercent = (totalTime - PULSE_START_TIME) / Constants.Obstacle.VAPORIZER_PULSE_LENGTH;
                setRotation(pulsePercent * 2 * Math.PI);
                double origRadius = getRadius();
                double pulseRadius = pulsePercent * Constants.Obstacle.VAPORIZER_RANGE;
                setRadius(pulseRadius);
                world.forCollidingUnsafe(this, this::handlePulse);
                setRadius(origRadius);
            }
        }
    }

    private void handlePulse(Entity entity) {
        if (entity instanceof Robot) {
            Robot robot = (Robot) entity;
            boolean newRobot = damagedRobots.add(robot);
            if (newRobot) {
                robot.setHealth(robot.getHealth() - Constants.Bullet.DAMAGE);
            }
        }
    }

}
