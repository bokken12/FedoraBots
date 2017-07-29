package client;

import java.io.IOException;

/**
 * Game adapters are used by the game manager to communicate with the game server.
 */
public interface GameAdapter extends Runnable {

    public void setManager(GameManager manager);

    public void sendJoin(short roomId, byte r, byte g, byte b) throws IOException;

    public void sendSpectate(short roomId) throws IOException;

    public void sendRobotUpdate(short id, double ax, double ay, double rotation) throws IOException;

    public void sendRobotShootRequest(short id) throws IOException;

    public short getRobotId();

    public void waitForSpectateOk();

}
