package com.game.gamelogic;

public abstract class AbstractBoard {
    public AbstractPiece[][] board;
    public AbstractBoard(int rows, int columns) {
        board = new AbstractPiece[rows][columns];
    }
    public abstract boolean canPlay(int x, int y);
}