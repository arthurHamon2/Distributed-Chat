package tupletest;

import concassess.testee.*;
import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import chat.ChatServer;

/**
 * A category of tests that involve one producer and multiple consumers on a
 * single channel.
 */
public abstract class ChatProducerConsumerTestCase extends TestCase {
	private ChatServer cs;
	static ConcTestRunner ctr;

	protected void setUp() {
		cs = new ChatServer(new tuplespaces.LocalTupleSpace(), 10,
				new String[] { "Foo", "Bar", "Zoq", "Fot", "Pik" });
	}

	protected void tearDown() {
		cs = null;
	}

	public void runProducerConsumerTest(int messages, int prodDelay,
			int[] consDelay) throws Throwable {
		TestRunnable tct[] = new TestRunnable[consDelay.length + 1];

		tct[0] = new Producer(prodDelay, messages, "Foo", cs, ctr);
		for (int i = 1; i <= consDelay.length; i++)
			tct[i] = new Consumer(consDelay[i - 1], messages, "Foo", cs);

		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tct);
		mttr.runTestRunnables();
	}
}
