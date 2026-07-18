package com.game.matchmaking;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub database that stores simple string records.
 */
public class DatabaseStub {
    // A simple list to store records.
    private List<String> records = new ArrayList<>();

    /**
     * Adds a record to the database.
     *
     * @param record The record to add.
     */
    public void addRecord(String record) {
        records.add(record);
    }

    /**
     * Retrieves all stored records.
     *
     * @return A new list containing all records.
     */
    public List<String> getRecords() {
        return new ArrayList<>(records);
    }

    /**
     * Checks if the record exists in the database.
     *
     * @param record The record to check.
     * @return True if the record is present, false otherwise.
     */
    public boolean containsRecord(String record) {
        return records.contains(record);
    }
}
