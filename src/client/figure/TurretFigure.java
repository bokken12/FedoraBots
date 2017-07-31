package client.figure;

import common.Constants;
import javafx.scene.Group;
import javafx.scene.transform.Translate;

public class TurretFigure extends ObstacleFigure {
    private Group turret;

    public TurretFigure(double radius) {
        super(radius, "/obstacles/turret.svg", Constants.Obstacle.TURRET_RANGE);

        turret = (Group) getChild().lookup("#turret");

        double turretCenterY = turret.getBoundsInParent().getMinY() + turret.getBoundsInParent().getHeight() / 2.0;
        turret.getTransforms().add(new Translate(0, turretCenterY - Figure.SVG_SIZE / 2.0));
        turret.setTranslateY(Figure.SVG_SIZE / 2.0 - turretCenterY);
    }

    public void setRotation(double angle) {
        turret.setRotate(angle);
    }

}
