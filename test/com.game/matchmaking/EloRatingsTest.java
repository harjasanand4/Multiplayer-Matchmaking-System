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
    void testSetEloTicTacToeAnyCase() {
        EloRatings ratings = new EloRatings(1000, 1100, 1200);

        // setElo lower-cases its key, so the case labels must be lower case too. This used to
        // throw for "ticTacToe" (the label was "ticTacToe"), so Tic-Tac-Toe ratings could never be saved.
        ratings.setElo("ticTacToe", 1050);
        assertEquals(1050, ratings.getElo("tictactoe"));
        ratings.setElo("TICTACTOE", 1060);
        assertEquals(1060, ratings.getElo("ticTacToe"));

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
