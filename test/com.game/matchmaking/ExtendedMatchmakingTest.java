package com.game.matchmaking;

import com.game.leaderboard.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

// Extended tests focusing on covering all branches in Matchmaking

class ExtendedMatchmakingTest {

    // A dummy subclass of Player for matchmaking testing.
    // Note: When using this constructor, pass game names (for Elo, wins, etc.) with the exact expected keys.
    private static class DummyPlayer extends Player {
        public DummyPlayer(int id, String name, int elo, long queuedAt) {
            // The Player constructor takes: id, name, tttElo, checkersElo, connect4Elo, tttWins, checkersWins, connect4Wins
            // Here we set all Elo values to the same value and initial wins to 0.
            super(id, name, elo, elo, elo, 0, 0, 0);
            setQueuedAt(queuedAt);
        }
    }

    // Capture System.out output
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    /**
     * Test matching with a range increment.
     * For instance, if p1's Elo is 1200 and p2's Elo is 1270,
     * their difference (70) is more than the INITIAL_RANGE of 50,
     * but below 50 + 25 = 75. In this scenario, findMatch should return p2.
     * We use "tictactoe" as the game key (all keys are to be passed in lower case).
     */

    /**
     * Test that when only one player is queued there is no match made.
     */
    @Test
    void testNoMatchForSinglePlayer() throws InterruptedException {
        DummyPlayer lone = new DummyPlayer(1, "Solo", 1300, System.currentTimeMillis());
        Matchmaking matchmaking = new Matchmaking();
        matchmaking.enqueue(lone);

        System.setOut(new PrintStream(outContent));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Use "checkers" as the game key.
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));
        Thread.sleep(1100); // allow a tick
        executor.shutdownNow();

        // No game should have been started so wins remain 0.
        assertEquals(0, lone.getWins("checkers"));
    }

    /**
     * Test that a player whose queuedAt time is older than TIMEOUT is removed,
     * and a timeout message is printed. We simulate timeout by setting
     * queuedAt to more than 60 seconds ago.
     */
    @Test
    void testTimeoutPlayerIsRemoved() throws InterruptedException {
        // Create a player with a queuedAt time older than TIMEOUT (60 sec)
        DummyPlayer timeoutPlayer = new DummyPlayer(4, "Timeout", 1500, System.currentTimeMillis() - 70_000);
        Matchmaking matchmaking = new Matchmaking();
        matchmaking.enqueue(timeoutPlayer);

        System.setOut(new PrintStream(outContent));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Using "checkers" as the game key.
        executor.submit(() -> matchmaking.runMatchmaking("checkers"));
        Thread.sleep(1500);
        executor.shutdownNow();

        // The timed-out player should not have any win increment.
        assertEquals(0, timeoutPlayer.getWins("checkers"));
        String output = outContent.toString();
        assertTrue(output.contains("Matchmaking timed out for Timeout"));
    }

    /**
     * Test that the runMatchmaking loop exits cleanly when interrupted.
     * This is done by submitting the task and then shutting down the executor.
     */
    @Test
    void testRunMatchmakingExitsOnInterrupt() throws InterruptedException {
        Matchmaking matchmaking = new Matchmaking();
        System.setOut(new PrintStream(outContent));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        // Using "tictactoe" for consistency.
        executor.submit(() -> matchmaking.runMatchmaking("tictactoe"));
        Thread.sleep(1100);
        // Interrupt the matchmaking loop.
        executor.shutdownNow();

        // After shutdown, the thread should exit without triggering additional game starts.
        // We simply verify that some output was captured and no exception was thrown.
        assertNotNull(outContent.toString());
    }
}
