package client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javafx.scene.paint.Color;

/***
 * Receives messages from the network and notifies the game manager accordingly.
 */
public class GameNetworkAdapter implements Runnable {

    private Semaphore awaitingId = new Semaphore(1);

    private Socket s;
    private InputStream inp;
    private GameManager g;

    private short robotId;

    public GameNetworkAdapter() throws IOException {
        s = new Socket("localhost", 8090);
        inp = s.getInputStream();
    }

    public void setManager(GameManager manager) {
        g = manager;
    }

    public void sendJoin(byte r, byte g, byte b) throws IOException {
        try {
            awaitingId.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not acquire awaiting semaphore.");
        }
        byte[] toSend = {(byte) 128, r, g, b};
        s.getOutputStream().write(toSend);
    }

    public void sendRobotUpdate(short id, double ax, double ay, double rotation) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(12);
        bb.put((byte) 129);
        bb.putShort(id);
        bb.putFloat((float) ax);
        bb.putFloat((float) ay);
        bb.put((byte) Math.round(rotation / 360 * 255));
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

    @Override
    public void run() {
        while (true) {
            try {
                int mType = inp.read();

                int bufferLen = 0;
                if (mType == 0 || mType == 1) {
                    int numEntities = inp.read();

                    if (mType == 0) bufferLen = numEntities * 9;
                    if (mType == 1) bufferLen = numEntities * 6;
                } else if (mType == 64) {
                    bufferLen = 2;
                } else {
                    throw new RuntimeException("Unknown message type " + mType + ".");
                }

                byte[] buffer = new byte[bufferLen];
                int i = 0;
                while (i < buffer.length) {
                    i += inp.read(buffer, i, buffer.length - i);
                }

                parseBuffer(mType, buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseBuffer(int type, byte[] buffer) {
        switch (type) {
            case 0:  parseStart(buffer); break;
            case 1:  parseState(buffer); break;
            case 64: parseJoined(buffer); break;
        }
    }

    private void parseStart(byte[] buffer) {
        GameState.RobotState[] state = new GameState.RobotState[buffer.length / 9];
        Map<Short, Color> colors = new HashMap<Short, Color>();

        for (int i = 0; i < buffer.length; i += 9) {
            short id = (short) ((buffer[i] << 8) + buffer[i + 1]);
            int x = (buffer[i + 2] << 4) + (buffer[i + 3] & 0x10);
            int y = (buffer[i + 3] << 8) + buffer[i + 4];
            byte rot = (byte) buffer[i + 5];
            state[i/9] = new GameState.RobotState(id, x, y, rot);
            colors.put(id, Color.rgb(buffer[i + 6] & 0xFF, buffer[i + 7] & 0xFF, buffer[i + 8] & 0xFF));
        }
        g.startGame(new GameState(state), colors);
    }

    private void parseState(byte[] buffer) {
        GameState.RobotState[] state = new GameState.RobotState[buffer.length / 6];
        for (int i = 0; i < buffer.length; i += 6) {
            short id = (short) (((buffer[i] & 0xFF) << 8) + (buffer[i + 1] & 0xFF));
            int x = ((buffer[i + 2] & 0xFF) << 4) + ((buffer[i + 3] & 0xFF) >> 4);
            int y = ((buffer[i + 3] & 0x0F) << 8) + (buffer[i + 4] & 0xFF);
            byte rot = (byte) buffer[i + 5];
            state[i/6] = new GameState.RobotState(id, x, y, rot);
        }
        g.updateState(new GameState(state));
    }

    private void parseJoined(byte[] buffer) {
        robotId = (short) (((buffer[0] & 0xFF) << 8) + (buffer[1] & 0xFF));
        awaitingId.release();
    }

}
