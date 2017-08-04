package client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import client.GameState.HealthMapState;
import javafx.scene.paint.Color;

/**
 * Responsible for keeping track of the current game status
 */
public class GameManager {
    private Collection<Consumer<GameState>> stateListeners = new ArrayList<Consumer<GameState>>();
    private Collection<Consumer<GameState>> beginListeners = new ArrayList<Consumer<GameState>>();
    private Collection<Consumer<GameState>> endListeners = new ArrayList<Consumer<GameState>>();
    private Collection<BiConsumer<Double, Double>> velocityListeners = new ArrayList<BiConsumer<Double, Double>>();
    private Collection<Consumer<Map<Short, HealthMapState>>> healthListeners = new ArrayList<Consumer<Map<Short, HealthMapState>>>();
    private Collection<Consumer<Map<Byte, Byte>>> obstacleListeners = new ArrayList<Consumer<Map<Byte, Byte>>>();
    private Map<Short, Color> colors;
    private GameAdapter adapter;

    private void setAdapterClass(Class<? extends GameAdapter> adapterClass) {
        try {
            adapter = adapterClass.newInstance();
            adapter.setManager(this);
            Thread t = new Thread(adapter);
            t.setDaemon(true);
            t.start();
        } catch (IllegalAccessException|InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        }
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

    public void addVelocityListener(BiConsumer<Double, Double> listener) {
        velocityListeners.add(listener);
    }

    public void addHealthListener(Consumer<Map<Short, HealthMapState>> listener) {
        healthListeners.add(listener);
    }

    public void addObstacleListener(Consumer<Map<Byte, Byte>> listener) {
        obstacleListeners.add(listener);
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

    public void updateRobotVelocity(double vx, double vy) {
        for (BiConsumer<Double, Double> vl : velocityListeners) {
            vl.accept(vx, vy);
        }
    }

    public void updateHealths(Map<Short, HealthMapState> healths) {
        for (Consumer<Map<Short, HealthMapState>> hl : healthListeners) {
            hl.accept(healths);
        }
    }

    public void updateObstacles(Map<Byte, Byte> obstacles) {
        for (Consumer<Map<Byte, Byte>> ol : obstacleListeners) {
            ol.accept(obstacles);
        }
    }

    public short joinNetworkGame(Robot robot, short roomId) {
        setAdapterClass(GameNetworkAdapter.class);
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

    public void spectateNetworkGame(short roomId) {
        setAdapterClass(GameNetworkAdapter.class);
        try {
            adapter.sendSpectate(roomId);
        } catch (IOException e) {
            throw new RuntimeException("Could not spectate a game because of a network error");
        }
        adapter.waitForSpectateOk();
    }

    public short joinLocalGame(Robot robot, int difficulty) {
        setAdapterClass(GameSimAdapter.class);
        Color c = robot.getColor();
        try {
            adapter.sendJoin((short) difficulty,
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

    public void sendRobotShootRequest(short robotId) {
        try {
            adapter.sendRobotShootRequest(robotId);
        } catch (IOException e) {
            System.out.println("Warning: Could not make the robot shoot because of a network error");
        }
    }
}
