package chatui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/** Echo an InputStream into specified output. */
public class InputStreamEchoer extends Thread {
	private BufferedReader r;
	private String name;
	private PrintStream log;

	/**
	 * Create an echoer.
	 * 
	 * @param is
	 *            Stream to echo from.
	 * @param nam
	 *            Name of stream (used in output to show message source).
	 * @param l
	 *            Logger to write stream to.
	 */
	public InputStreamEchoer(InputStream is, String nam, PrintStream l) {
		r = new BufferedReader(new InputStreamReader(is));
		name = nam;
		log = l;
	}

	public void run() {
		String l;

		do {
			try {
				l = r.readLine();
			} catch (IOException ie) {
				l = null;
			}
			if (l != null)
				log.println(name + ": " + l);
		} while (l != null);
	}
}
