package fedorabots.server;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import fedorabots.common.Constants;
import fedorabots.common.Profiler;
import javafx.geometry.Point2D;
import fedorabots.server.Room.GameAlreadyStartedException;
import fedorabots.server.TcpServer.Handler;
import fedorabots.server.sim.Sim;
import fedorabots.server.sim.entity.Bullet;
import fedorabots.server.sim.entity.Robot;
import fedorabots.server.sim.world.World;

/**
 * The <code>Manager</code> manages (wow!) the rooms of the game as well as
 * messages that come from the {@link fedorabots.server.TcpServer}. It is also responsible
 * for putting robots into the {@link fedorabots.server.Room}.
 */
public class Manager {

    private Map<Short, Room> rooms; // Room id --> Room
    private Map<Short, Room> robotRooms; // Robot id --> Room
    private Map<Handler, Short> idMap;
    private Map<Handler, Room> spectatorMap;
    private short id = 0;

    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

    public Manager() {
        rooms = new HashMap<Short, Room>();
        robotRooms = new HashMap<Short, Room>();
        // Used both by the main thread (doing simulation) and the networking
        // thread (adding robots to the room) so make sure to use synchronized
        // with this
        idMap = new HashMap<Handler, Short>();
        spectatorMap = new HashMap<Handler, Room>();
    }


    public static class ParseException extends RuntimeException {
        private static final long serialVersionUID = -7788243103185404249L;

        public ParseException(String message) {
            super(message);
        }
    }

    private void handleRobotJoin(ByteBuffer bb, TcpServer server, Handler handle) throws IOException {
        LOGGER.info("Handling robot join from " + handle + ".");
        short roomId = bb.getShort();
        Color robotColor = new Color(bb.get() & 0xFF, bb.get() & 0xFF, bb.get() & 0xFF);
        Room room = rooms.get(roomId);
        if (room == null) {
            //ByteBuffer out = ByteBuffer.allocate(1);
            //out.put((byte) 65);
            //out.rewind();
            handle.getOut().write(65);
            handle.getOut().flush();
            throw new ParseException("Cannot add a robot to a nonexistent room with id " + roomId + ".");
        }
        Point2D location = RoomLayout.getLocation(room);
        Robot ent = new Robot(id, robotColor,
                              location.getX(),
                              location.getY(),
                              Math.random() * 2 * Math.PI,
                              Constants.Robot.RADIUS,
                              Constants.Robot.MASS);

        synchronized (idMap) {

            try {
                boolean gameStarting = room.addRobot(ent);

                // Execute these lines after addRobot but before the if
                // statement so that they aren't executed if a
                // GameAlreadyStartedException is thrown but are executed before
                // the code in the if statement

                robotRooms.put(id, room);
                idMap.put(handle, id);
                LOGGER.info("creating robot with id " + id + " of handler " + handle);

                if (gameStarting) {
                    LOGGER.fine("Sending initial states to relevant robots");
                    ByteBuffer message = room.initialState();
                    Iterator<Map.Entry<Handler, Short>> iter = idMap.entrySet().iterator();
                    while(iter.hasNext()){
                    	Map.Entry<Handler, Short> connection = iter.next();
                    	if (room.equals(robotRooms.get(connection.getValue()))) {
                            message.rewind();
                            if(!TcpServer.sendToHandle(connection.getKey(), message, this))
                            	iter.remove();
                        }
                    }
                    Iterator<Map.Entry<Handler, Room>> iter2 = spectatorMap.entrySet().iterator();
                    while(iter2.hasNext()){
                    	Map.Entry<Handler, Room> connection = iter2.next();
                    	if (room.equals(connection.getValue())) {
                            message.rewind();
                            if(!TcpServer.sendToHandle(connection.getKey(), message, this))
                            	iter2.remove();
                        }
                    }

                    LOGGER.fine("Telling robots in room with id " + room.getId() + " that the game has begun");
                    Iterator<Map.Entry<Handler, Short>> iter3 = idMap.entrySet().iterator();
                    while(iter3.hasNext()){
                    	Map.Entry<Handler, Short> connection = iter3.next();
                    	if (room.equals(robotRooms.get(connection.getValue()))) {
                            LOGGER.finer("Telling " + handle);
                            connection.getKey().getOut().write(64);
                            short id2 = connection.getValue();
                            connection.getKey().getOut().write((id2 >> 8) & 0xff);
                            connection.getKey().getOut().write(id2 & 0xff);
                            connection.getKey().getOut().flush();
                        }
                    }
                }
            } catch (GameAlreadyStartedException e) {
                handle.getOut().write(66);
                handle.getOut().flush();
                throw new ParseException("Robot with id " + id + " tried to join a room with id " +
                                         room.getId() + " that already started its game");
            }
        }
        id++;
    }

