/**
 * 
 */
package sim;

/**
 * @author joelmanning
 *
 */
public abstract class PhysicsEntity extends Entity {
	
	private double vx, vy, ax, ay, mass, restitution;
	
	public PhysicsEntity(double x, double y, double radius, double mass, double restitution, double vx, double vy, double ax, double ay) {
		super(x, y, radius);
		this.mass = mass;
		this.restitution = restitution;
		this.vx = vx;
		this.vy = vy;
		this.ax = ax;
		this.ay = ay;
	}
	
	public PhysicsEntity(double x, double y, double radius, double mass, double restitution) {
		this(x, y, radius, mass, restitution, 0, 0, 0, 0);
	}
	
	public boolean collidesWith(PhysicsEntity other) {
		return true;
	}
	
	public void resolveCollision(PhysicsEntity other) {
		System.out.println("Resolving a collision between");
		System.out.println(this);
		System.out.println(other);
		double rvx = other.vx - vx;
		double rvy = other.vy - vy;
		double nx = other.getX() - getX();
		double ny = other.getY() - getY();
		double vnorm = rvx * nx + rvy * ny;
		if(vnorm > 0)
			return;
		double e = Math.min(other.restitution, restitution);
		double j = -(1 + e) * vnorm;
		j /= 1 / mass + 1/ other.mass;
		double ix = nx * j;
		double iy = ny * j;
		vx -= 1 / mass * ix;
		vy -= 1 / mass * iy;
		other.vx += 1 / other.mass * ix;
		other.vy += 1 / other.mass * iy;
		System.out.println("Collision resolved, now");
		System.out.println(this);
		System.out.println(other);
	}
	
	@Override
	public void tick(int length, World world) {
		setPosition(getX() + vx * length + ax * length * length / 2, getY() + vy * length + ay * length * length / 2);
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
	 * @return the restitution
	 */
	public double getRestitution() {
		return restitution;
	}

	/**
	 * @param mass the mass to set
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}

	/**
	 * @param restitution the restitution to set
	 */
	public void setRestitution(double restitution) {
		this.restitution = restitution;
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
		if(Double.doubleToLongBits(restitution) != Double.doubleToLongBits(other.restitution))
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
				+ ", restitution=" + restitution + ", toString()=" + super.toString() + "]";
	}
}
