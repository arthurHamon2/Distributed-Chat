package tupletest;

import concassess.testee.*;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import chat.ChatServer;
import chat.ChatListener;

public class SpamListener extends TestRunnable {
	private final ChatListener cl;
	private final String channel;
	private int spammers;
	private int[] nextSpam;
	private int messages;
	private ConcTestRunner ctr;

	public SpamListener(String c, ChatServer cs, int messages, int spammers,
			ConcTestRunner ctr) {
		channel = c;
		cl = cs.openConnection(c);
		nextSpam = new int[spammers];
		this.spammers = spammers;
		this.messages = messages;
		this.ctr = ctr;
	}

	public void runTest() {
		for (int i = 0; i < messages * spammers; i++) {
			String s = cl.getNextMessage();
			assertEquals("Message corrupt", " test ", s.substring(3, 9));
			assertEquals("Message channel wrong", channel, s.substring(0, 3));
			int v;
			try {
				v = Integer.parseInt(s.substring(9));
			} catch (Exception e) {
				fail("Message corrupt: " + s);
				throw new Error("JUnit hates you. Go away.");
			}
			int spam = v % messages;
			int spammer = v / messages;
			assertEquals("Message lost or reordered", nextSpam[spammer], spam);
			nextSpam[spammer]++;
			if (((i % spammers) == 0) && ctr != null)
				ctr.resetWatchdog();
		}
		System.err.println(this + " done listening.");
		cl.closeConnection();
		System.err.println(this + " closed connection.");
	}
}
