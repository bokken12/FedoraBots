/**
 * 
 */
package sim;

/**
 * @author joelmanning
 *
 */
public class Sim {
	private World world;
	
	public static void main(String[] args){
		
	}
	public Sim(World world){
		
	}
	
	public void run(){
		for(int tick = 0; true; tick++){
			world.forEach((e) -> e.tick());
		}
	}
	/**
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}
	/**
	 * @param world the world to set
	 */
	public void setWorld(World world) {
		this.world = world;
	}
}
