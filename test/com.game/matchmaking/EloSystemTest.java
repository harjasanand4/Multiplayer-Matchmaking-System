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

    @Test
    void testExactValuesAgainstTheEloFormula() {
        // Expected score of a 100-point favourite is 0.640, so a win gains K * 0.360.
        assertEquals(1611, EloSystem.updateRating(1600, 1500, 30, 1.0));
        assertEquals(1519, EloSystem.updateRating(1500, 1600, 30, 1.0));
        // Equal ratings: a win is worth half of K, a draw nothing.
        assertEquals(1516, EloSystem.updateRating(1500, 1500, 32, 1.0));
        assertEquals(1500, EloSystem.updateRating(1500, 1500, 32, 0.5));
    }

    @Test
    void testFavouriteLosingCostsMoreThanUnderdogLosing() {
        int favouriteLoss = 1600 - EloSystem.updateRating(1600, 1500, 30, 0.0);
        int underdogLoss = 1500 - EloSystem.updateRating(1500, 1600, 30, 0.0);
        assertTrue(favouriteLoss > underdogLoss);
        assertEquals(30, favouriteLoss + underdogLoss);  // the two expected scores add up to 1
    }
}
