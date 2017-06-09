/**
 * 
 */
package sim;

import java.util.concurrent.Semaphore;
import java.util.function.IntConsumer;

/**
 * @author joelmanning
 *
 */
public class Sim {
	private World world;
	private Semaphore lock;
	public static final int MAX_TICK_LENGTH = 16;
	
	public Sim(World world, Semaphore lock) {
		this.world = world;
		this.lock = lock;
	}
	
	public void run(IntConsumer tick) {
		long prev = System.currentTimeMillis();
		while(true) {
			int length = Math.min(MAX_TICK_LENGTH, (int) (System.currentTimeMillis() - prev));
			try {
				lock.acquire();
				world.forEach((e) -> e.tick(length, world));
				lock.release();
			} catch(InterruptedException e1) {
				e1.printStackTrace();
			}
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
