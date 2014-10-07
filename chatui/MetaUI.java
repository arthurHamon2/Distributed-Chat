package chatui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * GUI for starting Chat GUI instances. 
 */
@SuppressWarnings("serial")
public class MetaUI extends JFrame {
	protected final JButton start;
	private final int port;
	private final Process server;

	public static void echo(Process p) {
		new InputStreamEchoer(p.getInputStream(), p + " stdout", System.out).start();
		new InputStreamEchoer(p.getErrorStream(), p + " stderr", System.err).start();
	}
	
	public MetaUI() {
		super("Chat user interface startup class");
		final String classpath = System.getProperty("java.class.path"); 
		getContentPane().setLayout(new BorderLayout());
		start = new JButton("Start new client");
		getContentPane().add(start, BorderLayout.CENTER);
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", classpath,
				"tupleserver.TupleServer");
		try {
			server = pb.start();
		} catch (IOException e1) {
			throw new Error(e1);
		}

		BufferedReader pr = new BufferedReader(new InputStreamReader(
				server.getInputStream()));

		try {
			String s = pr.readLine();
			port = new Integer(s).intValue();
		} catch (Exception e) {
			throw new Error(e);
		}

		pb = new ProcessBuilder("java", "-cp", classpath, "chatui.ChatUI",
				"localhost:" + port, "10", "TestChannel", "AnotherTestChannel", "ThirdChannel");
		try {
			echo(pb.start());
		} catch (IOException e1) {
			throw new Error(e1);
		}

		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProcessBuilder pb = new ProcessBuilder("java", "-cp", classpath, "chatui.ChatUI",
						"localhost:" + port);
				try {
					echo(pb.start());
				} catch (IOException e1) {
					throw new Error(e1);
				}
			}
		});
		
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	server.destroy();
		    	dispose();
		    }
		});
		
		pack();
	}

	public static void main(String[] args) {
		new MetaUI().setVisible(true);
	}

}
