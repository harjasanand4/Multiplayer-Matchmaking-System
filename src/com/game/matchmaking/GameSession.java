package com.game.matchmaking;

/**
 * <p>
 *     Implements Game sessions. Used by Matchmaking.
 *     To be abstracted into individual game code.
 * </p>
 * TO-DO:
 *      1. Identifying different game sessions.
 *      2. Player implementation
 *      3. Abstract the game sessions.
 */
public class GameSession {
    /**
     * The players and their ID. To be updated after Player implementation
     */
    private String player1;
    private String player2;
    private boolean isActive;

    /**
     * Public constructor
     * @param player1 the id of player 1
     * @param player2 the id of player 2
     */
    public GameSession(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.isActive = true;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    /**
     * ends the game session
     * @param winner the id of the player who won the round
     */
    public void endSession(String winner) {
        this.isActive = false;
        System.out.println("Game over! Winner: " + winner);
    }

    /**
     * checks if the session is active
     * @return whether the session is active or not.
     */
    public boolean isGameActive() {
        return isActive;
    }
}

