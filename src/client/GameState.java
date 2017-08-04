package client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.paint.Color;

/**
 * Keeps track of robot positions, rotations, and colors.
 *
 * It is assumed that a GameState object is created without knowledge of the robots' colors.
 * Therefore, the color of the robots in the gamestate can be updated when discovered.
 */
public class GameState {
    private Map<Short, Color> colorMap;
    private RobotState[] robotStates;
    private BulletState[] bulletStates;
    private ObstacleState[] obstacleStates;

    /**
     * Creates a GameState with the specified robot states. Note that these
     * robot states will be modified as a side effect.
     */
    public GameState(RobotState[] robotStates) {
        this.robotStates = robotStates;
        for (RobotState s : robotStates) {
            s.setSurroundingState(this);
        }
    }

    /**
     * Creates a GameState with the specified robot states and obstacle states.
     * Note that these robot states will be modified as a side effect.
     */
    public GameState(RobotState[] robotStates, ObstacleState[] obstacleStates) {
        this(robotStates);
        this.obstacleStates = obstacleStates;
    }

    /**
     * Creates a GameState with the specified robot states and bullet states.
     * Note that these robot states will be modified as a side effect.
     */
    public GameState(RobotState[] robotStates, BulletState[] bulletStates) {
        this(robotStates);
        this.bulletStates = bulletStates;
    }

    /**
     * Creates a GameState with the specified robot states, obstacle states, and bullet states.
     * Note that these robot states will be modified as a side effect.
     */
    public GameState(RobotState[] robotStates, ObstacleState[] obstacleStates, BulletState[] bulletStates) {
        this(robotStates);
        this.obstacleStates = obstacleStates;
        this.bulletStates = bulletStates;
    }

    public static class RobotState {
        private short id;
        private int x;
        private int y;
        private byte rotation;
        private byte vAngle;
        private byte aAngle;
        private GameState surroundingState;

        public RobotState(short id, int x, int y, byte rotation, byte vAngle, byte aAngle) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.vAngle = vAngle;
            this.aAngle = aAngle;
        }

        public short getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public byte getRotation() { return rotation; }
        public byte getVelocityAngle() { return vAngle; }
        public byte getAccelAngle() { return aAngle; }

        public Color getColor() {
            return surroundingState.colorForRobot(id);
        }

        protected void setSurroundingState(GameState state) {
            surroundingState = state;
        }
    }

    public static class BulletState {
        private int x;
        private int y;
        private byte rotation;

        public BulletState(int x, int y, byte rotation) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public byte getRotation() { return rotation; }
    }

    public static class ObstacleState {
        private byte id;
        private byte type;
        private int x;
        private int y;
        private byte rotation;

        public ObstacleState(byte id, byte type, int x, int y, byte rotation) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }

        public byte getId() { return id; }
        public byte getType() { return type; }
        public int getX() { return x; }
        public int getY() { return y; }
        public byte getRotation() { return rotation; }
    }

    public static class HealthMapState {

        public static class DamageAngle {
            private short angle;

            public DamageAngle(short angle) {
                this.angle = angle;
            }

            public short getAngle() {
                return angle;
            }

            public boolean hasDamageAngle() {
                return (angle & 0x8000) != 0;
            }

            public double getDamageAngleRadians() {
                return (double) (angle & 0x7FFF) / 0x7FFF * 2 * Math.PI;
            }

            public byte getObstacleId() {
                return (byte) (angle & 0xFF);
            }
        }

        private double health;
        private List<DamageAngle> angles;

        public HealthMapState(double health, short angle) {
            this.health = health;
            this.angles = new ArrayList<DamageAngle>(3);
            angles.add(new DamageAngle(angle));
        }

        public double getHealth() { return health; }
        public List<DamageAngle> getAngles() { return angles; }
        public void addAngle(short angle) { angles.add(new DamageAngle(angle)); }

    }

    public void setColorMap(Map<Short, Color> cmap) {
        colorMap = cmap;
    }

    public RobotState[] robotStates() {
        return robotStates;
    }

    public BulletState[] bulletStates() {
        return bulletStates;
    }

    public ObstacleState[] obstacleStates() {
        return obstacleStates;
    }

    private Color colorForRobot(short id) {
        return colorMap.get(id);
    }
}
