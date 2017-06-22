package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Util;

/**
 * The Tcp Server is only responsible for handling incoming connections and
 * passing the raw buffer to the {@link server.Manager}.
 *
 * <p>Credits to https://gist.github.com/Botffy/3860641</p>
 */
public class TcpServer implements Runnable {
	public static final int PORT = 8090;
	private ServerSocketChannel ssc;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(256);
	// private Object mutex = new Object();
	private Collection<SelectionKey> clients = new HashSet<SelectionKey>();

	private Manager manager;

    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

	public TcpServer(Manager manager) throws IOException {
		this.manager = manager;
		this.ssc = ServerSocketChannel.open();
		this.ssc.socket().bind(new InetSocketAddress(PORT));
		this.ssc.configureBlocking(false);
		this.selector = Selector.open();

		this.ssc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
    public void run() {
		try {
			LOGGER.info("Server starting on port " + PORT);

			Iterator<SelectionKey> iter;
			SelectionKey key;
			while (this.ssc.isOpen()) {
				selector.select();
				iter = this.selector.selectedKeys().iterator();
				while(iter.hasNext()) {
					key = iter.next();
					iter.remove();

					if(key.isAcceptable()) this.handleAccept(key);
					if(key.isReadable()) this.handleRead(key);
				}
			}
		} catch(IOException e) {
			LOGGER.log(Level.SEVERE, "Error while running", e);
		}
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder(sc.socket().getInetAddress().toString())).append(":").append(sc.socket().getPort()).toString();
		sc.configureBlocking(false);
		SelectionKey client = sc.register(selector, SelectionKey.OP_READ, address);
		// synchronized (mutex) {
		// 	clients.add(client);
		// }
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();

		try {
			buf.clear();
			int read = 0;
			while ((read = ch.read(buf)) > 0) {
				buf.flip();
				if (buf.remaining() > 0) {
					byte[] bytes = new byte[buf.limit()];
					buf.get(bytes);
					try {
						manager.handleSent(bytes, this, key, ch);
					} catch (Manager.ParseException e) {
						LOGGER.log(Level.WARNING, "Error parsing input " + Util.toString(bytes), e);
					}
				}

				buf.clear();
			}

			if (read < 0) {
				LOGGER.fine(key.attachment() + " closed its session.");
				manager.handleClosed(key);
				ch.close();
				// synchronized (mutex) {
				// 	clients.remove(key);
				// }
			}
			else {
				LOGGER.finest(key.attachment() + " sent something");
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Error while reading", e);
			manager.handleClosed(key);
			ch.close();
			// synchronized (mutex) {
			// 	clients.remove(key);
			// }
		}
	}

	/**
	 * Sends a message (<code>buf</code>) to a client known by the given
	 * <code>key</code>.
	 */
	public static void sendToKey(SelectionKey key, ByteBuffer buf) {
		if(key.isValid() && key.channel() instanceof SocketChannel) {
			SocketChannel sch = (SocketChannel) key.channel();
			try {
				sch.write(buf);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not write to socket", e);
			}
		}
	}

	// public void broadcast(Function<SelectionKey, ByteBuffer> supplier) {
	// 	synchronized (mutex) {
	// 		Iterator<SelectionKey> it = clients.iterator();
	// 		while (it.hasNext()) {
	// 			SelectionKey key = it.next();
	// 			if(key.isValid() && key.channel() instanceof SocketChannel) {
	// 				SocketChannel sch = (SocketChannel) key.channel();
	// 				try {
	// 					ByteBuffer msgBuf = supplier.apply(key);
	// 					sch.write(msgBuf);
	// 					msgBuf.rewind();
	// 				} catch (IOException e) {
	// 					LOGGER.log(Level.WARNING, "Error while writing", e);
	// 					try {
	// 						sch.close();
	// 						it.remove();
	// 					} catch (IOException ee) {
	// 						LOGGER.log(Level.WARNING, "Error while closing", e);
	// 					}
	// 				}
	// 			}
	// 		}
	// 	}
	// }

	// public void broadcast(byte[] msg) {
	// 	ByteBuffer msgBuf = ByteBuffer.wrap(msg);
	// 	broadcast(key -> msgBuf);
	// }
}
