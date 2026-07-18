package com.game.leaderboard;
import com.game.matchmaking.*;

/**
 * A placeholder player class to use until the actual integration part, has components required by
 * Leaderboard
 */
public class Player {
    private final int id;                         // ID for the player used in the backend
    private final String name;                    // username of the player
    private final EloRatings eloRatings;          // elo ratings
    private final GameStats gameStats;            // game stats
    private long queuedAt;
    //public long getQueuedAt;

    public Player(int id, String name, int tttElo, int checkersElo, int connect4Elo, int tttwins, int checkersWins, int connect4Wins) {
        this.id = id;
        this.name = name;
        this.eloRatings = new EloRatings(tttElo, checkersElo, connect4Elo);
        this.gameStats = new GameStats(tttwins, checkersWins, connect4Wins);
        this.queuedAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public int getElo(String game){
        game = game.toLowerCase(); // NORMALIZE INPUT
        return switch (game) {
            case "connect4" -> eloRatings.getElo("connect4");
            case "checkers" -> eloRatings.getElo("checkers");
            case "tictactoe" -> eloRatings.getElo("tictactoe");
            default -> throw new IllegalArgumentException("Invalid game: " + game);
        };
    }

    public void setElo(String game, int elo){
        switch (game) {
            case "connect4" -> eloRatings.setElo("connect4", elo);
            case "checkers" -> eloRatings.setElo("checkers", elo);
            case "tictactoe" -> eloRatings.setElo("ticTacToe", elo);
            default -> throw new IllegalArgumentException("Invalid game: " + game);
        }
    }

    public int getWins(String game){
        return switch (game) {
            case "connect4" -> gameStats.getWins("connect4");
            case "checkers" -> gameStats.getWins("checkers");
            case "ticTacToe" -> gameStats.getWins("ticTacToe");
            default -> throw new IllegalStateException("Unexpected value: " + game);
        };
    }

    public long getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(long queuedAt) {
        this.queuedAt = queuedAt;
    }

    public void incrementWins(String game) {
        switch (game) {
            case "ticTacToe" -> gameStats.incrementWins("ticTacToe");
            case "checkers" -> gameStats.incrementWins("checkers");
            case "connect4" -> gameStats.incrementWins("connect4");
            default -> throw new IllegalStateException("Unexpected value: " + game);
        }
    }

    /**
     * Rudimentary toString method for the CLI
     * @return a string with all the attributes for the player in question.
     */
//    @Override
//    public String toString() {
//        return "Username: " + this.name
//                + "\nTic Tac Toe Elo: " + this.ticTacToeElo
//                + "\nCheckers Elo: "+this.checkersElo
//                + "\nConnect 4 Elo: " + this.connect4Elo
//                + "\nTic Tac Toe Wins: " + this.ticTacToeWins
//                + "\nCheckers Wins: " + this.checkersWins
//                + "\nConnect 4 Wins: " + this.connect4Wins;
//    }
}