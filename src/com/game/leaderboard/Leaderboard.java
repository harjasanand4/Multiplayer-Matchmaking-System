package com.game.leaderboard;

import com.game.leaderboard.Player;
import com.game.leaderboard.PlayerComparator.EloComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Implements the leaderboard for the game platform.
 *     The rankings, players, and the actual leaderboard goes here.
 * </p>
 */
public class Leaderboard {
    /**
     * List of players in the database
     */
    private static List<Player> players = new ArrayList<>();

    static {
        players.add(new Player(1, "himanshu", 800, 1000, 1200, 0, 0, 0));
        players.add(new Player(2, "tanishk", 600, 300, 900, 0, 0, 0));
        players.add(new Player(3, "harjas", 1000, 200, 400, 0, 0, 0));
        players.add(new Player(4, "surkhab", 200, 1200, 500, 0, 0, 0));
        players.add(new Player(5, "punar", 300, 100, 600, 0 ,0, 0));
    }

    /**
     * Public constructor
     */
    public Leaderboard() {
        //this.players = add new players
    }

    public static List<Player> getAllPlayers() {
        return players;
    }

    /**
     * Method to sort players within a specific ranking system based on their ranks.
     * @return sorted list of Players
     */
    public List<Player> sortRankingSystemPlayers(String game) {
        List<Player> sortedList = new ArrayList<>(players);
        Collections.sort(sortedList, new EloComparator(game, false));
        return sortedList;
    }

    /**
     * Method to display the leaderboard for each ranking system.
     */
    public void displayTopPlayers(int topN) {
        String[] games = {"checkers", "connect4", "ticTacToe"};
        for (String game : games) {
            System.out.println("Leaderboard for: " + game);
            List<Player> sortedUsernames = sortRankingSystemPlayers(game);

            for (int i = 0; i < Math.min(topN, sortedUsernames.size()); i++) {
                Player player = sortedUsernames.get(i);
                int id = player.getId();
                String username = player.getName();
                System.out.println("Rank " + (i + 1) + ": " + username);
                System.out.println("----------------------");
            }
        }
    }

}