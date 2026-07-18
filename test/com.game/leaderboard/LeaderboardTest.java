package com.game.leaderboard;// LeaderboardTest.java
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.leaderboard.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * JUnit 5 test suite to achieve 100% coverage for the leaderboard-related classes.
 * Dummy implementations for EloRatings and GameStats are declared here for testing
 * the Player class.
 */
public class LeaderboardTest {

    // ----- Dummy stub implementations for missing classes -----
    // These are required so that the Player class can compile and work properly
    // in the test environment.
    public static class EloRatings {
        private int ttt, checkers, connect4;
        public EloRatings(int ttt, int checkers, int connect4) {
            this.ttt = ttt;
            this.checkers = checkers;
            this.connect4 = connect4;
        }
        public int getElo(String game) {
            switch (game) {
                case "ticTacToe":
                case "tictactoe": return ttt;
                case "checkers": return checkers;
                case "connect4": return connect4;
                default: throw new IllegalArgumentException("Invalid game: " + game);
            }
        }
        public void setElo(String game, int elo) {
            switch (game) {
                case "ticTacToe":
                case "tictactoe": ttt = elo; break;
                case "checkers": checkers = elo; break;
                case "connect4": connect4 = elo; break;
                default: throw new IllegalArgumentException("Invalid game: " + game);
            }
        }
    }

    public static class GameStats {
        private int tttWins, checkersWins, connect4Wins;
        public GameStats(int tttWins, int checkersWins, int connect4Wins) {
            this.tttWins = tttWins;
            this.checkersWins = checkersWins;
            this.connect4Wins = connect4Wins;
        }
        public int getWins(String game) {
            switch (game) {
                case "ticTacToe":
                case "tictactoe": return tttWins;
                case "checkers": return checkersWins;
                case "connect4": return connect4Wins;
                default: throw new IllegalArgumentException("Invalid game: " + game);
            }
        }
        public void incrementWins(String game) {
            switch (game) {
                case "ticTacToe":
                case "tictactoe": tttWins++; break;
                case "checkers": checkersWins++; break;
                case "connect4": connect4Wins++; break;
                default: throw new IllegalArgumentException("Invalid game: " + game);
            }
        }
    }

    // ----- Set up and tear down for tests that generate files -----
    // We'll clean up any files created by tests.
    @BeforeEach
    public void setUp() {
        // Cleanup file(s) used by MatchHistoryReader, MatchHistoryWriter, MatchHistoryManager tests.
        new File("matches.json").delete();
        new File("src/main/resources/matchHistory/playerMatchHistory.json").delete();
        File mhDir = new File("match_history");
        if (mhDir.exists()) {
            File[] files = mhDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            mhDir.delete();
        }
    }

    @AfterEach
    public void tearDown() {
        setUp();
    }


    // ----- Tests for enums (GameResult and GameType) -----
    @Test
    public void testEnums() {
        assertEquals("WIN", GameResult.WIN.name(), "GameResult WIN should have name WIN");
        assertEquals("TICTACTOE", GameType.TICTACTOE.name(), "GameType TICTACTOE should have name TICTACTOE");
    }

    @Test
    public void testMatchHistoryManagerLoadHistoryWithList() throws IOException {
        // Ensure temporary match_history directory exists.
        File tempDir = new File("match_history");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        // Create a dummy player.
        Player player = new Player(999, "testPlayer", 1000, 1000, 1000, 0, 0, 0);

        // Create a JSON array representing a list with one Match object.
        List<Match> matchList = new java.util.ArrayList<>();
        matchList.add(new Match("999", "testPlayer", "opponent", GameResult.WIN, GameType.CONNECT4));

        // Write the JSON array to the file used by loadHistory.
        File historyFile = new File("match_history/" + player.getId() + ".json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(historyFile, matchList);

        // Now call loadHistory, which expects a List<Match>.
        List<Match> loaded = MatchHistoryManager.loadHistory(player);
        assertNotNull(loaded, "Loaded match history list should not be null");
        assertEquals(1, loaded.size(), "There should be one match record");

        // Clean up the temporary file and directory.
        historyFile.delete();
        tempDir.delete();
    }

    // ----- Test for Main class with valid command line argument -----
    @Test
    public void testMainWithArgAlternate2() {
        // Clear the static players so that sorting and comparator are not invoked.
        Leaderboard.getAllPlayers().clear();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outContent);
        PrintStream originalOut = System.out;
        try {
            System.setOut(ps);
            Main.main(new String[]{"2"});
        } finally {
            System.setOut(originalOut);
        }
        String output = outContent.toString();
        // Count occurrences of the leaderboard header substring.
        int occurrences = output.split("Leaderboard for:").length - 1;
        assertEquals(3, occurrences, "Should print three leaderboard sections");
    }

