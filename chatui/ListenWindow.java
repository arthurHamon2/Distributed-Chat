package chatui;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import chat.ChatListener;
import chat.ChatServer;

/**
 * Window for listener in chat GUI. 
 */
@SuppressWarnings("serial")
public class ListenWindow extends JFrame {
	protected final ChatListener listener;
	protected final ChatServer server;
	protected final JTextArea messages;
	protected final Object lock = new Object();
	protected volatile boolean done = false;
	protected final String name, channel;

	public ListenWindow(ChatServer s, String c, String n) {
		super(n);

		server = s;
		name = n;
		channel = c;

		getContentPane().setLayout(new BorderLayout());

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				done = true;
				server.writeMessage(channel, name + " has left the channel.");
				dispose();
			}
		});

		messages = new JTextArea(80, 25);
		messages.setEditable(false);
		getContentPane().add(new JScrollPane(messages), BorderLayout.CENTER);
		/* server.writeMessage(channel, name+" has entered the channel."); */

		listener = server.openConnection(channel);

		Thread t = new Thread() {
			public void run() {
				while (!done) {
					final String m = listener.getNextMessage();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							messages.append(m + "\n");
						}
					});
				}
				listener.closeConnection();
			}
		};
		t.setDaemon(true);
		t.start();

		setSize(480, 360);
	}
}
