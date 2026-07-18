package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameServer's matchmaking and player matching functionality
 */
public class GameServerMatchmakingTest {

    private TestGameServer gameServer;
    private TestClientHandler clientHandler1;
    private TestClientHandler clientHandler2;
    private Player player1;
    private Player player2;

    @BeforeEach
    public void setUp() {
        // Create a test implementation of GameServer
        gameServer = new TestGameServer();

        // Create test client handlers
        clientHandler1 = new TestClientHandler();
        clientHandler2 = new TestClientHandler();

        // Create test players with different IDs
        String player1Id = UUID.randomUUID().toString();
        String player2Id = UUID.randomUUID().toString();

        player1 = new Player(player1Id, "TestPlayer1", "player1@test.com", "password1", clientHandler1);
        player2 = new Player(player2Id, "TestPlayer2", "player2@test.com", "password2", clientHandler2);

        // Set players in handlers
        clientHandler1.setPlayer(player1);
        clientHandler2.setPlayer(player2);

        // Add players to lobby and connected clients
        gameServer.addToLobby(player1.getID(), player1);
        gameServer.addToLobby(player2.getID(), player2);
        gameServer.addToConnectedClients(player1.getID(), clientHandler1);
        gameServer.addToConnectedClients(player2.getID(), clientHandler2);

        // Set player state to LOBBY
        player1.setState(PlayerState.LOBBY);
        player2.setState(PlayerState.LOBBY);
    }

    @Test
    public void testMatchPlayers() {
        // Test matchPlayers method
        gameServer.matchPlayers(player1, player2, GameType.TICTACTOE);

        // Verify players are no longer in lobby
        assertFalse(gameServer.isInLobby(player1.getID()), "Player 1 should be removed from lobby");
        assertFalse(gameServer.isInLobby(player2.getID()), "Player 2 should be removed from lobby");

        // Verify players state has changed to IN_GAME
        assertEquals(PlayerState.IN_GAME, player1.getState(), "Player 1 state should be IN_GAME");
        assertEquals(PlayerState.IN_GAME, player2.getState(), "Player 2 state should be IN_GAME");

        // Verify that match found notifications were sent to both players
        assertTrue(clientHandler1.getLastMessage().contains("MATCH_FOUND"), "Player 1 should receive match found notification");
        assertTrue(clientHandler2.getLastMessage().contains("MATCH_FOUND"), "Player 2 should receive match found notification");

        // Verify game session was created
        assertNotNull(gameServer.getLastCreatedSession(), "Game session should be created");
        assertEquals(GameType.TICTACTOE, gameServer.getLastCreatedGameType(),
                "Game session should be of type TICTACTOE");
    }

    @Test
    public void testJoinRandomMatchmaking() {
        // Test joinRandomMatchmaking method
        gameServer.joinRandomMatchmaking(player1.getID(), GameType.TICTACTOE);

        // Verify player state has changed to MATCHMAKING
        assertEquals(PlayerState.MATCHMAKING, player1.getState(), "Player 1 state should be MATCHMAKING");

        // Add second player to matchmaking
        gameServer.joinRandomMatchmaking(player2.getID(), GameType.TICTACTOE);

        // Verify players are no longer in lobby
        assertFalse(gameServer.isInLobby(player1.getID()), "Player 1 should be removed from lobby");
        assertFalse(gameServer.isInLobby(player2.getID()), "Player 2 should be removed from lobby");

        // Verify player states have changed
        assertEquals(PlayerState.IN_GAME, player1.getState(), "Player 1 state should now be IN_GAME");
        assertEquals(PlayerState.IN_GAME, player2.getState(), "Player 2 state should now be IN_GAME");

        // Verify match notifications were sent
        assertTrue(clientHandler1.getLastMessage().contains("MATCH_FOUND"), "Player 1 should receive match found notification");
        assertTrue(clientHandler2.getLastMessage().contains("MATCH_FOUND"), "Player 2 should receive match found notification");
    }

    @Test
    public void testJoinRandomMatchmakingDifferentGameTypes() {
        // Add players to different game type queues
        gameServer.joinRandomMatchmaking(player1.getID(), GameType.TICTACTOE);
        gameServer.joinRandomMatchmaking(player2.getID(), GameType.CHECKERS);

        // Verify both players are still in MATCHMAKING state
        assertEquals(PlayerState.MATCHMAKING, player1.getState(), "Player 1 should still be in MATCHMAKING");
        assertEquals(PlayerState.MATCHMAKING, player2.getState(), "Player 2 should still be in MATCHMAKING");

        // Verify both players are still in lobby
        assertTrue(gameServer.isInLobby(player1.getID()), "Player 1 should still be in lobby");
        assertTrue(gameServer.isInLobby(player2.getID()), "Player 2 should still be in lobby");
    }

    @Test
    public void testLeaveMatchmaking() {
        // Add player to matchmaking
        gameServer.joinRandomMatchmaking(player1.getID(), GameType.TICTACTOE);

        // Verify player state has changed to MATCHMAKING
        assertEquals(PlayerState.MATCHMAKING, player1.getState(), "Player 1 state should be MATCHMAKING");

        // Player leaves matchmaking
        gameServer.leaveMatchmaking(player1.getID());

        // Verify player state has changed back to LOBBY
        assertEquals(PlayerState.LOBBY, player1.getState(), "Player 1 state should be back to LOBBY");
    }

