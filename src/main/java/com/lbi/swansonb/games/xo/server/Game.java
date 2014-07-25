package com.lbi.swansonb.games.xo.server;

import javax.websocket.Session;
import java.io.IOException;

public class Game {

	public static final String IT_IS_YOUR_TURN = "It is your turn.";
	public static final String YOU_ARE_NOW_IN_A_GAME = "You are now in a game.";
	public static final String IS_NOT_YOUR_TURN = "Is not your turn. Please wait.";
	public static final String PLAYER_1_DISCONNECTED = "Player 1 Disconnected.";
	public static final String PLAYER_2_DISCONNECTED = "Player 2 Disconnected.";
	public static final String GAME_OVER = "Game Over.";
	public static final String INVALID_MOVE = "Invalid move. Try again";
	public static final String POSITION_TAKEN = "Position taken. Try again.";
	public static final String PLEASE_WAIT = "Please wait. Other player is making a move";
	public static final String GAME_TIE = "Game tied.";
	public static final String GAME_WIN = "You Win!";
	public static final String GAME_LOST = "You Lost!";
	private static final String CLOSING_CONNECTIONS = "Closing connections.";

	private Session player1;
	private Session player2;

	private Session turn;
	private int turnCounter;
	private int[] game;

	public Game(Session player1, Session player2) {
		this.player1 = player1;
		this.player2 = player2;

		turnCounter = 0;
		turn = player1;
		game = new int[9];

		sendMessage(player1, YOU_ARE_NOW_IN_A_GAME);
		sendMessage(player2, YOU_ARE_NOW_IN_A_GAME);
		sendMessage(player1, IT_IS_YOUR_TURN);
		sendMessage(player2, PLEASE_WAIT);
	}

	public String handleTurn(String message, Session session) {
		if(!playersConnected()){
			return disconnected();
		} else if(isPlayerTurn(session)){
			Integer position = parseMessage(message);
			if(invalidPosition(position)) {
				return INVALID_MOVE;
			} else if(positionTaken(position)) {
				return POSITION_TAKEN;
			} else {
				game[position] = getPlayerNumber(session);
				if(isGameWon()){
					return winGame();
				} else if(isGameOver()) {
					return tieGame();
				} else {
					turn = getOtherPlayer();
					turnCounter += 1;
					sendMessage(turn, IT_IS_YOUR_TURN);
					return PLEASE_WAIT;
				}
			}
		} else {
			return IS_NOT_YOUR_TURN;
		}
	}

	private String winGame() {
		sendMessage(turn, GAME_WIN);
		sendMessage(getOtherPlayer(), GAME_LOST);
		sendBoard(turn);
		sendBoard(getOtherPlayer());
		turnCounter = 9;
		return GAME_OVER;
	}

	private String tieGame(){
		sendMessage(player1, GAME_TIE);
		sendMessage(player2, GAME_TIE);
		sendBoard(player1);
		sendBoard(player2);
		turnCounter = 9;
		return GAME_OVER;
	}

	private String disconnected() {
		sendMessage(player1, PLAYER_2_DISCONNECTED);
		sendMessage(player2, PLAYER_1_DISCONNECTED);
		turnCounter = 9;
		return GAME_OVER;
	}

	private boolean positionTaken(Integer position) {
		return game[position] != 0;
	}

	private boolean invalidPosition(Integer position) {
		return position == null || position < 0 || position > 9;
	}

	private Integer parseMessage(String message) {
		try {
			return Integer.parseInt(message);
		} catch (NumberFormatException e){
			return null;
		}
	}

	public void kickPlayers() {
		kickPlayer(player1, CLOSING_CONNECTIONS);
		kickPlayer(player2, CLOSING_CONNECTIONS);
	}

	private void kickPlayer(Session session, String message){
		try {
			if(session != null && session.isOpen()){
				sendMessage(session, message);
				session.close();
			}
		} catch (IOException e) {
			System.err.println("Error disconnecting player");
		}
	}

	public boolean isGameOver() {
		return turnCounter >= 9;
	}

	public boolean isGameWon() {
		int player = getPlayerNumber(turn);
		int winScore = player * 3;

		if(game[0] + game[1] + game[2] == winScore) return true;
		if(game[3] + game[4] + game[5] == winScore) return true;
		if(game[6] + game[7] + game[8] == winScore) return true;
		if(game[0] + game[3] + game[6] == winScore) return true;
		if(game[1] + game[4] + game[7] == winScore) return true;
		if(game[2] + game[5] + game[8] == winScore) return true;
		if(game[0] + game[4] + game[8] == winScore) return true;
		if(game[2] + game[4] + game[6] == winScore) return true;

		return false;
	}


	public int getPlayerNumber(Session session){
		return player1.equals(session) ? 1 : -1;
	}

	public boolean isPlayerInGame(String id) {
		String player1 = this.player1.getId();
		String player2 = this.player2.getId();
		return player1.equals(id) || player2.equals(id);
	}

	public boolean isPlayerTurn(Session session) {
		return turn.equals(session);
	}

	public Session getOtherPlayer(){
		if(player1.equals(turn)){
			return player2;
		} else {
			return player1;
		}
	}

	public boolean playersConnected(){
		return player1.isOpen() && player2.isOpen();
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

	public String letter(int i){
		int player = game[i];
		if(player == 1) {
			return "X";
		} else if(player == -1) {
			return "O";
		} else {
			return " ";
		}
	}

	public void sendBoard(Session session){
		sendMessage(session, letter(0) + letter(1) + letter(2));
		sendMessage(session, letter(3) + letter(4) + letter(5));
		sendMessage(session, letter(6) + letter(7) + letter(8));
	}
}
