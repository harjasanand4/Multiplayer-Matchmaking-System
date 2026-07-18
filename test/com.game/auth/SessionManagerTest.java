package com.game.auth;

import com.game.auth.session.SessionManager;
import com.game.auth.session.SessionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    private SessionManager sessionManager;
    private final String username = "testUser";
    private final String sessionToken = "token123";


    // login a dummy user
    @BeforeEach
    public void setup() {
        sessionManager = new SessionManager();
        sessionManager.loginUser(username, sessionToken);
    }


    /**
     * Test to check if the prompt is shown after 30 minutes of inactivity.
     */
    @Test
    public void testPromptAfter30Minutes() {
        SessionData session = sessionManager.getActiveSessions().get(username).get(sessionToken);
        session.setLastActiveTime(System.currentTimeMillis() - (31 * 60 * 1000)); // 31 minutes ago

        List<String> prompts = sessionManager.getSessionsToPrompt();
        assertTrue(prompts.contains(username + ":" + sessionToken));
    }

    /**
     * Test to check if the prompt is not shown before 30 minutes of inactivity.
     */

    @Test
    public void testNoPromptBefore30Minutes() {
        SessionData session = sessionManager.getActiveSessions().get(username).get(sessionToken);
        session.setLastActiveTime(System.currentTimeMillis() - (29 * 60 * 1000)); // 29 minutes ago

        List<String> prompts = sessionManager.getSessionsToPrompt();
        assertFalse(prompts.contains(username + ":" + sessionToken));
    }

    /**
     * Test to check if the session expires after 35 minutes of inactivity.
     */
    @Test
    public void testSessionExpiresAfter35MinutesOfInactivity() {
        SessionData session = sessionManager.getActiveSessions().get(username).get(sessionToken);
        session.setLastActiveTime(System.currentTimeMillis() - (36 * 60 * 1000)); // 36 minutes ago
        session.setPromptShown(true); // Simulate prompt shown

        sessionManager.expireInactiveSessions();

        assertFalse(sessionManager.isUserLoggedIn(username));
    }

    /**
     * Test to check if an active session is not expired.
     */
    @Test
    public void testActiveSessionIsNotExpired() {
        SessionData session = sessionManager.getActiveSessions().get(username).get(sessionToken);
        session.setLastActiveTime(System.currentTimeMillis()); // Now

        sessionManager.expireInactiveSessions();

        assertTrue(sessionManager.isUserLoggedIn(username));
    }

    /**
     * This test verifies that when a user logs in using loginUser(),
     * their session is properly stored in the activeSessions map.
     * It checks that both the username and session token are recorded.
     */
    @Test
    public void testLoginUserStoresSessionData() {
        SessionManager sessionManager = new SessionManager();
        String username = "user1";
        String sessionToken = "tokenABC";

        sessionManager.loginUser(username, sessionToken);

        assertTrue(sessionManager.getActiveSessions().containsKey(username));
        assertTrue(sessionManager.getActiveSessions().get(username).containsKey(sessionToken));
    }

    /**
     * This test ensures that logoutUser() successfully removes the specified session token
     * and removes the user entirely from activeSessions if no sessions remain.
     */
    @Test
    public void testLogoutUserRemovesSession() {
        SessionManager sessionManager = new SessionManager();
        String username = "user1";
        String sessionToken = "tokenABC";

        sessionManager.loginUser(username, sessionToken);
        sessionManager.logoutUser(username, sessionToken);

        assertFalse(sessionManager.isUserLoggedIn(username)); // User should be logged out
    }

    /**
     * This test ensures that logoutUser() only removes the specified session token
     * and retains the user's other active sessions.
     */
    @Test
    public void testLogoutUserKeepsOtherSessions() {
        SessionManager sessionManager = new SessionManager();
        String username = "user1";
        String sessionToken1 = "tokenABC";
        String sessionToken2 = "tokenXYZ";

        sessionManager.loginUser(username, sessionToken1);
        sessionManager.loginUser(username, sessionToken2);

        sessionManager.logoutUser(username, sessionToken1);

        assertTrue(sessionManager.isUserLoggedIn(username)); // User should still be logged in
        assertNull(sessionManager.getActiveSessions().get(username).get(sessionToken1)); // Session 1 should be removed
        assertNotNull(sessionManager.getActiveSessions().get(username).get(sessionToken2)); // Session 2 should still exist
    }

    /**
     * This test verifies that isUserLoggedIn() returns true after a user logs in.
     */
    @Test
    public void testIsUserLoggedInTrue() {
        SessionManager sessionManager = new SessionManager();
        String username = "user123";
        String sessionToken = "tokenABC";

        sessionManager.loginUser(username, sessionToken); // Login creates an active session

        assertTrue(sessionManager.isUserLoggedIn(username)); // Should return true
    }

    /**
     * This test verifies that isUserLoggedIn() returns false for users who have never logged in.
     */
    @Test
    public void testIsUserLoggedInFalseForNewUser() {
        SessionManager sessionManager = new SessionManager();

        assertFalse(sessionManager.isUserLoggedIn("ghostUser")); // No sessions created
    }

    /**
     * This test verifies that isUserLoggedIn() returns false after all of a user's sessions are removed.
     */
    @Test
    public void testIsUserLoggedInFalseAfterLogout() {
        SessionManager sessionManager = new SessionManager();
        String username = "user456";
        String sessionToken = "tokenXYZ";

        sessionManager.loginUser(username, sessionToken);
        sessionManager.logoutUser(username, sessionToken); // Logging out should remove last session

        assertFalse(sessionManager.isUserLoggedIn(username)); // Should return false now
    }

    /**
     * This test verifies that forceLogoutUser() removes all sessions for a given user.
     */
    @Test
    public void testForceLogoutUserRemovesAllSessions() {
        SessionManager sessionManager = new SessionManager();
        String username = "user123";

        // Simulate multiple active sessions
        sessionManager.loginUser(username, "token1");
        sessionManager.loginUser(username, "token2");

        sessionManager.forceLogoutUser(username); // Force logout should remove all sessions

        assertFalse(sessionManager.isUserLoggedIn(username)); // Should return false
    }

    /**
     * This test verifies that forceLogoutUser() does nothing if the user has no sessions.
     */
    @Test
    public void testForceLogoutUserWhenUserNotLoggedIn() {
        SessionManager sessionManager = new SessionManager();

        // No sessions exist for this user
        sessionManager.forceLogoutUser("ghostUser"); // Should not throw any errors

        assertFalse(sessionManager.isUserLoggedIn("ghostUser")); // Still false
    }

    /**
     * This test verifies that forceLogoutAllUsers() removes all sessions for all users.
     */
    @Test
    public void testForceLogoutAllUsers() {
        SessionManager sessionManager = new SessionManager();

        // Simulate multiple users with active sessions
        sessionManager.loginUser("user1", "token1");
        sessionManager.loginUser("user2", "token2");
        sessionManager.loginUser("user3", "token3");

        sessionManager.forceLogoutAllUsers(); // Should remove all sessions

        // All users should now be logged out
        assertFalse(sessionManager.isUserLoggedIn("user1"));
        assertFalse(sessionManager.isUserLoggedIn("user2"));
        assertFalse(sessionManager.isUserLoggedIn("user3"));
    }

    /**
     * This test verifies that calling forceLogoutAllUsers() when no users are logged in
     * does not cause any errors.
     */
    @Test
    public void testForceLogoutAllUsersOnEmptySessionMap() {
        SessionManager sessionManager = new SessionManager();

        // No users have logged in
        sessionManager.forceLogoutAllUsers(); // Should safely do nothing

        // Still expect no users to be logged in
        assertFalse(sessionManager.isUserLoggedIn("anyUser"));
    }

    /**
     * This test verifies that handleUserDeletion() correctly removes all sessions for the specified user.
     */
    @Test
    public void testHandleUserDeletion_RemovesUserSessions() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.loginUser("userToDelete", "tokenA");
        sessionManager.loginUser("userToDelete", "tokenB");

        sessionManager.handleUserDeletion("userToDelete");

        // The user should be fully logged out
        assertFalse(sessionManager.isUserLoggedIn("userToDelete"));
    }

    /**
     * This test verifies that calling handleUserDeletion() for a user that doesn't exist
     * does not throw an error or modify the session map.
     */
    @Test
    public void testHandleUserDeletion_NonExistentUser() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.loginUser("existingUser", "tokenX");

        sessionManager.handleUserDeletion("nonExistentUser");

        // The existing user should still be logged in
        assertTrue(sessionManager.isUserLoggedIn("existingUser"));
    }

    /**
     * This test verifies that updateActivity() updates the last active time
     * for a session when the username and session token are valid.
     */
    @Test
    public void testUpdateActivity_ValidSession() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.loginUser("activeUser", "session123");

        // Manually set an old last active time
        SessionData session = sessionManager.getActiveSessions().get("activeUser").get("session123");
        long oldTime = System.currentTimeMillis() - (10 * 60 * 1000); // 10 minutes ago
        session.setLastActiveTime(oldTime);

        // Call updateActivity
        sessionManager.updateActivity("activeUser", "session123");

        // Ensure the new last active time is more recent than the old one
        long updatedTime = session.getLastActiveTime();
        assertTrue(updatedTime > oldTime);
    }

    /**
     * This test checks that updateActivity() does not throw an exception
     * when called with a non-existent username.
     */
    @Test
    public void testUpdateActivity_InvalidUsername() {
        SessionManager sessionManager = new SessionManager();

        // No users have been logged in yet
        // Should not throw exception even though user doesn't exist
        assertDoesNotThrow(() -> sessionManager.updateActivity("ghostUser", "token999"));
    }

    /**
     * This test checks that updateActivity() does not update anything
     * when the session token does not exist for a valid user.
     */
    @Test
    public void testUpdateActivity_InvalidSessionToken() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.loginUser("validUser", "validToken");

        // Should not throw exception even though token is wrong
        assertDoesNotThrow(() -> sessionManager.updateActivity("validUser", "wrongToken"));

        // Confirm the original session is still valid
        assertTrue(sessionManager.getActiveSessions().get("validUser").containsKey("validToken"));
    }

    /**
     * This test checks that getActiveSessions() returns a non-null map,
     * even if no users have logged in yet.
     */
    @Test
    public void testGetActiveSessions_EmptyMap() {
        SessionManager sessionManager = new SessionManager();

        // Expect an empty but non-null map
        java.util.Map<String, java.util.HashMap<String, SessionData>> sessions = sessionManager.getActiveSessions();
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    /**
     * This test verifies that getActiveSessions() correctly reflects active user sessions.
     */
    @Test
    public void testGetActiveSessions_WithLoggedInUser() {
        SessionManager sessionManager = new SessionManager();
        sessionManager.loginUser("userA", "tokenA1");

        // Retrieve the sessions map
        java.util.Map<String, java.util.HashMap<String, SessionData>> sessions = sessionManager.getActiveSessions();

        // The map should contain "userA" and their session
        assertTrue(sessions.containsKey("userA"));
        assertTrue(sessions.get("userA").containsKey("tokenA1"));

        // The session data should not be null
        SessionData sessionData = sessions.get("userA").get("tokenA1");
        assertNotNull(sessionData);
    }

    /**
     * This test verifies that the singleton instance of SessionManager is the same
     * across multiple calls to getInstance().
     */
    @Test
    public void testSingletonInstanceIsSame() {
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();
        assertSame(instance1, instance2, "Both instances should refer to the same object");
    }

    /**
     * This test verifies that getCurrentUsername() returns the most recently logged-in user's username.
     */
    @Test
    public void testGetCurrentUsername() {
        SessionManager manager = SessionManager.getInstance();
        manager.forceLogoutAllUsers(); // Clear previous state

        manager.loginUser("user123", "token123");

        assertEquals("user123", manager.getCurrentUsername(), "Should return the last logged-in username");
    }

    /**
     * This test verifies that getCurrentSessionToken() returns the session token from the most recent login.
     */
    @Test
    public void testGetCurrentSessionToken() {
        SessionManager manager = SessionManager.getInstance();
        manager.forceLogoutAllUsers(); // Clear previous state

        manager.loginUser("user123", "token123");

        assertEquals("token123", manager.getCurrentSessionToken(), "Should return the last session token used");
    }

}
