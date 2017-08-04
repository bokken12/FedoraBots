package client.sensor;

import javafx.scene.paint.Color;

public class DetectedRobot extends DetectedEntity {

    private Color color;

    public DetectedRobot(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    private String colorString() {
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    @Override
    public String toString() {
        return "DetectedRobot [x=" + getX() + ", y=" + getY() + ", color=" + colorString() + "]";
    }
}
