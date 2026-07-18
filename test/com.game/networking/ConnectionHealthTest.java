package com.game.networking;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests ConnectionHealth
 */
public class ConnectionHealthTest {
    ConnectionHealth connectionHealth;

    /**
     * Loads connectionHealth with data that can be tested on
     */
    @BeforeEach
    void setup() {
        connectionHealth = new ConnectionHealth();
        connectionHealth.recordLatency("0000", 43);
        connectionHealth.recordPacketLoss("0000", 1);
        connectionHealth.recordLatency("1111", 500);
        connectionHealth.recordPacketLoss("1111", 9);
    }

    /**
     * Tests getters and setters for ConnectionHealth object
     */
    @Test
    void gettersandsettersTest() {
        // Getters
        assertEquals(500,connectionHealth.getLatency("1111"));
        assertEquals(9,connectionHealth.getPacketsLost("1111"));

        // Setters
        connectionHealth.recordLatency("1111", 2);
        assertEquals(2, connectionHealth.getLatency("1111"));
        connectionHealth.recordPacketLoss("1111", 0);
        assertEquals(0, connectionHealth.getPacketsLost("1111"));
    }

    /**
     * Tests if checkConnectionHealth can identify a bad connection
     */
    @Test
    void checkConnectionHealthBadConnectionTest() {
        String connection = connectionHealth.checkConnectionHealth("1111");
        assertEquals("WARNING: Network connection is unstable!", connection);
    }

    /**
     * Tests if checkConnectionHealth can identify a good connection
     */
    @Test
    void checkConnectionHealthGoodConnectionTest() {
        String connection = connectionHealth.checkConnectionHealth("0000");
        assertEquals("Connection is strong!", connection);
    }

    /**
     * Tests if checkConnectionHealth can catch if there doesn't exits a player
     */
    @Test
    void checkConnectionHealthNoPlayerExistTest() {
        String connection = connectionHealth.checkConnectionHealth("1234567890");
        assertEquals("Player ID doesn't exist", connection);
    }


}
