package client.figure;

import javafx.scene.shape.Shape;

public class VaporizerFigure extends ObstacleFigure {
    private Shape pulse;

    public VaporizerFigure(double radius) {
        super(radius, "/obstacles/vaporizer.svg");

        pulse = (Shape) getChild().lookup("#pulse");

        pulse.setScaleX(0);
        pulse.setScaleY(0);
    }

    public void setRotation(double angle) {
        double scale = angle / 360;
        pulse.setScaleX(scale);
        pulse.setScaleY(scale);
    }

}
