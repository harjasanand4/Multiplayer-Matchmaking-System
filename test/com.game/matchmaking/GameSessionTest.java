// File: src/test/java/com/game/matchmaking/GameSessionTest.java
package com.game.matchmaking;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    void testGameSessionLifecycle() {
        GameSession session = new GameSession("Alice", "Bob");
        assertEquals("Alice", session.getPlayer1());
        assertEquals("Bob", session.getPlayer2());
        assertTrue(session.isGameActive());

        // Capture system output when ending the session
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        session.endSession("Alice");
        assertFalse(session.isGameActive());
        String output = outContent.toString();
        assertTrue(output.contains("Game over! Winner: Alice"));

        // Restore original standard output
        System.setOut(originalOut);
    }
}
