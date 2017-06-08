/**
 * 
 */
package sim;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author joelmanning
 *
 */
public class VerticalSplitWorld extends World {
	
	private World left;
	private World right;
	private Set<Entity> things;
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public VerticalSplitWorld(double x, double y, double width, double height, World parent) {
		super(x, y, width, height, parent);
		left = World.generateWorld(x, y, width / 2, height, this);
		right = World.generateWorld(x + width / 2, y, width / 2, height, this);
		things = new HashSet<Entity>();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#forEach(java.util.function.Consumer)
	 */
	@Override
	public void forEachUnsafe(Consumer<Entity> consumer) {
		for(Entity e : things) {
			consumer.accept(e);
		}
		left.forEachUnsafe(consumer);
		right.forEachUnsafe(consumer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#forColliding(sim.Entity, java.util.function.Consumer)
	 */
	@Override
	public void forCollidingUnsafe(Entity source, Consumer<Entity> consumer) {
		if(left.fullyContains(source)) {
			left.forCollidingUnsafe(source, consumer);
		} else if(right.fullyContains(source)) {
			right.forCollidingUnsafe(source, consumer);
		} else {
			for(Entity e : things) {
				if(Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2) <= Math.pow(
						source.getRadius() + e.getRadius(), 2)) {
					consumer.accept(e);
				}
			}
			if(source.getX() - source.getRadius() < getX() + getWidth() / 2) {
				left.forCollidingUnsafe(source, consumer);
			}
			if(source.getX() + source.getRadius() > getX() + getWidth() / 2) {
				right.forCollidingUnsafe(source, consumer);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#closest(sim.Entity)
	 */
	@Override
	public Entity closest(Entity source) {
		Entity closest;
		double dmin;
		if(source.getX() + source.getRadius() < getX() + getWidth() / 2) {
			closest = left.closest(source);
			if(closest == null)
				closest = right.closest(source);
			dmin = Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
		} else if(source.getX() - source.getRadius() > getX() + getWidth() / 2) {
			closest = right.closest(source);
			if(closest == null)
				closest = left.closest(source);
			dmin = (int) (Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2));
		} else {
			closest = null;
			dmin = Double.MAX_VALUE;
		}
		for(Entity e : things) {
			double d = Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2);
			if(d < dmin) {
				dmin = d;
				closest = e;
			}
		}
		return closest;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#closest(sim.Entity, java.util.function.Predicate)
	 */
	@Override
	public Entity closest(Entity source, Predicate<Entity> condition) {
		Entity closest;
		double dmin;
		if(source.getX() + source.getRadius() < getX() + getWidth() / 2) {
			closest = left.closest(source, condition);
			if(closest == null)
				closest = right.closest(source, condition);
			dmin = Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
		} else if(source.getX() - source.getRadius() > getX() + getWidth() / 2) {
			closest = right.closest(source, condition);
			if(closest == null)
				closest = left.closest(source, condition);
			dmin = (int) (Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2));
		} else {
			closest = null;
			dmin = Double.MAX_VALUE;
		}
		for(Entity e : things) {
			if(condition.test(e)) {
				double d = Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2);
				if(d < dmin) {
					dmin = d;
					closest = e;
				}
			}
		}
		return closest;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#add(sim.Entity)
	 */
	@Override
	public void add(Entity entity) {
		if(!fullyContains(entity)) {
			getParent().add(entity);
		} else if(entity.getX() + entity.getRadius() < getX() + getWidth() / 2) {
			left.add(entity);
		} else if(entity.getX() - entity.getRadius() > getX() + getWidth() / 2) {
			right.add(entity);
		} else {
			things.add(entity);
			entity.setWorld(this);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#remove(sim.Entity)
	 */
	@Override
	public void remove(Entity entity) {
		if(!things.remove(entity)) {
			left.remove(entity);
			right.remove(entity);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#forColliding(int, int, int, int,
	 * java.util.function.Consumer)
	 */
	@Override
	public void forCollidingUnsafe(double x, double y, double width, double height, Consumer<Entity> consumer) {
		for(Entity e : things) {
			if(World.intersects(e, x, y, width, height)) {
				consumer.accept(e);
			}
		}
		if(x < getX() + getWidth() / 2) {
			left.forCollidingUnsafe(x, y, width, height, consumer);
		}
		if(x + width > getX() + getWidth() / 2) {
			right.forCollidingUnsafe(x, y, width, height, consumer);
		}
	}
}
