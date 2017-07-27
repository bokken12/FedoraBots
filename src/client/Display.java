package client;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import client.GameState.BulletState;
import client.GameState.RobotState;
import common.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Display extends Application {

    private static Display instance;
    private static CountDownLatch latch = new CountDownLatch(1);

    private Map<Short, RobotFigure> robots = new HashMap<Short, RobotFigure>();
    private Group robotCircles;
    private GameManager gm;
    private List<BulletFigure> bullets = new ArrayList<BulletFigure>();

    public Display() throws IOException {
        gm = new GameManager(new GameNetworkAdapter());
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

    public void goLaunch() {
        launch();
    }

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Framey");
		Group root = new Group();
		Scene scene = new Scene(root, Constants.World.WIDTH, Constants.World.HEIGHT, Color.LIGHTGRAY);
        primaryStage.setScene(scene);

        robotCircles = new Group();
        root.getChildren().add(robotCircles);

        primaryStage.show();

        gm.addStateListener(this::draw);
        gm.addBeginListener(this::initializeRobots);
        gm.addEndListener(this::destroyRobots);
        gm.addHealthListener(this::updateRobotHealths);
        latch.countDown();
    }

    private void initializeRobots(GameState state) {
        for (RobotState rs : state.robotStates()) {
            RobotFigure robot = new RobotFigure(Constants.Robot.RADIUS, rs.getColor()); // new Circle(10, rs.getColor());
            if (robots.put(rs.getId(), robot) != null) {
                throw new RuntimeException("A robot with ID " + rs.getId() + " wasn't cleared from the display.");
            }
        }

        Platform.runLater(() -> {
            for (RobotState rs : state.robotStates()) {
                robotCircles.getChildren().add(robots.get(rs.getId()));
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
        }
    }

}
