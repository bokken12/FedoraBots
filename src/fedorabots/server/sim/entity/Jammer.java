package fedorabots.server.sim.entity;

public class Jammer extends Obstacle {

    public Jammer(byte id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public byte getObstacleType() {
        return 3;
    }

}
