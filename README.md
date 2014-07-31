WebSocketXO
===========

A Java Websocket (Project Tyrus) Server implentation for playing Blind Noughts and Crosses (X0)

The objective of this workshop is to create a websocket client that plays XO against another player.

The documentation is available at https://tyrus.java.net/

This is a slight twist on normal XO where you will not be told the state of the board. As you submit the location of your move you will be told if that position is already taken.

The positions on the board are represented by the numbers 0 - 8 in the following configuration:

0 | 1 | 2  
--- | --- | ---
3 | 4 | 5
6 | 7 | 8


API
===

The server will send each player messages based on the current state of the game:

**You are now in a game.** - Self Explainitory

**Is not your turn. Please wait.** - If you send a message when it is not your turn

**It is your turn.** - It is your turn, you will need to send a position for your turn to the server

**Position taken. Try again.** - The position you sent is already taken. You will need to send another position for your move.

**Invalid move. Try again** - The move you sent was not recognised by the server

**You Win!** - Self Explainitory

**You Lost!** - Self Explaintory

When making a client the only message you need to send to the server is a position from 0 to 8.


Notes
====

When making a client please remeber you will need to block the main thread. Otherwise the program will end before it has a chance to communicate with the server.

e.g. 

```java
Thread.sleep() 
```

or

```java
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))
System.out.println("Please press a key to stop the client.");
reader.readLine();
```
