package server;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import common.Constants;
import server.Room.GameAlreadyStartedException;
import server.sim.PhysicsEntity;
import server.sim.Sim;

/**
 * The <code>Manager</code> manages (wow!) the rooms of the game as well as
 * messages that come from the {@link server.TcpServer}. It is also responsible
 * for putting robots into the {@link server.Room}.
 */
public class Manager {

    private Map<Short, Room> rooms; // Room id --> Room
    private Map<Short, Room> robotRooms; // Robot id --> Room
    private Map<SelectionKey, Short> idMap;
    private short id = 0;

    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

    public Manager() {
        rooms = new HashMap<Short, Room>();
        robotRooms = new HashMap<Short, Room>();
        // Used both by the main thread (doing simulation) and the networking
        // thread (adding robots to the room) so make sure to use synchronized
        // with this
        idMap = new HashMap<SelectionKey, Short>();
    }


    public static class ParseException extends RuntimeException {
        private static final long serialVersionUID = -7788243103185404249L;

        public ParseException(String message) {
            super(message);
        }
    }

    private void handleRobotJoin(ByteBuffer bb, TcpServer server, SelectionKey key, SocketChannel channel) throws IOException {
        short roomId = bb.getShort();
        Color robotColor = new Color(bb.get() & 0xFF, bb.get() & 0xFF, bb.get() & 0xFF);
        PhysicsEntity ent = new PhysicsEntity(id, robotColor,
                                                Math.random() * Constants.World.WIDTH,
                                                Math.random() * Constants.World.HEIGHT,
                                                Math.random() * 2 * Math.PI,
                                                Constants.Robot.RADIUS,
                                                Constants.Robot.MASS);
        Room room = rooms.get(roomId);
        if (room == null) {
            ByteBuffer out = ByteBuffer.allocate(1);
            out.put((byte) 65);
            out.rewind();
            channel.write(out);
            throw new ParseException("Cannot add a robot to a nonexistent room with id " + id + ".");
        }

        synchronized (idMap) {

            try {
                boolean addResult = room.addRobot(ent);

                // Execute these lines after addRobot but before the if
                // statement so that they aren't executed if a
                // GameAlreadyStartedException is thrown but are executed before
                // the code in the if statement

                robotRooms.put(id, room);
                idMap.put(key, id);

                if (addResult) {
                    LOGGER.fine("Sending initial states to relevant robots");
                    ByteBuffer message = ByteBuffer.wrap(room.initialStae());
                    for (Map.Entry<SelectionKey, Short> connection : idMap.entrySet()) {
                        if (room.equals(robotRooms.get(connection.getValue()))) {
                            TcpServer.sendToKey(connection.getKey(), message);
                            message.rewind();
                        }
                    }

                    LOGGER.fine("Telling robots in room with id " + room.getId() + " that the game has begun");
                    for (Map.Entry<SelectionKey, Short> connection : idMap.entrySet()) {
                        if (room.equals(robotRooms.get(connection.getValue()))) {
                            LOGGER.finer("Telling " + connection.getKey().attachment());
                            ByteBuffer out = ByteBuffer.allocate(3);
                            out.put((byte) 64);
                            out.putShort(connection.getValue());
                            out.rewind();
                            TcpServer.sendToKey(connection.getKey(), out);
                        }
                    }
                }
            } catch (GameAlreadyStartedException e) {
                ByteBuffer out = ByteBuffer.allocate(1);
                out.put((byte) 66);
                out.rewind();
                channel.write(out);
                throw new ParseException("Robot with id " + id + " tried to join a room with id " +
                                         room.getId() + " that already started its game");
            }
        }
        id++;
    }

    private void handleRobotUpdate(ByteBuffer bb, TcpServer server, SelectionKey key, SocketChannel channel) throws IOException {
        short robotId = bb.getShort();
        synchronized (idMap) {
            if (robotId != idMap.get(key)) {
                throw new ParseException(key.attachment() + " does not have permission to edit robot with id " + id + ".");
            }
        }
        PhysicsEntity ent = robotRooms.get(robotId).getRobot(robotId);
        if (ent == null) {
            throw new ParseException("Invalid robot ID " + robotId + ".");
        }

        ent.setAcceleration(bb.getFloat()/1e6, bb.getFloat()/1e6);
        ent.setRotation((bb.get() & 0xFF) / 255.0 * 2 * Math.PI);
    }

    public void handleSent(byte[] b, TcpServer server, SelectionKey key, SocketChannel channel) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(b);
        int mType = bb.get() & 0xFF;
        switch (mType) {
            case 128: handleRobotJoin(bb, server, key, channel); break;
            case 129: handleRobotUpdate(bb, server, key, channel); break;
            default:  throw new ParseException("Unknown message type " + mType + ".");
        }
    }

    public void broadcastRoomState(TcpServer server, Room room, byte[] state, Map<Short, byte[]> velocityStates) {
        synchronized (idMap) {
            for (Map.Entry<SelectionKey, Short> connection : idMap.entrySet()) {
                if (room.equals(robotRooms.get(connection.getValue()))) {
                    ByteBuffer msgBuf = ByteBuffer.allocate(state.length + 8);
                    msgBuf.put(state, 0, 2);
                    short id = connection.getValue();
                    msgBuf.put(velocityStates.get(id));
                    // if (id == null) {
                    //     msgBuf.position(msgBuf.position() + 8);
                    // } else {
                    //     msgBuf.put(velocityStates.get(id));
                    // }
                    msgBuf.put(state, 2, state.length - 2);
                    msgBuf.rewind();
                    TcpServer.sendToKey(connection.getKey(), msgBuf);
                }
            }
        }
    }

    public void addRoom(Room room) {
        room.setManager(this);
        rooms.put(room.getId(), room);
    }

    /**
     * Attempts to remove a room. If there are any occupants in the room, the
     * room will not be removed and the method will return false (as opposed to
     * true).
     */
    public boolean removeRoom(Room room) {
        if (room.occupancy() > 0) {
            return false;
        }
        rooms.remove(room.getId(), room);
        return true;
    }

    /**
     * Runs an infite loop where each room will tick.
     *
     * @param server    The server to send state update messages through
     */
    public void loopTickAllRoom(TcpServer server) {
        while (true) {
            long totalTickTime =0;
            for (Room room : rooms.values()) {
                totalTickTime += room.tick(server);
            }

            try {
                Thread.sleep(Math.max(0, (Sim.MIN_TICK_LENGTH-totalTickTime)/(long)1e6));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the closing of a session by a client by the given <code>key</code>.
     */
    public void handleClosed(SelectionKey key) {
        synchronized (idMap) {
            Short robotId = idMap.get(key);
            if (robotId != null) {
                if (robotRooms.get(robotId).removeRobotById(robotId)) {
                    LOGGER.finer("Removed robot with id " + robotId + " from the room since the client closed its session");
                }
                idMap.remove(key);
            }
        }
    }
}
