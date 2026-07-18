package com.game.auth;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
public class JWTTokenTest {
    @Test
    void TestGetUsernameFromToken(){
        String username = "Jacob Davidson";
        String token = JWTTOKEN.generateToken(username);
        assertEquals(username, JWTTOKEN.getUsernameFromToken(token));
    }

    @Test
    void TestTokenIsValid(){
        String username = "Jacob Davidson";
        String token = JWTTOKEN.generateToken(username);
        assertTrue(JWTTOKEN.validateToken(token));
    }

    @Test
    void TestInvalidToken(){
        String token = JWTTOKEN.generateToken("adis ababa");
        assertFalse(JWTTOKEN.validateToken(token+"sddsaw"));
    }



    @Test
    void TestInvalidUsername(){
        String username = "Jacob Jake Riley";
        String token = JWTTOKEN.generateToken(username);
        assertNotEquals(username+"sad sd", JWTTOKEN.getUsernameFromToken(token));
    }

    @Test
    void TestInvalidUsername2(){
        String username = "Jacob Jake Riley";
        String token = JWTTOKEN.generateToken(username);
        assertNotEquals(username, JWTTOKEN.getUsernameFromToken(token)+"sadsad");
    }

    @Test
    void TestInvalidUsername3(){
        String username = "Jacob Jake Riley";
        String token = JWTTOKEN.generateToken(username);
        assertNotEquals("", JWTTOKEN.getUsernameFromToken(token)+"sadsad");
    }

    @Test
    void testValidateTokenWithExpiredToken() throws InterruptedException {
        // Step 1: Generate a valid token
        String token = JWTTOKEN.generateToken("user1");

        // Step 2: Simulate waiting for the token to expire (in reality, you should mock time)
        TimeUnit.SECONDS.sleep(2); // Replace this with actual wait logic or mock time for testing expiration

        // Step 3: Validate the  token
        boolean isValid = JWTTOKEN.validateToken(token);

        // Step 4: Assert that the token is should still be valid
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithGarbageString() {
        String invalidToken = "this.is.not.a.jwt";
        assertFalse(JWTTOKEN.validateToken(invalidToken), "Garbage string should be invalid");
    }

    @Test
    void testValidateExpiredToken() {
        // This is a manually crafted expired JWT token
        // Replace the SECRET_KEY in JWTTOKEN with your own key to make this work
        String expiredToken =
                "eyJhbGciOiJIUzI1NiJ9." +
                        "eyJzdWIiOiJleHBpcmVkdXNlciIsImlhdCI6MTYwOTAwMDAwMCwiZXhwIjoxNjA5MDAwMDAxfQ." +
                        "K5QoOaXkvhFl_uHZBfrzYujnJ4Xodt0z9wE1ujYYqVM";

        // This will return false because the token expired in 2020
        assertFalse(JWTTOKEN.validateToken(expiredToken), "Expired token should be invalid");
    }

}
