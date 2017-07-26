package server.sim;

public class Bullet extends PhysicsEntity {

	public Bullet(double x, double y, double radius, double mass, double vx, double vy)
	{
		super((short) 0, null, x, y, 0, radius, mass, vx, vy, 0, 0);
	}

	public Bullet(double x, double y, double radius, double mass) {
		super((short) 0, null, x, y, 0, radius, mass);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bullet" + super.toString().substring(13);
	}
}
