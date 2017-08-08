package fedorabots.server.sim.entity;

public class Meteorite extends Obstacle {

    public Meteorite(byte id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public byte getObstacleType() {
        return 0;
    }

}
