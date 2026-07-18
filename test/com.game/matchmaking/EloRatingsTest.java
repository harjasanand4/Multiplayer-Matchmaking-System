// File: src/test/java/com/game/matchmaking/EloRatingsTest.java
package com.game.matchmaking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EloRatingsTest {

    @Test
    void testGetEloValid() {
        EloRatings ratings = new EloRatings(1000, 1100, 1200);
        // Note: getElo converts the parameter to lowercase.
        assertEquals(1000, ratings.getElo("tictactoe"));
        assertEquals(1100, ratings.getElo("CHECKERS"));
        assertEquals(1200, ratings.getElo("Connect4"));
    }

    @Test
    void testGetEloInvalidGame() {
        EloRatings ratings = new EloRatings(1000, 1100, 1200);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ratings.getElo("chess"));
        assertTrue(ex.getMessage().contains("Unknown game"));
    }

    @Test
    void testSetEloWithTicTacToeInvalid() {
        EloRatings ratings = new EloRatings(1000, 1100, 1200);

        // Because setElo uses game.toLowerCase() in the switch,
        // passing "ticTacToe" will be converted to "tictactoe" and not match the case label "ticTacToe",
        // causing it to throw an exception.
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ratings.setElo("ticTacToe", 1050));
        assertTrue(ex.getMessage().contains("Unknown game"));

        // These cases work since "checkers" and "connect4" are already lowercase.
        ratings.setElo("checkers", 1150);
        assertEquals(1150, ratings.getElo("checkers"));

        ratings.setElo("connect4", 1250);
        assertEquals(1250, ratings.getElo("connect4"));
    }

    @Test
    void testSetEloInvalidGame() {
        EloRatings ratings = new EloRatings(1000, 1100, 1200);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ratings.setElo("chess", 1300));
        assertTrue(ex.getMessage().contains("Unknown game"));
    }
}
