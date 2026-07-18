package com.game.networking;

/**
        * Constructor for a GameSession
     *
 */

public class TicTacToeSession extends GameSession {
    public TicTacToeSession(String sessionId,Player player1,Player player2) {
        super(sessionId, GameType.TICTACTOE);
        this.player1 = player1;
        this.player2 = player2;
        System.out.println("Tic Tac Toe Session Created: " + sessionId);
    }

    public Player getPlayer1(){
        return player1;
    }

    public Player getPlayer2(){
        return player2;
    }

    @Override
    public void start(ClientHandler player1Handler, ClientHandler player2Handler) {
        super.start(player1Handler, player2Handler);

        // Set the current player to player 1 at the start
        currentPlayer = player1;

        // Notify both players that the game has started
        sendMessageToPlayer(player1, "Tic Tac Toe Game Started: " + getSessionID());
        sendMessageToPlayer(player2, "Tic Tac Toe Game Started: " + getSessionID());

        // Notify current player that it's their turn
        sendMessageToPlayer(currentPlayer, "Your Turn");
   }



}







