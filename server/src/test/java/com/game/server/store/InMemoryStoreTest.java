package com.game.server.store;

import org.junit.jupiter.api.BeforeEach;

class InMemoryStoreTest extends StoreContract {
    private Store store;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
    }

    @Override
    Store store() {
        return store;
    }
}
