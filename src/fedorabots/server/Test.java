package fedorabots.server;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import fedorabots.common.Constants;
import fedorabots.server.sim.entity.Jammer;
import fedorabots.server.sim.entity.Meteorite;
import fedorabots.server.sim.entity.Turret;
import fedorabots.server.sim.entity.Vaporizer;
import fedorabots.server.sim.world.World;

public class Test {

    private static final Level LOG_LEVEL = Level.FINEST;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        rootLogger.setLevel(LOG_LEVEL);
        for (Handler h : handlers) {
            if(h instanceof ConsoleHandler)
                h.setLevel(LOG_LEVEL);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        World w = World.generateScrollingWorld(0, 0, Constants.World.WIDTH, Constants.World.HEIGHT);
        Room room = new Room(1, w);
        room.addObstacle(new Turret((byte) 0, -1, -1));
        room.addObstacle(new Vaporizer((byte) 1, -1, -1));
        room.addObstacle(new Jammer((byte) 2, -1, -1));
        for (byte i = 3; i < 23; i++) {
            room.addObstacle(new Meteorite(i, -1, -1));
        }
        // TestEntity phys = new TestEntity((short) 0, 16, 17, 10);
		// phys.setAcceleration(0.1, 0);
		// TestEntity phys2 = new TestEntity((short) 1, 263, 20, 10);
		// phys2.setAcceleration(-0.1, 0);
        // w.add(phys);
        // w.add(phys2);
        Manager manager = new Manager();
        manager.addRoom(room);
        TcpServer server = new TcpServer(manager);
        Thread t = new Thread(server);
        t.setDaemon(true);
        t.start();
        Thread.sleep(5000);
        // sim.run(tick -> manager.broadcastState(server, w.state(), w.velocityStates()));
        manager.loopTickAllRooms(server, true);
    }
}
