package com.game.server;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTest {

    @Test
    void defaultsRunWithoutADatabase() {
        final Config c = Config.fromEnv(Map.of());
        assertEquals(8080, c.port());
        assertNull(c.databaseUrl());
        assertEquals(100, c.tickMillis());
        assertEquals(50, c.policy().baseWindow());
        assertEquals(60_000, c.policy().ticketTimeoutMillis());
    }

    @Test
    void readsOverrides() {
        final Config c = Config.fromEnv(Map.of("PORT", "9000", "DATABASE_URL", "jdbc:postgresql://db/omg",
                "WINDOW_BASE", "30", "TICKET_TIMEOUT_S", "90", "DATABASE_URL_UNUSED", "x"));
        assertEquals(9000, c.port());
        assertEquals("jdbc:postgresql://db/omg", c.databaseUrl());
        assertEquals(30, c.policy().baseWindow());
        assertEquals(90_000, c.policy().ticketTimeoutMillis());
    }

    @Test
    void badNumbersFailFastWithTheVariableName() {
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Config.fromEnv(Map.of("TICK_MS", "fast")));
        assertTrue(e.getMessage().contains("TICK_MS"));
    }

    @Test
    void jsonLogLinesAreValidJson() {
        final String line = new JsonLogFormatter().format(new java.util.logging.LogRecord(java.util.logging.Level.INFO,
                "quote \" backslash \\ newline \n tab \t"));
        assertTrue(line.endsWith("}\n"));
        assertTrue(line.contains("\"msg\":\"quote \\\" backslash \\\\ newline \\n tab \\t\""), line);
    }
}
