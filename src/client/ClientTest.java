/**
 * 
 */
package client;

import javax.swing.JFrame;

import gui.Display;
import sim.Sim;
import sim.TestEntity;
import sim.World;

/**
 * @author joelmanning
 *
 */
public class ClientTest {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		World w = World.generateWorld(0, 0, 2000, 1000, null);
		w.add(new TestEntity(6, 7, 10));
		w.add(new TestEntity(40, 70, 23));
		w.add(new TestEntity(40, 60, 29));
		Sim sim = new Sim(w);
		Display disp = new Display(w, 500, 400);
		JFrame frame = new JFrame("Framey");
		frame.setSize(500, 400);
		frame.add(disp);
		frame.setVisible(true);
	}
	
}
