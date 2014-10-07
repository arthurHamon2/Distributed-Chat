package tupletest;

import net.sourceforge.groboutils.junit.v1.TestRunnable;
import chat.ChatServer;

public class Spammer extends TestRunnable {
	protected final int id;
	protected final ChatServer cs;
	protected final int me;

	public Spammer(int i, ChatServer s, int messages) {
		id = i;
		cs = s;
		me = messages;
	}

	public void runTest() {
		for (int i = 0; i < me; i++) {
			cs.writeMessage("Zoq", "Zoq test " + (id * me + i));
			cs.writeMessage("Pik", "Pik test " + (id * me + i));
		}
	}
}
