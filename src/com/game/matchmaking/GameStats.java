package com.game.matchmaking;

public class GameStats {
    private int tictactoeWins;
    private int checkersWins;
    private int connect4Wins;

    public GameStats(int tttWins, int checkersWins, int connect4Wins) {
        this.tictactoeWins = tttWins;
        this.checkersWins = checkersWins;
        this.connect4Wins = connect4Wins;
    }

    public int getWins(String game) {
        return switch (game.toLowerCase()) {
            case "tictactoe" -> tictactoeWins;
            case "checkers" -> checkersWins;
            case "connect4" -> connect4Wins;
            default -> throw new IllegalArgumentException("Unknown game: " + game);
        };
    }

    public void incrementWins(String game) {
        switch (game.toLowerCase()) {
            case "tictactoe" -> tictactoeWins++;
            case "checkers" -> checkersWins++;
            case "connect4" -> connect4Wins++;
            default -> throw new IllegalArgumentException("Unknown game: " + game);
        }
    }
}
