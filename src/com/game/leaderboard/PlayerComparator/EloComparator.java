package com.game.leaderboard.PlayerComparator;

import com.game.leaderboard.Player;

import java.util.Comparator;

public class EloComparator implements Comparator<Player> {
    private String mode;
    private boolean ascending;

    // mode: the specific Elo type (e.g., "blitz", "rapid")
    // ascending: true for low-to-high, false for high-to-low
    public EloComparator(String mode, boolean ascending) {
        this.mode = mode;
        this.ascending = ascending;
    }

    /**
     * 1 -> Tic Tac Toe
     * 2 -> Checkers
     * 3 -> Connect 4
     * @param p1 the first object to be compared.
     * @param p2 the second object to be compared.
     * @return
     */
    @Override
    public int compare(Player p1, Player p2) {
        switch (mode) {
            case "tictactoe":
                int ticTacToeElo1 = p1.getElo("tictactoe");
                int ticTacToeElo2 = p2.getElo("tictactoe");
                return ascending ? Integer.compare(ticTacToeElo1, ticTacToeElo2) : Integer.compare(ticTacToeElo2, ticTacToeElo1);
            case "checkers":
                int checkersElo1 = p1.getElo("checkers");
                int checkersElo2 = p2.getElo("checkers");
                return ascending ? Integer.compare(checkersElo1, checkersElo2) : Integer.compare(checkersElo2, checkersElo1);
            case "connect4":
                int connect4Elo1 = p1.getElo("connect4");
                int connect4Elo2 = p2.getElo("connect4");
                return ascending ? Integer.compare(connect4Elo1, connect4Elo2) : Integer.compare(connect4Elo2, connect4Elo1);
            default:
                throw new IllegalArgumentException("Invalid mode. Choose one of 1, 2, 3.");
        }

    }
}