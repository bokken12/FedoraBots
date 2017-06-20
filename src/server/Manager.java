package server;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import common.Constants;
import server.sim.PhysicsEntity;
import server.sim.World;

public class Manager {
    private World world;
    private Map<Short, PhysicsEntity> entities;
    private short id = 0;

    public Manager(World w) {
        this.world = w;
        entities = new HashMap<Short, PhysicsEntity>();
    }


    public static class ParseException extends RuntimeException {
        private static final long serialVersionUID = -7788243103185404249L;

        public ParseException(String message) {
            super(message);
        }
    }

    public ByteBuffer handleSent(byte[] b, TcpServer server) {
        int mType = b[0] & 0xFF;
        switch (mType) {
            case 128: {
                Color robotColor = new Color(b[1] & 0xFF, b[2] & 0xFF, b[3] & 0xFF);
                PhysicsEntity ent = new PhysicsEntity(id, robotColor,
                                                      Math.random() * Constants.World.WIDTH,
                                                      Math.random() * Constants.World.HEIGHT,
                                                      Math.random() * 2 * Math.PI,
                                                      Constants.Robot.RADIUS,
                                                      Constants.Robot.MASS);
                entities.put(id, ent);
                world.add(ent);

                ByteBuffer bb = ByteBuffer.allocate(3);
                bb.put((byte) 64);
                bb.putShort(id);
                id++;
                return bb;
            }
            case 129: {
                ByteBuffer bb = ByteBuffer.wrap(b, 1, b.length-1);
                short robotId = bb.getShort();
                PhysicsEntity ent = entities.get(robotId);
                if (ent == null) {
                    throw new ParseException("Invalid robot ID " + robotId + ".");
                }

                ent.setAcceleration(bb.getFloat()/1e6, bb.getFloat()/1e6);
                ent.setRotation((bb.get() & 0xFF) / 255.0 * 2 * Math.PI);
                return null;
            }
            default: {
                throw new ParseException("Unknown message type " + mType + ".");
            }
        }
    }
}
