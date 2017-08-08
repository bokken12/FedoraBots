package fedorabots.server.sim.world;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fedorabots.server.sim.entity.Entity;

public class ScrollingWorld extends World {

	private Set<Entity> things;
	private World child;

	public ScrollingWorld(double x, double y, double width, double height, World child) {
		super(x, y, width, height, null);
		this.child = child;
		things = new HashSet<Entity>();
	}

	@Override
	public void add(Entity entity) {
		// The statement below assumes the child will never be added more than 1 full scroll away.
		// If this assumption is false, there will need to be another mod.
		entity.setPositionUnsafe((entity.getX() - this.getX() + this.getWidth()) % this.getWidth() + this.getX(),
									(entity.getY() - this.getY() + this.getHeight()) % this.getHeight() + this.getY());

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
	 * @see sim.World#forEach(java.util.function.Consumer)
	 */
	@Override
	public void forEachUnsafe(Consumer<Entity> consumer) {
		for(Entity e : things) {
			if (!e.markedForRemoval()) {
				consumer.accept(e);
			}
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
			if(!e.markedForRemoval() && !source.equals(e)
					&& Math.pow(source.getX() - e.getX(), 2) + Math.pow(source.getY() - e.getY(), 2) <= Math.pow(
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
		double dmin = closest == null ? Double.MAX_VALUE : Math.pow(source.getX() - closest.getX(), 2)
				+ Math.pow(source.getY() - closest.getY(), 2);
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
		Entity closest = child.closest(source, condition);
		double dmin = closest == null ? Double.MAX_VALUE : Math.pow(source.getX() - closest.getX(), 2)
				+ Math.pow(source.getY() - closest.getY(), 2);
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
	 * @see sim.World#remove(sim.Entity)
	 */
	@Override
	public void remove(Entity entity) {
		if(!things.remove(entity))
			child.remove(entity);
	}

	@Override
	public void removeMarked() {
		super.removeMarked();
		child.removeMarked();
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
		child.forCollidingUnsafe(x, y, width, height, consumer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see sim.World#fullyContains(sim.Entity)
	 */
	@Override
	public boolean fullyContains(Entity entity) {
		return true;
	}

}
