package com.game.auth.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class manages user sessions.
 * It tracks logged-in users and allows session control.
 * This version supports multiple devices per user.
 * Admins can also log out other users.
 * @author: Boya Liu, Thomas Tabur
 * @email: boya.liu@ucalgary.ca, thomas.tabur@ucalgary.ca
 */

public class SessionManager {

    // This HashMap stores active users and their session tokens, along with their metadata
    // Each user can have multiple active sessions (for different devices).
    private HashMap<String, HashMap<String, SessionData>> activeSessions;
    private String currentUsername;
    private String currentSessionToken;

    private static SessionManager instance;

    public String getCurrentUsername() {
        // Return the first logged-in username (for simplicity)
//        if (!activeSessions.isEmpty()) {
//            return activeSessions.keySet().iterator().next();
//        }
        return currentUsername;
    }

    public String getCurrentSessionToken() {
        return currentSessionToken;
    }

    /**
     * Constructor method.
     * Initializes an empty session manager.
     */
    public SessionManager() {
        activeSessions = new HashMap<>(); // Create an empty session tracking system
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {  // Thread-safe initialization
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }
    /**
     * This method logs in a user and assigns them a session token.
     * The session token is needed for tracking multiple logins per user.
     *
     * @param username     The username of the user logging in.
     * @param sessionToken A unique token identifying this session.
     */
    public void loginUser(String username, String sessionToken) {
        // Initialize a new session map if the user doesn't have one
        if (!activeSessions.containsKey(username)) {
            activeSessions.put(username, new HashMap<>());
        }

        // Create session metadata
        long currentTime = System.currentTimeMillis(); // store the current Time using a UNIX timestamp
        SessionData sessionData = new SessionData(currentTime);

        // Add the session token and its data
        activeSessions.get(username).put(sessionToken, sessionData);
        this.currentUsername = username;
        this.currentSessionToken = sessionToken;
    }

    /**
     * This method logs out a user from a specific session.
     * If no sessions remain, the user is fully logged out.
     *
     * @param username     The username of the user logging out.
     * @param sessionToken The session token to remove.
     */
    public void logoutUser(String username, String sessionToken) {
        if (activeSessions.containsKey(username)) {
            HashMap<String, SessionData> sessions = activeSessions.get(username);
            sessions.remove(sessionToken); // Remove the specific session

            // If no sessions remain, remove the user from the tracking list
            if (sessions.isEmpty()) {
                activeSessions.remove(username);
            }
        }
    }

    /**
     * This method checks if a user is currently logged in.
     * A user is considered logged in if they have at least one active session.
     *
     * @param username The username to check.
     * @return true if the user has an active session, false otherwise.
     */
    public boolean isUserLoggedIn(String username) {
        return activeSessions.containsKey(username) && !activeSessions.get(username).isEmpty();
    }

    /**
     * This method logs out all sessions for a given user.
     * It is useful when a user deletes their account or an admin forces a logout.
     *
     * @param username The username to log out completely.
     */
    public void forceLogoutUser(String username) {
        activeSessions.remove(username); // Remove all active sessions for the user
    }

    /**
     * This method forces all users to log out.
     * It is useful for system maintenance or security incidents.
     */
    public void forceLogoutAllUsers() {
        activeSessions.clear(); // Remove all active sessions
    }

    /**
     * This method removes a user’s sessions when their account is deleted.
     * It ensures that deleted users are not still logged in.
     *
     * @param username The username of the deleted account.
     */
    public void handleUserDeletion(String username) {
        activeSessions.remove(username); // Remove sessions when the user is deleted
    }

    /**
     * Updates the last active time for a specific session
     *
     * @param username
     * @param sessionToken
     */
    public void updateActivity(String username, String sessionToken) {
        if (activeSessions.containsKey(username)) {
            HashMap<String, SessionData> sessions = activeSessions.get(username);
            if (sessions.containsKey(sessionToken)) {
                sessions.get(sessionToken).setLastActiveTime(System.currentTimeMillis());
            }
        }
    }

    /**
     * Identifies sessions that have been inactive for 30 minutes and should receive
     * an inactivity prompt. This method is intended to be polled by the frontend.
     *
     * The frontend should use this list to display a "Are you still there?" message.
     * If the user interacts, it should call updateActivity(); otherwise, after 5 more minutes,
     * the system will expire the session.
     *
     * @return A list of session identifiers (formatted as "username:sessionToken") that should be prompted.'
     *
     * @author: Thomas Tabur
     * @email: thomas.tabur@ucalgary.ca
     */
    public List<String> getSessionsToPrompt() {
        List<String> sessionsToPrompt = new ArrayList<>(); // Create a list to store sessions to prompt
        long now = System.currentTimeMillis(); // Get the current time
        long PROMPT_TIMEOUT = 30 * 60 * 1000; // 30 minutes

        for (String username : activeSessions.keySet()) {
            HashMap<String, SessionData> sessions = activeSessions.get(username); // Get the user's sessions
            for (Map.Entry<String, SessionData> entry : sessions.entrySet()) {
                String sessionToken = entry.getKey(); // Get the session token
                SessionData session = entry.getValue(); // Get the session data

                if (!session.isPromptShown() && now - session.getLastActiveTime() > PROMPT_TIMEOUT) {
                    session.setPromptShown(true); // Mark that we’ve shown the prompt
                    sessionsToPrompt.add(username + ":" + sessionToken); // Add this session to the list
                    // Frontend should now prompt this session
                }
            }
        }

        return sessionsToPrompt;
    }

    /**
     * Expires sessions that have been inactive for a total of 35 minutes:
     * 30 minutes of inactivity, followed by 5 minutes without user response
     * after the inactivity prompt was shown.
     *
     * This method should be called periodically (e.g., on a schedule or after user actions).
     * It removes expired sessions and fully logs out users with no remaining sessions.
     *
     * @author: Thomas Tabur
     * @email: thomas.tabur@ucalgary.ca
     */
    public void expireInactiveSessions() {
        long now = System.currentTimeMillis();
        long TOTAL_TIMEOUT = 35 * 60 * 1000; // 35 minutes
        Set<String> usersToRemove = new HashSet<>(); // Store users to remove entirely

        for (String username : activeSessions.keySet()) {
            HashMap<String, SessionData> sessions = activeSessions.get(username); // Get the user's sessions
            Set<String> expiredTokens = new HashSet<>(); // Store expired session tokens

            for (Map.Entry<String, SessionData> entry : sessions.entrySet()) {
                String sessionToken = entry.getKey(); // Get the session token
                SessionData session = entry.getValue(); // Get the session data

                if (session.isPromptShown() && now - session.getLastActiveTime() > TOTAL_TIMEOUT) {
                    expiredTokens.add(sessionToken); // Session is expired
                }
            }

            // Remove expired sessions
            for (String token : expiredTokens) {
                sessions.remove(token);
            }

            // If the user has no remaining sessions, remove them entirely
            if (sessions.isEmpty()) {
                usersToRemove.add(username);
            }
        }

        // Remove users with no active sessions
        for (String user : usersToRemove) {
            activeSessions.remove(user);
        }
    }

    /**
     * Provides read access to the activeSessions map.
     * Useful for testing or inspection of session state.
     *
     * @return A map of usernames to their active session tokens and session data.
     */
    public Map<String, HashMap<String, SessionData>> getActiveSessions() {
        return activeSessions;
    }



}


/**
 * Future improvements:
 * - Store session data in a real database instead of a HashMap.
 * - Implement session expiration for better security.
 * - Improve session token security (e.g., JWT or encrypted tokens).
 * - Log session activities for monitoring purposes.
 */
