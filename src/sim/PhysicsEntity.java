/**
 * 
 */
package sim;

/**
 * @author joelmanning
 *
 */
public abstract class PhysicsEntity extends Entity {

	private double vx, vy, ax, ay;
	
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
	
	public boolean collidesWith(PhysicsEntity other){
		return false;
	}
	
	@Override
	public void tick(){
		vx += ax;
		vy += ay;
		setPosition(getX() + vx, getY() + vy);
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
	 * @param vx the vx to set
	 */
	public void setVx(double vx) {
		this.vx = vx;
	}

	/**
	 * @param vy the vy to set
	 */
	public void setVy(double vy) {
		this.vy = vy;
	}

	/**
	 * @param ax the ax to set
	 */
	public void setAx(double ax) {
		this.ax = ax;
	}

	/**
	 * @param ay the ay to set
	 */
	public void setAy(double ay) {
		this.ay = ay;
	}
}
