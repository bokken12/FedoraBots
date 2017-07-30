package server.sim.entity;

public class Jammer extends Obstacle {

    public Jammer(double x, double y) {
        super(x, y);
    }

    @Override
    public byte getObstacleType() {
        return 3;
    }

}
