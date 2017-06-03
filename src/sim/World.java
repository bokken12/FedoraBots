/**
 * 
 */
package sim;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author joelmanning
 *
 */
public abstract class World {
	
	private double x, y, width, height;
	private World parent;
	
	public static final int MIN_WIDTH = 1;
	public static final int MIN_HEIGHT = 1;
	
	protected World(double x, double y, double width, double height, World parent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.parent = parent;
	}
	
	public static World generateWorld(double x, double y, double width, double height, World parent) {
		if(width > height) {
			if(width < MIN_WIDTH * 2) {
				return new BasicWorld(x, y, width, height, parent);
			} else {
				return new VerticalSplitWorld(x, y, width, height, parent);
			}
		} else {
			if(height < MIN_HEIGHT * 2) {
				return new BasicWorld(x, y, width, height, parent);
			} else {
				return new HorizontalSplitWorld(x, y, width, height, parent);
			}
		}
	}
	
	/**
	 * @return the parent
	 */
	public World getParent() {
		return parent;
	}
	
	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(World parent) {
		this.parent = parent;
	}
	
	public abstract void forEach(Consumer<Entity> consumer);
	
	public void forCondition(Predicate<Entity> condition, Consumer<Entity> consumer) {
		forEach((e) -> {
			if(condition.test(e)) {
				consumer.accept(e);
			}
		});
	}
	
	public abstract void forColliding(Entity source, Consumer<Entity> consumer);
	
	public abstract void forColliding(double x, double y, double width, double height, Consumer<Entity> consumer);
	
	public abstract Entity closest(Entity source);
	
	public abstract Entity closest(Entity source, Predicate<Entity> condition);
	
	public boolean fullyContains(Entity entity) {
		return entity.getX() - entity.getRadius() > x && entity.getX() + entity.getRadius() < x + width
				&& entity.getY() - entity.getRadius() > y && entity.getY() + entity.getRadius() < y + height;
	}
	
	public abstract void add(Entity entity);
	
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
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}
	
	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}
	
	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}
	
	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}
	
	public abstract void remove(Entity entity);
	
	protected static boolean intersects(Entity circle, double x, double y, double width, double height) {
		double rx = x + width / 2;
		double ry = y + height / 2;
		double circleDistancex = Math.abs(circle.getX() - rx);
		double circleDistancey = Math.abs(circle.getY() - ry);
		if(circleDistancex > (width / 2 + circle.getRadius())) {
			return false;
		}
		if(circleDistancey > (height / 2 + circle.getRadius())) {
			return false;
		}
		if(circleDistancex <= (width / 2)) {
			return true;
		}
		if(circleDistancey <= (height / 2)) {
			return true;
		}
		double cornerDistance_sq = Math.pow(circleDistancex - width / 2, 2) + Math.pow(circleDistancey - height / 2, 2);
		
		return cornerDistance_sq <= Math.pow(circle.getRadius(), 2);
	}
}
