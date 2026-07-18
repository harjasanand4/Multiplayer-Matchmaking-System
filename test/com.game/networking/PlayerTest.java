package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.player class
 */
public class PlayerTest {
    private Player player;
    private Player player2;
    private GameServer server;
    private ClientHandler senderHandler;
    private ClientHandler recipientHandler;
    private FriendRequest friendRequest;

    /**
     * Sets up a gameServer and players objects before all tests are run
     */
    @BeforeEach
    public void setUp() throws Exception {
        // Create a dummy GameServer.
        server = new GameServer(8888 + new Random().nextInt(1000));

        // Create dummy client handlers.
        senderHandler = new ClientHandler(null, server);
        recipientHandler = new ClientHandler(null, server);

        // Create dummy players. (Assuming your Player constructor is: Player(String id, String username, String email, String password, ClientHandler handler))
        player = new Player("sender", "Alice", "alice@mail.com", "pass", senderHandler);
        player2 = new Player("recipient", "Bob", "bob@mail.com", "pass", recipientHandler);

        senderHandler.setPlayer(player);
        recipientHandler.setPlayer(player2);

        server.getLobby().put(player.getID(), player);
        server.getLobby().put(player2.getID(), player2);
        server.getConnectedClients().put(player.getID(), senderHandler);
        server.getConnectedClients().put(player2.getID(), recipientHandler);
    }

    /**
     * Tests regarding the getter functions in player class
     */
    @Test
    public void testGetterFunctions() {
        assertEquals("sender", player.getID());
        assertEquals("Alice", player.getUsername());
        assertEquals("alice@mail.com", player.getEmail());
        assertEquals("pass", player.getPassword());
        assertEquals(PlayerState.LOBBY, player.getState());
    }

    /**
     * Tests if setter to a new clientHandler works
     */
    @Test
    public void testClientHandlerUpdater() {
        GameServer newServer = new GameServer(8888);
        ClientHandler newHandler = new ClientHandler(null, newServer);
        player.setClientHandler(newHandler);
        assertEquals(newHandler, player.getClientHandler());
    }

    @Test
    public void testFriendListManagement() {
        // Initial state
        assertTrue(player.getFriendList().isEmpty());

        // Add friend
        player.addFriend(player2);
        assertEquals(1, player.getFriendList().size());
        assertTrue(player.getFriendList().contains(player2));

        // Remove friend
        player.removeFriend(player2);
        assertTrue(player.getFriendList().isEmpty());
    }

    /**
     * Tests if player can send message to server
     */
    @Test
    public void testSendMessage() {
        //WORKS IF THE GAMESERVER SYSTEM IS SET UP (NEEDS FIXING)
        player.sendMessage("Hello", player2);
    }

    /**
     * Tests if player can change their state (Lobby, In-Game, Matchmaking)
     */
    @Test
    public void testPlayerStateChange() {
        player.setState(PlayerState.IN_GAME);
        assertEquals(PlayerState.IN_GAME, player.getState());
    }

    /**
     * Tests if player can add pending requests
     */
    @Test
    public void testAddPendingRequests(){
        player.addPendingRequest(friendRequest);
        assertTrue(player.getPendingRequests().contains(friendRequest));
    }

    /**
     * Tests if player can remove pending requests
     */
    @Test
    public void testRemovePendingRequests(){
        player.addPendingRequest(friendRequest);
        player.removePendingRequest(friendRequest);
        assertTrue(!player.getPendingRequests().contains(friendRequest));
    }
}