package chatui;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import chat.ChatServer;
import tupleserver.*;

/**
 * User interface for distributed chat system. 
 *
 * Start using:
 * <code>java chatui.ChatUI <host:port> [<buffer size> <channel names>]</code>
 *
 * The chat assignment must be implemented 
 *
 * <host:port> specify the name of the host and TCP port of the TupleServer
 * used for the chat system. Specifying <buffer size> and <channel names>
 * (space separated) create a new chat server in the tuple space instead of
 * connecting to an existing one.
 */

@SuppressWarnings("serial")
public class ChatUI extends JFrame {
	protected final ChatServer server;
	protected final JList channelList;
	protected final JButton sendMessage;
	protected final JButton newListener;
	protected final JButton flood;
	protected final JTextField message;
	protected final JPanel buttons;
	protected long listeners = 0;

	public ChatUI(String host, int port) {
		this(new ChatServer(new TupleProxy(host, port)));
	}

	public ChatUI(String host, int port, int bufferSize, String[] channels) {
		this(new ChatServer(new TupleProxy(host, port), bufferSize, channels));
	}

	private ChatUI(ChatServer cs) {
		super("Chat system test UI");
		getContentPane().setLayout(new BorderLayout());
		buttons = new JPanel();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		server = cs;
		String[] channels = server.getChannels();
		channelList = new JList(channels);
		getContentPane().add(channelList, BorderLayout.CENTER);
		message = new JTextField();
		message.setColumns(80);
		getContentPane().add(message, BorderLayout.NORTH);
		sendMessage = new JButton("Send message");
		sendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String c = (String) channelList.getSelectedValue();
				if (c != null)
					new Thread() {
						public void run() {
							server.writeMessage(c, message.getText());
						}
					}.start();
			}
		});
		buttons.add(sendMessage);
		newListener = new JButton("New listener");
		newListener.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String c = (String) channelList.getSelectedValue();
				if (c != null)
					new ListenWindow(server, c, "Listener " + c + "-"
							+ listeners++).setVisible(true);
			}
		});
		buttons.add(newListener);
		flood = new JButton("Message flood");
		flood.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String c = (String) channelList.getSelectedValue();
				if (c != null)
					new Thread() {
						public void run() {
							for (int i = 0; i < 100; i++)
								server.writeMessage(c, "Flood message " + i);
						}
					}.start();
			}
		});
		buttons.add(flood);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		setSize(480, 360);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("New server: java chatui.ChatUI <host:port> "
					+ "<buffersize> <channel names>");
			System.err.println("Existing server: java chatui.ChatUI "
					+ "<host:port>");
			System.exit(1);
		}

		String[] a = args[0].split(":");

		if (args.length > 1) {
			int buf = Integer.parseInt(args[1]);

			String[] chan = new String[args.length - 2];

			for (int i = 2; i < args.length; i++)
				chan[i - 2] = args[i];
			new ChatUI(a[0], new Integer(a[1]).intValue(), buf, chan)
					.setVisible(true);
		} else {
			new ChatUI(a[0], new Integer(a[1]).intValue()).setVisible(true);
		}
	}
}
