package com.game.auth;

import com.game.auth.session.SessionManager;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles user authentication.
 * It supports user registration, login, role-based access,
 * account deletion, password updates, and two-factor authentication (2FA).
 * @author: Boya Liu, Maneet Singh,
 * @email: boya.liu@ucalgary.ca, maneet.singh1@ucalgary.ca,
 */

public class AuthManager {

    DatabaseStub database;
    public Map<String, User> pending2FA = new HashMap<>(); // to store the user temporarily while waiting for 2FA

    private Integer FailedLoginCount = 0; // The variable used to count the number to failed logins; initially value is 0
    private static AuthManager instance;

    /**
     * Reading the saved user data from the file
     */
    private void loadUserFile(){
        try{
            UserReader.loadUserFromFile("user_authentication_data", database);
        }
        catch (IOException e){
            System.err.println("Filed to load user from file: " + e.getMessage());
        }
    }
    /**
     * writing the user data in file using JSON format
     */
    public void updateUsersFile(){
        try{
            UserWriter.WriteUser("user_authentication_data", database.getAllUsers());
        }
        catch (IOException e){
            System.err.println("Failed to save user data: " + e.getMessage());
        }
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager(new DatabaseStub());
        }
        return instance;
    }

    //Constructor to initialize AuthManager with a database instance.
    //@param database The database where user information is stored.
    public AuthManager(DatabaseStub database) {
        this.database = database; // Assign database reference
    }

    /**
     * Temporary overload method to support old calls without email
     * @param username The username chosen by the user.
     * @param password The password chosen by the user.
     * @return true if registration is successful, false otherwise.
     */
    public boolean register(String username, String password) {
        // to make sure we accept the usrname even if user enter it in uppercase
        username = username.toLowerCase();
        return register(username, password, ""); // Default email as empty string
    }

    /**
     * Start a registration process for a new user with an email and password. Ensures usernames are unique and applies password hashing.
     * @param username The username chosen by the user.
     * @param password The password chosen by the user.
     * @param email The email address of the user (for verification).
     * @return true if registration is successful, false otherwise.
     */
    public boolean register(String username, String password, String email) {
        // to read the database file and lead data into database
        loadUserFile();
        // to make sure we accept the username even if user enter it in uppercase
        username = username.toLowerCase();
        email = email.toLowerCase();
        // Step 1: Check if the username already exists in the database or in hashmap waiting for 2FA
        // If the user exists, registration cannot proceed
        if (database.getUser(username) != null || pending2FA.containsKey(username)) {
            System.out.println("Error: The username '" + username + "' is already taken. Please choose another one.");
            return false; // Stop registration
        }
        // if the email already associated with an account that is registered or waiting for 2FA
        if(pendingEmailExists(email)|| database.emailExists(email)){
            System.out.println("The email already in use");
            return false;
        }

        // Step 2: Generate a random salt for password hashing
        String generatedSalt = generateSalt();

        // Step 3: Hash the password using the generated salt
        String hashedPassword = hashPassword(password, generatedSalt);

        // Step 4: System sends user two-factor authentication code (6-digit number)
        String generatedTwoFactorCode = generateTwoFactorCode();
        EmailService.sendOtpEmail(email, generatedTwoFactorCode);
        System.out.println(generatedTwoFactorCode);

        // Step 5: Create a new User object with the given details
        User newUser = new User(username, hashedPassword + ":" + generatedSalt, email, "user", generatedTwoFactorCode);

        // Step 6: save the user into hashmap to wait for 2FA completion
        pending2FA.put(username, newUser);

        // Step 7: Print message to indicate user about sending of one time password
        System.out.println("A email with One Time Password has been sent to " + email + ".");

        return true; // First step of registration completed successfully
    }

    /**
     * Complete the 2FA process for the user registration. Ensure the user entered the correct 2FA code
     * @param username The username chosen by the user.
     * @param inputCode The 2FA code entered by the user
     * @return true if 2FA verified, false otherwise.
     */
    public boolean verify2FARegistration(String username, String inputCode) {
        // to make sure we accept the username even if user enter it in uppercase
        username = username.toLowerCase();
        User pendingUser = pending2FA.get(username); // get the user from hashmap

        //In case there is no pending user in the system
        if(pendingUser == null){
            System.out.println("no pending user found");
            return false;
        }
        if(!pendingUser.getTwoFactorCode().equals(inputCode)){
            System.out.println("Entered one time password does not match, try again");
            return false;
        }
        //to add the user into a database
        database.addUser(pendingUser);
        //update the JSON data file
        updateUsersFile();

        //Print a confirmation message for successful registration and send welcome email
        System.out.println("Registration successful! A verification email has been sent to " + pendingUser.getEmail() + ".");
        EmailService.welcomeEmail(pendingUser.getEmail());

        // Log this action for audit trail (simulating a database audit log)
        AuditLogger.log("User registration completed for username: " + username);

        //and deleting from a pending list after 2FA success
        pending2FA.remove(username);

        return true; // user registration completed successfully
    }

    /**
     * Allows a user to log in with 2FA.
     * @param id username or email entered by the user.
     * @param password The password input.
     * @return true if a user partially signed in pending 2FA, false otherwise.
     */
    public boolean login(String id, String password) {
        // to read the database file and lead data into database
        loadUserFile();
        // to make sure we accept the username/email even if user enter it in uppercase
        id = id.toLowerCase();
        // This mechanism enables a system to accept both username and password for user login
        User user = database.getUser(id);
        // check if username is entered by the user
        if (user == null) {
            // check if the id entered by the user might be their email
            if(!database.emailExists(id)) {
                return false; // User not found
            }
            else{
                // fetch username using email and then fetch user itself
                user = database.getUserByEmail(id);
            }
        }

        if (user.isSuspended()) {
            System.out.println("This account has been suspended and cannot log in.");
            return false; // Block login for suspended users
        }

        // Locks the user after 5 failed login attempts
        // By Tan Michael Olsen
        if (this.FailedLoginCount == 5){
            user.setLockOut();
            SetLogOutCountZero(); // reset the failed login count to 0
        }

        // Checks if the user account is locked
        // By Tan Michael Olsen
        if (user.isLockOut()){
            System.out.println("This account is temporarily locked after multiple unsuccessful login attempts.");
            return false;
        }

        String storedHash = user.getPasswordHash();
        String[] hashParts = storedHash.split(":");
        if (hashParts.length != 2) {
            System.out.println("Invalid stored password format.");
            return false;
        }

        String salt = hashParts[1]; // Extract salt
        String computedHash = hashPassword(password, salt); // Hash input password

        if (!computedHash.equals(hashParts[0])) {
            System.out.println("Incorrect password.");
            AddLogOutCount(); // increments the failed login count
            return false;
        }

        // Two-Factor Authentication Step
        System.out.print("A verification code has been sent to your email: ");
        // Step 4: System sends user two-factor authentication code (6-digit number)
        String generatedTwoFactorCode = generateTwoFactorCode();
        EmailService.sendOtpEmail(user.getEmail(), generatedTwoFactorCode);
        System.out.println(generatedTwoFactorCode);
        // save 2-factor code in a database for verification later
        user.setTwoFactor(generatedTwoFactorCode);

        // Log successful password match for audit (2FA still pending)
        AuditLogger.log("User " + user.getUsername() + " entered correct password and is waiting for 2FA verification.");

        return true;
    }

    /**
     * Complete the 2FA process for the user login
     * @param username associated with a particular user
     * @param inputCode The 2FA code entered by the user
     * @return true if 2FA verified, false otherwise.
     */
    public boolean verify2FALogin(String username, String inputCode) {
        // to make sure we accept the usrname even if user enter it in uppercase
        username = username.toLowerCase();
        // Two-factor authentication code does not match, user login unsuccessful
        if(!database.getUser(username).getTwoFactorCode().equals(inputCode)){
            System.out.println("Entered one time password does not match, try again");
            return false;
        }
        else {
            // Two-factor authentication code matches, user login successful
            System.out.println("Two factor authentication successful, user login successful");

            // 🟢 Log successful login
            AuditLogger.log("User login successful for username: " + username);

            return true;
        }
    }
    /**
     * to allow the user to resend the One Time Password
     * @param username associated with user we neeed to send OTP to
     * @return true if one time passwod sent succesfully, false otherwise
     */
    public boolean resend2FA(String username) {
        User user;
        if(pending2FA.containsKey(username)){
            user = pending2FA.get(username);
        } else if (database.getUser(username) !=  null) {
            user = database.getUser(username);
        }
        else{
            System.out.println("Username not found");
            return false;
        }
        System.out.print("A verification code has been sent to your email: ");
        // System sends user two-factor authentication code (6-digit number)
        String generatedTwoFactorCode = generateTwoFactorCode();
        EmailService.sendOtpEmail(user.getEmail(), generatedTwoFactorCode);
        // save 2-factor code in a database for verification later
        user.setTwoFactor(generatedTwoFactorCode);
        System.out.println(generatedTwoFactorCode);
        return true;
    }

    /**
     * Allows a user to delete their own account.
     * @param username The username of the account to delete.
     * @return true if deletion is successful, false otherwise.
     */
    public boolean deleteOwnAccount(String username) {
        if (database.deleteUser(username)) {
            updateUsersFile();
            System.out.println("Account successfully deleted.");

            // Log account deletion
            AuditLogger.log("User account deleted: " + username);

            return true;
        } else {
            System.out.println("Account deletion failed. User may not exist.");
            return false;
        }
    }

    /**
     * Allows a user to change their own password.
     * The user must enter their old password before setting a new one.
     * @param username The username of the user requesting the password change.
     * @param oldPassword The user's current password.
     * @param newPassword The new password the user wants to set.
     * @return true if password update is successful, false otherwise.
     */
    public boolean changeOwnPassword(String username, String oldPassword, String newPassword) {
        User userFromDatabase = database.getUser(username);
        if (userFromDatabase == null) {
            System.out.println("Error: The username '" + username + "' does not exist in the system.");
            return false;
        }

        String storedPasswordData = userFromDatabase.getPasswordHash();
        String[] passwordParts = storedPasswordData.split(":");

        if (passwordParts.length == 2) {
            String storedHashedPassword = passwordParts[0];
            String storedSalt = passwordParts[1];

            String computedOldPasswordHash = hashPassword(oldPassword, storedSalt);

            if (computedOldPasswordHash.equals(storedHashedPassword)) {
                String newGeneratedSalt = generateSalt();
                String newHashedPassword = hashPassword(newPassword, newGeneratedSalt);
                String finalStoredPassword = newHashedPassword + ":" + newGeneratedSalt;

                userFromDatabase.setPasswordHash(finalStoredPassword);
                database.updateUser(userFromDatabase);
                //add updated data in database file
                updateUsersFile();
                System.out.println("Password for user '" + username + "' has been updated successfully.");

                // Log password change
                AuditLogger.log("Password changed successfully for user: " + username);

                return true;
            } else {
                System.out.println("Error: The old password entered is incorrect.");
                return false;
            }
        }
        else {
            System.out.println("Error: Stored password format is invalid for user '" + username + "'.");
            return false;
        }
    }
    /**
     * Allows an admin to reset a user's password.
     * @param username The username of the user.
     * @param newPassword The new password.
     * @return true if reset is successful, false otherwise.
     */
    public boolean resetUserPassword(String username, String newPassword) {
        // to make sure we accept the username and email even if user enter it in uppercase
        username = username.toLowerCase();
        User user = database.getUser(username);
        if (user == null) {
            System.out.println("User not found.");
            return false;
        }

        String salt = generateSalt();
        String newHashedPassword = hashPassword(newPassword, salt);
        user.setPasswordHash(newHashedPassword + ":" + salt);
        database.updateUser(user);
        //add updated data in database file
        updateUsersFile();
        System.out.println("Password reset successfully.");

        // Log admin reset password action
        AuditLogger.log("Admin reset password for user: " + username);

        return true;
    }
    /**
     * Allows an user to set new password in case of forget password
     * @return true if reset is successful, false otherwise.
     */
    public boolean forgetPassword(String id) {
        id = id.toLowerCase();
        loadUserFile();
        User user = database.getUser(id); // Check if id is a username
        if (user == null) {
            user = database.getUserByEmail(id); // Check if id is an email
            if (user == null) {
                System.out.println("User not found for ID: " + id);
                return false; // Neither username nor email found
            }
        }
        System.out.print("A verification code has been sent to your email: ");
        // Step 4: System sends user two-factor authentication code (6-digit number)
        String generatedTwoFactorCode = generateTwoFactorCode();
        EmailService.sendOtpEmail(user.getEmail(), generatedTwoFactorCode);
        // save 2-factor code in a database for verification later
        user.setTwoFactor(generatedTwoFactorCode);
        System.out.println(generatedTwoFactorCode);

        // Log successful password match for audit (2FA still pending)
        AuditLogger.log("Forget password resquest: User " + user.getUsername() + " is waiting for 2FA verification");
        return true;
    }
    /**
     * Complete the 2FA authentication process for user password reset for forget password
     * @param username associated with a particular user
     * @param inputCode The 2FA code entered by the user
     * @return true if user eneterd correct 2FA code, false otherwise.
     */
    public boolean verify2ForgetPassword(String username, String inputCode) {
        // to make sure we accept the username even if user enter it in uppercase
        username = username.toLowerCase();
        User user = database.getUser(username);
        // Two-factor authentication code does not match, user login unsuccessful
        if(!database.getUser(username).getTwoFactorCode().equals(inputCode)){
            System.out.println("Entered one time password does not match, try again");
            return false;
        }
        else {
            System.out.println("Entered one time password matched");
            // 🟢 Log successful login
            AuditLogger.log("Forget password resquest: user successfuly verified 2FA: " + username);
            return true;
        }
    }
    /**
     * Complete the password reset request for forget password
     * @param username associated with a particular user
     * @param newPassword The new password that has been eneterd by user
     * @return true if password reset successfully, false otherwise.
     */
    public boolean forgetPasswordReset(String username, String newPassword) {
        // to make sure we accept the username even if user enter it in uppercase
        username = username.toLowerCase();
        User user = database.getUser(username);

        String storedPasswordData = user.getPasswordHash();
        String[] passwordParts = storedPasswordData.split(":");

        if (passwordParts.length == 2) {
            String storedHashedPassword = passwordParts[0];
            String storedSalt = passwordParts[1];

            String computedOldPasswordHash = hashPassword(newPassword, storedSalt);
            if (computedOldPasswordHash.equals(storedHashedPassword)) {
                System.out.println("Your password should be different from previousely used passwords");
                // Log password change
                AuditLogger.log("Forget password resquest failed: User selected same old password: " + username);
                return false;
            }
            else {
                String newGeneratedSalt = generateSalt();
                String newHashedPassword = hashPassword(newPassword, newGeneratedSalt);
                String finalStoredPassword = newHashedPassword + ":" + newGeneratedSalt;

                user.setPasswordHash(finalStoredPassword);
                database.updateUser(user);

                //add updated data in database file
                updateUsersFile();

                System.out.println("Password for user '" + username + "' has been updated successfully.");
                // Log password change
                AuditLogger.log("Forget password resquest: Password changed successfully for user: " + username);
            }
        }
        else {
            System.out.println("Error: Stored password format is invalid for user '" + username + "'.");
            return false;
        }
        return true;
    }

    /**
     * A getter function to allow switching to a different email address
     * @param username of the account we need to change email for
     * @param newEmail provided by the user
     * @return true, if a partial step for email verification is complete, otherwise return false
     */
    public boolean updateEmail(String username, String newEmail){
        // to make sure we accept the username and email even if user enter it in uppercase
        username = username.toLowerCase();
        newEmail = newEmail.toLowerCase();

        User user = database.getUser(username);
        if(user.getEmail().equals(newEmail)){
            System.out.println("This email is already associated with your account");
            return false;
        }
        // to verify if there is an account, already exists with the given email address
        else if(database.emailExists(newEmail)|| pendingEmailExists(newEmail)){
            System.out.println("The email is already in use");
            return false;
        }
        else {
            System.out.println("A email with one time password has been sent to user");
            // System sends user two-factor authentication code (6-digit number)
            String generatedTwoFactorCode = generateTwoFactorCode();
            EmailService.sendOtpEmail(user.getEmail(), generatedTwoFactorCode);
            // save 2-factor code in a database for verification later
            user.setTwoFactor(generatedTwoFactorCode);
            //partial email update successful, with 2FA pending
            return true;
        }
    }
    /**
     * The user will enter the 6-digit code and complete the email update process
     * @param newEmail email that user wants to update for their account
     * @param username belong to the user
     * @return true if email updated successfully after 2-factor authentication otherwise return false
     */
    public boolean emailUpdateVerification(String username,String newEmail, String inputCode){
        // to make sure we accept the username and email even if user enter it in uppercase
        username = username.toLowerCase();
        newEmail = newEmail.toLowerCase();
        // Two-facter authentication code does not match, email update unsuccessful
        if(!database.getUser(username).getTwoFactorCode().equals(inputCode)){
            System.out.println("Entered one time password does not match, try again");
            return false;
        }
        else {
            //Two-facter authentication code matches, email updates successful
            System.out.println("Two factor authentication successful");
            database.getUser(username).setEmail(newEmail);
            //add updated data in database file
            updateUsersFile();
            return true;
        }
    }

    /**
     * Grants administrator privileges to a user.
     * @param username The username of the user to promote.
     * @return true if successful, false otherwise.
     */
    public boolean promoteToAdmin(String username) {
        if(database.updateUserRole(username, "admin")) {
            //add updated data in database file
            updateUsersFile();
            return true;
        }
        return false;
    }

    /**
     * Demotes a user from administrator privileges to regular user.
     * @param username The username of the user to demote.
     * @return true if successful, false otherwise.
     *
     * @author: Thomas Tabur
     * @email: thomas.tabur@ucalgary.ca
     */
    public boolean demoteFromAdmin(String username) {
        User user = database.getUser(username);
        if (user == null) {
            return false;
        }
        else if(database.updateUserRole(username, "user")) {
            //add updated data in database file
            updateUsersFile();
            return true;
        }
        return false;
    }

    /**
     * Allows an admin to suspend (ban) a user.
     * Suspended users will not be allowed to log in to the system.
     * @param username The username of the user to suspend.
     * @return true if successful, false otherwise.
     */
    public boolean suspendUser(String username) {
        User user = database.getUser(username);
        if (user == null) {
            System.out.println("User not found. Cannot suspend.");
            return false;
        }
        user.setSuspended(true);
        database.updateUser(user);
        //add updated data in database file
        updateUsersFile();
        System.out.println("User '" + username + "' has been suspended.");
        return true;
    }

    /**
     * Allows an admin to unsuspend (unban) a user.
     * This will allow the user to log in again if they were banned.
     * @param username The username of the user to unsuspend.
     * @return true if successful, false otherwise.
     */
    public boolean unsuspendUser(String username) {
        User user = database.getUser(username);
        if (user == null) {
            System.out.println("User not found. Cannot unsuspend.");
            return false;
        }
        user.setSuspended(false);
        database.updateUser(user);
        //add updated data in database file
        updateUsersFile();
        System.out.println("User '" + username + "' has been unsuspended.");
        return true;
    }

    /**
     * Allows an admin to view all users.
     */
    public void viewAllUsers() {
        System.out.println("All registered users:");
        for (User user : database.getAllUsers()) {
            System.out.println("Username: " + user.getUsername() + ", Role: " + user.getRole());
        }
    }

    // Generates a random salt for password hashing
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // Hashes a password using SHA-256
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Didn't find hashing algorithm", e);
        }
    }

    // Generates a random 6-digit two-factor authentication code
    private String generateTwoFactorCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * A getter function to get failed login count
     * @return failed login count
     * @author: Tan Michael Olsen
     */
    private Integer GetLogOutCount(){
        return this.FailedLoginCount;
    }

    /**
     * A function that increments the failed login count by 1
     * @author: Tan Michael Olsen
     */
    private void AddLogOutCount(){
        this.FailedLoginCount +=1;
    }

    /**
     * a function that resets the failed login counts to 0
     * @author: Tan Michael Olsen
     */
    private void SetLogOutCountZero(){
        this.FailedLoginCount = 0;
    }
    /**
     * To check if an email exits in account pending for 2FA
     *@param email email entered by the user
     *@return true if email found, false otherwise
     */
    public boolean pendingEmailExists(String email) {
        for (User user : pending2FA.values()) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
    /**
     * This class verifies if the password entered by the user meets password requirements
     * this method is enforcing three password rules
     * 1. Password string must have uppercase letter
     * 2. password string must have lowercase letter
     * 3. password string must have a string
     * @param password entered by the user
     */
    public boolean passwordValidation(String password){
        if (password.length() < 6){
            return false;
        }
        // this code is inspired by code generated by chatgpt
        String upperCase = ".*[A-Z].*";
        String lowerCase = ".*[a-z].*";
        String digits = ".*\\d.*";

        return password.matches(upperCase) && password.matches(lowerCase) && password.matches(digits);
    }

    public DatabaseStub getDatabase() {
        return database;
    }
}
