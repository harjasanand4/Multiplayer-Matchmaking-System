package com.game.networking;


public class ConnectFourSession extends GameSession {
    /**
     * Constructor for a GameSession
     *
     * @param sessionId the unique ID of the session
     */

    public ConnectFourSession(String sessionId,Player player1, Player player2) {
        super(sessionId, GameType.CHECKERS);
        this.player1 = player1;
        this.player2 = player2;
    }
        @Override
        public void start(ClientHandler player1Handler, ClientHandler player2Handler) {
            super.start(player1Handler, player2Handler);

            // Set the current player to player 1 at the start
            currentPlayer = player1;

            // Notify both players that the game has started
            sendMessageToPlayer(player1, "Connect Four Game Started: " + getSessionID());
            sendMessageToPlayer(player2, "Connect Four Game Started: " + getSessionID());

            // Notify current player that it's their turn
            sendMessageToPlayer(currentPlayer, "Your Turn");
        }


        public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}