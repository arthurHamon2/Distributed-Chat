package tupletest;

import concassess.testee.*;
import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import chat.ChatServer;

/**
 * This test sets up a bunch of spammers and listeners connected to a pair of
 * channels and ensures that no messages are lost even when multiple spammers
 * flood the channels continuously.
 */
public class ChatSpamTest extends TestCase {
	public static final int SPAM_LISTENERS = 20;
	public static final int SPAMMERS = 25;
	public static final int SPAM_MESSAGES = 100;

	static ConcTestRunner ctr;

	private ChatServer cs;

	protected void setUp() {
		cs = new ChatServer(new tuplespaces.LocalTupleSpace(), 10,
				new String[] { "Foo", "Bar", "Zoq", "Fot", "Pik" });
	}

	protected void tearDown() {
		cs = null;
	}

	public void testSpamFlood() throws Throwable {
		executeSpamFlood(SPAMMERS, SPAM_LISTENERS, SPAM_MESSAGES);
	}

	void executeSpamFlood(int spammers, int listeners, int messages)
			throws Throwable {
		TestRunnable tct[] = new TestRunnable[listeners * 2 + spammers];

		for (int i = 0; i < spammers; i++) {
			tct[i] = new Spammer(i, cs, messages);
		}

		for (int i = 0; i < listeners; i++) {
			tct[spammers + i] = new SpamListener("Pik", cs, messages, spammers,
					(i == 0) ? ctr : null);
			tct[spammers + listeners + i] = new SpamListener("Zoq", cs,
					messages, spammers, null);
		}

		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tct);
		mttr.runTestRunnables();
	}

	public static void main(String[] args) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatSpamTest.class);
	}
}
