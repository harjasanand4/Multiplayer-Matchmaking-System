package com.game.networking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GameServerTest {
    private GameServer server;
    private ClientHandler senderHandler;
    private ClientHandler recipientHandler;
    private Player sender;
    private Player recipient;

    private int testPort;


    @BeforeEach
    void setup() throws Exception {
        // Create a dummy GameServer.
        server = new GameServer(8888 + new Random().nextInt(1000));

        // Create dummy client handlers.
        // Since these tests don't rely on a live socket, we can pass null or a dummy Socket.
        senderHandler = new ClientHandler(null, server);
        recipientHandler = new ClientHandler(null, server);

        // Create dummy players. (Assuming your Player constructor is: Player(String id, String username, String email, String password, ClientHandler handler))
        sender = new Player("sender", "Alice", "alice@mail.com", "pass", senderHandler);
        recipient = new Player("recipient", "Bob", "bob@mail.com", "pass", recipientHandler);

        // Set the players into their respective handlers.
        senderHandler.setPlayer(sender);
        recipientHandler.setPlayer(recipient);

        // I NEED TO ADD THE PLAYERS TO THE CONNECTED CLIENTS HASHMAP

        // Add players to the server maps. (WE DONT HAVE SUCH FUNCTIONS. SHOULD WE ADD THEM FOR TESTING?????)
        server.getLobby().put(sender.getID(), sender);
        server.getLobby().put(recipient.getID(), recipient);
        server.getConnectedClients().put(sender.getID(), senderHandler);
        server.getConnectedClients().put(recipient.getID(), recipientHandler);
    }

    @AfterEach
    void teardown() {
        server = null;
        senderHandler = null;
        recipientHandler = null;
        sender = null;
        recipient = null;
    }

    @Test
    void startTest() {
        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();
        assertTrue(server.isRunning());
        server.stop();
        assertFalse(server.isRunning());
    }

//    @Test
//    void broadcastTest() {
//        Chat message = new Chat("testing123", sender, recipient);
//        // the following code is from https://stackoverflow.com/questions/32241057/how-to-test-a-print-method-in-java-using-junit
//        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//        System.setOut(new PrintStream(outContent));
//
//        server.broadcastMessage(message);
//        ;
//
//        String expectedOutput = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + sender.getUsername() + ": " + "testing123\n"; // Notice the \n for new line.
//
//        // Do the actual assertion.
//        assertEquals(expectedOutput, outContent.toString());
//
//
//    }

    @Test
    void sendInviteTest() {
        server.sendInvite(sender.getID(), recipient.getID(), GameType.TICTACTOE);
        InviteRequest sentRequest = (InviteRequest) recipient.getPendingRequests().getLast();
        assertEquals(recipient.getID(), sentRequest.getRecipient().getID());
        assertEquals(sender.getID(), sentRequest.getSender().getID());
        assertEquals(GameType.TICTACTOE, sentRequest.getGameType());
    }

    @Test
    void rejectInviteTest() {
        server.sendInvite(sender.getID(), recipient.getID(), GameType.CONNECT4);
        server.handleInvite(sender.getID(), recipient.getID(), RequestStatus.REJECTED);
        InviteRequest sentRequest = (InviteRequest) recipient.getPendingRequests().getLast();
        assertEquals(RequestStatus.REJECTED, sentRequest.getStatus());
    }

    @Test
    void acceptInviteTest() {
        server.sendInvite(sender.getID(), recipient.getID(), GameType.CONNECT4);
        server.handleInvite(sender.getID(), recipient.getID(), RequestStatus.ACCEPTED);
        InviteRequest receivedRequest = (InviteRequest) recipient.getPendingRequests().getLast();
        assertEquals(RequestStatus.ACCEPTED, receivedRequest.getStatus());
    }



}


