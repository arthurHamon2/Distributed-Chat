package tupletest;

import concassess.testee.*;

/**
 * Like {@link ChatSpamTest}, but with fewer spammers, listeners and messages. 
 */
public class ChatSpamTestLite extends ChatSpamTest {
	public static final int SPAM_MESSAGES = 50;

	public void testSpamFlood() throws Throwable {
		executeSpamFlood(4, 4, SPAM_MESSAGES);
	}

	public static void main(String[] args) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatSpamTestLite.class);
	}
}
