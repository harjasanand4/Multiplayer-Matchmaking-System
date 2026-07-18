package com.game.leaderboard;

import com.game.leaderboard.PlayerComparator.EloComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class sortPlayersTest {
    List<Player> players;
    @Before
    public void setUp(){
        players.add(new Player(1, "himanshu", 800, 1000, 1200));
        players.add(new Player(2, "tanishk", 600, 300, 900));
        players.add(new Player(3, "harjas", 1000, 200, 400));
        players.add(new Player(4, "surkhab", 200, 1200, 500));
        players.add(new Player(5, "punar", 300, 100, 600));
    }

    @Test
    public void testSortPlayers() {
        Comparator<Player> EloComparator = new EloComparator('1', true);

        Collections.sort(players, EloComparator);

        System.out.println("Sorted list: ");
        for (Player p : players) {
            System.out.println(p);
        }
    }
}
