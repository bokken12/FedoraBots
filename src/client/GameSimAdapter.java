package client;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import common.Constants;
import javafx.geometry.Point2D;
import server.RoomLayout;
import server.sim.Sim;
import server.sim.entity.Bullet;
import server.sim.entity.Entity;
import server.sim.entity.Jammer;
import server.sim.entity.Meteorite;
import server.sim.entity.Obstacle;
import server.sim.entity.Robot;
import server.sim.entity.Turret;
import server.sim.entity.Vaporizer;
import server.sim.world.World;

/**
 * Runs a Sim locally and interfaces with a game manager.
 */
public class GameSimAdapter implements GameAdapter {

    private World world;
    private Sim sim;
    private volatile GameManager g;
    private short robotId = 0;
    private Semaphore gameStartedLock = new Semaphore(1);
    private Robot robot;
    private Collection<Obstacle> obstacles;

    public GameSimAdapter() {
        try {
            gameStartedLock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not acquire game started semaphore.");
        }
        world = World.generateScrollingWorld(0, 0, Constants.World.WIDTH, Constants.World.HEIGHT);
        sim = new Sim(world);
    }

    @Override
    public void setManager(GameManager manager) {
        g = manager;
    }

    @Override
    public void sendJoin(short difficulty, byte r, byte g, byte b) {
        RoomConfiguration config = getRoomConfiguration(difficulty);
        Color robotColor = new Color(r & 0xFF, g & 0xFF, b & 0xFF);
        robot = new Robot(robotId, robotColor,
                          Math.random() * Constants.World.WIDTH,
                          Math.random() * Constants.World.HEIGHT,
                          Math.random() * 2 * Math.PI,
                          Constants.Robot.RADIUS,
                          Constants.Robot.MASS);
        List<Entity> entitiesToAdd = config.getObstacles();
        entitiesToAdd.add(robot);
        int i = 0;
        while (!entitiesToAdd.isEmpty()) {
            int index = (int) (Math.random() * entitiesToAdd.size());
            Entity entity = entitiesToAdd.remove(index);
            Point2D location = RoomLayout.getLocation(i++);

            entity.setPositionUnsafe(location.getX(), location.getY());
            world.add(entity);
        }

        GameState.RobotState[] initialState = robotState();
        obstacles = world.getObstacles();
        GameState.ObstacleState[] obstacleStates = obstacles.stream().map(obs -> new GameState.ObstacleState(
            (byte) obs.getId(), obs.getObstacleType(), (int) obs.getX(), (int) obs.getY(), (byte) (obs.getRotation() / 2 / Math.PI * 255)
        )).toArray(GameState.ObstacleState[]::new);

        Map<Short, javafx.scene.paint.Color> colorMap = new HashMap<Short, javafx.scene.paint.Color>();
        colorMap.put(robotId, javafx.scene.paint.Color.rgb(robotColor.getRed(), robotColor.getGreen(), robotColor.getBlue()));

        this.g.startGame(new GameState(initialState, obstacleStates), colorMap);
        gameStartedLock.release();
    }

    @Override
    public void sendSpectate(short roomId) throws IOException {
        throw new RuntimeException("Cannot spectate a local game");
    }

    @Override
    public void sendRobotUpdate(short id, double ax, double ay, double rotation) throws IOException {
        if (id != robotId) {
            throwError("Invalid robot id " + id + ".");
        }

        robot.setAcceleration(ax, ay);
        robot.setRotation(rotation / 360 * 2 * Math.PI);
    }

    @Override
    public void sendRobotShootRequest(short id) throws IOException {
        if (id != robotId) {
            throwError("Invalid robot id " + id + ".");
        }

        double rotation = -robot.getRotation() + Math.PI / 2;
        double vx = Constants.Bullet.VELOCITY/1e3 * Math.cos(rotation);
        double vy = - (Constants.Bullet.VELOCITY/1e3 * Math.sin(rotation));
        double dist = (Constants.Robot.RADIUS + Constants.Bullet.RADIUS) * 1.1;
        double x = robot.getX() + dist * Math.cos(rotation);
        double y = robot.getY() - dist * Math.sin(rotation);

        world.add(new Bullet(x, y, Constants.Bullet.RADIUS, Constants.Bullet.MASS, vx, vy));
    }

    @Override
    public short getRobotId() {
        return robotId;
    }

    @Override
    public void waitForSpectateOk() {
        throw new RuntimeException("Spectating games is not implemented");
    }

