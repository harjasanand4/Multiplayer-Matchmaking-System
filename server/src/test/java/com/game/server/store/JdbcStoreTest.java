package com.game.server.store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the store contract against a real PostgreSQL. Set TEST_DATABASE_URL (and TEST_DATABASE_USER /
 * TEST_DATABASE_PASSWORD) to a database these tests may wipe; CI provides one as a service container.
 * Without it the tests are skipped, not failed.
 */
class JdbcStoreTest extends StoreContract {
    private static final String URL = System.getenv("TEST_DATABASE_URL");
    private JdbcStore store;
    private ConnectionPool pool;

    @BeforeEach
    void setUp() throws Exception {
        assumeTrue(URL != null && !URL.isBlank(), "TEST_DATABASE_URL not set");
        pool = new ConnectionPool(URL, System.getenv().getOrDefault("TEST_DATABASE_USER", "omg"),
                System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", "omg"), 8, 5_000);
        store = new JdbcStore(pool);
        store.migrate();
        store.migrate();  // idempotent
        pool.withConnection(c -> {
            try (Statement st = c.createStatement()) {
                st.execute("TRUNCATE players, matches");
            }
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        if (store != null) {
            store.close();
        }
    }

    @Override
    Store store() {
        return store;
    }

    @Test
    void healthCheckFailsWhenThePoolIsClosed() {
        assumeTrue(store.healthy());
        pool.close();
        assertFalse(store.healthy());
    }
}
