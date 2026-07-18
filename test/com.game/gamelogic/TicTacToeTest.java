package com.game.gamelogic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TicTacToeTest {

//    TicTacToeBoard EmptyBoard = new TicTacToeBoard();

    @Test
    public void testBoardInitializationSize() {
        TicTacToeBoard Board = new TicTacToeBoard();
        assertEquals(3, Board.getBoard().length);
        assertEquals(3, Board.getBoard()[0].length);
    }
    
    @Test
    public void testBoardInitializationCells() {
        TicTacToeBoard Board = new TicTacToeBoard();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                TicTacToePiece Piece = Board.getPiece(x, y);
                if (!Piece.getColour().equals("EMPTY") || !(Piece.getxPos() == x) && !(Piece.getyPos() == y)) {
                    fail();
                }
            }
        }
    }
    
    @Test
    public void setPiece() {
        TicTacToeBoard Board = new TicTacToeBoard();
        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST_PIECE");
        Board.setPiece(Piece);
        assertEquals(Piece, Board.getBoard()[1][1]);
    }
    
    @Test
    public void testCanPlay_EmptyBoard() {
        TicTacToeBoard Board = new TicTacToeBoard();
        assertTrue(Board.canPlay(0, 0));
    }

    @Test
    public void testCanPlay_OutOfBounds() {
        TicTacToeBoard Board = new TicTacToeBoard();
        assertFalse(Board.canPlay(-1, -1));
    }

    @Test
    public void testCanPlay_InOneFullCell() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST"));
        assertFalse(Board.canPlay(0, 0));
    }

    @Test
    public void testCanPlay_InOneEmptyCell() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(0, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(0, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 1, "TEST"));
        assertTrue(Board.canPlay(2, 2));
    }
    
    @Test
    public void testGetEmptyCells_NewBoard() { //TODO
        TicTacToeBoard Board = new TicTacToeBoard();
        TicTacToePiece[] EmptyCells = Board.getEmptyCells();
        int counter = 0;
        for (int i = 0; i < 9; i++) {
            if (EmptyCells[i] != null && EmptyCells[i].getColour().equals("EMPTY")) {
                counter++;
            }
        }
        assertEquals(9, counter);
    }

    @Test
    public void testGetEmptyCells_FullBoard() { //TODO
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(0, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(0, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 2, "TEST"));
        TicTacToePiece[] EmptyCells = Board.getEmptyCells();
        int counter = 0;
        for (int i = 0; i < 9; i++) {
            if (EmptyCells[i] != null && EmptyCells[i].getColour().equals("EMPTY")) {
                counter++;
            }
        }
        assertEquals(0, counter);
    }

    @Test
    public void testIfPlayerWon_EmptyBoard() {
        TicTacToeBoard Board = new TicTacToeBoard();
        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertFalse(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_ValidWinOnXRow() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(1, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 2, "TEST"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertTrue(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_ValidWinOnYCol() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 1, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 1, "TEST"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertTrue(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_ValidWinOnLeftDiagonal() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 2, "TEST"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertTrue(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_ValidWinOnRightDiagonal() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 0, "TEST"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertTrue(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_NoWinWithMultiplePieces() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(1, 2, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 0, "TEST"));
        Board.setPiece(new TicTacToePiece(2, 1, "TEST"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST");
        assertFalse(Board.checkIfPlayerWon(Piece));
    }

    @Test
    public void testIfPlayerWon_NoWinWithOpponentPieces() {
        TicTacToeBoard Board = new TicTacToeBoard();
        Board.setPiece(new TicTacToePiece(0, 0, "TEST_X"));
        Board.setPiece(new TicTacToePiece(0, 1, "TEST_X"));
        Board.setPiece(new TicTacToePiece(0, 2, "TEST_X"));
        Board.setPiece(new TicTacToePiece(1, 0, "TEST_X"));
        Board.setPiece(new TicTacToePiece(1, 2, "TEST_X"));
        Board.setPiece(new TicTacToePiece(2, 0, "TEST_X"));
        Board.setPiece(new TicTacToePiece(2, 1, "TEST_X"));
        Board.setPiece(new TicTacToePiece(2, 2, "TEST_X"));

        TicTacToePiece Piece = new TicTacToePiece(1, 1, "TEST_O");
        assertFalse(Board.checkIfPlayerWon(Piece));
    }

    //TicTacToeLogic Tests

    @Test
    public void testLogicStartGame_PlayerStart() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        assertFalse(Logic.getActivePlayer().isEmpty());
    }

    @Test
    public void testTimeout_ActivePlayerSwaps() throws InterruptedException {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        String ActivePlayer = Logic.getActivePlayer();
        Thread.sleep(30 * 1000);
        assertNotEquals(ActivePlayer, Logic.getActivePlayer());

    }

    @Test
    public void testTimeout_3OutsResignation() throws InterruptedException {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        String StartingPlayer = Logic.getActivePlayer();
        Thread.sleep(5 * 31 * 1000); // + 5 seconds just for code compiling
        System.out.println(Logic.getTimeOuts_X() + " " + Logic.getTimeOuts_O());
        assertEquals(StartingPlayer, Logic.getLoser());
    }

    @Test
    public void testResignation_GameRunning() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        Logic.playerResign("X");
        assertEquals("O", Logic.getWinner());
    }

    @Test
    public void testResignation_GameEnded() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        Logic.playerResign("X");
        Logic.playerResign("O"); // should not matter since game is over
        assertEquals("O", Logic.getWinner());
    }

    @Test
    public void testPlacePiece_Winner() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "0");
        Logic.startNewGame();
        String FirstPlayer = Logic.getActivePlayer();
        String SecondPlayer = FirstPlayer.equals("X") ? "0" : "X";
        Logic.placePiece(FirstPlayer, 0,0);
        Logic.placePiece(SecondPlayer, 1,0);
        Logic.placePiece(FirstPlayer, 0,1);
        Logic.placePiece(SecondPlayer, 1,1);
        Logic.placePiece(FirstPlayer, 0,2);
        assertEquals(FirstPlayer, Logic.getWinner());
    }

    @Test
    public void testPlacePiece_Tie() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "0");
        Logic.startNewGame();
        String FirstPlayer = Logic.getActivePlayer();
        String SecondPlayer = FirstPlayer.equals("X") ? "0" : "X";
        Logic.placePiece(FirstPlayer, 0,0);
        Logic.placePiece(SecondPlayer, 0,1);
        Logic.placePiece(FirstPlayer, 0,2);
        Logic.placePiece(SecondPlayer, 1,0);
        Logic.placePiece(FirstPlayer, 1,1);
        Logic.placePiece(SecondPlayer, 2,0);
        Logic.placePiece(FirstPlayer, 1,2);
        Logic.placePiece(SecondPlayer, 2,2);
        Logic.placePiece(FirstPlayer, 2,1);
        assertEquals("Draw", Logic.getWinner());
    }

    @Test
    public void testPlacePiece_WrongTurnOrder() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "0");
        Logic.startNewGame();
        String InactivePlayer = Logic.getActivePlayer().equals("X") ? "0" : "X";
        Logic.placePiece(InactivePlayer, 2,2);
        Logic.placePiece(InactivePlayer, 1,1);
        Logic.placePiece(InactivePlayer, 0,0);

        TicTacToeBoard Board = Logic.getGameBoard();
        TicTacToePiece[] EmptyCells = Board.getEmptyCells();
        int counter = 0;
        for (int i = 0; i < 9; i++) {
            if (EmptyCells[i] != null && EmptyCells[i].getColour().equals("EMPTY")) {
                counter++;
            }
        }
        assertEquals(9, counter);
    }

    @Test
    public void testRestartGame() {
        TicTacToeLogic Logic = new TicTacToeLogic("X", "O");
        Logic.startNewGame();
        Logic.playerResign("X");
        Logic.restartGame();
        assertEquals("X", Logic.getActivePlayer());
        assertEquals("", Logic.getWinner());
    }


}