    @Override
    public void run() {
        try {
            gameStartedLock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("Could not acquire game started semaphore.");
        } finally {
            gameStartedLock.release();
        }
        sim.run(tick -> {
            Collection<Robot> rvs = new LinkedList<Robot>();
            updateState();
            rvs.add(robot);
            List<Robot> robotsChangedHealth = world.healthChangedRobots(rvs);
            if (robotsChangedHealth.size() > 0) {
                Map<Short, Double> healthMap = new HashMap<Short, Double>();
                healthMap.put(robotId, robot.getHealth());
                g.updateHealths(healthMap);
            }
            List<Obstacle> obstaclesChangedRotation = world.rotationChangedObstacles(obstacles);
            if (obstaclesChangedRotation.size() > 0) {
                Map<Byte, Byte> obstacleMap = new HashMap<Byte, Byte>();
                for (Obstacle obs : obstaclesChangedRotation) {
                    obstacleMap.put((byte) obs.getId(), (byte) (obs.getRotation() / 2 / Math.PI * 255));
                }
                g.updateObstacles(obstacleMap);
            }
        });
    }

    private GameState.RobotState[] robotState() {
        GameState.RobotState[] robotState = { new GameState.RobotState(
            robot.getId(), (int) robot.getX(), (int) robot.getY(), (byte) (robot.getRotation() / 2 / Math.PI * 255),
            (byte) ((Math.atan2(robot.getVy(), robot.getVx()) + Math.PI / 2) / 2 / Math.PI * 255),
            (byte) ((Math.atan2(robot.getAy(), robot.getAx()) + Math.PI / 2) / 2 / Math.PI * 255)
        ) };
        return robotState;
    }

    private GameState.BulletState[] bulletStates() {
        Collection<Bullet> bullets = world.getBullets();
        return bullets.stream().map(b -> {
            return new GameState.BulletState((int) b.getX(), (int) b.getY(),
                (byte) (Math.atan2(b.getVy(), b.getVx()) / 2 / Math.PI * 255));
        }).toArray(GameState.BulletState[]::new);
    }

    private void updateState() {
        GameState st = new GameState(robotState(), bulletStates());
        g.updateState(st);
        g.updateRobotVelocity(robot.getVx()*1e3, robot.getVy()*1e3);
    }

    private void throwError(String error) {
        new RuntimeException(error).printStackTrace();
        System.exit(1);
    }

    private static class RoomConfiguration {
        public int numJammers;
        public int numMeteorites;
        public int numTurrets;
        public int numVaporizers;

        public RoomConfiguration(int jammers, int meteorites, int turrets, int vaporizers) {
            numJammers = jammers;
            numMeteorites = meteorites;
            numTurrets = turrets;
            numVaporizers = vaporizers;
        }

        public List<Entity> getObstacles() {
            List<Entity> obstacles = new ArrayList<Entity>(numJammers + numMeteorites + numTurrets + numVaporizers);
            byte obstacleIndex = 0;
            for (int i = 0; i < numJammers; i++) {
                obstacles.add(new Jammer(obstacleIndex++, -1, -1));
            }
            for (int i = 0; i < numMeteorites; i++) {
                obstacles.add(new Meteorite(obstacleIndex++, -1, -1));
            }
            for (int i = 0; i < numTurrets; i++) {
                obstacles.add(new Turret(obstacleIndex++, -1, -1));
            }
            for (int i = 0; i < numVaporizers; i++) {
                obstacles.add(new Vaporizer(obstacleIndex++, -1, -1));
            }
            return obstacles;
        }
    }

    private static RoomConfiguration getRoomConfiguration(short difficulty) {
        switch (difficulty) {
            case 0:  return new RoomConfiguration(0,  0,  0,  0 );
            case 1:  return new RoomConfiguration(0,  10, 0,  0 );
            case 2:  return new RoomConfiguration(0,  5,  2,  0 );
            case 3:  return new RoomConfiguration(0,  0,  0,  3 );
            case 4:  return new RoomConfiguration(2,  0,  0,  0 );
            case 5:  return new RoomConfiguration(2,  5,  2,  0 );
            case 6:  return new RoomConfiguration(0,  5,  3,  8 );
            case 7:  return new RoomConfiguration(3,  5,  3,  8 );
            case 8:  return new RoomConfiguration(3,  2,  6,  8 );
            case 9:  return new RoomConfiguration(4,  0, 10, 10 );
            default: throw new RuntimeException("Difficulty must be in the range 0 to 9 inclusive.");
        }
    }

}
