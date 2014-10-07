package tupleserver;

import tuplespaces.*;
import java.io.*;
import java.net.*;

/*
 Extends a LocalTupleSpace to allow network access from TupleProxies via
 TupleSockets.

 Run "java tupleserver.TupleServer" to create a tuple space and open access
 to it via TCP (on _all_ network interfaces; this class blatantly neglects
 security). The listening port is printed to standard output.

 Tuple space assignment must be completed and in class path for this to work.

 Tuple server never terminates normally.
 */
public class TupleServer extends LocalTupleSpace {
	public static String ACK = "ACK";
	public static String ANSWER = "ANSWER";
	private ServerSocket ssocket;

	public TupleServer() {
		try {
			ssocket = new ServerSocket(0);
		} catch (IOException ioe) {
			throw new RuntimeException("Server socket failure", ioe);
		}
	}

	public int getPort() {
		return ssocket.getLocalPort();
	}

	public static void main(String[] args) {
		TupleServer ts = new TupleServer();
		System.out.println(ts.getPort());
		ts.execute();
	}

	public Thread startListener() {
		try {
			Listener l = new Listener(ssocket.accept());
			l.start();
			return l;
		} catch (IOException ioe) {
			System.exit(1);
			throw new Error("Java is feeling very broken today.");
		}
	}

	public void execute() {
		while (true)
			startListener();
	}

	public class Listener extends Thread {
		private TupleSocket socket;

		Listener(Socket s) {
			socket = new TupleSocket(s);
		}

		public void run() {
			while (true) {
				try {
					char c = socket.readCommand();
					long id = socket.readId();
					String[] tuple = socket.readTuple();
					if (c == 'G')
						new Waiter(tuple, id, false).start();
					else if (c == 'R')
						new Waiter(tuple, id, true).start();
					else if (c == 'P') {
						TupleServer.super.put(tuple);
						synchronized (this) {
							socket.writeCommand('A', id);
							socket.flush();
						}
					} else
						throw new RuntimeException("Unknown command");
				} catch (IOException ioe) {
					break;
				}
			}
		}

		class Waiter extends Thread {
			String[] pattern;
			long id;
			boolean isRead;

			Waiter(String[] t, long i, boolean ir) {
				pattern = t;
				id = i;
				isRead = ir;
			}

			public void run() {
				String[] tuple;
				if (isRead)
					tuple = TupleServer.super.read(pattern);
				else
					tuple = TupleServer.super.get(pattern);
				synchronized (Listener.this) {
					try {
						socket.writeCommand('R', id);
						socket.writeTuple(tuple);
						socket.flush();
					} catch (IOException ioe) {
					}
				}
			}
		}
	}
}
