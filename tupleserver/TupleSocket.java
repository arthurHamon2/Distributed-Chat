package tupleserver;

import java.io.*;
import java.net.*;

/*
 Socket used to transfer data between TupleServers and TupleProxies.
 */
public class TupleSocket {
	Socket socket;
	DataInputStream in;
	DataOutputStream out;

	public TupleSocket(Socket socket) {
		this.socket = socket;
		try {
			socket.setTcpNoDelay(true);
			in = new DataInputStream(new BufferedInputStream(
					socket.getInputStream(), 4096));
			out = new DataOutputStream(new BufferedOutputStream(
					socket.getOutputStream(), 4096));
		} catch (Exception e) {
			throw new RuntimeException("Internal socket error", e);
		}
	}

	public void writeCommand(char c, long id) throws IOException {
		out.writeChar(c);
		out.writeLong(id);
	}

	public void writeTuple(String[] tuple) throws IOException {
		out.writeInt(tuple.length);
		for (int i = 0; i < tuple.length; i++) {
			if (tuple[i] == null)
				out.writeBoolean(false);
			else {
				out.writeBoolean(true);
				out.writeUTF(tuple[i]);
			}
		}
	}

	public void flush() throws IOException {
		out.flush();
	}

	public char readCommand() throws IOException {
		return in.readChar();
	}

	public long readId() throws IOException {
		return in.readLong();
	}

	public String[] readTuple() throws IOException {
		int length = in.readInt();
		String[] tuple = new String[length];

		for (int i = 0; i < length; i++) {
			if (in.readBoolean())
				tuple[i] = in.readUTF();
		}

		return tuple;
	}
}
