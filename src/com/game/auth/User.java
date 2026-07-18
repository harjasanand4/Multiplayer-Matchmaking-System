package com.game.auth;

/**
 * This class represents a user in the system.
 * It stores the username, password, email, role, and two-factor authentication code.
 * This is a basic version. More features may be added later.
 * @author: Boya Liu, Maneet Singh,
 * @email: boya.liu@ucalgary.ca, maneet.singh1@ucalgary.ca,
 */

public class User {

    // The username of the user
    private String username;

    // The hashed password of the user (stored as "hash:salt")
    private String passwordHash;

    // The email of the user (used for authentication)
    private String email;

    // The role of the user ("admin" or "user")
    private String role;

    // The two-factor authentication (2FA) code for added security
    private String twoFactorCode;

    // Chosen Avatar icon of the user (Has a default)
    private String avatarUrl;

    public boolean hasRememberMeEnabled() {
        return rememberMe;
    }

    // Indicates whether the user is currently suspended (banned from logging in)
    private boolean suspended;

    // Provides an Indication whether the user account is locked
    private boolean lockOut;
    private boolean rememberMe;

    public User(){
        // no argument constructor for the saving the data into file
    }
    /**
     * Constructor method.
     * This method initializes a user with a username, password, email, role, and 2FA code.
     *
     * @param username      The chosen username of the user.
     * @param passwordHash  The hashed password stored securely.
     * @param email         The email address associated with the user.
     * @param role          The user's role in the system ("admin" or "user").
     * @param twoFactorCode The code used for two-factor authentication.
     */
    public User(String username, String passwordHash, String email, String role, String twoFactorCode) {
        this.username = username; // Save username
        this.passwordHash = passwordHash; // Save password hash
        this.email = email; // Save email
        this.role = role; // Save user role
        this.twoFactorCode = twoFactorCode; // Save 2FA code
        this.suspended = false; // default the suspension status to false (user is not banned initially)
        this.lockOut = false; // sets the default lockout state as initially false
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String newPasswordHash) {
        passwordHash = newPasswordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String newEmail) {
        email = newEmail;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public void setTwoFactor(String code){
        this.twoFactorCode = code;
    }
    public String getTwoFactorCode() {
        return twoFactorCode;
    }
    public void setRememberMe(boolean rememberMe){
        this.rememberMe = rememberMe;
    }
    public boolean isRememberMe(){
        return rememberMe;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * This method checks whether the user is currently suspended.
     * Suspended users are not allowed to log in.
     *
     * @return true if the user is suspended, false otherwise
     */
    public boolean isSuspended() {
        return suspended;
    }

    /**
     * This method sets the suspension status of the user.
     *
     * @param suspended true to suspend the user, false to lift the suspension
     */
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }


    /**
     * This function is serve the functionality to flag a user account to be locked
     * @author: Tan Michael Olsen
     */
    public void setLockOut(){
        this.lockOut = true;
    }

    public void setLockOut(boolean lockOut) {  // Added for Jackson compatibility
        this.lockOut = lockOut;
    }
    /**
     * This function serve the functionality to unlock a user account.
     * @author: Tan Michael Olsen
     */
    public void setUnLock(){
        this.lockOut = false;
    }

    /**
     * A function that returns the lockout flag of the user account
     * @return the lockout state
     * @author:  Tan Michael Olsen
     */
    public boolean isLockOut(){
        boolean result = false;
        if(this.lockOut){
            result = true;
        }
        return result;
    }
}
