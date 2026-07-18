package com.game.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a simple testing class to test the functionality of EmailService.
 * It checks whether the sendOtpEmail and welcomeEmail methods can be executed without exceptions.
 *
 * These tests actually send requests to SendGrid server.
 * In real-world applications, these would be replaced with mocked HTTP calls.
 *
 * This test requires internet and a valid SendGrid API key to work.
 * Also requires JUnit 5 to be in the classpath manually (if not using Maven/Gradle).
 */
public class EmailServiceTest {

    private String testEmail;
    private String dummyOtp;

    @BeforeEach
    public void setup() {
        // Initialize with a test email and dummy OTP code
        testEmail = "test-recipient@example.com";  // This email will receive OTP/welcome email
        dummyOtp = "123456";
    }

    @Test
    public void testSendOtpEmail() {
        try {
            // Try sending OTP email to test email address
            EmailService.sendOtpEmail(testEmail, dummyOtp);
            // If we reach here, no exception was thrown — test passes
            assertTrue(true);
        } catch (Exception e) {
            // If an exception is thrown, the test fails
            fail("Sending OTP email failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testSendWelcomeEmail() {
        try {
            // Try sending welcome email to test email address
            EmailService.welcomeEmail(testEmail);
            // If we reach here, no exception was thrown — test passes
            assertTrue(true);
        } catch (Exception e) {
            // If an exception is thrown, the test fails
            fail("Sending welcome email failed with exception: " + e.getMessage());
        }
    }
}
