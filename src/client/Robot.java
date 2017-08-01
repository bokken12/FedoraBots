package client;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Supplier;

import common.Constants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * User-accessible class for creating robots
 */
public abstract class Robot {
    private Display d;
    private Color color = Color.BLACK;
    private short id;
    private boolean inGame = false;
    private GameManager gm;

    private double ax;
    private double ay;
    private double vx;
    private double vy;
    private double rotation;
    private long lastShoot;
    private double health;

    public Robot() {
        d = Display.getInstance();
        gm = d.getGameManager();
        gm.addVelocityListener((vx, vy) -> {
            this.vx = vx;
            this.vy = vy;
        });
        gm.addHealthListener(this::updateHealth);
        health = 1;
    }

    public void joinNetworkGame(short roomId) {
        id = gm.joinNetworkGame(this, roomId);
        inGame = true;
    }

    public void joinLocalGame() {
        id = gm.joinLocalGame(this);
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

    public void setRotation(double rot) {
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

    public BufferedImage getDisplayImage() {
        // return d.getImage();
        return staticImage();
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

    private void updateHealth(Map<Short, Double> healthMap) {
        Double newHealth = healthMap.get(id);
        if (newHealth != null) {
            health = newHealth;
        }
    }

}
