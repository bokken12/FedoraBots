package fedorabots.client;

import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import fedorabots.client.GameState.HealthMapState;
import fedorabots.client.GameState.RobotState;
import fedorabots.client.GameState.HealthMapState.DamageAngle;
import fedorabots.client.event.Bullet;
import fedorabots.client.event.BulletDamageEvent;
import fedorabots.client.event.Event;
import fedorabots.client.event.EventHandler;
import fedorabots.client.event.VaporizerDamageEvent;
import fedorabots.client.sensor.DetectedEntity;
import fedorabots.common.Constants;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * User-accessible class for creating robots
 */
public class Robot {
    private Display d;
    private Color color = Color.BLACK;
    private short id;
    private boolean inGame = false;
    private GameManager gm;

    private double ax;
    private double ay;
    private double vx;
    private double vy;
    private int x;
    private int y;
    private double rotation;
    private long lastShoot;
    private double health;

    private List<EventHandler<BulletDamageEvent>> bulletListeners = new ArrayList<EventHandler<BulletDamageEvent>>();
    private List<EventHandler<VaporizerDamageEvent>> vaporizerListeners = new ArrayList<EventHandler<VaporizerDamageEvent>>();

    public Robot() {
        try {
            d = new Display();
        } catch (ConnectException e) {
            new RuntimeException("Could not connect to server", e).printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        gm = d.getGameManager();
        gm.addVelocityListener((vx, vy) -> {
            this.vx = vx;
            this.vy = vy;
        });
        gm.addStateListener(this::handleState);
        gm.addHealthListener(this::updateHealth);
        health = 1;

        d.setOnKeyPressed(this::onKeyPressed);
        d.setOnKeyReleased(this::onKeyReleased);
        d.setOnKeyTyped(this::onKeyTyped);
    }

    /**
     * Join a game hosted over a network.
     *
     * @param roomId    The id of the room to join on the default server
     */
    public void joinNetworkGame(short roomId) {
        joinNetworkGame(roomId, null);
    }

    /**
     * Join a game hosted over a network, specifying a specific server hosting
     * the games.
     *
     * @param roomId    The id of the room to join on the specified server
     * @param hostname  Either the IP address or hostname of the server
     */
    public void joinNetworkGame(short roomId, String hostname) {
        id = gm.joinNetworkGame(hostname, this, roomId);
        inGame = true;
    }

    /**
     * Join a game hosted locally on your computer.
     *
     * @param difficulty    The difficulty of the game, ranging from 0 to 9 inclusive.
     */
    public void joinLocalGame(int difficulty) {
        id = gm.joinLocalGame(this, difficulty);
        inGame = true;
    }

    /**
     * Sets the robot's acceleration by its component parts.
     * The magnitude will be capped to <code>MAX_ACCELERATION</code> of {@link fedorabots.common.Constants.Robot}.
     *
     * @param ax    The x component of the acceleration
     * @param ay    The y component of the acceleration
     */
    public void setAcceleration(double ax, double ay) {
        if (isDead()) {
            throw new RuntimeException("This robot has died");
        }
        if (!inGame) {
            throw new RuntimeException("You must join a game to set the robot's acceleration");
        }
        if (Math.sqrt(ax * ax + ay * ay) > Constants.Robot.MAX_ACCELERATION) {
            throw new RuntimeException("The provided acceleration is too large");
        }
        this.ax = ax;
        this.ay = ay;
        gm.sendRobotUpdate(id, this);
    }

    /**
     * Sets the absolute rotation of the robot's blaster, in degrees.
     *
     * <p>A degree value of 360n (where n is an integer), will cause the blaster to
     * point northwards. Incrementing this will move the blaster in a clockwise
     * direction (so 360n + 90 makes the blaster point eastwards).</p>
     *
     * @param rot   The new rotation
     */
    public void setBlasterRotation(double rot) {
        if (isDead()) {
            throw new RuntimeException("This robot has died");
        }
        if (!inGame) {
            throw new RuntimeException("You must join a game to set the robot's acceleration");
        }
        this.rotation = (rot % 360 + 360) % 360;
        gm.sendRobotUpdate(id, this);
    }

    /**
     * Returns true if the robot's blaster is able to shoot.
     *
     * @return  A boolean indicating whether the blaster is ready to shoot.
     */
    public boolean canShoot() {
        return System.nanoTime() / 1e6 - lastShoot >= Constants.Robot.SHOOT_FREQUENCY;
    }

    /**
     * Attempts to shoot the robot's blaster. If the robot is unable to shoot
     * nothing happens.
     */
    public void shoot() {
        long cTime = (long) (System.nanoTime() / 1e6);
        if (cTime - lastShoot >= Constants.Robot.SHOOT_FREQUENCY) {
            lastShoot = cTime;
            gm.sendRobotShootRequest(id);
        }
    }

    private boolean isAffectedByJammer() {
        for (Point2D center : d.getJammerCenters()) {
            if (center.distance(x, y) <= Constants.Obstacle.JAMMER_RANGE + Constants.Robot.RADIUS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a BufferedImage representing the rendered display frame. If the
     * robot is in the range of a jammer the image will appear to consist of
     * static.
     *
     * @return  Said BufferedImage
     */
    public BufferedImage getDisplayImage() {
        if (isAffectedByJammer()) {
            return staticImage();
        }

        return d.getImage();
    }

    private BufferedImage staticImage() {
        // Algorithm adapted from http://sarathsaleem.github.io/grained
        double grainOpacity = 0.61;
        int grainDensity = 3;
        double grainWidth = 2.39 * 1.5;
        double grainHeight = 2.49 * 1.5;

        Canvas canvas = new Canvas(Constants.World.WIDTH, Constants.World.HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);

        for (int w = 0; w < canvas.getWidth(); w += grainDensity) {
            for (int h = 0; h < canvas.getHeight(); h += grainDensity) {
                double val = Math.random() * grainOpacity + (1 - grainOpacity);
                Supplier<Double> colorVal = () -> {
                    return Math.min(Math.max(val + Math.random()*0.2 - 0.1, 0), 1);
                };
                gc.setFill(Color.color(colorVal.get(), colorVal.get(), colorVal.get()));
                gc.fillRect(w, h, grainWidth, grainHeight);
            }
        }

        return Display.snapshot(() -> canvas.snapshot(null, null), (int) canvas.getWidth(), (int) canvas.getHeight());
    }

    /**
     * Returns the position of the robot on the world's horizontal axis, where 0
     * is the westernmost location of the world.
     *
     * @return  The x location of the robot.
     */
    public double getX() {
    	return x;
    }

    /**
     * Returns the position of the robot on the world's vertical axis,
     * where 0 is the northernmost location of the world.
     *
     * @return  The y location of the robot.
     */
    public double getY() {
    	return y;
    }

    /**
     * Returns the x component of the robot's acceleration.
     *
     * @return  The x component of the robot's acceleration.
     */
    public double getAx() {
        return ax;
    }

    /**
     * Returns the y component of the robot's acceleration.
     *
     * @return  The y component of the robot's acceleration.
     */
    public double getAy() {
        return ay;
    }

    /**
     * Returns the x component of the robot's velocity.
     *
     * @return  The x compnent of the robot's velocity
     */
    public double getVx() {
        return vx;
    }

    /**
     * Returns the y component of the robot's velocity.
     *
     * @return  The y component of the robot's velocity
     */
    public double getVy() {
        return vy;
    }

    /**
     * Returns the current rotation of the robot's blaster in degrees, in the
     * range 0 (included) to 360 (excluded).
     *
     * <p>A degree value of 0 indicates that the blaster is pointing northwards. A
     * slightly larger value indicates that the blaster has moved in a clockwise
     * direction (so 90 means the blaster is pointing eastwards).</p>
     *
     * @return  The robot's rotation
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the color of the robot. This can only be performed before the robot
     * joins a game, and later calls will not update the color in the game
     * (although the change will be reflected in the <code>getColor()</code>
     * method.
     *
     * @param color The new color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns the color of the robot.
     *
     * @return  The robot's color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the robot's current health, on a scale of 0 to 1.
     *
     * @return  The robot's health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Returns true if the robot's health is equal to zero.
     *
     * @return  a boolean indicating whether the robot is dead or not
     */
    public boolean isDead() {
        return health == 0;
    }

    /**
     * Registers an event handler for when the robot is hit by a bullet.
     *
     * @param listener  The event handler to register
     */
    public void addBulletDamageListener(EventHandler<BulletDamageEvent> listener) {
        bulletListeners.add(listener);
    }

    /**
     * Registers an event handler for when the robot is dealt damage by a vaporizer.
     *
     * @param listener  The event handler to register
     */
    public void addVaporizerDamageListener(EventHandler<VaporizerDamageEvent> listener) {
        vaporizerListeners.add(listener);
    }

    /**
     * Returns a list of nearby obstacles and robots.
     *
     * @return the list containing all obstacles and robots within a range of the robot
     */
    public List<DetectedEntity> nearbyEntities() {
        return d.nearbyEntities(new Point2D(x, y));
    }

    private void handleState(GameState st) {
        for (RobotState rs : st.robotStates()) {
            if (rs.getId() == id) {
                x = rs.getX();
                y = rs.getY();
            }
        }
    }

    private <T extends Event> void fireEvent(T event, List<EventHandler<T>> handlers) {
        for (EventHandler<T> handler : handlers) {
            handler.handle(event);
        }
    }

    private void updateHealth(Map<Short, HealthMapState> healthMap) {
        HealthMapState newHealth = healthMap.get(id);
        if (newHealth != null) {
            health = newHealth.getHealth();
            for (DamageAngle angle : newHealth.getAngles()) {
                if (angle.hasDamageAngle()) {
                    BulletDamageEvent event = new BulletDamageEvent(new Bullet(angle.getDamageAngleRadians()), this, Constants.Bullet.DAMAGE);
                    fireEvent(event, bulletListeners);
                } else {
                    VaporizerDamageEvent event = new VaporizerDamageEvent(d.vaporizerById(angle.getObstacleId()), this, Constants.Bullet.DAMAGE);
                    fireEvent(event, vaporizerListeners);
                }
            }
        }
    }

    public void onKeyPressed(KeyEvent event) {}
    public void onKeyReleased(KeyEvent event) {}
    public void onKeyTyped(KeyEvent event) {}

}