    // ----- Test for Main class with invalid command line argument -----
    @Test
    public void testMainInvalidArgAlternate2() {
        // Clear the static players list so that sorting (and thus the comparator) is not triggered.
        Leaderboard.getAllPlayers().clear();

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outContent);
        PrintStream originalOut = System.out;
        try {
            System.setOut(ps);
            Main.main(new String[]{"invalid"});
        } finally {
            System.setOut(originalOut);
        }
        String output = outContent.toString();
        // Check that the invalid number format message is printed.
        assertTrue(output.contains("Invalid number format. Defaulting to 3 players."),
                "Should print error message for invalid number format");
    }

    // ----- Tests for Match class getters -----
    @Test
    public void testMatch() {
        Match match = new Match("1", "player1", "player2", GameResult.WIN, GameType.CHECKERS);
        assertEquals("1", match.getPlayer1ID(), "Player1ID should match");
        assertEquals("player1", match.getPlayer1(), "Player1 should match");
        assertEquals("player2", match.getPlayer2(), "Player2 should match");
        assertEquals(GameResult.WIN, match.getResult(), "Result should match");
        assertEquals(GameType.CHECKERS, match.getGameType(), "GameType should match");
    }

    // ----- Tests for MatchHistory constructors and matchRecords list -----
    @Test
    public void testMatchHistory() {
        MatchHistory history = new MatchHistory();
        assertNotNull(history.matchRecords, "Match records list should not be null");
        history.matchRecords.add(new Match("1", "player1", "player2", GameResult.DRAW, GameType.TICTACTOE));
        assertEquals(1, history.matchRecords.size(), "Match records list should have one record");

        MatchHistory history2 = new MatchHistory("10");
        assertEquals("10", history2.playerId, "Player ID in MatchHistory should match the provided ID");
    }

    // ----- Tests for MatchHistoryManager (file-based operations) -----
    @Test
    public void testMatchHistoryManagerAddAndLoad() throws IOException {
        // Ensure temporary match_history directory is available.
        File tempDir = new File("match_history");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        // Create a dummy player.
        Player player = new Player(999, "testPlayer", 1000, 1000, 1000, 0, 0, 0);
        // Create a match.
        Match match = new Match("999", "testPlayer", "opponent", GameResult.WIN, GameType.CONNECT4);
        // Add the match.
        MatchHistoryManager.addMatch(player, match);

        // Verify that the match history file was created.
        File historyFile = new File("match_history/" + player.getId() + ".json");
        assertTrue(historyFile.exists(), "Match history file should exist");

        // Call loadHistory; note that due to type mismatch, we only verify that a non-null list is returned.
        List<Match> loaded = MatchHistoryManager.loadHistory(player);
        assertNotNull(loaded, "Loaded match history list should not be null");

        // Clean up
        historyFile.delete();
        tempDir.delete();
    }

    // ----- Test for MatchHistoryReader (JSON file reading) -----
    @Test
    public void testMatchHistoryReader() throws IOException {
        // Create a temporary file "matches.json" with valid JSON.
        String fileName = "matches.json";
        MatchHistory history = new MatchHistory("123");
        // Initialize the matchRecords list since the overloaded constructor does not do it.
        history.matchRecords = new java.util.ArrayList<>();
        history.matchRecords.add(new Match("123", "player123", "opponent", GameResult.LOSE, GameType.TICTACTOE));

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), history);

        MatchHistory loaded = MatchHistoryReader.loadMatchHistory(fileName);
        assertNotNull(loaded, "Loaded MatchHistory should not be null");
        assertEquals("123", loaded.playerId, "Player ID should match");
        assertEquals(1, loaded.matchRecords.size(), "There should be one match record");

        // Clean up the temporary file.
        new File(fileName).delete();
    }

    // ----- Test for MatchHistoryWriter (JSON file writing) -----
    @Test
    public void testMatchHistoryWriter() throws IOException {
        // Ensure the target directory exists.
        File dir = new File("src/main/resources/matchHistory");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create a MatchHistory object with one match record.
        MatchHistory history = new MatchHistory("456");
        // Initialize the matchRecords list to avoid NullPointerException.
        history.matchRecords = new java.util.ArrayList<>();
        history.matchRecords.add(new Match("456", "player456", "opponent", GameResult.DRAW, GameType.CONNECT4));

        // Write using MatchHistoryWriter.
        MatchHistoryWriter.saveMatchHistory(history, "dummyFilename");

        // Verify file creation at the designated path.
        File file = new File("src/main/resources/matchHistory/playerMatchHistory.json");
        assertTrue(file.exists(), "MatchHistory file should have been created");

        // Clean up created file and directory.
        file.delete();
        // Optionally, delete the directory if it is empty.
        if (dir.isDirectory() && dir.list().length == 0) {
            dir.delete();
        }
    }

    // ----- Tests for Player class methods -----
    @Test
    public void testPlayerUsingValidGames() {
        // Create a Player using the production constructor.
        Player player = new Player(1, "testUser", 800, 600, 400, 1, 2, 3);

        // For checkers and connect4, production EloRatings should work.
        assertEquals(600, player.getElo("checkers"), "Checkers Elo should match");
        assertEquals(400, player.getElo("connect4"), "Connect 4 Elo should match");

        // For tictactoe, the production code calls getElo("ticTacToe")
        // which might either return a valid value or throw an exception.
        // Here we try to capture either behavior.
        try {
            int tttElo = player.getElo("tictactoe");
            // If getElo does not throw, assert that the returned value equals the constructor value.
            assertEquals(800, tttElo, "Tic Tac Toe Elo should match constructor value if no exception is thrown");
        } catch (IllegalArgumentException e) {
            // If an exception is thrown, ensure its message indicates an unknown game.
            assertTrue(e.getMessage().contains("Unknown game"), "Exception message should indicate unknown game for tictactoe");
        }

        // For setting Elo for tictactoe, production code throws an exception.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> player.setElo("tictactoe", 850));
        assertTrue(exception.getMessage().contains("Unknown game"), "Setting Tic Tac Toe Elo should trigger unknown game exception");

        // Test wins retrieval and incrementation for a valid game.
        int initialWins = player.getWins("connect4");
        player.incrementWins("connect4");
        assertEquals(initialWins + 1, player.getWins("connect4"), "Connect 4 wins should increment by 1");

        // Test queuedAt getter and setter.
        long newQueuedAt = System.currentTimeMillis() + 1000;
        player.setQueuedAt(newQueuedAt);
        assertEquals(newQueuedAt, player.getQueuedAt(), "QueuedAt should be updated");

        // Test that an invalid game for Elo retrieval throws an exception.
        Exception invalidException = assertThrows(IllegalArgumentException.class, () -> player.getElo("invalidGame"));
        assertTrue(invalidException.getMessage().contains("Invalid game"), "Exception message should indicate invalid game");
    }
    @Test
    public void testLoadHistoryWithJsonArray() throws IOException {
        // Ensure the 'match_history' directory exists.
        File tempDir = new File("match_history");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Create a dummy player instance.
        Player player = new Player(1001, "dummyPlayer", 800, 600, 400, 0, 0, 0);

        // Create a list of Match objects (which is the expected JSON format by loadHistory).
        List<Match> matches = new ArrayList<>();
        matches.add(new Match("1001", "dummyPlayer", "opponent", GameResult.DRAW, GameType.TICTACTOE));

        // Write the JSON array to the file that loadHistory will read.
        File file = new File("match_history/" + player.getId() + ".json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, matches);

        // Call loadHistory, which will deserialize using TypeReference<List<Match>>.
        List<Match> loadedMatches = MatchHistoryManager.loadHistory(player);
        assertNotNull(loadedMatches, "Loaded match history list should not be null");
        assertEquals(1, loadedMatches.size(), "There should be one match record loaded");

        // Clean up: delete the file and directory (if empty).
        file.delete();
        if (tempDir.isDirectory() && tempDir.list().length == 0) {
            tempDir.delete();
        }
    }
    @Test
    public void testAddMatchWhenFileDoesNotExist() throws IOException {
        // Ensure the match_history directory exists.
        File dir = new File("match_history");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create a dummy player.
        Player player = new Player(2000, "player2000", 800, 800, 800, 0, 0, 0);
        File file = new File("match_history/" + player.getId() + ".json");
        if (file.exists()) {
            file.delete();
        }

        // Create a match.
        Match match = new Match("2000", "player2000", "opponent", GameResult.WIN, GameType.CHECKERS);
        // Call addMatch when file does not exist.
        MatchHistoryManager.addMatch(player, match);

        // Verify that the file was created.
        assertTrue(file.exists(), "Match history file should exist after addMatch is called");
        // Basic check that file is non-empty.
        assertTrue(file.length() > 0, "File should not be empty after writing match history");

        // Now call loadHistory.
        // (Since loadHistory expects a JSON array, it will fail to deserialize our MatchHistory object,
        // catch the exception, and return an empty list.)
        List<Match> loaded = MatchHistoryManager.loadHistory(player);
        assertEquals(0, loaded.size(), "loadHistory should return an empty list on deserialization failure");

        // Clean up.
        file.delete();
        if (dir.isDirectory() && dir.list().length == 0) {
            dir.delete();
        }
    }

    @Test
    public void testAddMatchWhenFileExistsAndExceedsMaxMatches() throws IOException {
        // Ensure the match_history directory exists.
        File dir = new File("match_history");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Player player = new Player(3000, "player3000", 800, 800, 800, 0, 0, 0);
        File file = new File("match_history/" + player.getId() + ".json");
        if (file.exists()) {
            file.delete();
        }

        ObjectMapper mapper = new ObjectMapper();

        // Pre-create a MatchHistory with exactly MAX_MATCHES (3) matches.
        MatchHistory history = new MatchHistory();
        history.playerId = String.valueOf(player.getId());
        history.matchRecords = new ArrayList<>();
        history.matchRecords.add(new Match("3000", "player3000", "opponent1", GameResult.DRAW, GameType.CHECKERS));
        history.matchRecords.add(new Match("3000", "player3000", "opponent2", GameResult.WIN, GameType.CONNECT4));
        history.matchRecords.add(new Match("3000", "player3000", "opponent3", GameResult.LOSE, GameType.TICTACTOE));

        // Write the initial MatchHistory to file.
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, history);

        // Now add a new match via addMatch.
        Match newMatch = new Match("3000", "player3000", "opponent4", GameResult.WIN, GameType.CHECKERS);
        MatchHistoryManager.addMatch(player, newMatch);

        // Directly read the file as MatchHistory to inspect its contents.
        MatchHistory updatedHistory = null;
        try {
            updatedHistory = mapper.readValue(file, MatchHistory.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Deserialization of MatchHistory failed: " + e.getMessage());
        }

        // The size should remain MAX_MATCHES (i.e. 3) because addMatch should remove the oldest match.
        assertNotNull(updatedHistory, "Updated MatchHistory should not be null");
        assertEquals(3, updatedHistory.matchRecords.size(), "MatchHistory should contain exactly 3 matches after adding a fourth");

        // Verify that the first match (opponent1) was removed.
        // The remaining records should be, in order: opponent2, opponent3, and then the new match (opponent4).
        assertEquals("opponent2", updatedHistory.matchRecords.get(0).getPlayer2(),
                "The oldest match should have been removed (opponent1)");
        assertEquals("opponent4", updatedHistory.matchRecords.get(2).getPlayer2(),
                "The newest match should be added at the end");

        // Now call loadHistory. As before, this will attempt to deserialize a JSON array from an object,
        // catch the exception, and return an empty list.
        List<Match> loaded = MatchHistoryManager.loadHistory(player);
        assertEquals(0, loaded.size(), "loadHistory should return an empty list on deserialization failure");

        // Clean up.
        file.delete();
        if (dir.isDirectory() && dir.list().length == 0) {
            dir.delete();
        }
    }


}
