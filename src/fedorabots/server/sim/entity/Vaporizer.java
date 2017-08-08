package fedorabots.server.sim.entity;

import java.util.HashSet;
import java.util.Set;

import fedorabots.common.Constants;
import javafx.geometry.Point2D;
import fedorabots.server.sim.world.World;

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
    private Set<Short> damagedRobots;
    private Mode mode;

    public Vaporizer(byte id, double x, double y) {
        super(id, x, y);
        damagedRobots = new HashSet<Short>();
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
                if (getClosestRobotInRange(Constants.Obstacle.VAPORIZER_RANGE, world, true) == null) {
                    mode = Mode.NOTARGET;
                } else {
                    mode = Mode.PULSING;
                }
            }
            if (mode == Mode.PULSING) {
                double pulsePercent = (totalTime - PULSE_START_TIME) / Constants.Obstacle.VAPORIZER_PULSE_LENGTH;
                setRotation(pulsePercent * 2 * Math.PI);
                double pulseRadius = getRadius() + pulsePercent * (Constants.Obstacle.VAPORIZER_RANGE - getRadius());

                robotsInRange(pulseRadius, world, false).forEach(this::handlePulse);
            }
        }
    }

    private void handlePulse(Robot robot) {
        boolean newRobot = damagedRobots.add(robot.getId());
        if (newRobot) {
            robot.setHealth(robot.getHealth() - Constants.Bullet.DAMAGE);
            robot.addDamageAngle(new Point2D(2.1 * Constants.Bullet.VELOCITY / 1e3, getId()));
        }
    }

}
