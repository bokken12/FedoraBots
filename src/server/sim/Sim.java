/**
 *
 */
package server.sim;

import java.util.function.DoubleConsumer;

/**
 * @author joelmanning
 *
 */
public class Sim {
	private World world;
	private long prev;
	private boolean startedTicking;
	// public static final long MAX_TICK_LENGTH = 160 * (long)1e6;
	public static final long MIN_TICK_LENGTH = 40 * (long)1e6;

	public Sim(World world) {
		this.world = world;
	}

	public long tick(DoubleConsumer tick) {
		if (!startedTicking) {
			prev = System.nanoTime();
			startedTicking = true;
		}

		long ctm = System.nanoTime();
		long nanoLength = ctm - prev;
		double millilength = nanoLength / 1e6;

		world.forEach((e) -> e.tick(millilength, world));
		prev += nanoLength;
		tick.accept(millilength);

		return System.nanoTime() - ctm;
	}

	public void run(DoubleConsumer tick) {
		long prev = System.nanoTime();
		while(true) {
			long ctm = System.nanoTime();
			long nanoLength = ctm - prev;
			double millilength = nanoLength / 1e6;

			world.forEach((e) -> e.tick(millilength, world));
			prev += nanoLength;
			tick.accept(millilength);
			// System.out.println("tick of length: " + millilength);

			long took = System.nanoTime() - ctm;
			try {
				Thread.sleep(Math.max(0, (MIN_TICK_LENGTH-took)/(long)1e6));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
