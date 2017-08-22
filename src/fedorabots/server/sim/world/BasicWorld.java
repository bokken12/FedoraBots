/**
 *
 */
package fedorabots.server.sim.world;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fedorabots.server.sim.entity.Entity;

/**
 * @author joelmanning
 *
 */
public class BasicWorld extends World {

	private Set<Entity> things;

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public BasicWorld(double x, double y, double width, double height, World parent) {
		super(x, y, width, height, parent);
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
			if (!e.markedForRemoval()) {
				consumer.accept(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see sim.World#forColliding(sim.Entity, java.util.function.Consumer)
	 */
	@Override
	public void forCollidingUnsafe(Entity source, Consumer<Entity> consumer) {
		for(Entity e : things) {
			if(!e.markedForRemoval() && !source.equals(e)
					&& Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2) <= Math.pow(
							source.getRadius() + e.getRadius(), 2)) {
				consumer.accept(e);
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
		Entity closest = null;
		double dmin = Double.MAX_VALUE;
		for(Entity e : things) {
			if(!e.markedForRemoval() && !source.equals(e)) {
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
	 * @see sim.World#closest(sim.Entity, java.util.function.Predicate)
	 */
	@Override
	public Entity closest(Entity source, Predicate<Entity> condition) {
		Entity closest = null;
		double dmin = Double.MAX_VALUE;
		for(Entity e : things) {
			if(!e.markedForRemoval() && !source.equals(e) && condition.test(e)) {
				double d = Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2);
				System.out.println("matches");
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
		things.remove(entity);
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
			if(!e.markedForRemoval() && World.intersects(e, x, y, width, height)) {
				consumer.accept(e);
			}
		}
	}

	@Override
	public World emptyClone() {
		return new BasicWorld(getX(), getY(), getWidth(), getHeight(), getParent());
	}
}
