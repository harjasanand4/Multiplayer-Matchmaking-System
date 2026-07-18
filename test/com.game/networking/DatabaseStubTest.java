package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.databasestub class
 */
public class DatabaseStubTest {
    private DatabaseStub db;
    private Player player;
    private GameState testGameState;

    /**
     * Sets up a player and a gameState object to interact to use for testing
     */
    @BeforeEach
    public void setUp() {
        db = new DatabaseStub();
        player = new Player("player", "Alice", "alice@mail.com", "pass", null);
        testGameState = new GameState("session", GameType.TICTACTOE, player, player);
    }

    /**
     * Tests the getters and insertion function for players into database
     */
    @Test
    void testInsertAndRetrievePlayer() {
        db.insertPlayer("player", player);
        Player retrieved = db.retrievePlayer("player");

        assertNotNull(retrieved);
        assertEquals("player", retrieved.getID());
        assertEquals("Alice", retrieved.getUsername());
    }

    /**
     * Tests that the retrieved function returns null when retrieving a nonexistent player in the database
     */
    @Test
    void testRetrieveNonExistentPlayer() {
        Player retrieved = db.retrievePlayer("nonexistent");
        assertNull(retrieved);
    }

    /**
     * Tests the getters and insertion function for gamestates into database
     */
    @Test
    void testInsertAndRetrieveGameState() {
        db.insertGameState("session", testGameState);
        GameState retrieved = db.retrieveGameState("session");

        assertNotNull(retrieved);
        assertEquals("session", retrieved.getSessionId());
        assertEquals(GameType.TICTACTOE, retrieved.getGameType());
    }

    /**
     * Tests that the retrieved function returns null when retrieving a nonexistent gamestate in the database
     */
    @Test
    void testRetrieveNonExistentGameState() {
        GameState retrieved = db.retrieveGameState("NONEXISTENT");
        assertNull(retrieved);
    }

    /**
     * Tests the getters and insertion function for MULTIPLE gamestates into database
     */
    @Test
    void testInsertMultipleGameStates() {
        // Insert first game state
        db.insertGameState("session", testGameState);

        // Create and insert a second game state
        GameState secondGameState = new GameState("session", GameType.CHECKERS, player, player);
        db.insertGameState("session", secondGameState);

        //Retrieve the most recent one
        GameState retrieved = db.retrieveGameState("session");

        assertNotNull(retrieved);
        assertEquals(GameType.CHECKERS, retrieved.getGameType());
    }

    /**
     * Tests if the empty database returns null for calling nonexistent players and gamestates.
     */
    @Test
    void testEmptyDatabaseInitially() {
        assertNull(db.retrievePlayer("player"));
        assertNull(db.retrieveGameState("session"));
    }
}