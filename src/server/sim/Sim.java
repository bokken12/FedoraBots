/**
 *
 */
package server.sim;

import java.util.function.IntConsumer;

/**
 * @author joelmanning
 *
 */
public class Sim {
	private World world;
	public static final int MAX_TICK_LENGTH = 16;

	public Sim(World world) {
		this.world = world;
	}

	public void run(IntConsumer tick) {
		long prev = System.currentTimeMillis();
		while(true) {
			int length = Math.min(MAX_TICK_LENGTH, (int) (System.currentTimeMillis() - prev));
			world.forEach((e) -> e.tick(length, world));
			prev += length;
			tick.accept(length);
			//System.out.println("tick of length: " + length);
		}
	}

	/**
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * @param world
	 *            the world to set
	 */
	public void setWorld(World world) {
		this.world = world;
	}
}
