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

/**
 * Credits to https://gist.github.com/Botffy/3860641
 */
public class TcpServer implements Runnable {
	public static final int PORT = 8090;
	private ServerSocketChannel ssc;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(256);
	private Object mutex = new Object();
	private Collection<SelectionKey> clients = new HashSet<SelectionKey>();

	private Manager manager;

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
			System.out.println("Server starting on port " + PORT);

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
			System.out.println("Error while running:");
			e.printStackTrace();
		}
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder(sc.socket().getInetAddress().toString())).append(":").append(sc.socket().getPort()).toString();
		sc.configureBlocking(false);
		SelectionKey client = sc.register(selector, SelectionKey.OP_READ, address);
		synchronized (mutex) {
			clients.add(client);
		}
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
						ByteBuffer res = manager.handleSent(bytes, this);
						if (res != null) {
							res.rewind();
							ch.write(res);
						}
					} catch (Manager.ParseException e) {
						e.printStackTrace();
					}
				}

				buf.clear();
			}

			if (read < 0) {
				System.out.println(key.attachment() + " closed its session.");
				ch.close();
				synchronized (mutex) {
					clients.remove(key);
				}
			}
			else {
				System.out.println(key.attachment() + " sent something");
			}
		} catch (IOException e) {
			System.err.println("Error while reading:");
			e.printStackTrace();
			ch.close();
			synchronized (mutex) {
				clients.remove(key);
			}
		}
	}

	public void broadcast(byte[] msg) {
		ByteBuffer msgBuf = ByteBuffer.wrap(msg);
		synchronized (mutex) {
			Iterator<SelectionKey> it = clients.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				if(key.isValid() && key.channel() instanceof SocketChannel) {
					SocketChannel sch = (SocketChannel) key.channel();
					try {
						sch.write(msgBuf);
						msgBuf.rewind();
					} catch (IOException e) {
						System.err.println("Error while writing:");
						e.printStackTrace();
						try {
							sch.close();
							it.remove();
						} catch (IOException ee) {
							System.err.println("Error while closing:");
							ee.printStackTrace();
						}
					}
				}
			}
		}
	}
}
