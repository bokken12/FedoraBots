package fedorabots.client.event;

public class Vaporizer {
    private int x;
    private int y;

    public Vaporizer(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x location
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y location
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Vaporizer [x=" + x + ", y=" + y + "]";
    }

}
