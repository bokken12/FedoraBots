package client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import common.Constants;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

public class Display extends Application {

    private Map<Short, Shape> robots = new HashMap<Short, Shape>();
    private Group robotCircles;
    private GameManager gm;

    public Display() throws IOException {
        gm = new GameManager(new GameNetworkAdapter());
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
    }

    private void initializeRobots(GameState state) {
        for (GameState.RobotState rs : state.robotStates()) {
            Shape robot = new Circle(10, rs.getColor());
            if (robots.put(rs.getId(), robot) != null) {
                throw new RuntimeException("A robot with ID " + rs.getId() + " wasn't cleared from the display.");
            }
        }

        Platform.runLater(() -> {
            for (GameState.RobotState rs : state.robotStates()) {
                robotCircles.getChildren().add(robots.get(rs.getId()));
            }
        });
    }

    private void draw(GameState state) {
        Platform.runLater(() -> {
            for (GameState.RobotState rs : state.robotStates()) {
                Shape robot = robots.get(rs.getId());
                if (robot == null) {
                    throw new RuntimeException("An unexpected robot with ID " + rs.getId() + " decided to join the game.");
                }

                robot.setTranslateX(rs.getX());
                robot.setTranslateY(rs.getY());
                robot.setRotate(rs.getRotation() / 255.0 * Math.PI * 2);
            }
        });
    }

    private void destroyRobots(GameState state) {
        robots.clear();
    }

}
