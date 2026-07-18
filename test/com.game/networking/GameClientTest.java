package com.game.networking;

import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameClientTest{
    private final String SESSION_ID = "test-session";
    private final GameType GAME_TYPE = GameType.TICTACTOE;

    private GameSession gameSession;


    @Test
public void GameClientTest() {

        String testPlayerId = "2222";
        String testUsername = "TestPlayer";
        gameSession = new GameSession(SESSION_ID,GAME_TYPE){

        };
        ClientHandler clientHandler = new ClientHandler(null, new GameServer(8888));



        GameClientStub gameClientStub = new GameClientStub(  // Create the GameClientStub instance to test
                testPlayerId,
                testUsername,
                gameSession,
                clientHandler
        );


        Player player = clientHandler.getPlayer();   // Test that the Player was created with correct values
        assertNotNull(player);//player should not be empty
        assertEquals(testPlayerId, player.getID()); // player Id matches
        assertEquals(testUsername, player.getUsername());//player username matches
        assertEquals(testUsername + "@gmail.com", player.getEmail());//player email matches


        assertEquals(player, clientHandler.getPlayer());//test client handler is set to the right player



        String testMove = "A1";
        gameClientStub.sendMove(testMove);// Test the sendMove method


        List<String> messagesSent = clientHandler.getMessagesSent();
        assertNotNull(messagesSent);  // messages should not be empty
        assertTrue(messagesSent.contains(testMove));//check if test move is in the list
    }
}
