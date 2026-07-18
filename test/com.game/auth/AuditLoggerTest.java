package com.game.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testLogSingleEntry() {
        AuditLogger.log("Test log");
        AuditLogger.showLogs();
        assertTrue(outputStream.toString().contains("Test log"));
    }

    @Test
    void testLogMultipleEntries() {
        AuditLogger.log("Log 1");
        AuditLogger.log("Log 2");
        AuditLogger.showLogs();
        String output = outputStream.toString();
        assertTrue(output.contains("Log 1"));
        assertTrue(output.contains("Log 2"));
    }

    @Test
    void testLogEmptyString() {
        AuditLogger.log("");
        AuditLogger.showLogs();
        assertTrue(outputStream.toString().contains(""));
    }

    @Test
    void testLogNullEntry() {
        assertDoesNotThrow(() -> AuditLogger.log(null));
        AuditLogger.showLogs();
        assertTrue(outputStream.toString().contains("null"));
    }

    @Test
    void testLogWithWhitespace() {
        AuditLogger.log("   ");
        AuditLogger.showLogs();
        assertTrue(outputStream.toString().contains("   "));
    }
}
