package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.gamestate class
 */
class GameStateTest {
    private static final String SESSION_ID = "test-session-123";
    private Player player1;
    private Player player2;
    private GameState connect4;
    private GameState checkers;
    private GameState ticTacToe;

    /**
     * Loads players and gameStates with data that can be tested on
     */
    @BeforeEach
    void setUp() {
        player1 = new Player("player1", "Alice", "alice@example.com", "pass", null);
        player2 = new Player("player2", "Bob", "bob@example.com", "pass", null);
        connect4 = new GameState("123", GameType.CONNECT4, player1, player2);
        checkers = new GameState("123", GameType.CHECKERS, player1, player2);
        ticTacToe = new GameState("123", GameType.TICTACTOE, player1, player2);
    }

    /**
     * Tests if getters work and constructor works
     */
    @Test
    void testCreatingGameState()  {
        ticTacToe = new GameState("123", GameType.TICTACTOE, player1, player2);
        assertEquals(GameType.TICTACTOE, ticTacToe.getGameType());
        assertEquals("123", ticTacToe.getSessionId());
        assertEquals(player1, ticTacToe.getPlayer1());
        assertEquals(player2, ticTacToe.getPlayer2());
    }
}