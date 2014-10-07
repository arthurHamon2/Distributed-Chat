package tupletest;

import concassess.testee.*;
import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import chat.ChatServer;
import chat.ChatListener;

/**
 * This test checks that messages are correctly transferred between a writing
 * thread and a listener that connects partway through a conversation. It checks
 * that messages are sent in the right order and that the correct messages are
 * sent to newly connecting listeners.
 */
public class ChatBasicTest extends TestCase {
	private ChatServer cs;
	static ConcTestRunner ctr;

	protected void setUp() {
		cs = new ChatServer(new tuplespaces.LocalTupleSpace(), 3, new String[] {
				"Foo", "Bar", "Zoq", "Fot", "Pik" });
	}

	protected void tearDown() {
		cs = null;
	}

	public static final int PHASES = 5;

	public void testLogContents() throws Throwable {
		TestRunnable tct[] = new TestRunnable[2];

		tct[0] = new TestRunnable() {
			public void runTest() {
				ChatListener sl = cs.openConnection("Zoq");
				for (int phase = 0; phase < PHASES; phase++) {
					for (int i = phase * 10; i < (phase + 1) * 10; i++)
						cs.writeMessage("Foo", "" + i);
					cs.writeMessage("Bar", "");
					if (ctr != null)
						ctr.resetWatchdog();
					sl.getNextMessage();
				}
			}
		};
		tct[1] = new TestRunnable() {
			public void runTest() {
				ChatListener sl = cs.openConnection("Bar");
				for (int phase = 0; phase < PHASES; phase++) {
					assertEquals("Unexpected message", "", sl.getNextMessage());
					ChatListener cl = cs.openConnection("Foo");
					for (int i = phase * 10 + 7; i < (phase + 1) * 10; i++) {
						String s = cl.getNextMessage();
						int j;
						try {
							j = Integer.parseInt(s);
						} catch (Exception e) {
							fail("Corrupt message '" + s + "'; expecting '" + i
									+ "'");
							throw new Error("JUnit horribly broken.");
						}
						assertFalse(
								"More than specified (rows) amount of messages sent to new listener",
								j < phase * 10 + 7);
						assertFalse("Received duplicate message", j < i);
						assertFalse("Message lost or out of sequence ", j > i);
					}
					cs.writeMessage("Zoq", "");
					cl.closeConnection();
				}
			}
		};

		MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tct);
		mttr.runTestRunnables();
	}

	public static void main(String args[]) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatBasicTest.class);
	}
}
