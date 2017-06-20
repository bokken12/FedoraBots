package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import javafx.scene.paint.Color;

/**
 * Responsible for keeping track of the current game status
 */
public class GameManager {
    private Collection<Consumer<GameState>> stateListeners = new ArrayList<Consumer<GameState>>();
    private Collection<Consumer<GameState>> beginListeners = new ArrayList<Consumer<GameState>>();
    private Collection<Consumer<GameState>> endListeners = new ArrayList<Consumer<GameState>>();
    private Map<Short, Color> colors;
    private GameNetworkAdapter adapter;

    public GameManager(GameNetworkAdapter adapter) {
        adapter.setManager(this);
        this.adapter = adapter;
        Thread t = new Thread(adapter);
        t.setDaemon(true);
        t.start();
    }

    public void addRobot(short robot, Color color) {
        if (colors.put(robot, color) == null) {
            throw new RuntimeException("Cannot override the color of the robot with ID " + robot + ".");
        }
    }

    public void removeRobot(short robot) {
        if (colors.remove(robot) == null) {
            throw new RuntimeException("Robot with ID " + robot + " was not previously in a game.");
        }
    }

    public void addStateListener(Consumer<GameState> listener) {
        stateListeners.add(listener);
    }

    public void addBeginListener(Consumer<GameState> listener) {
        beginListeners.add(listener);
    }

    public void addEndListener(Consumer<GameState> listener) {
        endListeners.add(listener);
    }

    public void startGame(GameState st, Map<Short, Color> colorMap) {
        colors = colorMap;
        st.setColorMap(colors);
        for (Consumer<GameState> bl : beginListeners) {
            bl.accept(st);
        }
    }

    public void updateState(GameState st) {
        st.setColorMap(colors);
        for (Consumer<GameState> sl : stateListeners) {
            sl.accept(st);
        }
    }

    public short joinGame(Robot robot) {
        Color c = robot.getColor();
        try {
            adapter.sendJoin((byte)(c.getRed() * 255),
                            (byte)(c.getGreen() * 255),
                            (byte)(c.getBlue() * 255));
        } catch (IOException e) {
            throw new RuntimeException("Could not join a game because of a network error");
        }
        return adapter.getRobotId();
    }

    public void sendRobotUpdate(short robotId, Robot robot) {
        try {
            adapter.sendRobotUpdate(robotId, robot.getAx(), robot.getAy(), robot.getRotation());
        } catch (IOException e) {
            System.out.println("Warning: Could not update the robot's state because of a network error");
        }
    }
}
