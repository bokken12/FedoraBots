package client;

import java.util.List;

import afester.javafx.svg.SvgLoader;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
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
    private static final int NUM_THRUSTERS = 3;
    private Group child;

    private Shape body;
    private Group blaster;
    private Shape[] linearThrusters = new Shape[NUM_THRUSTERS];
    private Shape[] radialThrusters = new Shape[NUM_THRUSTERS];
    private double[] thrusterAngles = new double[NUM_THRUSTERS];
    private Color thrusterColorOpaque;
    private Color thrusterColorTransparent;

    // static {
    //     for (int i = 0; i < )
    // }

    public RobotFigure(double radius, Color color) {
        super(LOADER.loadSvg(Display.class.getResourceAsStream(ROBOT_SVG)));
        child = (Group) getChildren().get(0);
        body = (Shape) child.lookup("#body");
        blaster = (Group) child.lookup("#blaster");

        for (int i = 0; i < NUM_THRUSTERS; i++) {
            linearThrusters[i] = (Shape) child.lookup("#thruster" + i);
            radialThrusters[i] = (Shape) child.lookup("#thruster" + i + "-radial");
            LinearGradient l = (LinearGradient) linearThrusters[i].getFill();
            thrusterAngles[i] = Math.atan2(l.getStartY() - l.getEndY(), l.getStartX() - l.getEndX());
        }

        List<Stop> stops = ((LinearGradient) linearThrusters[0].getFill()).getStops();
        thrusterColorOpaque = stops.stream().filter(stop -> stop.getColor().getOpacity() > 0.9).findFirst().get().getColor();
        thrusterColorTransparent = stops.stream().filter(stop -> stop.getColor().getOpacity() < 0.1).findFirst().get().getColor();

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
        setThrusterRotate(0);
    }

    public void setColor(Color color) {
        body.setFill(color);
    }

    public void setBlasterRotate(double angle) {
        blaster.setRotate(angle);
    }

    public void setThrusterRotate(double angle) {
        double radians = Math.toRadians(angle - 90);
        for (int i = 0; i < NUM_THRUSTERS; i++) {
            LinearGradient oldl = (LinearGradient) linearThrusters[i].getFill();
            RadialGradient oldr = (RadialGradient) radialThrusters[i].getFill();
            double dist = Math.sqrt(Math.max(Math.cos(radians - thrusterAngles[i]), 0));
            linearThrusters[i].setFill(
                new LinearGradient(oldl.getStartX(), oldl.getStartY(), oldl.getEndX(),
                                   oldl.getEndY(), oldl.isProportional(), oldl.getCycleMethod(),
                                   new Stop(0, dist == 0 ? thrusterColorTransparent : thrusterColorOpaque),
                                   new Stop(dist, thrusterColorTransparent))
            );
            radialThrusters[i].setFill(
                new RadialGradient(oldr.getFocusAngle(), oldr.getFocusDistance(),
                                   oldr.getCenterX(), oldr.getCenterY(), oldr.getRadius(),
                                   oldr.isProportional(), oldr.getCycleMethod(),
                                   new Stop(0, dist == 0 ? thrusterColorTransparent : thrusterColorOpaque),
                                   new Stop(dist, thrusterColorTransparent))
                                );
        }
    }

}
