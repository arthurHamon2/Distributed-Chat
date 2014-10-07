package chat;

import java.util.HashMap;
import java.util.Map;

import tuplespaces.TupleSpace;

public class ChatServer {
	
	private final TupleSpace tupleSpace;
	private final int rows;
	private final int nodeId;
	private final String[] channelNames;
	private final Map<String,ChatMessagePuller> messagePullers;
	
	private final static String CHATSERVER_DESCRIPTION = "1";
	private final static String CHANNEL_DESCRIPTION = "2";
	public final static String CHANNEL_LISTENER = "3";
	public final static String MESSAGE_COUNT = "4";
	
	public final static String NOT_FULL = "NF";
	public final static String NOT_EMPTY = "NE";
	
	public final static String NODE = "NODE";
	public final static String SEMAPHORE_WRITE = "SW";
	public final static String SEMAPHORE_READ = "SR";
	
	public ChatServer(TupleSpace t, int rows, String[] channelNames) {
		this.tupleSpace = t;
		this.rows = rows;
		this.channelNames = channelNames;

		// This is always the first node
		this.nodeId = 1;
		tupleSpace.put(NODE,String.valueOf(nodeId));
		
		setupChatServer();
		setupChannels();
		setupChannelListener();
		
		// Set up the map of threads waiting for reading the channels
		Map<String,ChatMessagePuller> messagePullers = new HashMap<String,ChatMessagePuller>(channelNames.length);
		for(String channel : channelNames){
			ChatMessagePuller messagePuller = new ChatMessagePuller(tupleSpace, channel, String.valueOf(this.nodeId), 0, this.rows);
			// The thread message puller is a daemon thread to make the things more simple
			messagePuller.setDaemon(true);
			messagePuller.start();
			messagePullers.put(channel,messagePuller);
		}
		this.messagePullers = new HashMap<String,ChatMessagePuller>(messagePullers);
		
		// Set up the semaphores
		tupleSpace.put(SEMAPHORE_READ);
		tupleSpace.put(SEMAPHORE_WRITE);
		
	}
	
 	private void setupChatServer(){
		tupleSpace.put(CHATSERVER_DESCRIPTION,String.valueOf(rows),String.valueOf(channelNames.length));
	}
 	
	private void setupChannels(){
		int i = 0;
		for(String channel : this.channelNames){
			tupleSpace.put(NODE,String.valueOf(nodeId),channelNames[i],"0");
			for(int j=0; j<rows; j++){
				tupleSpace.put(NOT_FULL,channel);
			}
			tupleSpace.put(CHANNEL_DESCRIPTION,String.valueOf(i),channel);
			i++;
		}
		
	}

	private void setupChannelListener(){
		for(String channel : this.channelNames){
			tupleSpace.put(CHANNEL_LISTENER,channel,String.valueOf(0));
			tupleSpace.put(MESSAGE_COUNT,channel,String.valueOf(0));
		}
	}
	
	public ChatServer(TupleSpace t) {
		this.tupleSpace = t;
		
		// Assign the right node number to this process
		int nbNodes = Integer.valueOf(tupleSpace.get(NODE,null)[1]);
		nbNodes++;
		this.nodeId = nbNodes;
		tupleSpace.put(NODE,String.valueOf(nbNodes));
		
		// Retrieve property rows from tupleSpace
		String[] chatServerDescription = t.read(CHATSERVER_DESCRIPTION,null,null);
		this.rows = Integer.valueOf(chatServerDescription[1]);
		
		// Retrieve property channel names from tupleSpace
		int channelNumber = Integer.valueOf(chatServerDescription[2]);
		String[] channelNames = new String[channelNumber];
		for(int i=0; i<channelNumber;i++){
			String[] channelName = t.read(CHANNEL_DESCRIPTION,String.valueOf(i),null);
			channelNames[i] = channelName[2];
			tupleSpace.put(NODE,String.valueOf(nodeId),channelNames[i],"0");
		}
		this.channelNames = channelNames;

		
		// Creating the message puller thread for each channel
		Map<String,ChatMessagePuller> messagePullers = new HashMap<String,ChatMessagePuller>(channelNames.length);
		for(String channel : channelNames){
			String messageCount = tupleSpace.read(MESSAGE_COUNT,channel,null)[2];
			ChatMessagePuller messagePuller = new ChatMessagePuller(tupleSpace, channel, String.valueOf(nbNodes), Integer.valueOf(messageCount), this.rows);
			messagePuller.setDaemon(true);
			messagePuller.start();
			messagePullers.put(channel,messagePuller);
		}
		this.messagePullers = new HashMap<String,ChatMessagePuller>(messagePullers);
		

	} 

	public String[] getChannels() {
		return channelNames;
	}

	public void writeMessage(String channel, String message) {
		tupleSpace.get(NOT_FULL,channel);
		tupleSpace.get(SEMAPHORE_WRITE);
		
		int messageCount = Integer.valueOf(tupleSpace.get(MESSAGE_COUNT,channel,null)[2]);
		// Put the message into the tuple space (initialization of the number of listeners at zero)
		tupleSpace.put(channel,String.valueOf(messageCount),message,"0");
				
		// Put the new counter into the tupleSpace
		int new_count = messageCount;
		tupleSpace.put(MESSAGE_COUNT,channel,String.valueOf(++new_count));
		
		// Delete the last tuple in the buffer
		if(messageCount - rows >= 0){
			tupleSpace.get(channel,String.valueOf(messageCount-rows),null,null);
		}
		
		tupleSpace.get(SEMAPHORE_READ);
		// get the number of listener of the channel
		int nbListeners = readCurrentListeners(channel);
		
		// If there is no listener, then we can proceed to another writing operation
		if(nbListeners <= 0){
			tupleSpace.put(NOT_FULL,channel);
		}
		else{
			// get the number of nodes
			int nbNodes = readNodeNumber();
			// For each node, we check if there is some listeners
			for(int node=1;node<=nbNodes;node++){
				// read how many listeners does the node have
				int nodeListeners = readListeners(node,channel);
				if(nodeListeners > 0){
					tupleSpace.put(NOT_EMPTY,channel,String.valueOf(node));
				}
			}
		}
		tupleSpace.put(SEMAPHORE_READ);
		
		tupleSpace.put(SEMAPHORE_WRITE);	
	}

	public ChatListener openConnection(String channel) {
		// Increment the number of listeners of the channel
		tupleSpace.get(SEMAPHORE_WRITE);
		tupleSpace.get(SEMAPHORE_READ);
		
		ChatMessagePuller puller = messagePullers.get(channel);
		int messageCount = Integer.valueOf(tupleSpace.read(MESSAGE_COUNT,channel,null)[2]);
		ChatListener chatL = new ChatListener(messageCount, rows, puller);	
		puller.addChatListener(chatL,messageCount);
		
		tupleSpace.put(SEMAPHORE_READ);
		tupleSpace.put(SEMAPHORE_WRITE);
		return chatL;
	}
	
	private int readCurrentListeners(String channel){
		return Integer.valueOf(tupleSpace.read(ChatServer.CHANNEL_LISTENER,channel,null)[2]);
	}
	
	private int readNodeNumber(){
		return Integer.valueOf(tupleSpace.read(NODE,null)[1]);
	}
	
	private int readListeners(int node, String channel){
		return Integer.valueOf(tupleSpace.read(NODE,String.valueOf(node),channel,null)[3]);
	}
}
