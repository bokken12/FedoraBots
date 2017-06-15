package client;

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

    public GameManager(GameNetworkAdapter adapter) {
        adapter.setManager(this);
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
}
