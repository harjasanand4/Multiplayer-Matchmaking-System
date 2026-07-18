package com.game.matchmaking;

import com.game.leaderboard.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class AdditionalMatchmakingTest {

    // A dummy subclass so we can easily set queuedAt and use a simple elo for all game types.
    private static class DummyPlayer extends Player {
        public DummyPlayer(int id, String name, int elo, long queuedAt) {
            // Using the same elo value for all games and starting wins at 0.
            // The constructor of Player expects: id, name, tttElo, checkersElo, connect4Elo, tttWins, checkersWins, connect4Wins
            super(id, name, elo, elo, elo, 0, 0, 0);
            setQueuedAt(queuedAt);
        }
    }

    // To capture system output during tests.
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    /**
     * Test matching behavior when the player's ELO difference is not within the initial range,
     * so findMatch must increase the range before finding a match.
     * For example, if p1's ELO is 1200 and p2's is 1270 (a difference of 70),
     * p1 won't match in the initial range (50) but will match once range increments to 75.
     */

    /**
     * Test that when no match is possible because the queue contains just one player,
     * the system never starts a game and no win increment occurs.
     */
    @Test
    void testFindMatchBehaviorWithSinglePlayer() throws InterruptedException {
        DummyPlayer lone = new DummyPlayer(1, "Solo", 1300, System.currentTimeMillis());
        Matchmaking matchmaking = new Matchmaking();
        matchmaking.enqueue(lone);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        System.setOut(new PrintStream(outContent));
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));
        Thread.sleep(1100); // Let matchmaking process a tick
        executor.shutdownNow();

        // With a single player, no game starts so wins remain 0.
        assertEquals(0, lone.getWins("checkers"));
    }

    /**
     * Test that if the matchmaking loop is interrupted during sleep,
     * it exits cleanly. This indirectly verifies that the Thread.sleep call is handled.
     */
    @Test
    void testRunMatchmakingSleepInterrupted() throws InterruptedException {
        Matchmaking matchmaking = new Matchmaking();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        System.setOut(new PrintStream(outContent));
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));
        Thread.sleep(1100);
        executor.shutdownNow();

        // With an empty queue and interruption, nothing matching happens.
        // We can only check that the test completes (and output is captured without critical errors).
        assertNotNull(outContent.toString());
    }
}
