package com.game.leaderboard;

import java.io.IOException;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * <p>
 *     Writer for match history.
 *     Writes to a JSON file.
 *     Dependencies: Uses the Jackson library for JSON parsing
 * </p>
 */

public class MatchHistoryWriter {
    public static void saveMatchHistory(MatchHistory history, String filename) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String filePath = "src/main/resources/matchHistory/playerMatchHistory.json";
        try {
            File matchHistory = new File(filePath);
            writer.writeValue(matchHistory, history);
        } catch (IOException e) {
            System.err.println("Error writing match history to file:");
        }
    }
}