    private void handleRobotUpdate(ByteBuffer bb, TcpServer server, Handler handle) throws IOException {
        LOGGER.fine("Handling robot update from " + handle + ".");
        short robotId = bb.getShort();
        synchronized (idMap) {
            if (robotId != idMap.get(handle)) {
                throw new ParseException(handle + " does not have permission to edit robot with id " + robotId +
                                         " (the handler can only edit robot with id " + idMap.get(handle) + ").");
            }
        }
        Robot ent = robotRooms.get(robotId).getRobot(robotId);
        if (ent == null) {
            throw new ParseException("Invalid robot ID " + robotId + ".");
        }

        double ax = bb.getFloat()/1e6;
        double ay = bb.getFloat()/1e6;
        if (!Double.isFinite(ax) || !Double.isFinite(ay)) {
            throw new ParseException(handle + " set a robot with id " + robotId + " to have a non-finite acceleration.");
        }
        ent.setAcceleration(ax, ay);
        ent.setRotation((bb.getShort() & 0xFFFF) * 1.0 / (Short.MAX_VALUE-Short.MIN_VALUE) * 2 * Math.PI);
    }

    private void handleRobotShoot(ByteBuffer bb, TcpServer server, Handler handle) throws IOException {
        LOGGER.fine("Handling robot shoot from " + handle + ".");
        short robotId = bb.getShort();
        synchronized (idMap) {
            if (robotId != idMap.get(handle)) {
                throw new ParseException(handle + " does not have permission to edit robot with id " + robotId +
                                         " (the handler can only edit robot with id " + idMap.get(handle) + ").");
            }
        }

        Room room = robotRooms.get(robotId);
        Robot robot = room.getRobot(robotId);
        if (robot == null) {
            throw new ParseException("Invalid robot ID " + robotId + ".");
        }

        double rotation = -robot.getRotation() + Math.PI / 2;
        double vx = Constants.Bullet.VELOCITY/1e3 * Math.cos(rotation);
        double vy = - (Constants.Bullet.VELOCITY/1e3 * Math.sin(rotation));
        double dist = (Constants.Robot.RADIUS + Constants.Bullet.RADIUS) * 1.3;
        double x = robot.getX() + dist * Math.cos(rotation);
        double y = robot.getY() - dist * Math.sin(rotation);
        room.addBullet(new Bullet(x, y, Constants.Bullet.RADIUS, Constants.Bullet.MASS, vx, vy));
    }

    private void handleDisplayJoin(ByteBuffer bb, TcpServer server, Handler handle) throws IOException {
        LOGGER.fine("Handling display join from " + handle + ".");
        short roomId = bb.getShort();

        Room room = rooms.get(roomId);
        if (room == null) {
            handle.getOut().write(65);
            handle.getOut().flush();
            throw new ParseException("Cannot add a robot to a nonexistent room with id " + id + ".");
        }

        // Send the initial state to the display if the game has started already
        if (room.hasStarted()) {
            ByteBuffer message = room.initialState();
            message.rewind();
            handle.getOut().write(message.array(), 0, message.limit());
            handle.getOut().flush();
        }

        handle.getOut().flush();


        synchronized (idMap) {
            spectatorMap.put(handle, room);
        }
    }

    public void handleSent(ByteBuffer bb, TcpServer server, Handler handle) throws IOException {
        int mType = bb.get() & 0xFF;
        switch (mType) {
            case 128: handleRobotJoin(bb, server, handle); break;
            case 129: handleRobotUpdate(bb, server, handle); break;
            case 130: handleRobotShoot(bb, server, handle); break;
            case 192: handleDisplayJoin(bb, server, handle); break;
            default:  throw new ParseException("Unknown message type " + mType + ".");
        }
    }

    public static int messageLength(int mType) {
        switch (mType) {
            case 128: return 5;
            case 129: return 12;
            case 130: return 2;
            case 192: return 2;
            default:  throw new ParseException("Unknown message type " + mType + ".");
        }
    }

