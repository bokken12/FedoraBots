package fedorabots.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import fedorabots.common.Constants;
import fedorabots.server.sim.entity.Jammer;
import fedorabots.server.sim.entity.Meteorite;
import fedorabots.server.sim.entity.Obstacle;
import fedorabots.server.sim.entity.Turret;
import fedorabots.server.sim.entity.Vaporizer;
import fedorabots.server.sim.world.World;

public class Cli {

    private static final Level LOG_LEVEL = Level.INFO;

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
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Manager manager = new Manager();
        TcpServer server = new TcpServer(manager);
        Thread t = new Thread(server);
        t.setDaemon(true);
        t.start();

        Thread tickThread = new Thread(() -> {
            manager.loopTickAllRoom(server);
        });
        tickThread.setDaemon(true);
        tickThread.start();

        Thread.sleep(100); // Give threads time to log initial stuff

        mainLoop(reader, manager);
    }

    private static int getInteger(BufferedReader reader) {
        Integer result = null;
        while (result == null) {
            try {
                String line = reader.readLine();
                if (line.toLowerCase().startsWith("quit")) {
                    System.exit(0);
                } else {
                    result = Integer.parseInt(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading process input");
            } catch (NumberFormatException e) {
                System.out.println("You didn't enter a number. Please enter a number. Please.");
            }
        }
        return result;
    }

    private static void mainLoop(BufferedReader reader, Manager manager) {
        System.out.println("Starting server manager client.");
        while (true) {
            System.out.println("You are about to create a new room.");
            System.out.println("If you do not want to create a new room do nothing.");
            System.out.println("If you want to exit this program and stop the server type \"quit\" then press enter.");
            System.out.println();
            System.out.print("To create a new room please give it an id: ");

            World w = World.generateScrollingWorld(0, 0, Constants.World.WIDTH, Constants.World.HEIGHT);
            Room room = new Room(getInteger(reader), w);
            List<Obstacle> obstaclesToAdd = new ArrayList<Obstacle>();
            byte obstacleId = 0;

            obstacleId += addObstacles(reader, obstaclesToAdd, Meteorite.class, obstacleId);
            obstacleId += addObstacles(reader, obstaclesToAdd, Turret.class, obstacleId);
            obstacleId += addObstacles(reader, obstaclesToAdd, Vaporizer.class, obstacleId);
            obstacleId += addObstacles(reader, obstaclesToAdd, Jammer.class, obstacleId);

            while (!obstaclesToAdd.isEmpty()) {
                int index = (int) (Math.random() * obstaclesToAdd.size());
                room.addObstacle(obstaclesToAdd.remove(index));
            }

            manager.addRoom(room);

            System.out.println("Yay you've successfully created a room!");
            System.out.println();
        }
    }

    private static int addObstacles(BufferedReader reader, List<Obstacle> obstacleList, Class<? extends Obstacle> obstacleClass, byte startingId) {
        String plural = obstacleClass.getSimpleName().toLowerCase() + "s";
        System.out.print("Please enter the number of " + plural + " to add to the room: ");
        int numObstacles = getInteger(reader);
        try {
            for (int i = 0; i < numObstacles; i++) {
                Constructor<? extends Obstacle> cstr = obstacleClass.getConstructor(byte.class, double.class, double.class);
                obstacleList.add(cstr.newInstance(startingId++, -1, -1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return numObstacles;
    }

}
