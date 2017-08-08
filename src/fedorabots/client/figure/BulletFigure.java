package fedorabots.client.figure;

import fedorabots.common.Constants.Bullet;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * For now bullets are simply circles.
 */
public class BulletFigure extends Circle implements Figure {

    public BulletFigure() {
        super(Bullet.RADIUS, Color.DARKORANGE);
    }
}
