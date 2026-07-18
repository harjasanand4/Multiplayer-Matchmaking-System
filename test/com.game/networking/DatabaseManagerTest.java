package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Tests covering the entire com.game.networking.databasemanager class
 */
public class DatabaseManagerTest {

    private DatabaseStub databaseStub;
    private DatabaseManager databaseManager;
    private Player player1;
    private Player player2;
    private GameState gameState;

    /**
     * Sets up a databaseStub, databaseManager, gamestate, and players objects before all tests are run
     */
    @BeforeEach
    public void setUp() {
        databaseStub = new DatabaseStub();
        databaseManager = new DatabaseManager(databaseStub);
        player1 = new Player("654321", "Alice", "alice@gmail.com", "password123", null);
        player2 = new Player("123456", "Bob", "bob@gmail.com", "password456", null);
        gameState = new GameState("session", GameType.TICTACTOE, player1, player2);
    }

    /**
     * Tests if you can save a player and retrieve a player from the database
     */
    @Test
    void testSavePlayerAndRetrievePlayer() {
        databaseManager.savePlayer(player1);
        Player retrieved = databaseStub.retrievePlayer(player1.getID());

        assertNotNull(retrieved);
        assertEquals("Alice", retrieved.getUsername());
    }

    /**
     * Tests if you can save a gamestate and retrieve a gamestate from the database
     */
    @Test
    void testSaveGameStateAndRetrieve() {
        databaseManager.saveGameState("session", gameState);

        GameState retrieved = databaseManager.getLatestGameState("session");

        assertNotNull(retrieved);
        assertEquals("session", retrieved.getSessionId());
        assertEquals(GameType.TICTACTOE, retrieved.getGameType());
    }

    /**
     * Tests if getting gameStates returns null if it does not exist
     */
    @Test
    void testGetGameStates() {
        ArrayList<GameState> states = databaseManager.getGameStates("NONEXISTENT");
        assertNull(states);
    }

    /**
     * Tests if you can save multiple gamestates and retrieves the LATEST game state added.
     */
    @Test
    void testSaveMultipleGameStates() {
        GameState gameState2 = new GameState("session", GameType.TICTACTOE, player1, player2);
        GameState gameState3 = new GameState("session", GameType.TICTACTOE, player1, player2);

        databaseManager.saveGameState("session", gameState);
        databaseManager.saveGameState("session", gameState2);
        databaseManager.saveGameState("session", gameState3);

        GameState latest = databaseManager.getLatestGameState("session");

        assertEquals(gameState3, latest);
    }

    @Test
    void testLoadGameStates(){

    }

}
