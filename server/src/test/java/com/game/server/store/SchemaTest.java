package com.game.server.store;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTest {

    @Test
    void splitterIgnoresCommentsAndBlankStatements() {
        assertEquals(List.of("CREATE TABLE a (x INT)", "SELECT 1"),
                JdbcStore.statements("-- comment; with a semicolon\nCREATE TABLE a (x INT); -- trailing\n\nSELECT 1;\n"));
    }

    @Test
    void shippedSchemaIsIdempotent() throws Exception {
        try (var in = JdbcStore.class.getResourceAsStream("/db/schema.sql")) {
            final String sql = new String(in.readAllBytes());
            for (String statement : JdbcStore.statements(sql)) {
                assertTrue(statement.contains("IF NOT EXISTS"), "re-runnable at every startup: " + statement);
            }
        }
    }
}
