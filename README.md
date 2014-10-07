Distributed-Chat
================

This project has been made in Java. It comes with a TupleSpace server that you need to start first. This server allows you to start a chat room by connecting to it. As said in the title, it's a distributed chat, you can connect to the server from different machines. Once a chat room is created, you can send messages to a channel that will be spread accross all listeners to this channel. If there are some listeners joining during a conversation, they will get the last messages (the number of back-up messages is defined by the buffer size given when a chat room is started).

How to launch the project
-------------------------
**The easy way, this will start a server and create a chat client with a buffer size of ten and three channels**

javac chatui/MetaUI.java 
java chatui/MetaUI

**The manual way**

*Start the tuple server, which returns the port you must listen to:*
javac tupleserver/TupleServer.java
java tupleserver/TupleServer&
*Start a chat room*
javac chatui/ChatUI.java
java chatui/ChatUI {ip_address}:{port_returned} 5 channel_test & # Creating a chat room with a buffer size of 5 and one channel.
*Start a new client which will use the same settings first created chat room*
java chatui/ChatUI {ip_address}:{port_returned} &

You can find more detailed on the implementation below: 

I. Tuple space implementation
=============================

1. How I ensure tuple space correctness 
---------------------------------------

I chose a map as data structure to store the tuples in my tuple space implementation. The map contains a key, which is represented by the length of the pattern and a list of pattern (with the same length) as values. All the methods of the tuple space use two objects to synchronize. First we synchronize the method on the map of the tuple space. Once we have found the list which contains the same pattern length as the pattern we are looking for, we synchronize on this list. This synchronize system ensures that there is no data corruption. We also use the wait/notifyAll primitives to either wait for a tuple or notify to all waiting threads that a new tuple has been added in the tuple space. Note that a tuple is added at first in the list to be more efficient when we proceed to a search in the list right after adding a tuple in the tuple space. To be more efficient, we are not comparing the tuples with their characters but with their hash codes which is faster.

After attending to the exercise sessions, this task is highly similar to the implementation of a concurrent queue. As we demonstrate how to ensure the correctness of a concurrent queue, there is no need to do so with this tuple space implementation. The challenging part was in choosing the data structure to store the tuples and on which objects we should synchronize.

II. Chat system implementation
=============================
Before talking about the correctness of the chat system, I will show how I built it in UML:

![alt text](https://github.com/arthurHamon2/Distributed-Chat/blob/master/images/UML.png "UML")


The chat server has as many chat message pullers as channels. A chatMessagePuller object runs in a thread. Its role is to read the data from the chat server tuple space from its channel and distribute the data to all listeners of the channel (of its node). Each chat listeners have their own tuple space and they read data from there when the chat message puller writes in it. It avoids a lot a thread waiting on patterns in the same tuple space. Whatever the number of listeners, there will always be the same number of objects waiting in the tuple space. This is a picture to summarize the configuration of a node:

![alt text](https://github.com/arthurHamon2/Distributed-Chat/blob/master/images/General.png "Summary")

1. Bounded buffer
-----------------

I took the bounded buffer algorithm we studied in the exercise session 4 and 5. I did some modifications to the algorithm as the same data needs to be read by all the listeners. For the write operation, if there are no listeners on a given channel, the writer can consider that the data has been read and it releases a "not full" tuple to not block the buffer. The read operation releases a tuple "not full" only when the data has been distributed to all listeners. To ensure synchronization between different nodes, I used the tuples described on the picture below:

 ![alt text](https://github.com/arthurHamon2/Distributed-Chat/blob/master/images/Tuples.png "Tuples used")

2. Message consistency across listeners
---------------------------------------

To ensure the consistency of the messages across listeners, I used a simple counter in the message tuple as an id message. The counter is incremented during the write operation, which is stored in the tuple "MESSAGE\_COUNT", after each write (note that a write operation is atomic thanks to the semaphore "write" represented by the tuple SEMAPHORE\_WRITE). When the puller thread puts a tuple in each listener local tuple spaces, it keeps only the message id and the data. A listener has its own message index which tells the next id message to read. So, as a write operation is atomic, the id messages are unique and the listeners read the data according to their own indexes. To release a "NOT FULL" tuple from the read operation, we just compare the listener number of the channel and how many times the message has been distributed. If it is equal, then we can release a "NOT\_FULL" tuple, otherwise, we just update the listener number of the message in the tuple.

3. Deleting and adding listeners
--------------------------------

When adding a listener, we first, try to get the two tuples which are used as semaphores for the write and read operation, when these are acquired, no write or read operations can be proceeded. Then we can add the listener and read the last "row" messages without losing data. Deleting a listener is trickier as if we are deleting the last listener of a channel node, we need to clean up the messages left in the tuple space (which means read them all and check if we need to send a "NOT\_FULL" tuple to avoid blocking the buffer). We also need to clean the "NOT\_EMPTY" tuples in this case to avoid that, when a new listener is added, it tries to read a message without the insurance there is one.

III- Methods used to ensure correctness
=======================================

I tried to read my program line by line and prove that there are no problems in my solution. I tried to improve my proof as long as I find bugs or errors while testing. I used the test package and launch it many and at the same time to maximize the chance to see a problem occurred. I also did some manual test with the UI which has been really useful when I identified a bug and then I could have repeated my scenario to ensure that I have fixed the bug.
