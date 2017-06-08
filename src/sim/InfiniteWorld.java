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
public class InfiniteWorld extends World {
	
	private Set<Entity> things;
	private World child;
	
	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public InfiniteWorld(World child) {
		super(Double.NaN, Double.NaN, Double.NaN, Double.NaN, null);
		this.child = child;
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
		child.forEachUnsafe(consumer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#forColliding(sim.Entity, java.util.function.Consumer)
	 */
	@Override
	public void forCollidingUnsafe(Entity source, Consumer<Entity> consumer) {
		for(Entity e : things) {
			if(Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2) <= Math.pow(
					source.getRadius() + e.getRadius(), 2)) {
				consumer.accept(e);
			}
		}
		child.forCollidingUnsafe(source, consumer);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see sim.World#closest(sim.Entity)
	 */
	@Override
	public Entity closest(Entity source) {
		Entity closest = child.closest(source);
		double dmin = closest == null ? Double.MAX_VALUE : Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
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
		Entity closest = child.closest(source, condition);
		double dmin = closest == null ? Double.MAX_VALUE : Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
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
		if(child.fullyContains(entity)) {
			child.add(entity);
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
		if(!things.remove(entity))
			child.remove(entity);
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
		child.forCollidingUnsafe(x, y, width, height, consumer);
	}

	/* (non-Javadoc)
	 * @see sim.World#fullyContains(sim.Entity)
	 */
	@Override
	public boolean fullyContains(Entity entity) {
		return true;
	}
}
