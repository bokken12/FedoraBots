package client.figure;

import common.Constants;
import javafx.scene.shape.Shape;

public class VaporizerFigure extends ObstacleFigure {
    private Shape pulse;
    private static double MIN_SCALE = Constants.Obstacle.RADIUS / Constants.Obstacle.VAPORIZER_RANGE;

    public VaporizerFigure(double radius) {
        super(radius, "/obstacles/vaporizer.svg");

        pulse = (Shape) getChild().lookup("#pulse");

        pulse.setScaleX(MIN_SCALE);
        pulse.setScaleY(MIN_SCALE);
    }

    public void setRotation(double angle) {
        double scale = MIN_SCALE + angle / 360 * (1 - MIN_SCALE);
        pulse.setScaleX(scale);
        pulse.setScaleY(scale);
    }

}
