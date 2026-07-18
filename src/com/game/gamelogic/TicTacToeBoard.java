package com.game.gamelogic;

public class TicTacToeBoard extends AbstractBoard {

    public TicTacToeBoard(){
        super(3, 3);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                setPiece(new TicTacToePiece(x, y, "EMPTY"));
            }
        }
    }

    public void setPiece(TicTacToePiece Piece){
        board[Piece.getxPos()][Piece.getyPos()] = Piece;
    }

    public TicTacToePiece getPiece(int x, int y){ // for testing only
        return (TicTacToePiece) board[x][y];
    }

    public boolean checkIfPlayerWon(TicTacToePiece Piece) {
        setPiece(Piece);
        //you only need to check the rows, columns, and diagonals of the most recently placed piece
        //this reduced the total amount of checks from 24 max to 8 max
        int placedx = Piece.getxPos();
        int placedy = Piece.getyPos();
        String PlacedColour = Piece.getColour();

        boolean winningLinePresent = true; //check row x
        for (int y = 1; y < 3; y++) {
            if (!board[placedx][(placedy + y) % 3].getColour().equals(PlacedColour)) {
                winningLinePresent = false;
                break;
            }
        }
        if (winningLinePresent) {
            return true;
        }

        winningLinePresent = true; //check column y
        for (int x = 1; x < 3; x++) {
            if (!board[(placedx + x) % 3][placedy].getColour().equals(PlacedColour)) {
                winningLinePresent = false;
                break;
            }
        }
        if (winningLinePresent) {
            return true;
        }

        if (placedx == placedy) { //check left-leaning diagonal
            winningLinePresent = true;
            for (int dia = 1; dia < 3; dia++) {
                if (!board[(placedx + dia) % 3][(placedy + dia) % 3].getColour().equals(PlacedColour)) {
                    winningLinePresent = false;
                    break;
                }
            }
            if (winningLinePresent) {
                return true;
            }
        }
        if ((placedx + placedy % 3) == 2) { //check right-leaning diagonal
            winningLinePresent = true;
            for (int dia = 1; dia < 3; dia++) {
                if (!board[(placedx - dia + 3) % 3][(placedy + dia) % 3].getColour().equals(PlacedColour)) {
                    winningLinePresent = false;
                    break;
                }
            }
            if (winningLinePresent) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlay(int x, int y) {
        if (0 <= x && x < 3 && 0 <= y && y < 3) {                   // make sure x and y are inbound
            return this.board[x][y].getColour().equals("EMPTY");                        //if x y position in board is empty, then should be null, meaning canPlay should return true.
        }
        return false;
    }

    public AbstractPiece[][] getBoard() {
        return board;
    }

    public TicTacToePiece[] getEmptyCells() {
        TicTacToePiece[] EmptyCells = new TicTacToePiece[9];
        int counter = 0;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (board[x][y].getColour().equals("EMPTY")) {
                    EmptyCells[counter] = (TicTacToePiece) board[x][y];
                    counter++;
                }
            }
        }
        return EmptyCells;
    }

}