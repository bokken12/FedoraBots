package client.sensor;

import client.sensor.DetectedEntity;

public class DetectedObstacle extends DetectedEntity {
    public static enum ObstacleType { JAMMER, METEORITE, TURRET, VAPORIZER };

    private ObstacleType type;

    public DetectedObstacle(int x, int y, ObstacleType type) {
        super(x, y);
        this.type = type;
    }

    public ObstacleType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DetectedObstacle [x=" + getX() + ", y=" + getY() + ", type=" + getType() + "]";
    }

}
