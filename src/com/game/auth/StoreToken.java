package com.game.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class provides methods to store and retrieve a JWT token securely in a PKCS12 KeyStore.
 * The token is stored as a SecretKey with AES algorithm and retrieved as a string.
 * @author: Tan Michael Olsen
 */
public class StoreToken {

    private static final String KEYSTORE_PATH = "ACCESS-TOKEN.p12";
    private static final String KEYSTORE_PASSWORD = "@SENGPROJECT1";
    private static final String ENTRY_PASSWORD = "@SENGPROJECT2";
    private static final String ENTRY_ALIAS = "jwt-token";

    /**
     * Stores the provided JWT token in the KeyStore.
     * @param jwtToken The JWT token to be stored.
     * @throws Exception If an error occurs while storing the token.
     */
    public static void storeJWT(String jwtToken) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        if (doesKeystoreExist()) {
            try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
                keystore.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }
        } else {
            keystore.load(null, null);
        }

        // Store the JWT as a SecretKey with AES algorithm
        byte[] jwtBytes = jwtToken.getBytes("UTF-8");
        SecretKey secretKey = new SecretKeySpec(jwtBytes, "AES"); // Use AES, but no length restriction
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        keystore.setEntry(ENTRY_ALIAS, secretKeyEntry, new KeyStore.PasswordProtection(ENTRY_PASSWORD.toCharArray()));

        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
            keystore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        }

        System.out.println("JWT token stored successfully in KeyStore: " + jwtToken);
    }

    /**
     * Retrieves the JWT token from the KeyStore.
     * @return The JWT token as a string.
     * @throws Exception If an error occurs while retrieving the token.
     */
    public static String retrieveJWT() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keystore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keystore.getEntry(ENTRY_ALIAS,
                new KeyStore.PasswordProtection(ENTRY_PASSWORD.toCharArray()));
        if (secretKeyEntry == null) {
            throw new Exception("No JWT token found in keystore");
        }

        SecretKey secretKey = secretKeyEntry.getSecretKey();
        String jwtToken = new String(secretKey.getEncoded(), "UTF-8");

        System.out.println("JWT token retrieved from KeyStore: " + jwtToken);
        return jwtToken;
    }

    /**
     * Checks if the keystore file exists.
     * @return true if the keystore file exists, false otherwise.
     */
    public static boolean doesKeystoreExist() {
        File keystoreFile = new File(KEYSTORE_PATH);
        return keystoreFile.exists();
    }

    /**
     * Creates a new empty keystore file if it doesn’t exist.
     * @throws Exception If an error occurs while creating the keystore.
     */
    private static void createKeystore() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(null, null);
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_PATH)) {
            keystore.store(fos, KEYSTORE_PASSWORD.toCharArray());
        }
        System.out.println("Keystore created successfully.");
    }

    /**
     * Deletes the keystore file if it exists.
     *
     * @return true if the file was successfully deleted, false otherwise.
     * @throws SecurityException if a security manager exists and denies deletion.
     * @author: Tan Michael Olsen
     */
    public static boolean deleteKeystore() {
        File keystoreFile = new File(KEYSTORE_PATH);
        if (keystoreFile.exists()) {
            return keystoreFile.delete();
        }
        return false;
    }

}
