/**
 *
 */
package server.sim;

/**
 * @author joelmanning
 *
 */
public abstract class PhysicsEntity extends Entity {

	private double vx, vy, ax, ay, mass;

	public PhysicsEntity(short id, double x, double y, double radius, double mass, double vx, double vy, double ax, double ay) {
		super(id, x, y, radius);
		this.mass = mass;
		this.vx = vx;
		this.vy = vy;
		this.ax = ax;
		this.ay = ay;
	}

	public PhysicsEntity(short id, double x, double y, double radius, double mass) {
		this(id, x, y, radius, mass, 0, 0, 0, 0);
	}

	public boolean collidesWith(PhysicsEntity other) {
		return true;
	}

	public void resolveCollision(PhysicsEntity other) {
		System.out.println("Resolving a collision between");
		System.out.println(this);
		System.out.println(other);
		double distsq = Math.pow(getX() - other.getX(), 2) + Math.pow(getY() - other.getY(), 2);
		double dp = (vx - other.vx) * (getX() - other.getX()) + (vy - other.vy) * (getY() - other.getY());

		if (dp >= 0)
			return;

		System.out.println(dp);
		double common = 2 * dp / distsq / (mass + other.mass);

		// double oldvx = vx, oldvy = vy;
		vx -= common * other.mass * (getX() - other.getX());
		vy -= common * other.mass * (getY() - other.getY());

		other.vx -= common * mass * (other.getX() - getX());
		other.vy -= common * mass * (other.getY() - getY());

		// vx = (vx * (mass - other.mass) + (2 * other.mass * other.vx)) / (mass + other.mass);
		// vy = (vy * (mass - other.mass) + (2 * other.mass * other.vy)) / (mass + other.mass);
		// other.vx = (other.vx * (other.mass - mass) + (2 * mass * oldvx)) / (mass + other.mass);
		// other.vy = (other.vy * (other.mass - mass) + (2 * mass * oldvy)) / (mass + other.mass);
		System.out.println("Collision resolved, now");
		System.out.println(this);
		System.out.println(other);
	}

	@Override
	public void tick(double length, World world) {
		setPosition(getX() + vx * length, getY() + vy * length);
		vx += ax * length;
		vy += ay * length;
		world.forCollidingUnsafe(this, (e) -> {
			if(e instanceof PhysicsEntity && collidesWith((PhysicsEntity) e)) {
				resolveCollision((PhysicsEntity) e);
			}
		});
	}

	/**
	 * @return the vx
	 */
	public double getVx() {
		return vx;
	}

	/**
	 * @return the vy
	 */
	public double getVy() {
		return vy;
	}

	/**
	 * @return the ax
	 */
	public double getAx() {
		return ax;
	}

	/**
	 * @return the ay
	 */
	public double getAy() {
		return ay;
	}

	/**
	 * @param vx
	 *            the vx to set
	 */
	public void setVx(double vx) {
		this.vx = vx;
	}

	/**
	 * @param vy
	 *            the vy to set
	 */
	public void setVy(double vy) {
		this.vy = vy;
	}

	/**
	 * @param ax
	 *            the ax to set
	 */
	public void setAx(double ax) {
		this.ax = ax;
	}

	/**
	 * @param ay
	 *            the ay to set
	 */
	public void setAy(double ay) {
		this.ay = ay;
	}

	/**
	 * @return the mass
	 */
	public double getMass() {
		return mass;
	}

	/**
	 * @param mass the mass to set
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!super.equals(obj))
			return false;
		if(getClass() != obj.getClass())
			return false;
		PhysicsEntity other = (PhysicsEntity) obj;
		if(Double.doubleToLongBits(ax) != Double.doubleToLongBits(other.ax))
			return false;
		if(Double.doubleToLongBits(ay) != Double.doubleToLongBits(other.ay))
			return false;
		if(Double.doubleToLongBits(mass) != Double.doubleToLongBits(other.mass))
			return false;
		if(Double.doubleToLongBits(vx) != Double.doubleToLongBits(other.vx))
			return false;
		if(Double.doubleToLongBits(vy) != Double.doubleToLongBits(other.vy))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PhysicsEntity [vx=" + vx + ", vy=" + vy + ", ax=" + ax + ", ay=" + ay + ", mass=" + mass
				+ ", toString()=" + super.toString() + "]";
	}
}
