package client;

import java.util.List;

import afester.javafx.svg.SvgLoader;
import common.Constants.Robot;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
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
    private static final double HB_RADIUS_O = Robot.RADIUS + 5;
    private static final double HB_RADIUS_I = Robot.RADIUS + 2;
    private static final Color HB_START_COLOR = Color.GREEN.interpolate(Color.LIGHTGRAY, 0.2);
    private static final Color HB_END_COLOR = Color.RED.interpolate(Color.LIGHTGRAY, 0.2);
    private Group child;

    private Shape body;
    private Group blaster;
    private SVGPath health;
    private Shape[] linearThrusters = new Shape[NUM_THRUSTERS];
    private Shape[] radialThrusters = new Shape[NUM_THRUSTERS];
    private double[] thrusterAngles = new double[NUM_THRUSTERS];
    private Color thrusterColorOpaque;
    private Color thrusterColorTransparent;
    private Rotate rotate;

    // static {
    //     for (int i = 0; i < )
    // }

    public RobotFigure(double radius, Color color) {
        super(healthBar(), LOADER.loadSvg(Display.class.getResourceAsStream(ROBOT_SVG)));
        health = (SVGPath) getChildren().get(0);
        child = (Group) getChildren().get(1);
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
        child.getTransforms().add(new Scale(scale, scale, ROBOT_SVG_SIZE/2, ROBOT_SVG_SIZE/2));

        // The SVG has a center (0, 0) but the program needs a center of
        // (width/2, height/2)
        child.setTranslateX(-ROBOT_SVG_SIZE/2);
        child.setTranslateY(-ROBOT_SVG_SIZE/2);

        child.getTransforms().add(rotate = new Rotate(0, ROBOT_SVG_SIZE/2, ROBOT_SVG_SIZE/2));

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
            double dist = Math.cbrt(Math.max(Math.cos(radians - thrusterAngles[i]), 0));
            linearThrusters[i].setFill(
                new LinearGradient(oldl.getStartX(), oldl.getStartY(), oldl.getEndX(),
                                   oldl.getEndY(), oldl.isProportional(), oldl.getCycleMethod(),
                                   new Stop(0, dist < 0.35 ? thrusterColorTransparent : thrusterColorOpaque),
                                   new Stop(dist, thrusterColorTransparent))
            );
            radialThrusters[i].setFill(
                new RadialGradient(oldr.getFocusAngle(), oldr.getFocusDistance(),
                                   oldr.getCenterX(), oldr.getCenterY(), oldr.getRadius(),
                                   oldr.isProportional(), oldr.getCycleMethod(),
                                   new Stop(0, dist < 0.35 ? thrusterColorTransparent : thrusterColorOpaque),
                                   new Stop(dist, thrusterColorTransparent))
                                );
        }
    }

    public void setRotation(double angle) {
        rotate.setAngle(angle);
    }

    public void setHealth(double value) {
        health.setContent(hbSVG(value));
        health.setFill(HB_START_COLOR.interpolate(HB_END_COLOR, value));
    }

    private static String hbSVG(double value) {
        double sta = (0.15 + 0.7 * (1-value)) * 2 * Math.PI;
        double enda = 0.85 * 2 * Math.PI;
        String sweep = enda - sta > Math.PI ? "1" : "0";

        Point2D sto = polar(HB_RADIUS_O, sta);
        Point2D endo = polar(HB_RADIUS_O, enda);
        Point2D sti = polar(HB_RADIUS_I, sta);
        Point2D endi = polar(HB_RADIUS_I, enda);
        return "M" + sti.getX() + "," + sti.getY() +
               " L" + sto.getX() + "," + sto.getY() +
               " A " + HB_RADIUS_O + " " + HB_RADIUS_O + " 0 " + sweep + " 1 " +
               endo.getX() + " " + endo.getY() +
               " L" + endi.getX() + "," + endi.getY() +
               " A " + HB_RADIUS_I + " " + HB_RADIUS_I + " 0 " + sweep + " 0 " +
               sti.getX() + " " + sti.getY();
    }

    /**
     * Generates the display of the robot's health points
     */
    private static SVGPath healthBar() {
        SVGPath p = new SVGPath();
        p.setContent(hbSVG(1));
        p.setFill(HB_START_COLOR);

        return p;
    }

    private static Point2D polar(double r, double a) {
        double adjA = -a + Math.PI / 2;
        return new Point2D(r * Math.cos(adjA), -r * Math.sin(adjA));
    }

}
