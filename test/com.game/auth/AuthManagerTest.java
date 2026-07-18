package com.game.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class AuthManagerTest {
    //DatabaseStub object for testing
    private DatabaseStub testDatabase;
    // AuthManger object for testing
    private AuthManager authManager;

    // Data for user testing
    private String username = "gamer20";
    private String password = "hello99";
    private String email = "test@test"; // random email address

    @BeforeEach
    public void setup() {
        testDatabase = new DatabaseStub();
        authManager = new AuthManager(testDatabase);
    }

    /**
     * to clear all the previous user data before testing
     */

    @BeforeEach
    public void clearUserData(){
        try(FileWriter write =  new FileWriter("user_authentication_data")){
            write.write("[]");
        }
        catch (IOException e){
            System.err.println("Failed to clear file: " + e.getMessage());
        }
    }

    /**
     * test the get instance method
     */
    @Test
    public void GetInstanceTest() {
        AuthManager instance1 = AuthManager.getInstance();
        AuthManager instance2 = AuthManager.getInstance();

        assertSame(instance1, instance2);
    }

    /**
     * Test first step of the user registration
     */
    @Test
    public void userRegistrationTest() {
        assertTrue(authManager.register(username, password, email));
    }

    /**
     * Test registration without the user of username
     */
    @Test
    public void userRegistrationTest1() {
        assertTrue(authManager.register(username, password));
    }
    /**
     * To check if a system rejects the email if there is already a registration initialised for that email
     */
    @Test
    public void userRegistrationTest2() {
        //user 1
        authManager.register("user1", password, email);
        // user2 with trying to register with same email
        assertFalse(authManager.register("user2", password, "test@tEst"));
    }

    /**
     * Testing if a system denies registration for already existing user
     */
    @Test
    public void userRegistrationTest3() {
        //for 1st user
        // Creating a new user with pending 2FA
        authManager.register(username, password, email);
        // completing the 2FA for the added user
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());

        // for 2nd user
        String username2 = "Gamer20";     // username is same as first
        String password2 = "hello99";
        String email2 = "test1@test1";
        assertFalse(authManager.register(username2, password2, email2));
    }

    /**
     * Testing if a system allows the completion of 2FA process for pending user
     */
    @Test
    public void verify2FARegistrationTest() {
        authManager.register("Gamer21", password, "test2@test2");
        assertTrue(authManager.verify2FARegistration("gAmer21", authManager.pending2FA.get("gamer21").getTwoFactorCode()));
    }

    /**
     * verify if the system rejects the verification if there is not pending user present
     */
    @Test
    public void verify2FARegistrationTest2() {
        assertFalse(authManager.verify2FARegistration(username, "000000"));
    }
    /**
     * verify if the system rejects the wrong 2FA entered by the user
     */
    @Test
    public void verify2FARegistrationTest3() {
        authManager.register(username, password, email);
        assertFalse(authManager.verify2FARegistration(username, "000000"));
    }

    /**
     * verify the partial user login mechanism before 2-factor authentication is working
     */
    @Test
    public void login_test() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.login("GAMER20","hello99"));
    }
    /**
     * verify the partial user login using email before 2-factor authentication is working
     */
    @Test
    public void login_test1() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.login("TEST@test","hello99"));
    }

    /**
     * verify if the system denies login attempts with a wrong password
     */
    @Test
    public void login_test2() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertFalse(authManager.login("gaMer20","000000"));
    }

    /**
     * verify if the system denies login attempts when a user does not exist
     */
    @Test
    public void login_test3() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertFalse(authManager.login("champion","hello99"));
    }

    /**
     * verify if 2-factor authentication is working for login user
     */
    @Test
    public void login_test4() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.login(username, password);
        assertTrue(authManager.verify2FALogin("Gamer20",testDatabase.getUser(username).getTwoFactorCode()));
    }

    /**
     * verify if 2-factor authentication denying access if user enters wrong One Time Password
     */
    @Test
    public void login_test5() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.login(username, password);
        assertFalse(authManager.verify2FALogin(username,"000000"));
    }

    /**
    *  to verify if a partial email update works for a user pending 2FA
     */
    @Test
    public void updatedEmailTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.updateEmail(username, "tEst1@test1"));
    }

    /**
     *  to verify if a complete email update works
     */
    @Test
    public void updatedEmailTest2(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.updateEmail(username, "test1@test1");
        assertTrue(authManager.emailUpdateVerification("Gamer20","Test1@test1",testDatabase.getUser(username).getTwoFactorCode()));
    }
    /**
     *  to verify if a system does not allow email change if wrong One time password has been entered
     */
    @Test
    public void updatedEmailTest3(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.updateEmail(username, "test1@test1");
        assertFalse(authManager.emailUpdateVerification(username,"test1@test1", "0000000"));
    }

    /**
     *  to verify if a system does not accept an already used email address by user
     */
    @Test
    public void updatedEmailTest4() {
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertFalse(authManager.updateEmail(username, "test@teSt"));
    }

    /**
     *  to verify if a system does not accept an email address that is associated with a different account
     */
    @Test
    public void updatedEmailTest5() {
        //account 1
        authManager.register("gamer21", password, "tEst1@test1");
        authManager.verify2FARegistration("gamer21", authManager.pending2FA.get("gamer21").getTwoFactorCode());
        //account 2
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertFalse(authManager.updateEmail(username, "tesT1@test1"));
    }
    @Test
    public void testAuditLoggerIntegration() {
        AuditLogger.log("Unit test for logging...");
        AuditLogger.showLogs(); // test if output record successfully
    }
    /**
     *  to verify if first step of forget password mechanism works
     */
    @Test
    public void forgetPasswordTest(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.forgetPassword("GAMER20"));
    }
    /**
     *  to verify if second step(2FA) of forget password mechanism works
     */
    @Test
    public void forgetPasswordTest2(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.forgetPassword(username);
        assertTrue(authManager.verify2ForgetPassword("Gamer20", testDatabase.getUser(username).getTwoFactorCode()));
    }
    /**
     *  to verify if third step(2FA) of forget password mechanism works
     */
    @Test
    public void forgetPasswordTest3(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.forgetPassword(username);
        authManager.verify2ForgetPassword(username, testDatabase.getUser(username).getTwoFactorCode());
        assertTrue(authManager.forgetPasswordReset("GAMER20", "hello100"));
    }
    /**
     *  to verify if system denies the request when user use same old password
     */
    @Test
    public void forgetPasswordTest4(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.forgetPassword(username);
        authManager.verify2ForgetPassword(username, testDatabase.getUser(username).getTwoFactorCode());
        assertFalse(authManager.forgetPasswordReset(username, "hello99"));
    }
    /**
     *  to verify if systsme denies the request with wrong (2FA)
     */
    @Test
    public void forgetPasswordTest5(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.forgetPassword(username);
        assertFalse(authManager.verify2ForgetPassword(username, "0000000"));
    }
    /**
     *  to verify if system denies the request if user does not exits
     */
    @Test
    public void forgetPasswordTest6(){
        //registering new user
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertFalse(authManager.forgetPassword("gamer21"));
    }
    /**
     * to test the resend One Time Password email feature
     */
    @Test
    public void resendOTPTest(){
        authManager.register(username, password, "maneetsingh231@gmail.com");
        authManager.resend2FA(username);
        assertTrue(authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode()));
    }

    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest(){
        assertTrue(authManager.passwordValidation("Hello4"));
    }
    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest2(){
        assertTrue(authManager.passwordValidation("Hello4$"));
    }
    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest3(){
        assertFalse(authManager.passwordValidation("Hellos"));
    }
    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest4(){
        assertFalse(authManager.passwordValidation("hello2"));
    }
    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest5(){
        assertFalse(authManager.passwordValidation("HELLOSS"));
    }
    /**
     * this tests the password rule verification system
     */
    @Test
    public void passwordValidationTest6(){
        assertFalse(authManager.passwordValidation("He2"));
    }
    /**
     * to test view all user methods
     */
    @Test
    public void viewAllUserTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        authManager.viewAllUsers();
        System.setOut(System.out);
        String expected = String.join(System.lineSeparator(),
                "All registered users:" ,
                "Username: gamer20, Role: user"
        );
        assertEquals(expected, outputStream.toString().trim());
    }

    /**
     * to test suspend user method
     */
    @Test
    public void testUserSuspension(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.suspendUser(username);
        User user = testDatabase.getUser(username);
        assertTrue(user.isSuspended());
    }
    /**
     * test unsuspend user method
     */
    @Test
    public void testUserSuspension2(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.suspendUser(username);
        User user = testDatabase.getUser(username);
        assertTrue(user.isSuspended());
        authManager.unsuspendUser(username);
        assertFalse(user.isSuspended());
    }
    /**
     * test promote to admin
     */
    @Test
    public void promoteToAdminTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.promoteToAdmin(username);
        User user = testDatabase.getUser(username);
        assertEquals(user.getRole(), "admin");
    }
    /**
     * test demote back to normal user from admin
     */
    @Test
    public void promoteToAdminTest2(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        authManager.promoteToAdmin(username);
        User user = testDatabase.getUser(username);
        assertEquals(user.getRole(), "admin");
        authManager.demoteFromAdmin(username);
        assertEquals(user.getRole(),"user");
    }
    /**
     * test for deleting the user account
     */
    @Test
    public void deleteOwnAccountTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.deleteOwnAccount(username));
    }
    /**
     * test change your own password functionality in user profile settings
     */
    @Test
    public void changeOwnPasswordTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.changeOwnPassword(username, password, "password05"));
    }
    /**
     * test reset use password by admin
     */
    @Test
    public void  resetUserPasswordTest(){
        authManager.register(username, password, email);
        authManager.verify2FARegistration(username, authManager.pending2FA.get(username).getTwoFactorCode());
        assertTrue(authManager.resetUserPassword(username, "password05"));
    }

}