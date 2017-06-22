package client;

import common.Constants;
import javafx.scene.paint.Color;

/**
 * User-accessible class for creating robots
 */
public abstract class Robot {
    private Color color = Color.BLACK;
    private short id;
    private boolean inGame = false;
    private GameManager gm;

    private double ax;
    private double ay;
    private double vx;
    private double vy;
    private double rotation;

    public Robot() {
        Display d = Display.getInstance();
        gm = d.getGameManager();
        gm.addVelocityListener((vx, vy) -> {
            this.vx = vx;
            this.vy = vy;
        });
    }

    public void joinGame(short roomId) {
        id = gm.joinGame(this, roomId);
        inGame = true;
    }

    public void setAcceleration(double ax, double ay) {
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
        if (!inGame) {
            throw new RuntimeException("You must join a game to set the robot's acceleration");
        }
        this.rotation = rot % 360;
        gm.sendRobotUpdate(id, this);
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

}
