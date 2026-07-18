package com.game.gamelogic;

public class Connect4Board extends AbstractBoard {
    public Connect4Board() {
        super(6, 7);
    }

    public boolean canPlay(int row, int column){ // Since connect4, this should be called with row = 0
        boolean rowInBounds = row > -1 && row < 6;
        if (!rowInBounds) {
            return false;
        }
        boolean columnInBounds = column > -1 && column < 7;
        if (!columnInBounds) {
            return false;
        }
        return board[0][column] == null;
    }

    public boolean hasWon(String colour) {
        return winRow(colour) || winColumn(colour) || winBackSlash(colour) || winForwardSlash(colour);
    }

    public boolean boardFull() {
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == null) {
                return false;
            }
        }
        return true;
    }

    private boolean winRow(String colour) {
        int consecutive = 0;
        for (int i = 0; i < board.length; i++) {
            consecutive = 0;
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != null) {
                    if (board[i][j].getColour().equals(colour)) {
                        consecutive++;
                    } else {
                        consecutive = 0;
                    }
                } else {
                    consecutive = 0;
                }
                if (consecutive == 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean winColumn(String colour) {
        int consecutive = 0;
        for (int i = 0; i < board[0].length; i++) {
            consecutive = 0;
            for (int j = 0; j < board.length; j++) {
                if (board[j][i] != null) {
                    if (board[j][i].getColour().equals(colour)) { // playerColour is temporary
                        consecutive++;
                    } else {
                        consecutive = 0;
                    }
                } else {
                    consecutive = 0;
                }
                if (consecutive == 4) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean winBackSlash(String colour) {
        // Check all possible backslash diagonals (\)
        for (int startRow = 0; startRow <= board.length - 4; startRow++) {
            for (int startCol = 0; startCol <= board[0].length - 4; startCol++) {
                int consecutive = 0;
                for (int i = 0; i < 4; i++) {
                    int row = startRow + i;
                    int col = startCol + i;
                    if (board[row][col] != null && board[row][col].getColour().equals(colour)) {
                        consecutive++;
                    } else {
                        consecutive = 0;
                    }
                    if (consecutive == 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean winForwardSlash(String colour) {
        // Check all possible forward slash diagonals (/)
        for (int startRow = 3; startRow < board.length; startRow++) {
            for (int startCol = 0; startCol <= board[0].length - 4; startCol++) {
                int consecutive = 0;
                for (int i = 0; i < 4; i++) {
                    int row = startRow - i;
                    int col = startCol + i;
                    if (board[row][col] != null && board[row][col].getColour().equals(colour)) {
                        consecutive++;
                    } else {
                        consecutive = 0;
                    }
                    if (consecutive == 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void play(String colour, int column) {
        int lowestRow = -1;
        for (int i = 0; i < board.length; i++) {
            if (board[i][column] == null) {
                lowestRow = i;
            }
        }
        board[lowestRow][column] = new Connect4Piece(lowestRow, column, colour);
    }

    public void autoMove(String colour) {
        int column = (int) (Math.random() * 7);
        while (!canPlay(0, column)) {
            column = (int) (Math.random() * 7);
        }
        play(colour, column);
    }
}