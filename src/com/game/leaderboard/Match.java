package com.game.leaderboard;

/**
 * Match class.
 * Currently, uses JSONs to parse different matches, results, and game type
 * and saves them.
 * Future Expectations: Will use a database to implement matches, the winner and game types.
 */

public class Match {
    private String player1ID;
    private String player1;
    private String player2;
    private GameResult result;
    private GameType gameType;

    public Match() {}

    public Match(String player1ID, String player1, String player2, GameResult result, GameType gameType) {
        this.player1ID = player1ID;
        this.player1 = player1;
        this.player2 = player2;
        this.result = result;
        this.gameType = gameType;
    }

    public String getPlayer1ID(){
        return player1ID;
    }
    public String getPlayer1(){
        return player1;
    }

    public String getPlayer2(){
        return player2;
    }
    public GameResult getResult() {
        return result;
    }

    public GameType getGameType() {
        return gameType;
    }
}
