package com.game.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the functionality of ProfileManager.
 * It verifies leaderboard score updates, retrievals, and game history tracking.
 * These tests ensure the behavior is correct under different conditions.
 */
public class ProfileManagerTest {

    private ProfileManager profileManager;

    /**
     * This method runs before each test case to create a fresh ProfileManager.
     */
    @BeforeEach
    public void setUp() {
        profileManager = new ProfileManager(); // Initialize new ProfileManager before each test
    }

    /**
     * Test updating and retrieving a user's ranking.
     */
    @Test
    public void testUpdateAndGetUserRanking() {
        profileManager.updateRanking("Alice", 1500); // Set score
        int score = profileManager.getUserRanking("Alice"); // Get score
        assertEquals(1500, score); // Score should match
    }

    /**
     * Test getting a ranking for a user who has no score yet.
     */
    @Test
    public void testGetRankingForNonExistentUser() {
        int score = profileManager.getUserRanking("Ghost"); // User not added
        assertEquals(0, score); // Default score should be 0
    }

    /**
     * Test overwriting a user's existing ranking.
     */
    @Test
    public void testOverwriteRanking() {
        profileManager.updateRanking("Bob", 1000); // First score
        profileManager.updateRanking("Bob", 2000); // Update score
        assertEquals(2000, profileManager.getUserRanking("Bob")); // Should reflect updated score
    }

    /**
     * Test adding and retrieving game history for a user.
     */
    @Test
    public void testAddAndGetGameHistory() {
        profileManager.addGameHistory("Charlie", "Won against AI in 3 rounds"); // Save result
        String history = profileManager.getGameHistory("Charlie"); // Retrieve
        assertEquals("Won against AI in 3 rounds", history); // Should match saved result
    }

    /**
     * Test getting game history for a user who has none.
     */
    @Test
    public void testGetGameHistoryForNonExistentUser() {
        String history = profileManager.getGameHistory("UnknownUser"); // No history added
        assertEquals("No history available", history); // Should return default message
    }

    /**
     * Test overwriting previous game history for a user.
     */
    @Test
    public void testOverwriteGameHistory() {
        profileManager.addGameHistory("Dana", "Lost in sudden death");
        profileManager.addGameHistory("Dana", "Won flawless victory"); // Overwrite
        assertEquals("Won flawless victory", profileManager.getGameHistory("Dana")); // Should return updated
    }
}
