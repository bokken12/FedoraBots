/**
 *
 */
package server.sim;

import javafx.scene.canvas.GraphicsContext;

/**
 * @author joelmanning
 *
 */
public abstract class Entity {
	private short id;
	private double x, y, radius;
	private World world;

	/**
	 * @param x
	 * @param y
	 * @param radius
	 */
	public Entity(short id, double x, double y, double radius) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	public void tick(double millis, World world){}

	public void paint(GraphicsContext g){}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
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
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if(Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
			return false;
		if(Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if(Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	/**
	 * @return the id
	 */
	public short getId() {
		return id;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		setPosition(x, y);
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		setPosition(x, y);
	}

	public void setPosition(double x, double y){
		if(world != null){
			world.remove(this);
			this.x = x;
			this.y = y;
			world.add(this);
		}
	}

	/**
	 * Updates the entity's internal x and y
	 *
	 * Caution: This does not handle jumps between sub-worlds. This method
	 * should only be used if the entity is guaranteed to end up in the same
	 * world after the move.
	 */
	public void setPositionUnsafe(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * @param world the world to set
	 */
	void setWorld(World world) {
		this.world = world;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Entity [x=" + x + ", y=" + y + ", radius=" + radius + ", world=" + world + "]";
	}
}
