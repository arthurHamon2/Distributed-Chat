package tupletest;

import net.sourceforge.groboutils.junit.v1.TestRunnable;
import chat.ChatServer;
import chat.ChatListener;
import java.util.Random;

class Consumer extends TestRunnable {
	private int delay, messages;
	private String channel;
	protected Random rnd = new Random();
	private ChatListener cl;

	public Consumer(int d, int m, String c, ChatServer cs) {
		delay = d;
		messages = m;
		channel = c;
		cl = cs.openConnection(c);
	}

	public void runTest() {
		for (int i = 0; i < messages; i++) {
			if (delay > 0)
				try {
					synchronized (this) {
						wait(delay + rnd.nextInt(delay));
					}
				} catch (InterruptedException ie) {
					throw new Error("Unexpected interruption");
				}
			String s = cl.getNextMessage();
			int r;
			try {
				r = Integer.parseInt(s);
			} catch (Exception e) {
				fail("Message lost or corrupt");
				return; /* Unreachable, but Java doesn't know that. */
			}
			assertEquals("Message lost or out of sequence", i, r);
		}
	}

	public String toString() {
		return "Consumer on " + channel;
	}
}
