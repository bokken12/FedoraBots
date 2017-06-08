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
	
	public PhysicsEntity(double x, double y, double radius, double vx, double vy, double ax, double ay) {
		super(x, y, radius);
		this.vx = vx;
		this.vy = vy;
		this.ax = ax;
		this.ay = ay;
	}
	
	public PhysicsEntity(double x, double y, double radius) {
		this(x, y, radius, 0, 0, 0, 0);
	}
	
	public boolean collidesWith(PhysicsEntity other) {
		return true;
	}
	
	public void resolveCollision(PhysicsEntity other) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(ax);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ay);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mass);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(restitution);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(vx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(vy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
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
}
