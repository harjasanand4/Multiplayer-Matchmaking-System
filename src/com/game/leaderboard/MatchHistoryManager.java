package com.game.leaderboard;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MatchHistoryManager {
    private static final String BASE_DIR = "match_history/";
    private static final int MAX_MATCHES = 3;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    public static void addMatch(Player player, Match newMatch) {
        File dir = new File(BASE_DIR);
        dir.mkdirs(); // make sure match_history folder exists

        File file = new File(BASE_DIR + player.getId() + ".json");
        MatchHistory history;

        if (file.exists()) {
            try {
                history = mapper.readValue(file, MatchHistory.class);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            history = new MatchHistory();
            history.playerId = String.valueOf(player.getId());
        }

        // Maintain max 3 matches (LinkedList for easy removal)
        LinkedList<Match> limited = new LinkedList<>(history.matchRecords);
        if (limited.size() == MAX_MATCHES) {
            limited.removeFirst(); // remove oldest
        }
        limited.add(newMatch);
        history.matchRecords = limited;

        try {
            writer.writeValue(file, history);
            System.out.println("Match saved for " + player.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Match> loadHistory(Player player) {
        File file = new File(BASE_DIR + player.getId() + ".json");

        if (!file.exists()) return new ArrayList<>();

        try {
            return mapper.readValue(file, new TypeReference<List<Match>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


}
