package tupletest;

import concassess.testee.*;

/**
 * This test connects a writing thread to two listeners through a channel and
 * tests the result of reading from the channel much slower than writing.
 */
public class ChatFastProducerTest extends ChatProducerConsumerTestCase {
	public static final int MESSAGES = 1000;

	public void testFastProducerSlowConsumers() throws Throwable {
		runProducerConsumerTest(MESSAGES, 0, new int[] { 10, 25 });
	}

	public static void main(String args[]) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatFastProducerTest.class);
	}
}
