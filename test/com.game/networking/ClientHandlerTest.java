package com.game.networking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the entire com.game.networking.clienthandler class
 */
public class ClientHandlerTest {
    private Player sender;
    private Player recipient;
    private GameServer server;
    private ClientHandler senderHandler;
    private ClientHandler recipientHandler;

    @BeforeEach
    public void setup() {
        // Create a dummy GameServer.
        server = new GameServer(8888 + new Random().nextInt(1000));

        // Create dummy client handlers.
        senderHandler = new ClientHandler(null, server);
        recipientHandler = new ClientHandler(null, server);

        // Create dummy players. (Assuming your Player constructor is: Player(String id, String username, String email, String password, ClientHandler handler))
        sender = new Player("0000", "Alice", "alice@mail.com", "pass", senderHandler);
        recipient = new Player("1111", "Bob", "bob@mail.com", "pass", recipientHandler);

        senderHandler.setPlayer(sender);
        recipientHandler.setPlayer(recipient);

        senderHandler.setClientId("0000");
        recipientHandler.setClientId("1111");

        server.getLobby().put(sender.getID(), sender);
        server.getLobby().put(recipient.getID(), recipient);
        server.getConnectedClients().put(sender.getID(), senderHandler);
        server.getConnectedClients().put(recipient.getID(), recipientHandler);
    }

    /**
     * Tests regarding the getter functions in clientHandler class
     */
    @Test
    public void testGetterFunctions() {
        //assertTrue(senderHandler.isConnected());
        assertEquals(sender, senderHandler.getPlayer());
        assertTrue(senderHandler.isRunning());
        senderHandler.setClientId("123456");
        assertEquals("123456", senderHandler.getClientId());
    }
    /**
     * Tests if reconnection feature correctly changes connection state from disconnected to reconnected to connected
     */
    @Test
    public void testReconnection() {
        sender.getClientHandler().disconnect();
        sender.getClientHandler().reconnect();
        assertEquals(ConnectionState.CONNECTED, sender.getClientHandler().getConnectionState());
    }

    /**
     * Tests if disconnection feature correctly changes connection state to disconnected
     */
    @Test
    public void testDisconnection() {
        sender.getClientHandler().disconnect();
        assertEquals(ConnectionState.DISCONNECTED, sender.getClientHandler().getConnectionState());
    }

    @Test
    void sendFriendRequestTest() {
        senderHandler.sendFriendRequest("1111");

        assertTrue(!recipient.getPendingRequests().isEmpty());
    }

    @Test
    void acceptFriendRequestTest() {
        // send the request first
        senderHandler.sendFriendRequest("1111");

        recipientHandler.acceptFriendRequest("0000");
        assertTrue(sender.getFriendList().contains(recipient));
        assertTrue(recipient.getFriendList().contains(sender));

        assertTrue(recipient.getPendingRequests().isEmpty());
    }

    @Test
    void declineFriendRequestTest() {
        // send the request first
        senderHandler.sendFriendRequest("1111");

        recipientHandler.declineFriendRequest("0000");
        assertTrue(!sender.getFriendList().contains(recipient));
        assertTrue(!recipient.getFriendList().contains(sender));

        assertTrue(recipient.getPendingRequests().isEmpty());
    }

    @Test
    void testMonitorConnectionHealthOutput() {

        ConnectionHealth connectionHealth = new ConnectionHealth();

        connectionHealth.recordLatency("0000", 50);                     // Simulate 50ms latency.
        connectionHealth.recordPacketLoss("0000", 0);      // Simulate 0% packet loss.

        // Capture the System.out output.
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        // Call monitorConnectionHealth to print its status.
        senderHandler.monitorConnectionHealth(connectionHealth);

        // Flush and restore original System.out.
        System.out.flush();
        System.setOut(originalOut);

        String output = outContent.toString();
        System.out.print("Captured output: " + output);

        assertEquals("\rLatency:50ms | Packet Loss:0.0% | Connection is strong!",output);

    }

    @Test
    public void testStop(){
        assertEquals(ConnectionState.CONNECTED,senderHandler.getConnectionState());

        senderHandler.stop(); //call stop functions in clienthandler

        assertEquals(ConnectionState.DISCONNECTED,senderHandler.getConnectionState());

        assertFalse(senderHandler.isRunning()); //check the client handler stop
    }

}





