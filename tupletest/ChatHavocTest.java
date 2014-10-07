package tupletest;

import concassess.testee.*;
import junit.framework.TestCase;
import chat.ChatServer;
import chat.ChatListener;
import java.util.Random;
import java.util.LinkedList;

/**
 * A test case which tries to create havoc in the chat system by stress testing
 * it with message floods combined with new listeners connecting and old ones
 * disconnecting concurrently on multiple channels. The test will do one of the
 * four things PHASES times:
 * <ol>
 * <li>Make a listener disconnect or connect another listener. There is a
 * slightly higher probability that a listener is created than one being
 * removed.</li>
 * <li>Send a single message from the main thread and then create a new thread
 * to send 1000 messages.</li>
 * <li>Sleep for a random amount of time.</li>
 * </ol>
 * 
 * Author: Ari Sundholm <asundhol@cs.hut.fi> $Date: 2010-03-18 14:15:27 $
 */

public class ChatHavocTest extends TestCase {
	private ChatServer cs;
	static ConcTestRunner ctr;
	private Random r;

	private static final String[] channels = new String[] { "Foo", "Bar", "Zoq" /*
																				 * ,
																				 * "Fot"
																				 * ,
																				 * "Pik"
																				 */};

	protected void setUp() {
		cs = new ChatServer(new tuplespaces.LocalTupleSpace(), 10, channels);
		r = new Random();
	}

	protected void tearDown() {
		cs = null;
	}

	static final int PHASES = 100;
	private static final int MAXLISTENERS = 40;
	private LinkedList<HavocListener> listeners = new LinkedList<HavocListener>();
	private LinkedList<Thread> spammers = new LinkedList<Thread>();
	public static final int RESETS = PHASES + MAXLISTENERS + PHASES;

	class HavocListener extends Thread {
		private ChatListener listener;
		private volatile boolean running;

		public HavocListener(String channel) {
			listener = cs.openConnection(channel);
			running = true;
		}

		public void run() {
			System.err.println("Havoc listener started");

			// int i = 0;
			while (running) {
				listener.getNextMessage();
				/*
				 * if(i++ % 500 == 0) ctr.resetWatchdog();
				 */
			}

			listener.closeConnection();
			System.err.println("Havoc listener ended");
		}

		public void setRunning(boolean val) {
			running = val;
		}
	}

	private String currentChannel = channels[0];

	public void testHavoc() throws Throwable {
		for (int phase = 0; phase < PHASES; phase++) {
			int action = (phase < 7) ? phase : r.nextInt(7);
			switch (action) {
			case 0:
			case 1:
			case 2:

				int action2 = r.nextInt(10);

				// Add a new listener only if 1) there are no listeners
				// or 2) the maximum amount of listeners has not been
				// reached and the dice was in our favor. Else remove a
				// listener at random.
				if (listeners.size() == 0
						|| (listeners.size() < MAXLISTENERS && action2 > 2)) {
					HavocListener hl = new HavocListener(currentChannel);
					listeners.add(hl);
					hl.start();
					break;
				} else {
					int i = r.nextInt(listeners.size());
					listeners.get(i).setRunning(false);
				}

				break;
			case 3:
				// Send a single message in order to get a better chance at
				// locking up the main thread with incorrect chat
				// implementations.
				System.err.println("Main thread sending a single message");
				cs.writeMessage(currentChannel, "Main thread message");
				// Intentional fall-through.
			case 4:
			case 5:
				// New thread, 200 messages
				Thread spammer = new Thread() {
					private final ChatServer myCS = cs;
					private final String myCurrentChannel = currentChannel;

					public void run() {
						System.err.println("Non-main thread starting flood");
						for (int i = 0; i < 200; ++i) {
							myCS.writeMessage(myCurrentChannel,
									"Non-main thread message flood " + i);
							/*
							 * if(i % 250 == 0) ctr.resetWatchdog();
							 */
						}
						// ctr.resetWatchdog();

						System.err.println("Non-main thread ended flood");
					}
				};
				spammers.add(spammer);
				spammer.start();

				break;
			case 6:
			default:
				// Sleep for about 50 milliseconds + 500 000 nanoseconds on
				// average,
				// with a maximum of 100 milliseconds + 1 000 000 nanoseconds.
				// long sleepmillis = (long) (50 * (r.nextGaussian() + 1.0));
				// int sleepnanos = (int) (500000 * (r.nextGaussian() + 1.0));
				long sleepmillis = r.nextInt(100);
				int sleepnanos = r.nextInt(1000000);
				try {
					System.err.println("Sleeping for " + sleepmillis
							+ " milliseconds and " + sleepnanos
							+ " nanoseconds.");
					Thread.sleep(sleepmillis, sleepnanos);
				} catch (InterruptedException e) {
					// No can do.
				}

				break;
			}

			if (ctr != null)
				ctr.resetWatchdog();
		}

		// Tell all listeners to go home. Mommy's waiting.
		for (HavocListener hl : listeners)
			hl.setRunning(false);

		// Make sure the listeners don't get stuck in cs.getNextMessage().
		for (String channel : channels)
			cs.writeMessage(channel, "Quit message");

		// ctr.resetWatchdog();

		// It's best to go home hand in hand with all your friends.
		if (ctr != null)
			for (int i = 0; i < MAXLISTENERS - listeners.size() + PHASES
					- spammers.size(); i++)
				ctr.resetWatchdog();

		while (listeners.size() > 0) {
			listeners.poll().join();
			if (ctr != null)
				ctr.resetWatchdog();
		}

		while (spammers.size() > 0) {
			spammers.poll().join();
			if (ctr != null)
				ctr.resetWatchdog();
		}
	}

	public static void main(String args[]) {
		ctr = new ConcTestRunner(args);
		ctr.start(ChatHavocTest.class);
	}
}
