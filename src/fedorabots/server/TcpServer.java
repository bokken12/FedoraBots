package fedorabots.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
				handler.start();
				LOGGER.info("created handler");
			}
		} catch(IOException e) {
			LOGGER.log(Level.SEVERE, "Error while running", e);
		}
	}

	/**
	 * Sends a message (<code>buf</code>) to a client known by the given
	 * <code>key</code>.
	 */
	public static boolean sendToHandle(Handler handler, ByteBuffer buf, Manager manager) {
			try {
				handler.getOut().write(buf.array(), 0, buf.limit());
				return true;
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Could not write to socket from " + handler, e);
			}
		return false;
	}

	class Handler extends Thread {

		private Socket sock;
		private InputStream in;
		private OutputStream out;
		private short hid;

		private Handler(Socket sock) throws IOException{
			this.sock = sock;
			out = sock.getOutputStream();
			out.flush();
			in = sock.getInputStream();
			hid = next_id;
			next_id++;
			LOGGER.info("creating handler");
		}

		public void run(){
			LOGGER.info("starting handler");
			try {
				while(!sock.isClosed()){
					byte type = (byte) in.read();
					int numToRead = Manager.messageLength(type & 0xFF);

					ByteBuffer bb = ByteBuffer.allocate(numToRead + 1);
					bb.put(type);
					for (int i = 0; i < numToRead; i++) {
						bb.put((byte) in.read());
					}
					bb.rewind();

					LOGGER.info("Created buffer " + Util.toString(bb));
					try {
						manager.handleSent(bb, TcpServer.this, this);
					} catch (ParseException e) {
						LOGGER.log(Level.WARNING, "bad message type", e);
					}
				}
			} catch(Exception e){
				LOGGER.log(Level.SEVERE, "Error while running handler", e);
				manager.handleClosed(this);
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

		public String toString() {
			return sock.getRemoteSocketAddress().toString();
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
