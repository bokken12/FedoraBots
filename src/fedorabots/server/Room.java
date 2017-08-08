package fedorabots.server;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fedorabots.common.Profiler;
import javafx.geometry.Point2D;
import fedorabots.server.sim.Sim;
import fedorabots.server.sim.entity.Bullet;
import fedorabots.server.sim.entity.Obstacle;
import fedorabots.server.sim.entity.Robot;
import fedorabots.server.sim.world.World;

/**
 * A room hosts games of a specific configuration. It handles robots joining it, and starts the game when enough robots have joined.
 */
public class Room {
    private int nRobots;
    private World world;
    private Sim sim;
    private short id;
    private Map<Short, Robot> robots;
    private Collection<Obstacle> obstacles;
    private boolean gameStarted = false;
    private Manager manager;

    private static short globalId;
    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

    /**
     * An exception thrown when a robot tries to join a room in which the game
     * has already begun.
     */
    public static class GameAlreadyStartedException extends Exception {
        private static final long serialVersionUID = 6929883604798800075L;

        public GameAlreadyStartedException(String msg) {
            super(msg);
        }
    }

    /**
     * Creates a room that holds <code>robotLimit</code> robots in a given {@link server.sim.world.World}.
     */
    public Room(int robotLimit, World w) {
        nRobots = robotLimit;
        world = w;
        sim = new Sim(world);
        id = globalId++;
        robots = new HashMap<Short, Robot>();
        obstacles = world.getObstacles();
    }

    /**
     * Sets the {@link server.Manager} that the room will use to dispatch state messages through.
     */
    public void setManager(Manager m) {
        manager = m;
    }

    /**
     * Returns the id of the room, which in most cases (if there are less than
     * about 65,000 rooms present) will be unique.
     */
    public short getId() {
        return id;
    }

    /**
     * Adds a robot to the room, returning true if the room is full and the game
     * should be started.
     */
    public boolean addRobot(Robot robot) throws GameAlreadyStartedException {
        LOGGER.fine("Adding robot with id " + robot.getId() + " to room with id " + id);
        if (gameStarted) {
            throw new GameAlreadyStartedException("Room with id " + id + " is already full");
        }
        robots.put(robot.getId(), robot);
        synchronized (world) {
            world.add(robot);
        }

        LOGGER.info("Room with id " + id + " has " + robots.size() + "/" + nRobots + " robots");

        if (robots.size() >= nRobots) {
            gameStarted = true;
            LOGGER.info("Starting game in room with id " + id + "!");
        }
        return gameStarted;
    }

    /**
     * Removes a robot from the room, returning {@code true} if the robot was removed.
     * Note that robots can only be removed before the game has started.
     */
    public boolean removeRobotById(short robotId) {
        if (!gameStarted) {
            Robot ent = robots.remove(robotId);
            if (ent != null) {
                world.remove(ent);
                LOGGER.info("Room with id " + id + " has " + robots.size() + "/" + nRobots + " robots");
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a bullet to the room's world
     */
    public void addBullet(Bullet b) {
        synchronized (world) {
            world.add(b);
        }
    }

    /**
     * Adds an obstacle to the room's world.
     *
     * If the obstacle's location is (-1, -1), it's location will be
     * automatically generated.
     */
    public void addObstacle(Obstacle o) {
        if (o.getX() == -1 && o.getY() == -1) {
            Point2D location = RoomLayout.getLocation(this);
            o.setPositionUnsafe(location.getX(), location.getY());
        }
        synchronized (world) {
            world.add(o);
        }
        obstacles.add(o);
    }

    /**
     * Returns the number of robots in the room.
     */
    public int occupancy() {
        return robots.size();
    }

    /**
     * Returns a boolean indicating whether the game has started
     */
    public boolean hasStarted() {
        return gameStarted;
    }

    /**
     * Returns all the robots in the room, mapped to their ids.
     */
    public Map<Short, Robot> robotsById() {
        return robots;
    }

    /**
     * Returns the robot with the given id in the room.
     */
    public Robot getRobot(short id) {
        return robots.get(id);
    }

    /**
     * Tells the room's manager to send a state update message over the given server.
     */
    public void broadcastState(TcpServer server, Collection<Robot> robots) {
        manager.broadcastRoomState(server, this, robots, world);
    }

    /**
     * Dispatches the sim's tick method if the game has started, returning the
     * amount of time the tick took.
     *
     * @param server    The server to send state update messages through
     */
    public long tick(TcpServer server) {
        if (gameStarted) {
            synchronized (world) {
                return sim.tick(tick -> {
                    Profiler.time("Broadcast state");
                    Collection<Robot> rvs = robots.values();
                    broadcastState(server, rvs);
                    Profiler.timeEnd("Broadcast state");
                    List<Robot> robotsChangedHealth = world.healthChangedRobots(rvs);
                    if (robotsChangedHealth.size() > 0) {
                        manager.broadcastBuf(server, this, world.healthStates(robotsChangedHealth));
                        for (Robot robot : robotsChangedHealth) {
                            if (robot.getHealth() == 0) {
                                robots.remove(robot.getId());
                            }
                        }
                    }
                    List<Obstacle> obstaclesChangedRotation = world.rotationChangedObstacles(obstacles);
                    if (obstaclesChangedRotation.size() > 0) {
                        manager.broadcastBuf(server, this, world.obstacleStates(obstaclesChangedRotation));
                    }
                });
            }
        } else {
            return 0;
        }
    }

    /**
     * Returns a representation of the initial state of the world
     */
    public ByteBuffer initialState() {
        Collection<Robot> rvs = robots.values();
        ByteBuffer buf = ByteBuffer.allocate(World.initialStateLength(rvs, obstacles) + 3);
        buf.put((byte) 0);
        buf.put((byte) rvs.size());
        buf.put((byte) obstacles.size());
        world.writeStartingState(buf, rvs, obstacles);
        return buf;
    }

}
