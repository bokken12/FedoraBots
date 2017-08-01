package client.figure;

import common.Constants;

public class JammerFigure extends ObstacleFigure {

    public JammerFigure(double radius) {
        super(radius, "/obstacles/jammer.svg", Constants.Obstacle.JAMMER_RANGE);
    }

    public void setRotation(double angle) {
        // Do nothing
    }

}
