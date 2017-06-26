package client;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

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
                Queue<RobotState> q = new LinkedList<RobotState>();
                for (int i = 0; i < 15; i++) {
                    q.add(rs);
                }
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
                robot.setRotate(angle);
                robot.setBlasterRotate((rs.getRotation() / 255.0 * 360) - angle);
                robot.setThrusterRotate((rs.getAccelAngle() / 255.0 * 360) - angle);
            }
        });
    }

    private void destroyRobots(GameState state) {
        robots.clear();
    }

}
