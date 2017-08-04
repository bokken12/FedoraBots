package client.event;

import common.Constants;

public class Bullet {
    private double vAngle;

    public Bullet(double vAngle) {
        this.vAngle = vAngle;
    }

    /**
     * Returns the angle of this bullet's velocity, with 0 being Northwards, pi/2 being eastwards, etc.
     */
    public double getAngle() {
        return 2 * Math.PI - vAngle;
    }

    /**
     * Returns the x component of this bullet's velocity
     */
    public double getVx() {
        return Math.cos(vAngle - Math.PI / 2) * Constants.Bullet.VELOCITY;
    }

    /**
     * Returns the y component of this bullet's velocity
     */
    public double getVy() {
        return Math.sin(vAngle - Math.PI / 2) * Constants.Bullet.VELOCITY;
    }

    @Override
    public String toString() {
        return "Bullet [angle=" + (getAngle() * 180 / Math.PI) + " Fgetdeg, vx=" + getVx() + ", vy=" + getVy() + "]";
    }

}
