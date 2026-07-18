package com.game.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class acts as a simple database for storing user information.
 * It does not use a real database. Instead, it stores users in memory.
 * This is only for testing. A real database should be used later.
 * @author: Boya Liu, Maneet Singh,
 * @email: boya.liu@ucalgary.ca, maneet.singh1@ucalgary.ca,
 */

public class DatabaseStub {

    // This HashMap stores usernames and their corresponding User objects.
    private HashMap<String, User> users;

    /**
     * Constructor method.
     * This method initializes an empty database.
     */
    public DatabaseStub() {
        users = new HashMap<>(); // Create an empty HashMap
    }

    /**
     * This method adds a user to the database.
     *
     * @param user The user object to store in the database.
     */
    public void addUser(User user) {
        users.put(user.getUsername(), user); // Store user in the HashMap
    }

    /**
     * This method retrieves a user from the database.
     * If the user is not found, it returns null.
     *
     * @param username The username to look up.
     * @return The User object if found, or null if not found.
     */
    public User getUser(String username) {
        return users.get(username);
    }

    /**
     * This method removes a user from the database.
     *
     * @param username The username of the user to remove.
     * @return true if the user was removed, false otherwise.
     */
    public boolean deleteUser(String username) {
        return users.remove(username) != null;
    }

    /**
     * This method updates a user's role.
     *
     * @param username The username of the user to update.
     * @param newRole  The new role to assign.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateUserRole(String username, String newRole) {
        User user = getUser(username);
        if (user != null) {
            user.setRole(newRole);
            return true;
        }
        return false;
    }

    /**
     * This method updates an existing user's information.
     * If the user exists, their data is updated.
     *
     * @param updatedUser The user object with updated information.
     * @return true if update was successful, false otherwise.
     */
    public boolean updateUser(User updatedUser) {
        if (users.containsKey(updatedUser.getUsername())) {
            users.put(updatedUser.getUsername(), updatedUser);
            return true;
        }
        return false;
    }

    /**
     * This method checks if email already exists in the database
     *
     * @param email email entered by the user
     * @return true if email found, false otherwise
     */
    public boolean emailExists(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
    return false;
    }
    /**
     * This method able to find the username associated with an email
     *
     * @param email email entered by the user
     * @return user associated with that email
     */
    public User getUserByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * This method retrieves all users in the database.
     * @return A list containing all users.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
