package com.game.leaderboard;

import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * <p>
 *     This class implements a player's match history.
 *     May be used by profile to load a player's match history when they load profile.
 * </p>
 */
public class MatchHistory {
    /**
     * List of match records.
     */
    public String playerId;
    public List<Match> matchRecords;

    /**
     * Public constructor
     */
    public MatchHistory() {
        this.matchRecords = new ArrayList<>();
    }

    public MatchHistory(String playerID) {
        this.playerId = playerID;
    }

//    public MatchHistory getMatchRecord(String username) {
//        File file = new File(BASE_DIR + username + ".json");
//
//        if (!file.exists()) {
//            System.out.println("Match history not found for user: " + username);
//            return null;
//        }
//
//        try {
//            return mapper.readValue(file, MatchHistory.class);
//        } catch (IOException e) {
//            System.out.println("Failed to read match history for user: " + username);
//            return null;
//        }
//    }

}
