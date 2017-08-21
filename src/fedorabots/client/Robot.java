package fedorabots.client;

import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
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
    }

    public void joinNetworkGame(short roomId) {
        joinNetworkGame(roomId, null);
    }

    public void joinNetworkGame(short roomId, String hostname) {
        id = gm.joinNetworkGame(this, roomId);
        inGame = true;
    }

    public void joinLocalGame(int difficulty) {
        id = gm.joinLocalGame(this, difficulty);
        inGame = true;
    }

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

    public boolean canShoot() {
        return System.nanoTime() / 1e6 - lastShoot >= Constants.Robot.SHOOT_FREQUENCY;
    }

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
    
    public double getX() {
    	return x;
    }
    
    public double getY() {
    	return y;
    }

    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getRotation() {
        return rotation;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public double getHealth() {
        return health;
    }

    public boolean isDead() {
        return health == 0;
    }

    public void addBulletDamageListener(EventHandler<BulletDamageEvent> listener) {
        bulletListeners.add(listener);
    }

    public void addVaporizerDamageListener(EventHandler<VaporizerDamageEvent> listener) {
        vaporizerListeners.add(listener);
    }

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

}
