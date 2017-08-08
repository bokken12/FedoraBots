package fedorabots.client.sensor;

public abstract class DetectedEntity {

    private int x;
    private int y;

    public DetectedEntity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
