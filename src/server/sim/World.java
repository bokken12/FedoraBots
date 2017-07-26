/**
 *
 */
package server.sim;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author joelmanning
 *
 */
public abstract class World {

	private double x, y, width, height;
	private World parent;

	public static final int MIN_WIDTH = 40;
	public static final int MIN_HEIGHT = 40;

	protected World(double x, double y, double width, double height, World parent) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.parent = parent;
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

	private byte[] state(int offset) {
		// Hackery for compilation
		final int[] num = new int[1];

		forEachUnsafe(entity -> num[0]++);
		final byte[] state = new byte[offset * num[0] + 2];
		state[0] = 1;
		state[1] = (byte) num[0];

		num[0] = 2;
		forEachUnsafe(entity -> {
			if (entity instanceof Robot) {
				// System.out.print(entity.getX() + " " + entity.getY() + "        ");
				state[num[0] + 0] = (byte) (entity.getId() >> 8);
				state[num[0] + 1] = (byte) (entity.getId() & 0xFF);
				state[num[0] + 2] = (byte) ((int) entity.getX() >> 4);
				state[num[0] + 3] = (byte) ((((int) entity.getX() & 0x0F) << 4) + ((int) entity.getY() >> 8));
				state[num[0] + 4] = (byte) ((int) entity.getY() & 0xFF);
				state[num[0] + 5] = (byte) (entity.getRotation() / 2 / Math.PI * 255);
				state[num[0] + 5] = (byte) (entity.getRotation() / 2 / Math.PI * 255);
				Robot pe = (Robot) entity;
				state[num[0] + 6] = (byte) ((Math.atan2(pe.getVy(), pe.getVx()) + Math.PI / 2) / 2 / Math.PI * 255);
				state[num[0] + 7] = (byte) ((Math.atan2(pe.getAy(), pe.getAx()) + Math.PI / 2) / 2 / Math.PI * 255);
				num[0] += offset;
			}
		});
		// System.out.println();

		return state;
	}

	public byte[] state() {
		return state(8);
	}

	public byte[] startingState() {
		byte[] state = state(11);
		state[0] = 0;

		// Hackery for compilation
		final int[] num = new int[1];
		num[0] = 2;

		forEach(entity -> {
			if (entity instanceof Robot) {
				state[num[0] + 8] = (byte) entity.getColor().getRed();
				state[num[0] + 9] = (byte) entity.getColor().getGreen();
				state[num[0] + 10] = (byte) entity.getColor().getBlue();
				num[0] += 11;
			}
		});

		return state;
	}

	public Map<Short, byte[]> velocityStates() {
		Map<Short, byte[]> m = new HashMap<Short, byte[]>();
		forEachUnsafe(entity -> {
			if (entity instanceof Robot) {
				Robot pe = (Robot) entity;
				ByteBuffer bb = ByteBuffer.allocate(8);
				bb.putFloat((float) (pe.getVx()*1e3));
				bb.putFloat((float) (pe.getVy()*1e3));
				m.put(entity.getId(), bb.array());
			}
		});
		return m;
	}

	public byte[] bulletStates() {
		List<Bullet> bullets = new ArrayList<Bullet>();
		forEachUnsafe(entity -> {
			if (entity instanceof Bullet) {
				bullets.add((Bullet) entity);
			}
		});

		byte[] bStates = new byte[bullets.size() * 4];
		for (int i = 0; i < bullets.size(); i++) {
			Bullet bullet = bullets.get(i);
			bStates[i*4 + 0] = (byte) ((int) bullet.getX() >> 4);
			bStates[i*4 + 1] = (byte) ((((int) bullet.getX() & 0x0F) << 4) + ((int) bullet.getY() >> 8));
			bStates[i*4 + 2] = (byte) ((int) bullet.getY() & 0xFF);
			bStates[i*4 + 3] = (byte) (Math.atan2(bullet.getVy(), bullet.getVx()) / 2 / Math.PI * 255);
		}

		return bStates;
	}
}
