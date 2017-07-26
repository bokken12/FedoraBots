package client;

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
     * Creates a GameState with the specified robot states and bullet states.
     * Note that these robot states will be modified as a side effect.
     */
    public GameState(RobotState[] robotStates, BulletState[] bulletStates) {
        this(robotStates);
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

    public void setColorMap(Map<Short, Color> cmap) {
        colorMap = cmap;
    }

    public RobotState[] robotStates() {
        return robotStates;
    }

    public BulletState[] bulletStates() {
        return bulletStates;
    }

    private Color colorForRobot(short id) {
        return colorMap.get(id);
    }
}
