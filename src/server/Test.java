package server;

import java.io.IOException;
import java.util.Arrays;

import common.Constants;
import server.sim.Sim;
import server.sim.TestEntity;
import server.sim.World;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        World w = World.generateScrollingWorld(0, 0, Constants.World.WIDTH, Constants.World.HEIGHT);
        TestEntity phys = new TestEntity((short) 0, 16, 17, 10);
		phys.setVx(0.1);
		TestEntity phys2 = new TestEntity((short) 1, 263, 20, 10);
		phys2.setVx(-0.1);
        w.add(phys);
        w.add(phys2);
        Sim sim = new Sim(w);
        TcpServer server = new TcpServer();
        Thread t = new Thread(server);
        t.setDaemon(true);
        t.start();
        Thread.sleep(5000);
        server.broadcast(w.startingState());
        sim.run(tick -> server.broadcast(w.state()));
    }
}
