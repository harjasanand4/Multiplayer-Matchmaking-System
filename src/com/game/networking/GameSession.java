package com.game.networking;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a GameSession which records a list of states of a current game.
 *
 */
public abstract class GameSession {

    private String sessionID;
    final GameType gameType;
    private boolean isActive;
    protected Player player1;
    protected Player player2;
    public Player currentPlayer;

    /**
     * Constructor for a GameSession
     *
     * @param sessionId the unique ID of the session
     * @param gameType  the type of game being played
     */
    public GameSession(String sessionId, GameType gameType) {
        this.gameType = gameType;
        this.sessionID = sessionId;
        isActive = true;
    }

    public void start(ClientHandler c1, ClientHandler c2) {
        GameState initState;
        if (gameType.equals(GameType.CHECKERS)) {
            initState = new GameState(sessionID, gameType, c1.getPlayer(), c2.getPlayer());
        } else if (gameType.equals(GameType.TICTACTOE)) {
            initState = new GameState(sessionID, gameType, c1.getPlayer(), c2.getPlayer());
        } else if (gameType.equals(GameType.CONNECT4)) {
            initState = new GameState(sessionID, gameType, c1.getPlayer(), c2.getPlayer());
        } else {
            throw new RuntimeException("Unsupported gameType: " + gameType);
        }
    }

    /**
     * Adds the player to an existing game
     *
     * @param player1 a player
     * @param player2 a player
     */
    public void addPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Removes the players from a game
     */
    public void removePlayers() {
        player1 = null;
        player2 = null;
    }

    public String getSessionID() {
        return sessionID;
    }


    /**
     * Sends a message to the Player indicating turns
     *
     * @param player  Player to send message to
     * @param message Message to send
     */
    public void sendMessageToPlayer(Player player, String message) {  //sends messages to players
        if (player != null && player.getClientHandler() != null) {
            player.getClientHandler().sendMessage(message);
        }
    }

    /**
     * update move from player and relays to both players
     */
    public boolean UpdateMove(String playerId, String move) {

        System.out.println(move + "from player:" + playerId);

        String moveUpdate = "Move:" + playerId + ":" + move;  // send move updates to both players
        sendMessageToPlayer(player1, moveUpdate);
        sendMessageToPlayer(player2, moveUpdate);
        if (currentPlayer == player1) {    //switch turn after player one made a move
            currentPlayer = player2;
            sendMessageToPlayer(player1, "Opponents turn");
            sendMessageToPlayer(player2, "Now it's your turn");
        } else {
            currentPlayer = player1;
            sendMessageToPlayer(player1, "Your turn");
            sendMessageToPlayer(player2, "Opponents turn");
        }
        return true;
    }

    public void endGame() {
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }


}

