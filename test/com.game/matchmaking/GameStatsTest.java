// File: src/test/java/com/game/matchmaking/GameStatsTest.java
package com.game.matchmaking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStatsTest {

    @Test
    void testGetWinsAndIncrement() {
        GameStats stats = new GameStats(0, 0, 0);
        // Initial wins
        assertEquals(0, stats.getWins("tictactoe"));
        assertEquals(0, stats.getWins("checkers"));
        assertEquals(0, stats.getWins("connect4"));

        // Increment wins and verify counts
        stats.incrementWins("tictactoe");
        assertEquals(1, stats.getWins("tictactoe"));

        stats.incrementWins("checkers");
        stats.incrementWins("checkers");
        assertEquals(2, stats.getWins("checkers"));

        stats.incrementWins("connect4");
        stats.incrementWins("connect4");
        stats.incrementWins("connect4");
        assertEquals(3, stats.getWins("connect4"));
    }

    @Test
    void testGetWinsInvalidGame() {
        GameStats stats = new GameStats(0, 0, 0);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> stats.getWins("chess"));
        assertTrue(ex.getMessage().contains("Unknown game"));
    }

    @Test
    void testIncrementWinsInvalidGame() {
        GameStats stats = new GameStats(0, 0, 0);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> stats.incrementWins("chess"));
        assertTrue(ex.getMessage().contains("Unknown game"));
    }
}
