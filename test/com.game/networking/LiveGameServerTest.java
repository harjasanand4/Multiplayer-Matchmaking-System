package com.game.networking;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class LiveGameServerTest {
    private GameServer server;
  
    private Player player1;
    private Player player2;


    @BeforeEach
    void setup() throws Exception {
        int port = 8000 + new Random().nextInt(1000);
        server = new GameServer(port);
        new Thread(() -> server.start()).start();

        Thread.sleep(500);

        // Connect 2 sockets
        Socket senderSocket = new Socket("localhost", port);
        Socket recipientSocket = new Socket("localhost", port);

        PrintWriter outSender = new PrintWriter(senderSocket.getOutputStream(), true);
        PrintWriter outRecipient = new PrintWriter(recipientSocket.getOutputStream(), true);

        outSender.println("Alice,alice@mail.com,pass,sender");
        outRecipient.println("Bob,bob@mail.com,pass,recipient");

        Thread.sleep(500); // Let server register players

        player1 = server.getPlayerById("sender");
        player2 = server.getPlayerById("recipient");

    }

    @Test
    void Start() throws Exception{
        assertTrue(server.isRunning());
    }


    @Test
    void testCreateTicTacToeGame(){
        GameSession session = server.createGameSession(GameType.TICTACTOE,player1,player2);

        assertNotNull(session,"game is not null");
        assertTrue(session instanceof TicTacToeSession);
        assertNotNull(session.getSessionID());

        TicTacToeSession ticTacToeSession = (TicTacToeSession) session;
        assertEquals(player1,ticTacToeSession.getPlayer1());
        assertEquals(player2,ticTacToeSession.getPlayer2());

    }

    @Test
    void testCreateConnectFourGame(){
        GameSession session = server.createGameSession(GameType.CONNECT4,player1,player2);

        assertNotNull(session,"game is not null");
        assertTrue(session instanceof ConnectFourSession);
        assertNotNull(session.getSessionID());

        ConnectFourSession connectFourSession = (ConnectFourSession) session;
        assertEquals(player1,connectFourSession.getPlayer1());
        assertEquals(player2,connectFourSession.getPlayer2());
    }

    @Test
    void testCreateCheckersGame(){
        GameSession session = server.createGameSession(GameType.CHECKERS,player1,player2);

        assertNotNull(session,"game is not null");
        assertTrue(session instanceof CheckerSession);
        assertNotNull(session.getSessionID());

        CheckerSession checkerSession = (CheckerSession) session;
        assertEquals(player1,checkerSession.getPlayer1());
        assertEquals(player2,checkerSession.getPlayer2());
    }




    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }
}


