package com.game.gamelogic;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class
CheckerTest {

    @Test
    public void testRowSize8(){
        CheckersPiece[][] checkerBoard = new CheckersPiece[8][8];
        int expectedRow = 8;
        assertTrue(expectedRow == checkerBoard.length);
    }

    @Test
    public void testColumnSize8(){
        CheckersPiece[][] checkerBoard = new CheckersPiece[8][8];
        int expectedCol = 8;
        assertTrue(expectedCol == checkerBoard[0].length);
    }

    @Test
    public void initializeBoardTest(){

        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard actual = logic.getBoard();




        CheckersBoard expected = new CheckersBoard(8,8);

        //White Piece Row 0
        expected.setPiece(new CheckersPiece(0, 1, "w"));
        expected.setPiece(new CheckersPiece(0, 3, "w"));
        expected.setPiece(new CheckersPiece(0, 5, "w"));
        expected.setPiece(new CheckersPiece(0, 7, "w"));

        //White Piece Row 1
        expected.setPiece(new CheckersPiece(1, 0, "w"));
        expected.setPiece(new CheckersPiece(1, 2, "w"));
        expected.setPiece(new CheckersPiece(1, 4, "w"));
        expected.setPiece(new CheckersPiece(1, 6, "w"));

        //White Piece Row 2
        expected.setPiece(new CheckersPiece(2, 1, "w"));
        expected.setPiece(new CheckersPiece(2, 3, "w"));
        expected.setPiece(new CheckersPiece(2, 5, "w"));
        expected.setPiece(new CheckersPiece(2, 7, "w"));

        //Black Piece Row 5
        expected.setPiece(new CheckersPiece(5, 0, "r"));
        expected.setPiece(new CheckersPiece(5, 2, "r"));
        expected.setPiece(new CheckersPiece(5, 4, "r"));
        expected.setPiece(new CheckersPiece(5, 6, "r"));

        //Black Piece Row 6
        expected.setPiece(new CheckersPiece(6, 1, "r"));
        expected.setPiece(new CheckersPiece(6, 3, "r"));
        expected.setPiece(new CheckersPiece(6, 5, "r"));
        expected.setPiece(new CheckersPiece(6, 7, "r"));

        //Black Piece Row 7
        expected.setPiece(new CheckersPiece(7, 0, "r"));
        expected.setPiece(new CheckersPiece(7, 2, "r"));
        expected.setPiece(new CheckersPiece(7, 4, "r"));
        expected.setPiece(new CheckersPiece(7, 6, "r"));

        String expectedPiece;
        String actualPiece;

        for(int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                if(expected.getPiece(i,j) != null) {
                    expectedPiece = expected.getPiece(i, j).getColour();
                    actualPiece = actual.getPiece(i, j).getColour();
                    assertEquals(expectedPiece, actualPiece);
                }
            }
        }
    }

    @Test
    public void noWhite(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(2, 1, "r"));

        String actual = board.checkForWinner();

        String expected = "Red";

        assertEquals(actual, expected);
    }

    @Test
    public void noRed(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(2, 1, "w"));

        String actual = board.checkForWinner();

        String expected = "White";

        assertEquals(actual, expected);
    }

    @Test
    public void whiteNoMove(){

        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 2, "w"));
        board.setPiece(new CheckersPiece(2, 1, "r"));
        board.setPiece(new CheckersPiece(2, 3, "r"));
        board.setPiece(new CheckersPiece(4, 1, "r"));
        board.setPiece(new CheckersPiece(4, 3, "r"));

        String actual = board.checkForWinner();

        String expected = "Red";

        assertEquals(actual, expected);
    }

    @Test
    public void whiteHasOneMove(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 2, "w"));
        board.setPiece(new CheckersPiece(4, 1, "r"));


        String actual = board.checkForWinner();

        String expected = "none";

        assertEquals(actual, expected);
    }

    @Test
    public void whiteInCornerNoMove(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 0, "w"));
        board.setPiece(new CheckersPiece(5, 1, "r"));


        String actual = board.checkForWinner();
        String expected = "Red";

        assertEquals(actual, expected);
    }

    @Test
    public void whiteInCornerOneMove(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 0, "w"));
        board.setPiece(new CheckersPiece(3, 5, "r"));


        String actual = board.checkForWinner();
        String expected = "none";

        assertEquals(actual, expected);
    }

    @Test
    public void redNoMove(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 1, "r"));
        board.setPiece(new CheckersPiece(3, 0, "w"));
        board.setPiece(new CheckersPiece(3, 2, "w"));


        String actual = board.checkForWinner();
        String expected = "White";

        assertEquals(actual, expected);
    }

    @Test
    public void blackHasOneMove(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(5, 2, "r"));
        board.setPiece(new CheckersPiece(4, 1, "w"));


        String actual = board.checkForWinner();

        String expected = "none";

        assertEquals(actual, expected);
    }

    @Test
    public void countMoveKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 4, "r"));

        board.getPiece(3,4).setKing(true);

        assertTrue(board.getPiece(3, 4).isKing());


    }

    @Test
    public void countMoveRedKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 4, "r"));

        board.getPiece(3,4).setKing(true);

        assertTrue(board.getPiece(3, 4).isKing());

        int expected = 4;

        int actual = board.count_moves(board.getPiece(3,4));

        assertEquals(expected, actual);

    }

    @Test
    public void countMoveWhiteKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 4, "w"));

        board.getPiece(3,4).setKing(false);

        assertFalse(board.getPiece(3, 4).isKing());

        int expected = 4;

        int actual = board.count_moves(board.getPiece(3,4));

        assertFalse(expected == actual);
        assertTrue(actual == 2);

    }

    @Test
    public void countMove1RedKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(3, 2, "r"));
        board.setPiece(new CheckersPiece(2, 1, "w"));
        board.setPiece(new CheckersPiece(2, 3, "w"));
        board.setPiece(new CheckersPiece(4, 1, "w"));

        board.getPiece(3,2).setKing(true);

        assertTrue(board.getPiece(3, 2).isKing());

        int expected = 1;

        int actual = board.count_moves(board.getPiece(3,2));

        assertEquals(expected, actual);

    }

    @Test
    public void countMove3WhiteKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(2, 1, "w"));
        board.setPiece(new CheckersPiece(1, 0, "w"));


        board.getPiece(2,1).setKing(true);

        assertTrue(board.getPiece(2, 1).isKing());

        int expected = 3;

        int actual = board.count_moves(board.getPiece(2,1));

        assertEquals(expected, actual);

    }

    @Test
    public void captureWhiteUpRight(){

        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 1, "r"));
        board.setPiece(new CheckersPiece(3, 2, "w"));


        boolean actual = logic.canCapture(board.getPiece(4,1));

        assertTrue(actual);
    }

    @Test
    public void captureWhiteUpLeft(){

        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 2, "r"));
        board.setPiece(new CheckersPiece(3, 1, "w"));


        boolean actual = logic.canCapture(board.getPiece(4,2));

        assertTrue(actual);
    }

    @Test
    public void captureRedDownLeft(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 3, "w"));
        board.setPiece(new CheckersPiece(5, 2, "r"));


        boolean actual = logic.canCapture(board.getPiece(4,3));

        assertTrue(actual);
    }

    @Test
    public void captureRedDownRight(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }

        board.setPiece(new CheckersPiece(4, 3, "w"));
        board.setPiece(new CheckersPiece(5, 4, "r"));


        boolean actual = logic.canCapture(board.getPiece(4,3));

        assertTrue(actual);
    }

    @Test
    public void whiteKingCaptureUpRight(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set White Piece to be king
        CheckersPiece piece1 = new CheckersPiece(2,1, "w");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(1, 2, "r"));


        boolean actual = logic.canCapture(board.getPiece(2,1));

        assertTrue(actual);
    }

    @Test
    public void redKingCannotCaptureDownRight(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(6,1, "r");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(7, 0, "w"));


        boolean actual = logic.canCapture(board.getPiece(6,1));

        assertFalse(actual);
    }


    @Test
    public void redMove() {
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();
    }


    @Test
    public void whiteMoveDownLeft(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(3,2, "w");
        piece1.setKing(true);
        board.setPiece(piece1);



        String active = logic.getActivePlayer();

        if(active.equals("w")) {
            boolean actual = logic.move("w", 3, 2, 4, 1);
            System.out.println("run");
            assertTrue(actual);
        }else{
            assertEquals(active,"r");
        }
    }

    @Test
    public void redCaptureWhiteUpRight(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(4,1, "r");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(3,2, "w"));


        String active = logic.getActivePlayer();

        if(active.equals("r")) {
            boolean actual = logic.move("r", 4, 1, 2, 3);
            System.out.println("running");
            assertTrue(actual);
        }else{
            assertEquals(active,"w");
        }
    }

    @Test
    public void redCaptureWhiteUpLeft(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(5,2, "r");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(4,1, "w"));


        String active = logic.getActivePlayer();

        if(active.equals("r")) {
            boolean actual = logic.move("r", 5, 2, 3, 0);
            System.out.println("running2");
            assertTrue(actual);
        }else{
            assertEquals(active,"w");
        }
    }

    @Test
    public void whiteCaptureRedDownRight(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(2,3, "w");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(3,2, "r"));


        String active = logic.getActivePlayer();

        if(active.equals("w")) {
            boolean actual = logic.move("w", 2, 3, 4, 1);
            System.out.println("running3");
            assertTrue(actual);
        }else{
            assertEquals(active,"r");
        }
    }

    @Test
    public void announceRedWin(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(4,1, "r");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(3,2, "w"));
        String active = logic.getActivePlayer();
        if(active.equals("r")) {
            boolean capture = logic.move("r", 4, 1, 2, 3);
            String actual = logic.checkWin();
            String win = logic.getWinner();
            System.out.println("Running test");

            assertEquals(actual, "r");
            assertEquals(win, "r");
        }else {
            String actual = logic.checkWin();

            System.out.println("Running test");
            assertEquals(actual, null);
        }

    }

    @Test
    public void announceWhiteWin(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(3,3, "w");
        piece1.setKing(true);
        board.setPiece(piece1);

        board.setPiece(new CheckersPiece(2,4, "r"));
        String active = logic.getActivePlayer();

        if(active.equals("w")) {
            boolean capture = logic.move("w", 3, 3, 1, 5);
            String actual = logic.checkWin();
            String win = logic.getWinner();
            System.out.println("Running test 2");

            assertEquals(actual, "w");
            assertEquals(win, "w");
        }else {
            String actual = logic.checkWin();

            System.out.println("Running test2");
            assertEquals(actual, null);
        }

    }


    @Test
    public void redMovePromoteKing(){
        TimerUpdateListener listener = new TimerUpdateListener() {
            @Override
            public void onTimerUpdate(int player1Time, int player2Time) {
                return;
            }
        };
        CheckersLogic logic = new CheckersLogic("r", "w");
        logic.setTimerUpdateListener(listener);
        logic.startNewGame();
        CheckersBoard board = logic.getBoard();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                CheckersPiece piece = board.getPiece(i, j);
                if (piece != null) {
                    board.removePiece(piece);
                }
            }
        }


        //Set Red Piece to be king
        CheckersPiece piece1 = new CheckersPiece(1,0, "r");
        board.setPiece(piece1);



        String active = logic.getActivePlayer();

        if(active.equals("r")) {
            boolean move = logic.move("r", 1, 0, 0, 1);
            System.out.println("Promote Red");

            boolean actual = board.getPiece(0,1).isKing();
            assertTrue(actual);
        }else{
            System.out.println("not red");
            assertEquals(active,"w");
        }
    }



}
