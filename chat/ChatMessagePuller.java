package chat;

import java.util.LinkedList;
import java.util.List;

import tuplespaces.TupleSpace;

public class ChatMessagePuller extends Thread {

	private final TupleSpace tupleSpace;
	private final String channel;
	private List<ChatListener> chatListeners;
	private final String nodeId;
	
	// These variables may be used by more than one thread 
	private volatile int nextMessage;
	private volatile int size;
	private volatile boolean deleteLastSem = false;

	public ChatMessagePuller(TupleSpace t, String channel, String id,int nextMessage, int rows) {
		this.chatListeners = new LinkedList<ChatListener>();
		this.tupleSpace = t;
		this.channel = channel;
		this.nextMessage = nextMessage;
		this.nodeId = id;
		this.size = 0;
	}

	public void addChatListener(ChatListener c, int messageCount) {
		synchronized (this) {
			chatListeners.add(c);
		}
		// The listener is first and we need to update the nextMessage of the thread !
		if (size <= 0) {
			nextMessage = messageCount;
		}

		this.size++;
		// Update the total number of listeners for the channel
		int nbListeners = Integer.valueOf(tupleSpace.get(ChatServer.CHANNEL_LISTENER, channel, null)[2]);
		tupleSpace.put(ChatServer.CHANNEL_LISTENER, channel,String.valueOf(++nbListeners));
		// Update the total number of listeners of the node
		int nbNodeListener = Integer.valueOf(tupleSpace.get(ChatServer.NODE,String.valueOf(nodeId), channel, null)[3]);
		tupleSpace.put(ChatServer.NODE, String.valueOf(nodeId), channel,String.valueOf(++nbNodeListener));

		// The nextId takes care of the last "row" messages
		int nextId = c.getNextId();
		// Retrieves the last "row" messages
		while (nextId < nextMessage) {
			// make sure to update the number of listeners which read this message by one to avoid synchronization problems.
			String[] message = tupleSpace.get(channel, String.valueOf(nextId),null, null);
			int addListener = Integer.valueOf(message[3]) + 1;
			tupleSpace.put(channel, String.valueOf(nextId),message[3], String.valueOf(addListener));
			// Distribute the message to the tuple space of the new listener
			c.getTupleSpace().put(message[1], message[2]);
			nextId++;
		}
	}

	public void removeChatListener(ChatListener chatListener) {
		// This call need to stop any operation on the tuple space
		tupleSpace.get(ChatServer.SEMAPHORE_WRITE);
		tupleSpace.get(ChatServer.SEMAPHORE_READ);
		// Make sure the remove operation is well synchronized
		synchronized (this) {
			if(chatListeners.remove(chatListener) == false){
				System.err.println("chat listener did not find !");
			}
		}
		size--;
		// Update the total number of listeners for the channel
		int currentListeners = Integer.valueOf(tupleSpace.get(ChatServer.CHANNEL_LISTENER, channel, null)[2]);
		tupleSpace.put(ChatServer.CHANNEL_LISTENER, channel,String.valueOf(--currentListeners));
		// Update the total number of listeners of the node
		int nbNodeListener = Integer.valueOf(tupleSpace.get(ChatServer.NODE,String.valueOf(nodeId), channel, null)[3]);
		tupleSpace.put(ChatServer.NODE, String.valueOf(nodeId), channel,String.valueOf(--nbNodeListener));

		if (size == 0) {
			// Compute how many messages would have been read by the last listener of this node
			int messageCount = Integer.valueOf(tupleSpace.read(ChatServer.MESSAGE_COUNT, channel, null)[2]);

			final int messageLeft = messageCount - nextMessage;
			// Make sure that the messages which have not been read (before removing the last listener of this node) 
			// will not block the chat system
			for (int i = 0; i < messageLeft; i++) {
				int nbListeners = Integer.valueOf(tupleSpace.read(channel,String.valueOf(i + nextMessage), null, null)[3]);
				// If the message has been read by all the listeners, then the writers must wait 
				// this message to be released
				if (nbListeners >= currentListeners)
					tupleSpace.put(ChatServer.NOT_FULL, this.channel);
			}
			// Impossible to say how many "NOT EMPTY" tuples are still left in the tuple
			// space but we can delete at least the n-1th "NOT EMPTY" tuples 
			for (int i = 1; i < messageLeft; i++) {
				tupleSpace.get(ChatServer.NOT_EMPTY, this.channel, nodeId);
			}
			// If there is no messages left, then we do not need to delete the
			// last "NOT EMPTY" tuple 
			if(messageLeft != 0)
				deleteLastSem = true;
		}
		tupleSpace.put(ChatServer.SEMAPHORE_READ);
		tupleSpace.put(ChatServer.SEMAPHORE_WRITE);
	}

	@Override
	public void run() {
		while (true) {
			tupleSpace.get(ChatServer.NOT_EMPTY, this.channel, nodeId);
			tupleSpace.get(ChatServer.SEMAPHORE_READ);
			if (size > 0 && !deleteLastSem) {
				// Read the message at the current message position and update it 
				String[] message = tupleSpace.get(channel,String.valueOf(nextMessage), null, null);
				int nbListeners = Integer.valueOf(message[3]) + size;
				tupleSpace.put(message[0], message[1], message[2],String.valueOf(nbListeners));

				// Get the number of listener 
				int currentListeners = Integer.valueOf(tupleSpace.read(ChatServer.CHANNEL_LISTENER, channel, null)[2]);
				// check if the message have been read by all the current listeners
				if (nbListeners >= currentListeners) {
					// the writers can write another message in the buffer
					tupleSpace.put(ChatServer.NOT_FULL, this.channel);
				}
				// Distribute the message to the listeners of this node (in their local tuple space)
				for (ChatListener cl : chatListeners) {
					cl.getTupleSpace().put(message[1], message[2]);
				}

				nextMessage++;
			} else {
				// We skip the read operation because the "NOT EMPTY" semaphore needed to be deleted 
				// to keep the count right.
				deleteLastSem = false;
			}
			tupleSpace.put(ChatServer.SEMAPHORE_READ);
		}
	}

}
