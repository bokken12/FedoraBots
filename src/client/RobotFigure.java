package client;

import java.io.InputStream;

import afester.javafx.svg.SvgLoader;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;

/**
 * A class that handles the creation of Robot figures to display in the GUI. It
 * loads the robot SVG file then changes various properties such as the color
 * and shooter rotation.
 */
public class RobotFigure extends Group {

    private static final int ROBOT_SVG_SIZE = 64;
    private static final String ROBOT_SVG = "/tank.svg";
    private static final SvgLoader LOADER = new SvgLoader();
    private Group child;

    private Shape body;
    private Group blaster;

    public RobotFigure(double radius, Color color) {
        super(LOADER.loadSvg(Display.class.getResourceAsStream(ROBOT_SVG)));
        child = (Group) getChildren().get(0);
        body = (Shape) child.lookup("#body");
        blaster = (Group) child.lookup("#blaster");

        // Rotate the blaster around the center
        double blasterCenterY = blaster.getBoundsInParent().getMinY() + blaster.getBoundsInParent().getHeight() / 2.0;
        blaster.getTransforms().add(new Translate(0, blasterCenterY - ROBOT_SVG_SIZE / 2.0));
        blaster.setTranslateY(ROBOT_SVG_SIZE / 2.0 - blasterCenterY);

        // Scale down the robot to the correct size
        double scale = radius * 2 / ROBOT_SVG_SIZE;
        child.setScaleX(scale);
        child.setScaleY(scale);

        // The SVG has a center (0, 0) but the program needs a center of
        // (width/2, height/2)
        child.setTranslateX(-ROBOT_SVG_SIZE/2);
        child.setTranslateY(-ROBOT_SVG_SIZE/2);

        setColor(color);
    }

    public void setColor(Color color) {
        body.setFill(color);
    }

    public void setBlasterRotate(double angle) {
        blaster.setRotate(angle);
    }

}
