package com.game.auth;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTokenTest {

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String INVALID_TOKEN = "invalid.token.value";

    @Test
    void testDoesKeystoreExistFalseInitially() {
        StoreToken.deleteKeystore();
        assertFalse(StoreToken.doesKeystoreExist(), "Keystore should not exist initially.");
    }

    @Test
    void testCreateKeystore() throws Exception {
        // Delete keystore first to ensure it doesn't exist
        StoreToken.deleteKeystore();

        // Ensure createKeystore method is called when necessary
        StoreToken.storeJWT(TEST_TOKEN);

        // After calling storeJWT, the keystore should exist
        assertTrue(StoreToken.doesKeystoreExist(), "Keystore should be created after storing a token.");
    }

    @Test
    void testStoreJWT() throws Exception {
        // Ensure storing a token works
        StoreToken.storeJWT(TEST_TOKEN);
        assertTrue(StoreToken.doesKeystoreExist(), "Keystore should exist after storing token.");
    }

    @Test
    void testRetrieveJWT() throws Exception {
        StoreToken.storeJWT(TEST_TOKEN);
        String retrieved = StoreToken.retrieveJWT();
        assertEquals(TEST_TOKEN, retrieved, "Retrieved token should match the original.");
    }

    @Test
    void testStoreJWT_Overwrite() throws Exception {
        String newToken = "new.token.value.overwrite.test";
        StoreToken.storeJWT(newToken);
        assertEquals(newToken, StoreToken.retrieveJWT());
    }

    @Test
    void testDeleteKeystore() throws Exception {
        // Test deletion of the keystore after creating and storing a token
        StoreToken.deleteKeystore();
        assertFalse(StoreToken.doesKeystoreExist(), "Keystore should no longer exist after deletion.");

        // Recreate the keystore and store a token
        StoreToken.storeJWT(TEST_TOKEN);
        assertTrue(StoreToken.doesKeystoreExist(), "Keystore should exist again after recreation.");
    }

    @Test
    void testRetrieveJWTWhenKeystoreMissing() {
        // Delete the keystore before checking retrieval
        StoreToken.deleteKeystore();

        Exception exception = assertThrows(Exception.class, StoreToken::retrieveJWT);
        assertTrue(exception.getMessage().contains("ACCESS-TOKEN.p12"),
                "Expected error due to missing keystore.");
    }

//    @Test
//    void testStoreJWTWhenAliasDoesNotExist() throws Exception {
//        // Delete the keystore and simulate an invalid alias situation
//        StoreToken.deleteKeystore();
//
//        // Store a JWT token, then delete the alias entry directly (simulate).
//        StoreToken.storeJWT(TEST_TOKEN);
//
//        // Manually modify the keystore or simulate the missing alias
//        File keystoreFile = new File("ACCESS-TOKEN.p12");
//        if (keystoreFile.exists()) {
//            keystoreFile.delete(); // Delete keystore file to simulate the absence of alias
//        }
//
//        // Try retrieving the token again after deletion
//        Exception exception = assertThrows(Exception.class, StoreToken::retrieveJWT);
//        assertTrue(exception.getMessage().contains("No JWT token found in keystore"),
//                "Expected error due to missing alias.");
//    }

//    @Test
//    void testInvalidTokenStorage() {
//        // Test storing an invalid token and retrieving it
//        Exception exception = assertThrows(Exception.class, () -> StoreToken.storeJWT(INVALID_TOKEN));
//        assertTrue(exception.getMessage().contains("Invalid token format"),
//                "Expected error due to invalid token.");
//    }

    @Test
    void testKeystoreFileDeletion() throws Exception {
        // Ensure that the keystore can be deleted if it exists
        StoreToken.storeJWT(TEST_TOKEN);
        File keystoreFile = new File("ACCESS-TOKEN.p12");
        assertTrue(keystoreFile.exists(), "Keystore should exist before deletion.");

        boolean deleted = StoreToken.deleteKeystore();
        assertTrue(deleted, "Keystore should be deleted.");
        assertFalse(keystoreFile.exists(), "Keystore should no longer exist after deletion.");
    }
}
