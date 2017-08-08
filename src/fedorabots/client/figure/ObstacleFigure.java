package fedorabots.client.figure;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * A superclass for all obstacle figures to display in the GUI. It has various
 * methods for responding to data sent from the sim.
 */
public abstract class ObstacleFigure extends Group implements Figure {
    private static final Color RANGE_COLOR = Color.web("#C9C9C9");

    private Group child;
    private double range;

    public ObstacleFigure(double radius, String filename) {
        this(radius, filename, 0);
    }

    public ObstacleFigure(double radius, String fileName, double range) {
        super(Figure.loadSvg(fileName));
        child = (Group) getChildren().get(0);
        this.range = range;

        Figure.setGroupSize(child, radius * 2);
    }

    public abstract void setRotation(double angle);

    protected Group getChild() {
        return child;
    }

    public static ObstacleFigure forType(byte type, double radius) {
        switch (type) {
            case 0: return new MeteoriteFigure(radius);
            case 1: return new TurretFigure(radius);
            case 2: return new VaporizerFigure(radius);
            case 3: return new JammerFigure(radius);
            default: throw new RuntimeException("No obstacle figure for type " + type + ".");
        }
    }

    public Node getRangeMarker() {
        if (range == 0) {
            return null;
        }
        Circle rangeMarker = new Circle(0, 0, range, Color.TRANSPARENT);
        rangeMarker.setStroke(RANGE_COLOR);
        rangeMarker.setStrokeWidth(2);
        return rangeMarker;
    }

}
