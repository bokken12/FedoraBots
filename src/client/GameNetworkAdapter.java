package client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javafx.scene.paint.Color;

/**
 * Receives messages from the network and notifies the game manager accordingly.
 */
public class GameNetworkAdapter implements Runnable {

    private Semaphore awaitingId = new Semaphore(1);
    private Semaphore awaitingSpectateOk = new Semaphore(1);

    private Socket s;
    private InputStream inp;
    private volatile GameManager g;

    private short robotId;

    public GameNetworkAdapter() throws IOException {
        s = new Socket(getHost(), 8090);
        inp = s.getInputStream();
    }

    private static String getHost() {
        String host = System.getProperty("server.host");
        if (host == null) {
            return "localhost";
        }
        return host;
    }

    public void setManager(GameManager manager) {
        g = manager;
    }

    public void sendJoin(short roomId, byte r, byte g, byte b) throws IOException {
        try {
            awaitingId.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not acquire awaiting semaphore.");
        }
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte) 128);
        bb.putShort(roomId);
        bb.put(r);
        bb.put(g);
        bb.put(b);
        s.getOutputStream().write(bb.array());
    }

    public void sendSpectate(short roomId) throws IOException {
        try {
            awaitingSpectateOk.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not acquire awaiting semaphore.");
        }
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.put((byte) 192);
        bb.putShort(roomId);
        s.getOutputStream().write(bb.array());
    }

    public void sendRobotUpdate(short id, double ax, double ay, double rotation) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(13);
        bb.put((byte) 129);
        bb.putShort(id);
        bb.putFloat((float) ax);
        bb.putFloat((float) ay);
        bb.putShort((short) Math.round(rotation / 360 * (Short.MAX_VALUE - Short.MIN_VALUE)));
        s.getOutputStream().write(bb.array());
    }

    public void sendRobotShootRequest(short id) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(3);
        bb.put((byte) 130);
        bb.putShort(id);
        s.getOutputStream().write(bb.array());
    }

    public short getRobotId() {
        try {
            awaitingId.acquire();
            return robotId;
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not retrieve robot id.");
        } finally {
            awaitingId.release();
        }
    }

    public void waitForSpectateOk() {
        try {
            awaitingSpectateOk.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not check whether the server is okay with being spectated.");
        } finally {
            awaitingSpectateOk.release();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int mType = inp.read();
                int numEntities = 0;

                int bufferLen = 0;
                if (mType == 0 || mType == 1 || mType == 2) {
                    numEntities = inp.read();

                    if (mType == 0) bufferLen = numEntities * 11;
                    if (mType == 1) {
                        int numBullets = (inp.read() << 8) + inp.read();
                        bufferLen = numEntities * 8 + 8 + numBullets * 4;
                    }
                    if (mType == 2) bufferLen = numEntities * 3;
                } else if (mType == 64) {
                    bufferLen = 2;
                } else if (mType == 3 || mType == 65 || mType == 66) {
                    bufferLen = 0;
                } else {
                    throw new RuntimeException("Unknown message type " + mType + ".");
                }

                byte[] buffer = new byte[bufferLen];
                int i = 0;
                while (i < buffer.length) {
                    i += inp.read(buffer, i, buffer.length - i);
                }

                parseBuffer(mType, buffer, numEntities);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void throwError(String error) {
        new RuntimeException(error).printStackTrace();
        System.exit(1);
    }

    private void parseBuffer(int type, byte[] buffer, int numEntities) {
        switch (type) {
            case 0:  parseStart(buffer); break;
            case 1:  parseState(buffer, numEntities); break;
            case 2:  parseHealths(buffer); break;
            case 3:  parseSpectateOk(buffer); break;
            case 64: parseJoined(buffer); break;
            case 65: throwError("The room the robot tried to join does not exist."); break;
            case 66: throwError("The room the robot tried to join already started its game."); break;
            default: throwError("Invalid message type " + type + ".");
        }
    }

    private void parseStart(byte[] buffer) {
        GameState.RobotState[] state = new GameState.RobotState[buffer.length / 11];
        Map<Short, Color> colors = new HashMap<Short, Color>();

        for (int i = 0; i < buffer.length; i += 11) {
            short id = (short) ((buffer[i] << 8) + buffer[i + 1]);
            int x = (buffer[i + 2] << 4) + (buffer[i + 3] & 0x10);
            int y = (buffer[i + 3] << 8) + buffer[i + 4];
            byte rot = (byte) buffer[i + 5];
            byte vangle = (byte) buffer[i + 6];
            byte aangle = (byte) buffer[i + 7];
            state[i/11] = new GameState.RobotState(id, x, y, rot, vangle, aangle);
            colors.put(id, Color.rgb(buffer[i + 8] & 0xFF, buffer[i + 9] & 0xFF, buffer[i + 10] & 0xFF));
        }
        g.startGame(new GameState(state), colors);
    }

    private void parseState(byte[] buffer, int numEntities) {
        GameState.RobotState[] state = new GameState.RobotState[numEntities];
        GameState.BulletState[] bullets = new GameState.BulletState[(buffer.length - numEntities*8 - 8) / 4];
        ByteBuffer buf = ByteBuffer.wrap(buffer, 0, 8);
        double vx = buf.getFloat();
        double vy = buf.getFloat();
        for (int i = 8; i < numEntities * 8 + 8; i += 8) {
            short id = (short) (((buffer[i] & 0xFF) << 8) + (buffer[i + 1] & 0xFF));
            int x = ((buffer[i + 2] & 0xFF) << 4) + ((buffer[i + 3] & 0xFF) >> 4);
            int y = ((buffer[i + 3] & 0x0F) << 8) + (buffer[i + 4] & 0xFF);
            byte rot = (byte) buffer[i + 5];
            byte vangle = (byte) buffer[i + 6];
            byte aangle = (byte) buffer[i + 7];
            state[(i-8)/8] = new GameState.RobotState(id, x, y, rot, vangle, aangle);
        }
        for (int i = numEntities * 8 + 8; i < buffer.length; i += 4) {
            int x = ((buffer[i + 0] & 0xFF) << 4) + ((buffer[i + 1] & 0xFF) >> 4);
            int y = ((buffer[i + 1] & 0x0F) << 8) + (buffer[i + 2] & 0xFF);
            byte rot = (byte) buffer[i + 3];
            bullets[(i-numEntities*8-8)/4] = new GameState.BulletState(x, y, rot);
        }
        g.updateState(new GameState(state, bullets));
        g.updateRobotVelocity(vx, vy);
    }

    private void parseHealths(byte[] buffer) {
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        Map<Short, Double> healths = new HashMap<Short, Double>();
        for (int i = 0; i < buffer.length; i += 3) {
            healths.put(bb.getShort(), (bb.get() & 0xFF) / 255.0);
        }
        g.updateHealths(healths);
    }

    private void parseJoined(byte[] buffer) {
        robotId = (short) (((buffer[0] & 0xFF) << 8) + (buffer[1] & 0xFF));
        awaitingId.release();
    }

    private void parseSpectateOk(byte[] buffer) {
        awaitingSpectateOk.release();
    }

}
