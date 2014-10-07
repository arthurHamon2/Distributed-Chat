package tupletest;

import concassess.testee.*;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import chat.ChatServer;
import java.util.Random;

class Producer extends TestRunnable {
	private int delay, messages;
	private String channel;
	protected Random rnd = new Random();
	private final ChatServer cs;
	private final ConcTestRunner ctr;

	public Producer(int d, int m, String c, ChatServer s, ConcTestRunner ct) {
		delay = d;
		messages = m;
		channel = c;
		cs = s;
		ctr = ct;
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
			cs.writeMessage(channel, "" + i);
			if (ctr != null)
				ctr.resetWatchdog();
		}
	}
}
