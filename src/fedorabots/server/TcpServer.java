package fedorabots.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fedorabots.common.Util;
import fedorabots.server.Manager.ParseException;

/**
 * The Tcp Server is only responsible for handling incoming connections and
 * passing the raw buffer to the {@link fedorabots.server.Manager}.
 *
 * <p>Credits to https://gist.github.com/Botffy/3860641</p>
 */
public class TcpServer implements Runnable {
	public static final int PORT = 8090;
	private static short next_id = 0;
	private ServerSocket ss;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(16);
	private static Map<Short, Handler> handlers = new HashMap<Short, Handler>();

	private Manager manager;

    private static final Logger LOGGER = Logger.getLogger(Room.class.getName());

	public TcpServer(Manager manager) throws IOException {
		this.manager = manager;
		this.ss  = new ServerSocket(PORT);
	}

	@Override
    public void run() {
		try {
			LOGGER.info("Server starting on port " + PORT);
			while(!ss.isClosed()){
				Handler handler = new Handler(ss.accept());
				handlers.put(handler.getHid(), handler);
				handler.start();
				LOGGER.info("created handler");
			}
		} catch(IOException e) {
			LOGGER.log(Level.SEVERE, "Error while running", e);
		}
	}

	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder(sc.socket().getInetAddress().toString())).append(":").append(sc.socket().getPort()).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, address);
	}

	/**
	 * Sends a message (<code>buf</code>) to a client known by the given
	 * <code>key</code>.
	 */
	public static boolean sendToKey(short key, ByteBuffer buf, Manager manager) {
			Handler h = handlers.get(key);
			try {
				h.getOut().write(buf.array(), 0, buf.limit());
				return true;
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not write to socket from " + h.getHid(), e);
			}
		return false;
	}
	
	class Handler extends Thread {
		
		private Socket sock;
		private InputStream in;
		private OutputStream out;
		private ByteBuffer buf;
		private short hid;
		
		private Handler(Socket sock) throws IOException{
			this.sock = sock;
			out = sock.getOutputStream();
			out.flush();
			in = sock.getInputStream();
			buf = ByteBuffer.allocate(16);
			hid = next_id;
			next_id++;
			LOGGER.info("creating handler");
		}
		
		public void run(){
			LOGGER.info("starting handler");
			try {
				while(!sock.isClosed()){
					byte type = (byte) in.read();
					LOGGER.info("Got message of type: " + type + " which translates to " + (byte) type);
					int numToRead = Manager.messageLength(type & 0xFF);
					//buf.clear();
					//buf.limit(numToRead + 1);
					//buf.put(type);
					byte[] bytes = new byte[numToRead + 1];
					bytes[0] = type;
					in.read(bytes, 1, numToRead);
					LOGGER.info("Created buffer " + Util.toString(bytes));
					try {
						manager.handleSent(ByteBuffer.wrap(bytes), TcpServer.this, this);
					} catch (ParseException e) {
						LOGGER.log(Level.WARNING, "bad message type", e);
					}
				}
			} catch(Exception e){
				LOGGER.log(Level.SEVERE, "Error while running handler", e);
			}
		}

		/**
		 * @return the sock
		 */
		public Socket getSock() {
			return sock;
		}

		/**
		 * @return the in
		 */
		public InputStream getIn() {
			return in;
		}

		/**
		 * @return the out
		 */
		public OutputStream getOut() {
			return out;
		}

		/**
		 * @return the hid
		 */
		public short getHid() {
			return hid;
		}

		/**
		 * @param sock the sock to set
		 */
		public void setSock(Socket sock) {
			this.sock = sock;
		}

		/**
		 * @param in the in to set
		 */
		public void setIn(InputStream in) {
			this.in = in;
		}

		/**
		 * @param out the out to set
		 */
		public void setOut(OutputStream out) {
			this.out = out;
		}

		/**
		 * @param hid the hid to set
		 */
		public void setHid(short hid) {
			this.hid = hid;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + hid;
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Handler other = (Handler) obj;
			if(!getOuterType().equals(other.getOuterType()))
				return false;
			if(hid != other.hid)
				return false;
			return true;
		}

		private TcpServer getOuterType() {
			return TcpServer.this;
		}
		
	}

}
