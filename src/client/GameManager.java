package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.scene.paint.Color;

/**
 * Responsible for keeping track of the current game status
 */
public class GameManager {
    // private Collection<Consumer<GameState>> stateListeners = new ArrayList<Consumer<GameState>>();
    // private Collection<Consumer<GameState>> beginListeners = new ArrayList<Consumer<GameState>>();
    // private Collection<Consumer<GameState>> endListeners = new ArrayList<Consumer<GameState>>();
    // private Collection<BiConsumer<Double, Double>> velocityListeners = new ArrayList<BiConsumer<Double, Double>>();
    private volatile Consumer<GameState> stateListener = null;
    private volatile Consumer<GameState> beginListener = null;
    private volatile Consumer<GameState> endListener = null;
    private volatile BiConsumer<Double, Double> velocityListener = null;
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
        stateListener = listener;
        // stateListeners.add(listener);
        // System.out.println(stateListeners);
    }

    public void addBeginListener(Consumer<GameState> listener) {
        System.out.println(toString());
        System.out.println(Thread.currentThread());
        Thread.dumpStack();
        beginListener = listener;
        // beginListeners.add(listener);
    }

    public void addEndListener(Consumer<GameState> listener) {
        endListener = listener;
        // endListeners.add(listener);
    }

    public void addVelocityListener(BiConsumer<Double, Double> listener) {
        velocityListener = listener;
        // velocityListeners.add(listener);
    }

    public void startGame(GameState st, Map<Short, Color> colorMap) {
        colors = colorMap;
        st.setColorMap(colors);
        // System.out.println("Begin: " + beginListeners);
        // for (Consumer<GameState> bl : beginListeners) {
            // bl.accept(st);
        // }
        System.out.println(toString());
        System.out.println(Thread.currentThread());
        Thread.dumpStack();
        beginListener.accept(st);
    }

    public void updateState(GameState st) {
        st.setColorMap(colors);
        // System.out.println("State: " + stateListeners);
        // for (Consumer<GameState> sl : stateListeners) {
            // sl.accept(st);
        // }
        stateListener.accept(st);
    }

    public void updateRobotVelocity(double vx, double vy) {
        // for (BiConsumer<Double, Double> vl : velocityListeners) {
            // vl.accept(vx, vy);
        // }
        velocityListener.accept(vx, vy);
    }

    public short joinGame(Robot robot, short roomId) {
        Color c = robot.getColor();
        try {
            adapter.sendJoin(roomId,
                            (byte)(c.getRed() * 255),
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
