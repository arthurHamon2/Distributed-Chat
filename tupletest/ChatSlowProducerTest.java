package tupletest;

import concassess.testee.*;

/**
 * This test connects a writing thread to two listeners through a channel and
 * tests the result of writing to the channel much slower than reading.
 */
public class ChatSlowProducerTest extends ChatProducerConsumerTestCase {
	public static final int MESSAGES = 1000;

	public void testSlowProducerFastConsumers() throws Throwable {
		runProducerConsumerTest(MESSAGES, 10, new int[] { 0, 1 });
	}

	public static void main(String args[]) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatSlowProducerTest.class);
	}
}
