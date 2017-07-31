package client;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import common.Constants;
import server.sim.Sim;
import server.sim.entity.Bullet;
import server.sim.entity.Robot;
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
    public void sendJoin(short roomId, byte r, byte g, byte b) {
        Color robotColor = new Color(r & 0xFF, g & 0xFF, b & 0xFF);
        robot = new Robot(robotId, robotColor,
                          Math.random() * Constants.World.WIDTH,
                          Math.random() * Constants.World.HEIGHT,
                          Math.random() * 2 * Math.PI,
                          Constants.Robot.RADIUS,
                          Constants.Robot.MASS);
        world.add(robot);

        GameState.RobotState[] initialState = robotState();
        GameState.ObstacleState[] obstacles = world.getObstacles().stream().map(obs -> new GameState.ObstacleState(
            (byte) obs.getId(), obs.getObstacleType(), (int) obs.getX(), (int) obs.getY(), (byte) (obs.getRotation() / 2 / Math.PI * 255)
        )).toArray(GameState.ObstacleState[]::new);

        Map<Short, javafx.scene.paint.Color> colorMap = new HashMap<Short, javafx.scene.paint.Color>();
        colorMap.put(robotId, javafx.scene.paint.Color.rgb(robotColor.getRed(), robotColor.getGreen(), robotColor.getBlue()));

        this.g.startGame(new GameState(initialState, obstacles), colorMap);
        gameStartedLock.release();
        System.out.println("Starting");
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

}
