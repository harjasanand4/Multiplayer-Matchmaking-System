package com.game.matchmaking;

import com.game.leaderboard.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Matchmaking {
    private final List<Player> queue = new ArrayList<>();
    private final int INITIAL_RANGE = 50;
    private final int RANGE_INCREMENT = 25;
    private final long TIMEOUT = 60_000;

    public synchronized void enqueue(Player p) {
        queue.add(p);
    }

    public synchronized void runMatchmaking(String game) {
        while (true) {
            Iterator<Player> it = queue.iterator();
            while (it.hasNext()) {
                Player p1 = it.next();
                Player match = findMatch(p1, game);
                if (match != null) {
                    it.remove();
                    queue.remove(match);
                    startGame(p1, match, game);
                } else if (System.currentTimeMillis() - p1.getQueuedAt() > TIMEOUT) {
                    System.out.println("⏳ Matchmaking timed out for " + p1.getName());
                    it.remove();
                }
            }
            try {
                Thread.sleep(1000); // tick every second
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private Player findMatch(Player p1, String game) {
        int range = INITIAL_RANGE;
        int p1Elo = p1.getElo(game);

        while (range <= 400) {
            for (Player p2 : queue) {
                if (!p2.getName().equals(p1.getName())) {
                    int p2Elo = p2.getElo(game);
                    if (Math.abs(p1Elo - p2Elo) <= range) {
                        return p2;
                    }
                }
            }
            range += RANGE_INCREMENT;
        }
        return null;
    }

    private void startGame(Player p1, Player p2, String game) {
        System.out.printf("🎮 Game started: %s vs %s in %s (ELO: %d vs %d)%n",
                p1.getName(), p2.getName(),
                game, p1.getElo(game), p2.getElo(game));

        // Simulate p1 winning for now
        p1.incrementWins(game);
        System.out.printf("🏆 %s now has %d wins in %s%n", p1.getName(), p1.getWins(game));
    }
}