    public void broadcastRoomState(TcpServer server, Room room, Collection<Robot> robots, World world) {
        Profiler.time("Generate state");
        Profiler.time("Get bullets");
        Collection<Bullet> bullets = world.getBullets();
        Profiler.timeEnd("Get bullets");
        ByteBuffer msgBuf = ByteBuffer.allocate(World.stateLength(robots, bullets) + 4);
        msgBuf.put((byte) 1);
        msgBuf.put((byte) robots.size());
        msgBuf.putShort((short) bullets.size());
        msgBuf.position(msgBuf.position() + 8); // Skip velocity states
        world.writeState(msgBuf, robots);
        world.writeBulletStates(msgBuf, bullets);

        Map<Short, byte[]> velocityStates = world.velocityStates(robots);
        Profiler.timeEnd("Generate state");

        synchronized (idMap) {
            Profiler.time("Send state");
            Iterator<Map.Entry<Handler, Short>> iter = idMap.entrySet().iterator();
            while(iter.hasNext()){
            	Map.Entry<Handler, Short> connection = iter.next();
            	if (room.equals(robotRooms.get(connection.getValue()))) {
                    short id = connection.getValue();
                    if (velocityStates.get(id) != null) {
                        msgBuf.position(4);
                        msgBuf.put(velocityStates.get(id));
                    }
                    msgBuf.rewind();
                    if(!TcpServer.sendToHandle(connection.getKey(), msgBuf, this))
                    	iter.remove();
                }
            }
            Iterator<Map.Entry<Handler, Room>> iter2 = spectatorMap.entrySet().iterator();
            while(iter2.hasNext()){
            	Map.Entry<Handler, Room> connection = iter2.next();
            	if (room.equals(connection.getValue())) {
                    msgBuf.rewind();
                    if(!TcpServer.sendToHandle(connection.getKey(), msgBuf, this))
                    	iter.remove();
                }

            }
            Profiler.timeEnd("Send state");
        }
    }

    public void broadcastBuf(TcpServer server, Room room, ByteBuffer msgBuf) {
        synchronized (idMap) {
        	Iterator<Map.Entry<Handler, Short>> iter = idMap.entrySet().iterator();
        	while(iter.hasNext()){
        		Map.Entry<Handler, Short> connection = iter.next();
        		if (room.equals(robotRooms.get(connection.getValue()))) {
                    msgBuf.rewind();
                    if(!TcpServer.sendToHandle(connection.getKey(), msgBuf, this))
                    	iter.remove();
                }
        	}
        	Iterator<Map.Entry<Handler, Room>> iter2 = spectatorMap.entrySet().iterator();
        	while(iter.hasNext()){
        		Map.Entry<Handler, Room> connection = iter2.next();
        		if (room.equals(connection.getValue())) {
                    msgBuf.rewind();
                    if(!TcpServer.sendToHandle(connection.getKey(), msgBuf, this))
                    	iter2.remove();
                }
        	}
        }
    }

    public void addRoom(Room room) {
        room.setManager(this);
        synchronized (rooms) {
            rooms.put(room.getId(), room);
        }
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
        synchronized (rooms) {
            rooms.remove(room.getId(), room);
        }
        return true;
    }

    /**
     * Runs an infite loop where each room will tick.
     *
     * @param server      The server to send state update messages through
     * @param reloadRooms Whether rooms should be recreated once they end
     */
    public void loopTickAllRooms(TcpServer server, boolean reloadRooms) {
        while (true) {
            long totalTickTime = 0;
            synchronized (rooms) {
                for (Map.Entry<Short, Room> ent : rooms.entrySet()) {
                    Room room = ent.getValue();
                    if (room.hasStarted()) {
                        totalTickTime += room.tick(server);
                    }
                    if (reloadRooms && room.hasEnded()) {
                        LOGGER.info("Resetting room with id " + room.getId());
                        ent.setValue(room.resetCopy());
                    }
                }
            }

            try {
                Thread.sleep(Math.max(0, (Sim.MIN_TICK_LENGTH-totalTickTime)/(long)1e6));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClosed(Handler handler) {
        synchronized (idMap) {
            Short robotId = idMap.get(handler);
            if (robotId != null) {
                if (robotRooms.get(robotId).removeRobotById(robotId)) {
                    LOGGER.finer("Removed robot with id " + robotId + " from the room since the client closed its session");
                }
                idMap.remove(handler);
            }
            spectatorMap.remove(handler);
        }
    }
}
