package com.game.gamelogic;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class Connect4Test {

    @Test
    public void timerTest(){
        Connect4Logic test = new Connect4Logic();
        test.startNewGame("Croy","Rhys");
        test.setActivePlayer("Croy");
        int temp = 1;
        String currentPlayer = test.getActivePlayer();
        boolean run = true;
        while (test.getActivePlayer().equals("Croy")){
            test = test;
            currentPlayer= test.getActivePlayer();

        }
        assertFalse(test.getActivePlayer().equals("Croy"));
        while (test.getActivePlayer().equals("Rhys")){
            test = test;
            currentPlayer= test.getActivePlayer();

        }
        assertFalse(test.getActivePlayer().equals("Rhys"));
    }

    @Test
    public void winPlace(){
        Connect4Logic test = new Connect4Logic();
        for (int i = 0; i < 10; i++) {
            test.startNewGame("Croy","Rhys");
            String temp = test.getActivePlayer();
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 1);


            assertTrue(temp.equals(test.getWinner()));
            if (temp.equals("Croy")){
                assertEquals(1, test.getGameState());
            }else {
                assertEquals(2, test.getGameState());
            }
        }

    }

    @Test
    public void testDraw(){
        Connect4Logic test = new Connect4Logic();
        test.startNewGame("Croy","Rhys");
        String temp = test.getActivePlayer();
        for (int i = 0; i < 2; i++) {
            test.placePiece(test.getActivePlayer(), 0);
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 3);
            test.placePiece(test.getActivePlayer(), 4);
            test.placePiece(test.getActivePlayer(), 5);
            test.placePiece(test.getActivePlayer(), 6);
        }
        for (int i = 0; i < 2; i++) {
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 3);
            test.placePiece(test.getActivePlayer(), 4);
            test.placePiece(test.getActivePlayer(), 5);
            test.placePiece(test.getActivePlayer(), 6);
            test.placePiece(test.getActivePlayer(), 0);
        }
        for (int i = 0; i < 2; i++) {
            test.placePiece(test.getActivePlayer(), 0);
            test.placePiece(test.getActivePlayer(), 1);
            test.placePiece(test.getActivePlayer(), 2);
            test.placePiece(test.getActivePlayer(), 3);
            test.placePiece(test.getActivePlayer(), 4);
            test.placePiece(test.getActivePlayer(), 5);
            test.placePiece(test.getActivePlayer(), 6);
        }

        assertEquals(3, test.getGameState());



    }

    @Test
    public void emptyBoardTest() {
        Connect4Board board = new Connect4Board();
        assertFalse(board.hasWon("colour"));
        assertFalse(board.boardFull());
    }

    @Test
    public void canPlayBoardTest() {
        Connect4Board board = new Connect4Board();
        assertTrue(board.canPlay(0, 0));
        assertTrue(board.canPlay(0, 6));
        assertFalse(board.canPlay(0, -1));
        assertFalse(board.canPlay(0, 7));
        for (int i = 0; i < 5; i++) {
            board.play("colour", 0);
        }
        assertTrue(board.canPlay(0, 0));
        board.play("colour", 0);
        assertFalse(board.canPlay(0, 0));
    }

    @Test
    public void boardFullTest() {
        Connect4Board board = new Connect4Board();
        assertFalse(board.boardFull());
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 6; i++) {
                board.play("colour", j);
            }
        }
        assertTrue(board.boardFull());
    }

    @Test
    public void autoMoveTest() {
        Connect4Board board = new Connect4Board();
        for (int j = 0; j < 7; j++) {
            for (int i = 0; i < 5; i++) {
                board.play("colour", j);
            }
        }
        board.autoMove("colour");
        boolean worked = false;
        for (int i = 0; i < 7; i++) {
            if (!board.canPlay(0, i)) {
                worked = true;
            }
        }
        assertTrue(worked);
    }

    @Test
    public void winRowBoardTest() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 0);
        board.play("colour", 1);
        board.play("colour", 2);
        board.play("colour", 3);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 4);
        board.play("opponent", 5);
        assertTrue(board.hasWon("colour"));
    }

    @Test
    public void winColumnBoardTest() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 0);
        board.play("colour", 0);
        board.play("colour", 0);
        board.play("colour", 0);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 0);
        board.play("opponent", 0);
        assertTrue(board.hasWon("colour"));
    }

    @Test
    public void winBackslashBoardTest1() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 0);
        board.play("opponent", 0);
        board.play("opponent", 0);
        board.play("opponent", 1);
        board.play("opponent", 1);
        board.play("opponent", 2);
        board.play("colour", 0);
        board.play("colour", 1);
        board.play("colour", 2);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 3);
        assertTrue(board.hasWon("colour"));
    }

    @Test
    public void winBackslashBoardTest2() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 4);
        board.play("opponent", 4);
        board.play("opponent", 4);
        board.play("opponent", 4);
        board.play("opponent", 5);
        board.play("opponent", 5);
        board.play("opponent", 5);
        board.play("opponent", 6);
        board.play("opponent", 6);
        board.play("colour", 3);
        board.play("colour", 4);
        board.play("colour", 5);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 6);
        assertTrue(board.hasWon("colour"));
    }

    @Test
    public void winForwardSlashBoardTest1() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 0);
        board.play("opponent", 0);
        board.play("opponent", 1);
        board.play("opponent", 1);
        board.play("opponent", 1);
        board.play("opponent", 2);
        board.play("opponent", 2);
        board.play("opponent", 2);
        board.play("opponent", 2);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("opponent", 3);
        board.play("colour", 0);
        board.play("colour", 1);
        board.play("colour", 2);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 3);
        assertTrue(board.hasWon("colour"));
    }

    @Test
    public void winForwardSlashBoardTest2() {
        Connect4Board board = new Connect4Board();
        board.play("opponent", 4);
        board.play("opponent", 5);
        board.play("opponent", 5);
        board.play("opponent", 6);
        board.play("opponent", 6);
        board.play("opponent", 6);
        board.play("colour", 3);
        board.play("colour", 4);
        board.play("colour", 5);
        assertFalse(board.hasWon("colour"));
        board.play("colour", 6);
        assertTrue(board.hasWon("colour"));
    }

}
