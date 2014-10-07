package tupleserver;

import tuplespaces.*;
import java.io.*;
import java.net.*;

/**
 * Tuple space implementation that connects to a TupleServer, allowing a tuple
 * space to be shared between processes using TCP.
 *
 * Tuple space assignment must be completed and in class path for this to work.
 */
public class TupleProxy extends LocalTupleSpace implements Runnable {
	private long inid, outid;

	public static String ACK = "ACK";
	public static String ANSWER = "ANSWER";
	private TupleSocket socket;

	public TupleProxy(String host, int port) {
		super();
		try {
			socket = new TupleSocket(new Socket(host, port));
		} catch (Exception e) {
			throw new RuntimeException("Invalid host", e);
		}

		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public TupleProxy(Socket s) {
		super();
		socket = new TupleSocket(s);

		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public void run() {
		/* Read incoming messages and put them in tuple space. */
		while (true) {
			char t;
			long id;
			String[] tuple;

			try {
				t = socket.readCommand();
				id = socket.readId();
			} catch (IOException ioe) {
				break;
			}

			if (t == 'R') {
				String[] read;

				try {
					read = socket.readTuple();
				} catch (IOException ioe) {
					break;
				}
				tuple = new String[read.length + 2];
				tuple[0] = ANSWER;
				tuple[1] = String.valueOf(id);
				for (int i = 0; i < read.length; i++)
					tuple[i + 2] = read[i];
				super.put(tuple);
			} else if (t == 'A') {
				tuple = new String[2];
				tuple[0] = ACK;
				tuple[1] = String.valueOf(id);
				super.put(tuple);
			} else
				throw new RuntimeException("Unknown command: " + t);
		}
	}

	public void put(String... tuple) {
		long id;

		synchronized (this) {
			id = outid++;
			try {
				socket.writeCommand('P', id);
				socket.writeTuple(tuple);
				socket.flush();
			} catch (IOException ioe) {
				throw new RuntimeException("IO error in put", ioe);
			}
		}

		super.get(ACK, String.valueOf(id));
	}

	public String[] read(String... pattern) {
		return fetch(pattern, true);
	}

	public String[] get(String... pattern) {
		return fetch(pattern, false);
	}

	public String[] fetch(String[] pattern, boolean isRead) {
		long id;

		synchronized (this) {
			id = inid++;
			try {
				socket.writeCommand(isRead ? 'R' : 'G', id);
				socket.writeTuple(pattern);
				socket.flush();
			} catch (IOException ioe) {
				throw new RuntimeException("IO error in put", ioe);
			}
		}

		pattern = new String[2 + pattern.length];
		pattern[0] = ANSWER;
		pattern[1] = String.valueOf(id);

		String[] result = super.get(pattern);
		String[] real = new String[result.length - 2];
		for (int i = 0; i < real.length; i++)
			real[i] = result[i + 2];
		return real;
	}
}
