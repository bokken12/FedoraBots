package client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import client.GameState.BulletState;
import client.GameState.ObstacleState;
import client.GameState.RobotState;
import client.figure.BulletFigure;
import client.figure.JammerFigure;
import client.figure.ObstacleFigure;
import client.figure.RobotFigure;
import common.Constants;
import common.ModdedBufferedImage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Display extends Application {

    private static Display instance;
    private static CountDownLatch latch = new CountDownLatch(1);

    private Map<Short, RobotFigure> robots = new HashMap<Short, RobotFigure>();
    private Scene scene;
    private Group robotCircles;
    private GameManager gm;
    private List<BulletFigure> bullets = new ArrayList<BulletFigure>();
    private Map<Byte, ObstacleFigure> obstacles = new HashMap<Byte, ObstacleFigure>();

    private static SnapshotParameters snapshotParams = new SnapshotParameters();

    static {
        snapshotParams.setViewport(new Rectangle2D(0, 0, Constants.World.WIDTH, Constants.World.HEIGHT));
        snapshotParams.setFill(Color.LIGHTGRAY);
    }

    public Display() throws IOException {
        gm = new GameManager();
        instance = this;
    }

    public synchronized static Display getInstance() {
        if (instance == null) {
            Thread displayLauncher = new Thread(() -> {
                try {
                    launch();
                    System.exit(0);
                } catch (Exception e) {
                    Throwable cause = e.getCause().getCause();
                    if (cause instanceof ConnectException) {
                        new RuntimeException("Could not connect to server", cause).printStackTrace();
                    } else {
                        cause.printStackTrace();
                    }
                    System.exit(1);
                }
            });
            displayLauncher.setDaemon(false);
            displayLauncher.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public GameManager getGameManager() {
        return gm;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide a room id to spectate.");
            System.exit(1);
        }

        try {
            Short.parseShort(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("The provided room id must be a valid number.");
            System.exit(1);
        }

        Application.launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Framey");
		Group root = new Group();
		scene = new Scene(root, Constants.World.WIDTH, Constants.World.HEIGHT, Color.LIGHTGRAY);
        primaryStage.setScene(scene);

        robotCircles = new Group();
        root.getChildren().add(robotCircles);

        primaryStage.show();

        gm.addStateListener(this::draw);
        gm.addBeginListener(this::initializeRobots);
        gm.addEndListener(this::destroyRobots);
        gm.addHealthListener(this::updateRobotHealths);
        gm.addObstacleListener(this::updateObstacleRotations);

        if (getParameters().getRaw().size() > 0) {
            gm.spectateNetworkGame(Short.parseShort(getParameters().getRaw().get(0)));
        }

        primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ImageDisplay.closeAllWindows();
                Platform.exit();
            }
        });

        latch.countDown();
    }

    public static BufferedImage snapshot(Supplier<WritableImage> generator, int width, int height) {
        Semaphore awaitingImageSemaphore = new Semaphore(1);
        try {
            awaitingImageSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ModdedBufferedImage result = new ModdedBufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setFakeType(true);
        Platform.runLater(() -> {
            SwingFXUtils.fromFXImage(generator.get(), result);
            result.setFakeType(false);
            awaitingImageSemaphore.release();
        });
        try {
            awaitingImageSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public BufferedImage getImage() {
        return snapshot(() -> robotCircles.snapshot(snapshotParams, null), Constants.World.WIDTH, Constants.World.HEIGHT);
    }

    private void initializeRobots(GameState state) {
        for (RobotState rs : state.robotStates()) {
            RobotFigure robot = new RobotFigure(Constants.Robot.RADIUS, rs.getColor()); // new Circle(10, rs.getColor());
            robot.setTranslateX(rs.getX());
            robot.setTranslateY(rs.getY());
            if (robots.put(rs.getId(), robot) != null) {
                throw new RuntimeException("A robot with ID " + rs.getId() + " wasn't cleared from the display.");
            }
        }

        for (ObstacleState os : state.obstacleStates()) {
            ObstacleFigure obs = ObstacleFigure.forType(os.getType(), Constants.Obstacle.RADIUS);
            obs.setTranslateX(os.getX());
            obs.setTranslateY(os.getY());
            if (obstacles.put(os.getId(), obs) != null) {
                throw new RuntimeException("An obstacle with ID " + os.getId() + " wasn't cleared from the display.");
            }
        }

        Platform.runLater(() -> {
            for (ObstacleState os : state.obstacleStates()) {
                Node rangeMarker = obstacles.get(os.getId()).getRangeMarker();
                if (rangeMarker != null) {
                    rangeMarker.setTranslateX(os.getX());
                    rangeMarker.setTranslateY(os.getY());
                    robotCircles.getChildren().add(rangeMarker);
                }
            }
            for (RobotState rs : state.robotStates()) {
                robotCircles.getChildren().add(robots.get(rs.getId()));
            }
            for (ObstacleState os : state.obstacleStates()) {
                robotCircles.getChildren().add(obstacles.get(os.getId()));
            }
        });
    }

    private void draw(GameState state) {
        Platform.runLater(() -> {
            for (RobotState rs : state.robotStates()) {
                short rId = rs.getId();
                RobotFigure robot = robots.get(rId);
                if (robot == null) {
                    throw new RuntimeException("An unexpected robot with ID " + rId + " decided to join the game.");
                }

                robot.setTranslateX(rs.getX());
                robot.setTranslateY(rs.getY());
                double angle = (rs.getVelocityAngle() / 255.0 * 360);
                robot.setRotation(angle);
                robot.setBlasterRotate((rs.getRotation() / 255.0 * 360) - angle);
                robot.setThrusterRotate((rs.getAccelAngle() / 255.0 * 360) - angle);
            }

            int i = 0;
            for (BulletState bs : state.bulletStates()) {
                BulletFigure bf;
                if (i < bullets.size()) {
                    // Reuse a bullet
                    bf = bullets.get(i);
                } else {
                    bf = new BulletFigure();
                    robotCircles.getChildren().add(bf);
                    bullets.add(bf);
                }

                bf.setTranslateX(bs.getX());
                bf.setTranslateY(bs.getY());
                bf.setRotate(bs.getRotation());
                i++;
            }

            // Remove leftover bullets
            for (int j = bullets.size() - 1; j >= i; j--) {
                robotCircles.getChildren().remove(bullets.get(j));
                bullets.remove(j);
            }
        });
    }

    private void destroyRobots(GameState state) {
        robots.clear();
    }

    private void updateRobotHealths(Map<Short, Double> healths) {
        for (Map.Entry<Short, Double> entry : healths.entrySet()) {
            robots.get(entry.getKey()).setHealth(entry.getValue());
            if (entry.getValue() == 0) {
                Platform.runLater(() -> {
                    robotCircles.getChildren().remove(robots.get(entry.getKey()));
                });
            }
        }
    }

    private void updateObstacleRotations(Map<Byte, Byte> obsRotations) {
        for (Map.Entry<Byte, Byte> entry : obsRotations.entrySet()) {
            obstacles.get(entry.getKey()).setRotation((entry.getValue() & 0xFF) / 255.0 * 360);
        }
    }

    public List<Point2D> getJammerCenters() {
        return obstacles.values().stream()
            .filter(JammerFigure.class::isInstance)
            .map(fig -> new Point2D(fig.getTranslateX(), fig.getTranslateY()))
            .collect(Collectors.toList());
    }

}
