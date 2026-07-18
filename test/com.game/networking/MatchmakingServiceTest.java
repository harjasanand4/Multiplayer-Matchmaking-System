package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MatchmakingService without using Mockito
 */
public class MatchmakingServiceTest {

    private GameServer gameServer;
    private MatchmakingService matchmakingService;
    private TestClientHandler client1Handler;
    private TestClientHandler client2Handler;
    private Player player1;
    private Player player2;

    @BeforeEach
    public void setUp() {
        // Create a test implementation of GameServer
        gameServer = new TestGameServer();

        // Create the matchmaking service with our test game server
        matchmakingService = new MatchmakingService(gameServer);

        // Create test client handlers
        client1Handler = new TestClientHandler();
        client2Handler = new TestClientHandler();

        // Create test players with different IDs
        player1 = new Player(UUID.randomUUID().toString(), "TestPlayer1", "player1@test.com", "password1", client1Handler);
        player2 = new Player(UUID.randomUUID().toString(), "TestPlayer2", "player2@test.com", "password2", client2Handler);

        // Set the player in the client handler
        client1Handler.setPlayer(player1);
        client2Handler.setPlayer(player2);
    }

    @Test
    public void testJoinRandomQueue() {
        // Add player to queue
        matchmakingService.joinRandomQueue(player1, GameType.TICTACTOE);

        // Check if the client received a queue status message
        assertTrue(client1Handler.getLastMessage().contains("QUEUE_STATUS"));
        assertTrue(client1Handler.getLastMessage().contains("Searching for a random opponent"));
    }

    @Test
    public void testLeaveQueue() {
        // Add player to queue
        matchmakingService.joinRandomQueue(player1, GameType.TICTACTOE);

        // Leave queue
        matchmakingService.leaveQueue(player1.getID());

        // Check if the client received a queue left message
        assertTrue(client1Handler.getLastMessage().contains("QUEUE_LEFT"));
    }


    @Test
    public void testNoMatchForDifferentGameTypes() {
        // Add two players wanting different game types
        matchmakingService.joinRandomQueue(player1, GameType.TICTACTOE);
        matchmakingService.joinRandomQueue(player2, GameType.CONNECT4);

        // Process queue immediately
        List<MatchmakingService.MatchedPair> matches = matchmakingService.processImmediateMatch();

        // Verify no matches were found
        assertEquals(0, matches.size());
    }

    @Test
    public void testMatchNotification() {
        // Test that match notification works
        String sessionId = "test-session";

        // Notify player 1 of match with player 2
        matchmakingService.notifyMatchFound(player1, player2, sessionId, GameType.CHECKERS);

        // Check if player 1 received the match notification
        String message = client1Handler.getLastMessage();
        assertTrue(message.contains("MATCH_FOUND"));
        assertTrue(message.contains(sessionId));
        assertTrue(message.contains(player2.getID()));
    }

    /**
     * Test implementation of ClientHandler for testing
     */
    private static class TestClientHandler extends ClientHandler {
        private String lastMessage;
        private Player player;

        public TestClientHandler() {
            super(null, null); // Passing null as we won't use the actual socket or server
        }

        @Override
        public void sendMessage(String message) {
            this.lastMessage = message;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        @Override
        public Player getPlayer() {
            return player;
        }
    }

    /**
     * Test implementation of GameServer for testing
     */
    private static class TestGameServer extends GameServer {
        public TestGameServer() {
            super(0); // port doesn't matter for testing
        }

        @Override
        public void matchPlayers(Player p1, Player p2, GameType gameType) {
            // Do nothing for the test
        }

        @Override
        public Player getPlayerById(String playerId) {
            return null; // Not used in tests
        }
    }
}