    @Test
    public void testJoinMatchmakingInvalidState() {
        // Set player to IN_GAME state
        player1.setState(PlayerState.IN_GAME);

        // Try to join matchmaking
        gameServer.joinRandomMatchmaking(player1.getID(), GameType.TICTACTOE);

        // Verify player state hasn't changed
        assertEquals(PlayerState.IN_GAME, player1.getState(), "Player 1 state should still be IN_GAME");
    }

    private static class TestGameServer extends GameServer {
        private GameSession lastCreatedSession;
        private GameType lastCreatedGameType;
        private final Map<String, Player> testLobby = new ConcurrentHashMap<>();
        private final Map<String, ClientHandler> testConnectedClients = new ConcurrentHashMap<>();
        private final Map<String, GameType> lastRequestedGameType = new ConcurrentHashMap<>(); // New map to track game types
        private MatchmakingService matchmakingService;

        public TestGameServer() {
            super(0); // port doesn't matter for testing
            this.matchmakingService = new TestMatchmakingService(this);
        }

        public void addToLobby(String playerId, Player player) {
            testLobby.put(playerId, player);
        }

        public void addToConnectedClients(String playerId, ClientHandler handler) {
            testConnectedClients.put(playerId, handler);
        }

        public boolean isInLobby(String playerId) {
            return testLobby.containsKey(playerId);
        }

        @Override
        public Map<String, Player> getLobby() {
            return testLobby;
        }

        @Override
        public Map<String, ClientHandler> getConnectedClients() {
            return testConnectedClients;
        }

        @Override
        public Player getPlayerById(String playerId) {
            return testLobby.get(playerId);
        }

        @Override
        public GameSession createGameSession(GameType gameType, Player player1, Player player2) {
            lastCreatedSession = new TestGameSession(UUID.randomUUID().toString(),gameType);
            lastCreatedGameType = gameType;
            return lastCreatedSession;
        }

        @Override
        public void matchPlayers(Player p1, Player p2, GameType gameType) {
            // Remove players from lobby
            testLobby.remove(p1.getID());
            testLobby.remove(p2.getID());

            // Set players to IN_GAME state
            p1.setState(PlayerState.IN_GAME);
            p2.setState(PlayerState.IN_GAME);

            // Create the game session
            GameSession session = createGameSession(gameType, p1, p2);
            session.addPlayers(p1, p2);

            // Notify both players that a match has been found
            matchmakingService.notifyMatchFound(p1, p2, session.getSessionID(), gameType);
            matchmakingService.notifyMatchFound(p2, p1, session.getSessionID(), gameType);
        }

        @Override
        public void joinRandomMatchmaking(String playerId, GameType gameType) {
            Player player = getPlayerById(playerId);

            if (player != null && player.getState() == PlayerState.LOBBY) {
                // Player is in the lobby, they can join matchmaking
                player.setState(PlayerState.MATCHMAKING);

                // Store the game type this player requested
                lastRequestedGameType.put(playerId, gameType);

                // Check if there's another player in matchmaking for the same game type
                for (Player p : testLobby.values()) {
                    if (p != player && p.getState() == PlayerState.MATCHMAKING) {
                        // Only match if game types match
                        GameType otherPlayerGameType = lastRequestedGameType.get(p.getID());
                        if (gameType == otherPlayerGameType) {
                            matchPlayers(player, p, gameType);
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void leaveMatchmaking(String playerId) {
            Player player = getPlayerById(playerId);
            if (player != null && player.getState() == PlayerState.MATCHMAKING) {
                player.setState(PlayerState.LOBBY);
                // Clean up the game type tracking
                lastRequestedGameType.remove(playerId);
            }
        }

        public GameSession getLastCreatedSession() {
            return lastCreatedSession;
        }

        public GameType getLastCreatedGameType() {
            return lastCreatedGameType;
        }

        @Override
        public boolean isRunning() {
            return true;
        }
    }
    /**
     * Test implementation of MatchmakingService for testing
     */
    private static class TestMatchmakingService extends MatchmakingService {
        public TestMatchmakingService(GameServer gameServer) {
            super(gameServer);
        }

        @Override
        public void notifyMatchFound(Player player, Player opponent, String sessionId, GameType gameType) {
            String message = String.format("MATCH_FOUND|%s|%s|%s|%s",
                    sessionId,
                    opponent.getID(),
                    opponent.getUsername(),
                    gameType.toString());

            player.getClientHandler().sendMessage(message);
        }
    }

    /**
     * Test implementation of GameSession for testing
     */
    private static class TestGameSession extends GameSession {
        public TestGameSession(String sessionId ,GameType gameType) {
            super(sessionId,gameType);
        }

        @Override
        public void start(ClientHandler handler1, ClientHandler handler2) {
            // Just a stub for testing
        }
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
}