/**
 * 
 */
package client;

import java.util.concurrent.Semaphore;

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
		World w = World.generateWorld(0, 0, 2000, 1000);
		TestEntity phys = new TestEntity(16, 17, 10);
		phys.setVx(0);
		phys.setVy(0);
		w.add(phys);
		//w.add(new TestEntity(40, 70, 23));
		//w.add(new TestEntity(40, 60, 29));
		Semaphore lock = new Semaphore(1);
		Sim sim = new Sim(w, lock);
		Display disp = new Display(w, lock, 500, 400);
		JFrame frame = new JFrame("Framey");
		frame.setSize(500, 400);
		frame.add(disp);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sim.run(l -> disp.repaint());
		System.exit(0);
		/*while(true){
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			phys.tick(1, w);
			disp.setXViewPoint(disp.getXViewPoint() + 1);
			disp.repaint();
		}*/
	}
	
}
