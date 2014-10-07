package chat;

import tuplespaces.LocalTupleSpace;
import tuplespaces.TupleSpace;

public class ChatListener {

	private final TupleSpace internalTupleSpace;
	private final ChatMessagePuller puller;
	private volatile int nextId;
	
	public ChatListener(int nextMessage, int rows, ChatMessagePuller puller){
		this.internalTupleSpace = new LocalTupleSpace();
		// Rewind the index to get the last "row" messages
		this.nextId = (nextMessage - rows < 0) ? 0 : nextMessage - rows;
		this.puller = puller;
	}
	
	public String getNextMessage() {
		// Get the next message in its local tuple space
		String message = internalTupleSpace.get(String.valueOf(nextId),null)[1];		
		nextId++;
		return message;
	}

	public void closeConnection() {
		// Call the remove method of the thread which pulls the messages to this listener
		puller.removeChatListener(this);
	}
	
	public TupleSpace getTupleSpace(){
		return internalTupleSpace;
	}
	
	public int getNextId(){
		return nextId;
	}
}
