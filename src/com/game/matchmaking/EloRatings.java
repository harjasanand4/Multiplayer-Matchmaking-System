package com.game.matchmaking;

public class EloRatings {
    private int ticTacToeElo;
    private int checkersElo;
    private int connect4Elo;

    public EloRatings(int ticTacToe, int checkers, int connect4) {
        this.ticTacToeElo = ticTacToe;
        this.checkersElo = checkers;
        this.connect4Elo = connect4;
    }

    public int getElo(String game) {
        return switch (game.toLowerCase()) {
            case "tictactoe" -> ticTacToeElo;
            case "checkers" -> checkersElo;
            case "connect4" -> connect4Elo;
            default -> throw new IllegalArgumentException("Unknown game: " + game);
        };
    }

    public void setElo(String game, int value) {
        switch (game.toLowerCase()) {
            case "ticTacToe" -> ticTacToeElo = value;
            case "checkers" -> checkersElo = value;
            case "connect4" -> connect4Elo = value;
            default -> throw new IllegalArgumentException("Unknown game: " + game);
        }
    }
}
