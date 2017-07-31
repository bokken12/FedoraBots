package client.figure;

import javafx.scene.Group;

/**
 * A superclass for all obstacle figures to display in the GUI. It has various
 * methods for responding to data sent from the sim.
 */
public abstract class ObstacleFigure extends Group implements Figure {
    private Group child;

    public ObstacleFigure(double radius, String fileName) {
        super(Figure.loadSvg(fileName));
        child = (Group) getChildren().get(0);

        Figure.setGroupSize(child, radius * 2);
    }

    public abstract void setRotation(double angle);

    protected Group getChild() {
        return child;
    }

}
