package server.sim.entity;

public class Meteorite extends Obstacle {

    public Meteorite(double x, double y) {
        super(x, y);
    }

    @Override
    public byte getObstacleType() {
        return 0;
    }

}
