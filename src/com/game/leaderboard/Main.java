package com.game.leaderboard;

import com.game.leaderboard.Leaderboard;

public class Main {
    public static void main(String[] args) {
        Leaderboard leaderboard = new Leaderboard();

        // Check command line argument for number of players to display
        int topN = 3; // default
        if (args.length > 0) {
            try {
                topN = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Defaulting to 3 players.");
            }
        }

        leaderboard.displayTopPlayers(topN);
    }
}
