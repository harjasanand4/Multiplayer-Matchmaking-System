package com.game.networking;

import com.game.networking.*;
import com.sendgrid.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.Socket;
import java.util.List;
import java.util.UUID;


public class TicTacToeSessionTest {

    private ClientHandler player1Handler;
    private ClientHandler player2Handler;

    private GameServer server;

    private Player player1;
    private Player player2;

    private TicTacToeSession ticTacToeSession;


        @BeforeEach
        public void setUp() {


            player1Handler = new ClientHandler(null, server);  //use dummy socket
            player2Handler = new ClientHandler(null, server);


            player1 = new Player("0000", "Alice", "alice@mail.com", "pass", player1Handler);// Create players
            player2 = new Player("1111", "Bob", "bob@mail.com", "pass", player2Handler);


            player1Handler.setPlayer(player1);//set client connections
            player2Handler.setPlayer(player2);



            ticTacToeSession = new TicTacToeSession("123", player1,player2) {  // create new tictactoe session between player1 and player 2

            };
            ticTacToeSession.addPlayers(player1, player2);  // add players into session
        }

    @Test
    public void testUpdateMove() {

        ticTacToeSession.start(player1Handler, player2Handler);// Start Tic Tac Toe Session

        String movePlayer1 = "A1";  // Test move from player1
        boolean resultPlayer1 = ticTacToeSession.UpdateMove("0000", movePlayer1); //store the result of update move from player 1


        assertTrue(resultPlayer1);// Verify result


        List<String> player1Messages = player1Handler.getMessagesSent();// Verify messages for player1's move
        List<String> player2Messages = player2Handler.getMessagesSent();

        assertTrue(player1Messages.contains("Move:0000:" + movePlayer1));
        assertTrue(player2Messages.contains("Move:0000:" + movePlayer1));

        assertTrue(player1Messages.contains("Opponents turn"));
        assertTrue(player2Messages.contains("Now it's your turn"));


        assertEquals(player2, ticTacToeSession.getPlayer2()); //test if player 1 has switched to player 2


        String movePlayer2 = "B2";// Test move from player2
        boolean resultPlayer2 = ticTacToeSession.UpdateMove("1111", movePlayer2); // Using player2's ID


        assertTrue(resultPlayer2); // check the result of the messages


        assertTrue(player1Messages.contains("Move:1111:" + movePlayer2));
        assertTrue(player2Messages.contains("Move:1111:" + movePlayer2));

        assertTrue(player1Messages.contains("Your turn"));
        assertTrue(player2Messages.contains("Opponents turn"));


        assertEquals(player1, ticTacToeSession.getPlayer1());// Verify current player changed back to player1
    }

}
