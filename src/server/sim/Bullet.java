package server.sim;

public class Bullet extends PhysicsEntity {

	public Bullet(short id, double x, double y, double radius,
				  double mass, double vx, double vy)
	{
		super(id, null, x, y, 0, radius, mass, vx, vy, 0, 0);
	}

	public Bullet(short id, double x, double y, double radius, double mass) {
		super(id, null, x, y, 0, radius, mass);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bullet" + super.toString().substring(13);
	}
}
