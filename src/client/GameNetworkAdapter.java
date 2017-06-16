package client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

/***
 * Receives messages from the network and notifies the game manager accordingly.
 */
public class GameNetworkAdapter implements Runnable {

    private Socket s;
    private InputStream inp;
    private GameManager g;

    public GameNetworkAdapter() throws IOException {
        s = new Socket("localhost", 8090);
        inp = s.getInputStream();
    }

    public void setManager(GameManager manager) {
        g = manager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int mType = inp.read();
                int numEntities = inp.read();

                int bufferLen = 0;
                if (mType == 0) bufferLen = numEntities * 9;
                if (mType == 1) bufferLen = numEntities * 6;

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
            case 0: parseStart(buffer); break;
            case 1: parseState(buffer); break;
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

}
