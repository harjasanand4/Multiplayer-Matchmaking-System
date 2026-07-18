package com.game.auth;

import java.util.HashMap;

/**
 * This class manages user profiles.
 * It keeps track of user rankings and game history.
 * This is a simple version for testing. More features will be added later.
 * @author: Boya Liu,
 * @email: boya.liu@ucalgary.ca,
 */

class ProfileManager {

    // Stores the ranking of each user (username -> score)
    private HashMap<String, Integer> leaderboard;

    // Stores the game history of each user (username -> last game result)
    private HashMap<String, String> gameHistory;

    /**
     * Constructor method.
     * This method initializes empty ranking and game history records.
     */
    public ProfileManager() {
        leaderboard = new HashMap<>(); // Creates an empty leaderboard
        gameHistory = new HashMap<>(); // Creates an empty game history
    }

    /**
     * This method updates the ranking of a user.
     * If the user already has a score, it will be replaced.
     *
     * @param username The name of the player whose ranking is being updated.
     * @param newScore The new ranking score for the player.
     */
    public void updateRanking(String username, int newScore) {
        leaderboard.put(username, newScore); // Save the new score
    }

    /**
     * This method gets the ranking of a user.
     * If the user does not exist, it returns a default score of 0.
     *
     * @param username The name of the player whose score is needed.
     * @return The score of the player, or 0 if the user is not found.
     */
    public int getUserRanking(String username) {
        if (leaderboard.containsKey(username)) { // Checks if the user exists
            return leaderboard.get(username); // Returns the user's score
        }
        return 0; // Default value if user is not found
    }

    /**
     * This method saves a game result for a user.
     * It replaces any previous game result for that user.
     *
     * @param username The name of the player.
     * @param result A short text describing the game outcome.
     */
    public void addGameHistory(String username, String result) {
        gameHistory.put(username, result); // Save the game result
    }

    /**
     * This method retrieves the last game result of a user.
     * If no game history is available, it returns a default message.
     *
     * @param username The name of the player.
     * @return The last recorded game result, or a default message if no history exists.
     */
    public String getGameHistory(String username) {
        if (gameHistory.containsKey(username)) { // Checks if the user has history
            return gameHistory.get(username); // Returns the last recorded game result
        }
        return "No history available"; // Default message when no game history exists
    }
}
