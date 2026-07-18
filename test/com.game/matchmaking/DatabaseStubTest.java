package com.game.matchmaking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseStubTest {

    @Test
    void testAddAndRetrieveRecords() {
        DatabaseStub db = new DatabaseStub();

        // Initially the database should be empty.
        assertTrue(db.getRecords().isEmpty());

        // Add two records.
        db.addRecord("Record1");
        db.addRecord("Record2");

        // Test that records are contained.
        assertTrue(db.containsRecord("Record1"));
        assertTrue(db.containsRecord("Record2"));
        assertFalse(db.containsRecord("NonExistent"));

        // Test that getRecords returns the correct number of records.
        assertEquals(2, db.getRecords().size());

        // Test that modifying the returned list doesn't affect the internal list.
        var records = db.getRecords();
        records.add("NewRecord");
        // The original internal list should still have 2 items.
        assertEquals(2, db.getRecords().size());
    }
}
