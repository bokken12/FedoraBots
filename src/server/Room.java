package server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import server.sim.PhysicsEntity;
import server.sim.Sim;
import server.sim.World;

/**
 * A room hosts games of a specific configuration. It handles robots joining it, and starts the game when enough robots have joined.
 */
public class Room {
    private int nRobots;
    private World world;
    private Sim sim;
    private short id;
    private Map<Short, PhysicsEntity> entities;
    private boolean gameStarted = false;
    private Manager manager;

    private static short globalId;
    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

    /**
     * Creates a room that holds <code>robotLimit</code> robots in a given {@link server.sim.World}.
     */
    public Room(int robotLimit, World w) {
        nRobots = robotLimit;
        world = w;
        sim = new Sim(world);
        id = globalId++;
        entities = new HashMap<Short, PhysicsEntity>();
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
    public boolean addRobot(PhysicsEntity robot) {
        LOGGER.fine("Adding robot with id " + robot.getId() + " to room with id " + id);
        entities.put(robot.getId(), robot);
        world.add(robot);

        LOGGER.info("Room with id " + id + " has " + entities.size() + "/" + nRobots + " robots");

        if (entities.size() >= nRobots) {
            gameStarted = true;
            LOGGER.info("Starting game in room with id " + id + "!");
        }
        return gameStarted;
    }

    /**
     * Returns the number of robots in the room.
     */
    public int occupancy() {
        return entities.size();
    }

    /**
     * Returns all the robots in the room, mapped to their ids.
     */
    public Map<Short, PhysicsEntity> robotsById() {
        return entities;
    }

    /**
     * Returns the robot with the given id in the room.
     */
    public PhysicsEntity getRobot(short id) {
        return entities.get(id);
    }

    /**
     * Tells the room's manager to send a state update message over the given server.
     */
    public void broadcastState(TcpServer server, byte[] state, Map<Short, byte[]> velocityStates) {
        manager.broadcastRoomState(server, this, state, velocityStates);
    }

    /**
     * Dispatches the sim's tick method if the game has started, returning the
     * amount of time the tick took.
     *
     * @param server    The server to send state update messages through
     */
    public long tick(TcpServer server) {
        if (gameStarted) {
            return sim.tick(tick -> broadcastState(server, world.state(), world.velocityStates()));
        } else {
            return 0;
        }
    }

    /**
     * Returns a representation of the initial state of the world
     */
    public byte[] initialStae() {
        return world.startingState();
    }

}
