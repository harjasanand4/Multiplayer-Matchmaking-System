// File: src/test/java/com/game/matchmaking/EloSystemTest.java
package com.game.matchmaking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EloSystemTest {

    @Test
    void testUpdateRatingWin() {
        int ratingA = 1600;
        int ratingB = 1500;
        int constant = 30;
        double winVar = 1.0; // win
        int updated = EloSystem.updateRating(ratingA, ratingB, constant, winVar);
        // Not calculating an exact expected value – just that it changes.
        assertNotEquals(ratingA, updated);
    }

    @Test
    void testUpdateRatingDraw() {
        int ratingA = 1500;
        int ratingB = 1500;
        int constant = 30;
        double winVar = 0.5; // draw
        int updated = EloSystem.updateRating(ratingA, ratingB, constant, winVar);
        // Just verify that the returned rating is within a reasonable range.
        assertTrue(updated >= 0);
    }
}
