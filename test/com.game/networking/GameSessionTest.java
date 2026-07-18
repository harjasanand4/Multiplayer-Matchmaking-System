package com.game.networking;

import com.game.networking.*;
import com.sendgrid.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    private ClientHandler clientHandler1;
    private ClientHandler clientHandler2;
    private Player player1;
    private Player player2;
    private GameServer server;
    private GameSession gameSession;

    private final String SESSION_ID = "test-session";
    private final GameType GAME_TYPE = GameType.TICTACTOE;

    @BeforeEach
    public void setUp() {
        server = new GameServer(8888+ new Random().nextInt(1000));// Create a test game server


        clientHandler1 = new ClientHandler(null, server);// Create client handlers (without actual sockets)
        clientHandler2 = new ClientHandler(null, server);


        player1 = new Player("0000", "Alice", "alice@mail.com", "pass", clientHandler1);// Create players
        player2 = new Player("1111", "Bob", "bob@mail.com", "pass", clientHandler2);


        clientHandler1.setPlayer(player1);// Connect players to client handlers
        clientHandler2.setPlayer(player2);
        clientHandler1.setClientId("player1");
        clientHandler2.setClientId("player2");


        gameSession = new GameSession(SESSION_ID, GAME_TYPE) {// Create a test game session

        };
        gameSession.addPlayers(player1, player2);
    }
    @Test
    public void testUpdateMovePlayer1() {

        gameSession.start(clientHandler1, clientHandler2);
        gameSession.currentPlayer = player1; // Set current player to player1


        String moveData = "A1";// Call the UpdateMove method
        boolean result = gameSession.UpdateMove(player1.getID(), moveData);


        List<String> player1Messages = clientHandler1.getMessagesSent();// Verify messages were sent to both players by checking messagesSent lists
        List<String> player2Messages = clientHandler2.getMessagesSent();

        String ExpectedMove = "Move:"+ player1.getID()+":"+ moveData;

        assertTrue(player1Messages.contains(ExpectedMove));
        assertTrue(player2Messages.contains(ExpectedMove));


        assertTrue(player1Messages.contains("Opponents turn"));// Verify turn messages
        assertTrue(player2Messages.contains("Now it's your turn"));


        assertEquals(player2, gameSession.currentPlayer);// Verify that the current player was switched to player2


        assertTrue(result);// Verify the result
    }

    @Test
    void testRemovePlayers(){
        assertEquals(player1,gameSession.player1);  //check if player1 and player 1 in the game session are the same
        assertEquals(player1,gameSession.player1);
        gameSession.removePlayers();  //call remove players
        assertNull(gameSession.player1);  // verify players are removed
        assertNull(gameSession.player2);
    }

    @Test
    void EndGameTest(){
        gameSession.start(clientHandler1,clientHandler2);  //start game using these two client handlers

        gameSession.endGame(); //call end game method
        assertFalse(gameSession.isActive());//check if game ended
    }



}