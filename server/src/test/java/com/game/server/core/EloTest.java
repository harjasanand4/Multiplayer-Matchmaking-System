package com.game.server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EloTest {

    @Test
    void expectedScoreMatchesTheTextbookValues() {
        assertEquals(0.5, Elo.expectedScore(1500, 1500), 1e-12);
        assertEquals(0.6401, Elo.expectedScore(1600, 1500), 1e-4);
        assertEquals(0.9091, Elo.expectedScore(1900, 1500), 1e-4);  // 400 points: 10 to 1
        assertEquals(1.0, Elo.expectedScore(1600, 1500) + Elo.expectedScore(1500, 1600), 1e-12);
    }

    @Test
    void updatesByKTimesSurprise() {
        assertEquals(1516, Elo.updated(1500, 1500, 1.0, 32));
        assertEquals(1484, Elo.updated(1500, 1500, 0.0, 32));
        assertEquals(1500, Elo.updated(1500, 1500, 0.5, 32));
        // Favourite wins: small gain. Underdog wins: big gain.
        assertEquals(1611, Elo.updated(1600, 1500, 1.0, 30));
        assertEquals(1519, Elo.updated(1500, 1600, 1.0, 30));
    }

    @Test
    void ratingPointsAreConservedBetweenEqualKPlayers() {
        for (int a = 900; a <= 2100; a += 150) {
            for (int b = 900; b <= 2100; b += 150) {
                final int newA = Elo.updated(a, b, 1.0, 24);
                final int newB = Elo.updated(b, a, 0.0, 24);
                assertTrue(Math.abs((newA + newB) - (a + b)) <= 1, "rounding only, a=" + a + " b=" + b);
            }
        }
    }

    @Test
    void provisionalPlayersMoveFaster() {
        assertEquals(Elo.K_PROVISIONAL, Elo.kFactor(0));
        assertEquals(Elo.K_PROVISIONAL, Elo.kFactor(Elo.PROVISIONAL_GAMES - 1));
        assertEquals(Elo.K_ESTABLISHED, Elo.kFactor(Elo.PROVISIONAL_GAMES));
    }

    @Test
    void rejectsImpossibleScores() {
        assertThrows(IllegalArgumentException.class, () -> Elo.updated(1500, 1500, 1.5, 32));
    }
}
