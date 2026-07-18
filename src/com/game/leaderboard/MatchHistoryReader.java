package com.game.leaderboard;

import java.io.IOException;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 *     Reader for Match History implementation
 *     Reads from a JSON file.
 *     Dependencies: Uses the Jackson library for JSON parsing
 * </p>
 */

public class MatchHistoryReader {
    public static MatchHistory loadMatchHistory(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        String filePath = "matches.json";
        try {
            return mapper.readValue(new File(filePath), MatchHistory.class);
        } catch (IOException e) {
            System.err.println("Error reading match history from file: " + filename);
            return null;
        }
    }
}
