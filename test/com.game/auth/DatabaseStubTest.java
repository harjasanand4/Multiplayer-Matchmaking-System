package com.game.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test class checks all major functionalities of the DatabaseStub class.
 * It ensures that adding, retrieving, updating, deleting users, and email lookup
 * all behave correctly under different test scenarios.
 */
public class DatabaseStubTest {

    private DatabaseStub database;
    private User sampleUser;

    /**
     * This method runs before each test case.
     * It sets up a fresh instance of DatabaseStub and adds a sample user.
     */
    @BeforeEach
    public void setUp() {
        database = new DatabaseStub(); // Create a new database for each test
        sampleUser = new User("boya", "hashed:abc123", "boya@example.com", "user", "123456");
        database.addUser(sampleUser); // Add one user to work with
    }

    /**
     * This test checks whether a user can be added and then retrieved successfully.
     */
    @Test
    public void testAddAndRetrieveUser() {
        User retrieved = database.getUser("boya");
        assertNotNull(retrieved); // Ensure user is not null
        assertEquals("boya@example.com", retrieved.getEmail()); // Email should match
    }

    /**
     * This test checks the behavior when trying to retrieve a non-existent user.
     */
    @Test
    public void testGetNonExistingUser() {
        User result = database.getUser("bob");
        assertNull(result); // No such user, should return null
    }

    /**
     * This test checks whether a user can be successfully deleted.
     */
    @Test
    public void testDeleteUser() {
        boolean deleted = database.deleteUser("boya");
        assertTrue(deleted); // Deletion should succeed

        User result = database.getUser("boya");
        assertNull(result); // User should no longer exist
    }

    /**
     * This test checks the delete method for a user that does not exist.
     */
    @Test
    public void testDeleteNonExistingUser() {
        boolean deleted = database.deleteUser("charlie");
        assertFalse(deleted); // Deletion should fail
    }

    /**
     * This test checks if updating a user’s role works correctly.
     */
    @Test
    public void testUpdateUserRole() {
        boolean updated = database.updateUserRole("boya", "admin");
        assertTrue(updated); // Role update should succeed

        assertEquals("admin", database.getUser("boya").getRole()); // Role should change
    }

    /**
     * This test checks if trying to update role of a non-existent user fails.
     */
    @Test
    public void testUpdateUserRoleNonExistent() {
        boolean updated = database.updateUserRole("ghost", "admin");
        assertFalse(updated); // Update should fail
    }

    /**
     * This test verifies the updateUser method actually updates user information.
     */
    @Test
    public void testUpdateUserInfo() {
        User updatedUser = new User("boya", "newHash:xyz789", "boya@example.com", "user", "654321");
        boolean success = database.updateUser(updatedUser);
        assertTrue(success); // Update should succeed

        assertEquals("newHash:xyz789", database.getUser("boya").getPasswordHash()); // Hash should update
    }

    /**
     * This test checks that updateUser fails for a user not in database.
     */
    @Test
    public void testUpdateUserNotFound() {
        User nonExistent = new User("ghost", "pass:123", "ghost@mail.com", "user", "000000");
        boolean result = database.updateUser(nonExistent);
        assertFalse(result); // Should fail to update
    }

    /**
     * This test checks the emailExists method for an existing email.
     */
    @Test
    public void testEmailExistsTrue() {
        assertTrue(database.emailExists("boya@example.com")); // Should return true
    }

    /**
     * This test checks emailExists method for an email that does not exist.
     */
    @Test
    public void testEmailExistsFalse() {
        assertFalse(database.emailExists("nobody@nowhere.com")); // Should return false
    }

    /**
     * This test checks retrieval of a user by email.
     */
    @Test
    public void testGetUserByEmail() {
        User user = database.getUserByEmail("boya@example.com");
        assertNotNull(user); // Should find the user
        assertEquals("boya", user.getUsername()); // Username must match
    }

    /**
     * This test ensures getUserByEmail returns null for unknown email.
     */
    @Test
    public void testGetUserByEmailNotFound() {
        User user = database.getUserByEmail("noone@notfound.com");
        assertNull(user); // Should not find anything
    }

    /**
     * This test checks the retrieval of all users.
     */
    @Test
    public void testGetAllUsers() {
        assertEquals(1, database.getAllUsers().size()); // Only one user in DB
    }
}
