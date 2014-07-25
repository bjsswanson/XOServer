package com.lbi.swansonb.games.xo.server;

import org.glassfish.tyrus.server.Server;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@ServerEndpoint(value = "/xo", configurator = XOServerConfigurator.class)
public class XOServer {

	private static final String ERROR = "Unable to process turn.";
	private static final String YOU_ARE_NOW_IN_A_GAME = "You are now in a game.";
	private static final String NOT_ENOUGH_PLAYERS = "Not enough players. You are in a queue.";
	private static final String PLAYER_CONNECTED = "Player Connected.";
	private static final String QUEUEING_PLAYER = "Queueing Player: ";
	private static final String CREATING_GAME = "Creating game.";
	private static final String REMOVING_DEAD_GAME = "Removing dead game.";
	private static final String REMOVING_DEAD_QUEUED_PLAYER = "Removing dead queued player.";

	private LinkedList<Session> queued = new LinkedList<Session>();
	private List<Game> games = new ArrayList<Game>();

	@OnOpen
	public void onOpen(Session session){
		System.out.println(PLAYER_CONNECTED);
		cleanup();

		if(queued.size() > 0){
			System.out.println(CREATING_GAME);
			Session player1 = session;
			Session player2 = queued.pop();
			games.add(new Game(player1, player2));
		} else {
			System.out.println(QUEUEING_PLAYER + queued.size());
			queued.add(session);
			sendMessage(session, NOT_ENOUGH_PLAYERS);
		}
	}

	@OnMessage
    public String onMessage(String message, Session session) {
		String id = session.getId();
		Game game = getGame(id);

		if(isGaming(game, session)){
			return game.handleTurn(message, session);
		} else {
			return ERROR;
		}
    }

	private void cleanup() {
		Iterator<Game> gIt = games.iterator();
		while(gIt.hasNext()){
			Game game = gIt.next();
			if(game.isGameOver()){
				System.out.println(REMOVING_DEAD_GAME);
				game.kickPlayers();
				gIt.remove();
			}
		}

		Iterator<Session> qIt = queued.iterator();
		while(qIt.hasNext()){
			Session session = qIt.next();
			if(!session.isOpen()){
				System.out.println(REMOVING_DEAD_QUEUED_PLAYER);
				qIt.remove();
			}
		}
	}

	public boolean isQueued(String id) {
		return queued.contains(id);
	}

	public boolean isGaming(Game game, Session session) {
		return game != null && game.isPlayerInGame(session.getId());
	}

	public Game getGame(String id) {
		for(Game game : games) {
			if(game.isPlayerInGame(id)){
				return game;
			}
		}

		return null;
	}

	public void sendMessage(Session session, String message){
		try {
			if(session.isOpen()){
				session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			System.err.println("Error sending message");
		}
	}

	public static void main(String[] args){
		Server server = new Server("localhost", 8025, "/websocket", null, XOServer.class);
	    try {
	        server.start();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print("Please press a key to stop the server.");
	        reader.readLine();
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        server.stop();
	    }
	}
}