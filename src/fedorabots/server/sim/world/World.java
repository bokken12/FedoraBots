/**
 *
 */
package fedorabots.server.sim.world;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fedorabots.common.Constants;
import fedorabots.common.Profiler;
import javafx.geometry.Point2D;
import fedorabots.server.sim.entity.Bullet;
import fedorabots.server.sim.entity.Entity;
import fedorabots.server.sim.entity.Obstacle;
import fedorabots.server.sim.entity.Robot;

/**
 * @author joelmanning
 *
 */
public abstract class World {

	private double x, y, width, height;
	private World parent;
	private Stack<Entity> toRemove;

	public static final int MIN_WIDTH = 40;
	public static final int MIN_HEIGHT = 40;

	protected World(double x, double y, double width, double height, World parent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.parent = parent;
		toRemove = new Stack<Entity>();
	}

	public static World generateInfiniteWorld(double x, double y, double width, double height) {
		World w = generateWorld(x, y, width, height, null);
		w.setParent(new InfiniteWorld(w));
		return w;
	}

	public static World generateScrollingWorld(double x, double y, double width, double height) {
		World w = generateWorld(x, y, width, height, null);
		w.setParent(new ScrollingWorld(x, y, width, height, w));
		return w.getParent();
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

	public abstract void forEachUnsafe(Consumer<Entity> consumer);

	public void forEach(Consumer<Entity> consumer){
		List<Entity> safeIterable = new ArrayList<Entity>();
		forEachUnsafe(e -> safeIterable.add(e));
		for(Entity e: safeIterable){
			consumer.accept(e);
		}
	}

	public void forConditionUnsafe(Predicate<Entity> condition, Consumer<Entity> consumer) {
		forEachUnsafe((e) -> {
			if(condition.test(e)) {
				consumer.accept(e);
			}
		});
	}

	public void forCondition(Predicate<Entity> condition, Consumer<Entity> consumer){
		List<Entity> safeIterable = new ArrayList<Entity>();
		forConditionUnsafe(condition, e -> safeIterable.add(e));
		for(Entity e: safeIterable){
			consumer.accept(e);
		}
	}

	public abstract void forCollidingUnsafe(Entity source, Consumer<Entity> consumer);

	public void forColliding(Entity source, Consumer<Entity> consumer){
		List<Entity> safeIterable = new ArrayList<Entity>();
		forCollidingUnsafe(source, e -> safeIterable.add(e));
		for(Entity e: safeIterable){
			consumer.accept(e);
		}
	}

	public abstract void forCollidingUnsafe(double x, double y, double width, double height, Consumer<Entity> consumer);

	public void forColliding(double x, double y, double width, double height, Consumer<Entity> consumer){
		List<Entity> safeIterable = new ArrayList<Entity>();
		forCollidingUnsafe(x, y, width, height, e -> safeIterable.add(e));
		for(Entity e: safeIterable){
			consumer.accept(e);
		}
	}

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

	public void markForRemoval(Entity entity) {
		toRemove.add(entity);
	}

	public void removeMarked() {
		while (!toRemove.isEmpty()) {
			remove(toRemove.pop());
		}
	}

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "World [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}

	private void writePosition(ByteBuffer buf, double x, double y) {
		buf.put((byte) ((int) x >> 4));
		buf.put((byte) ((((int) x & 0x0F) << 4) + ((int) y >> 8)));
		buf.put((byte) ((int) y & 0xFF));
	}

	private void writeState(ByteBuffer buf, int offset, Collection<Robot> robots) {
		Profiler.time("Compute state");;

		for (Robot entity : robots) {
			// System.out.print(entity.getX() + " " + entity.getY() + "        ");
			buf.put((byte) (entity.getId() >> 8));
			buf.put((byte) (entity.getId() & 0xFF));
			writePosition(buf, entity.getX(), entity.getY());
			buf.put((byte) (entity.getRotation() / 2 / Math.PI * 255));
			Robot pe = (Robot) entity;
			buf.put((byte) ((Math.atan2(pe.getVy(), pe.getVx()) + Math.PI / 2) / 2 / Math.PI * 255));
			buf.put((byte) ((Math.atan2(pe.getAy(), pe.getAx()) + Math.PI / 2) / 2 / Math.PI * 255));
			buf.position(buf.position() - 8 + offset);
		}
		Profiler.timeEnd("Compute state");
		// System.out.println();
	}

	public void writeState(ByteBuffer buf, Collection<Robot> robots) {
		writeState(buf, 8, robots);
	}

	public void writeStartingState(ByteBuffer buf, Collection<Robot> robots, Collection<Obstacle> obstacles) {
		writeState(buf, 11, robots);
		 // Go back to beginning, then forward 8
		buf.position(buf.position() - robots.size() * 11 + 8);

		int i = robots.size();
		for (Robot entity : robots) {
			buf.put((byte) entity.getColor().getRed());
			buf.put((byte) entity.getColor().getGreen());
			buf.put((byte) entity.getColor().getBlue());
			i--;
			if (i > 0) {
				buf.position(buf.position() + 8);
			}
		}

		for (Obstacle obs : obstacles) {
			buf.put((byte) obs.getId());
			buf.put(obs.getObstacleType());
			writePosition(buf, obs.getX(), obs.getY());
		}
	}

	public Map<Short, byte[]> velocityStates(Collection<Robot> robots) {
		Profiler.time("Compute vel states");
		Map<Short, byte[]> m = new HashMap<Short, byte[]>();
		for (Robot entity : robots) {
			Robot pe = (Robot) entity;
			ByteBuffer bb = ByteBuffer.allocate(8);
			bb.putFloat((float) (pe.getVx()*1e3));
			bb.putFloat((float) (pe.getVy()*1e3));
			m.put(entity.getId(), bb.array());
		}
		Profiler.timeEnd("Compute vel states");
		return m;
	}

	private <T> Collection<T> getEntitiesOfClass(Class<T> cl) {
		Collection<T> entities = new ArrayList<T>();
		forEachUnsafe(entity -> {
			if (cl.isInstance(entity)) {
				entities.add(cl.cast(entity));
			}
		});
		return entities;
	}

	public Collection<Bullet> getBullets() {
		return getEntitiesOfClass(Bullet.class);
	}

	public Collection<Obstacle> getObstacles() {
		return getEntitiesOfClass(Obstacle.class);
	}

	public void writeBulletStates(ByteBuffer buf, Collection<Bullet> bullets) {
		Profiler.time("Compute bul states");

		for (Bullet bullet : bullets) {
			writePosition(buf, bullet.getX(), bullet.getY());
			buf.put((byte) (Math.atan2(bullet.getVy(), bullet.getVx()) / 2 / Math.PI * 255));
		}
		Profiler.timeEnd("Compute bul states");
	}

	public List<Robot> healthChangedRobots(Collection<Robot> robots) {
		List<Robot> hcRobots = new ArrayList<Robot>();
		for (Robot robot : robots) {
			if (robot.hasHealthChanged()) {
				hcRobots.add(robot);
				if (robot.getHealth() == 0) {
					robot.getWorld().remove(robot);
				}
			}
		}

		return hcRobots;
	}

	public static short formatDamageAngle(Point2D angle) {
		if (angle.magnitude() > Constants.Bullet.VELOCITY * 2 / 1e3) {
			return (short) angle.getY();
		} else {
			int toPut = 0x8000 | (int) ((Math.atan2(angle.getY(), angle.getX()) + Math.PI / 2) / 2 / Math.PI * 0x7FFF);
			return (short) toPut;
		}
	}

	public ByteBuffer healthStates(List<Robot> robots) {
		int numStates = robots.stream().collect(Collectors.summingInt(robot -> robot.getDamageAngles().size()));

		ByteBuffer healthStates = ByteBuffer.allocate(numStates * 5 + 2);
		healthStates.put((byte) 2);
		healthStates.put((byte) numStates);
		for (Robot robot : robots) {
			for (Point2D bulletAngle : robot.getDamageAngles()) {
				healthStates.putShort(robot.getId());
				healthStates.put((byte) (robot.getHealth() * 255));
				healthStates.putShort(formatDamageAngle(bulletAngle));
			}
			robot.clearDamageAngles();
		}

		healthStates.rewind();
		return healthStates;
	}

	public List<Obstacle> rotationChangedObstacles(Collection<Obstacle> obstacles) {
		return obstacles.stream().filter(Obstacle::hasRotationChanged).collect(Collectors.toList());
	}

	public ByteBuffer obstacleStates(List<Obstacle> obstacles) {
		ByteBuffer obstacleStates = ByteBuffer.allocate(obstacles.size() * 2 + 2);
		obstacleStates.put((byte) 3);
		obstacleStates.put((byte) obstacles.size());
		for (Obstacle obstacle : obstacles) {
			obstacleStates.put((byte) obstacle.getId());
			obstacleStates.put((byte) (obstacle.getRotation() / 2 / Math.PI * 255));
		}

		obstacleStates.rewind();
		return obstacleStates;
	}

	public static int stateLength(Collection<Robot> robots, Collection<Bullet> bullets) {
		return 8 + robots.size() * 8 + bullets.size() * 4;
	}

	public static int initialStateLength(Collection<Robot> robots, Collection<Obstacle> obstacles) {
		return robots.size() * 11 + obstacles.size() * 5;
	}
}
