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
public class HorizontalSplitWorld extends World {

	private World top;
	private World bottom;
	private Set<Entity> things;

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public HorizontalSplitWorld(double x, double y, double width, double height, World parent) {
		super(x, y, width, height, parent);
		top = World.generateWorld(x, y, width, height / 2, this);
		bottom = World.generateWorld(x, y + height / 2, width, height / 2, this);
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
		top.forEachUnsafe(consumer);
		bottom.forEachUnsafe(consumer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see sim.World#forColliding(sim.Entity, java.util.function.Consumer)
	 */
	@Override
	public void forCollidingUnsafe(Entity source, Consumer<Entity> consumer) {
		if(top.fullyContains(source)) {
			top.forCollidingUnsafe(source, consumer);
		} else if(bottom.fullyContains(source)) {
			bottom.forCollidingUnsafe(source, consumer);
		} else {
			for(Entity e : things) {
				if(!e.markedForRemoval() && !source.equals(e)
						&& Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2) <= Math.pow(
								source.getRadius() + e.getRadius(), 2)) {
					consumer.accept(e);
				}
			}
			if(source.getY() - source.getRadius() < getY() + getHeight() / 2) {
				top.forCollidingUnsafe(source, consumer);
			}
			if(source.getY() + source.getRadius() > getY() + getHeight() / 2) {
				bottom.forCollidingUnsafe(source, consumer);
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
		if(source.getY() + source.getRadius() < getY() + getHeight() / 2) {
			closest = top.closest(source);
			if(closest == null)
				closest = bottom.closest(source);
			if(closest != null)
				dmin = Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
		} else if(source.getY() - source.getRadius() > getY() + getHeight() / 2) {
			closest = bottom.closest(source);
			if(closest == null)
				closest = top.closest(source);
			if(closest != null)
				dmin = (int) (Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2));
		} else {
			closest = null;
			dmin = Double.MAX_VALUE;
		}
		for(Entity e : things) {
			if(!e.markedForRemoval() && !source.equals(e)) {
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
	 * @see sim.World#closest(sim.Entity, java.util.function.Predicate)
	 */
	@Override
	public Entity closest(Entity source, Predicate<Entity> condition) {
		Entity closest = null;
		double dmin = Double.MAX_VALUE;
		if(source.getY() + source.getRadius() < getY() + getHeight() / 2) {
			closest = top.closest(source, condition);
			if(closest == null)
				closest = bottom.closest(source, condition);
			if(closest != null)
				dmin = Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2);
		} else if(source.getY() - source.getRadius() > getY() + getHeight() / 2) {
			closest = bottom.closest(source, condition);
			if(closest == null)
				closest = top.closest(source, condition);
			if(closest != null)
				dmin = (int) (Math.pow(source.getX() - closest.getX(), 2) + Math.pow(source.getY() - closest.getY(), 2));
		}
		for(Entity e : things) {
			if(!e.markedForRemoval() && !source.equals(e) && condition.test(e)) {
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
		} else if(entity.getY() + entity.getRadius() < getY() + getHeight() / 2) {
			top.add(entity);
		} else if(entity.getY() - entity.getRadius() > getY() + getHeight() / 2) {
			bottom.add(entity);
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
			top.remove(entity);
			bottom.remove(entity);
		}
	}

	@Override
	public void removeMarked() {
		super.removeMarked();
		top.removeMarked();
		bottom.removeMarked();
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
		if(y < getY() + getHeight() / 2) {
			top.forCollidingUnsafe(x, y, width, height, consumer);
		}
		if(y + height > getY() + getHeight() / 2) {
			bottom.forCollidingUnsafe(x, y, width, height, consumer);
		}
	}

	@Override
	public World emptyClone() {
		return new HorizontalSplitWorld(getX(), getY(), getWidth(), getHeight(), getParent());
	}
}
